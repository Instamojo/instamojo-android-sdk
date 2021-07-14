package com.instamojo.android.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.instamojo.android.helpers.CardUtil
import com.instamojo.android.models.Card
import com.instamojo.android.models.CardPaymentResponse
import com.instamojo.android.models.GatewayOrder
import com.instamojo.android.network.ImojoService
import com.instamojo.android.network.Resource
import java.util.*

class CardsRepo(val imojoService: ImojoService) {

    fun checkout(submissionUrl: String, order: GatewayOrder, card: Card, mSelectedBankCode: String, mSelectedTenure: Int): LiveData<Resource<CardPaymentResponse>> {
        val mediatorLiveData = MediatorLiveData<Resource<CardPaymentResponse>>()
        mediatorLiveData.value = Resource.loading()
        val liveData = imojoService.collectCardPayment(submissionUrl, populateCardRequest(order, card,
                mSelectedBankCode, mSelectedTenure))
        mediatorLiveData.addSource(liveData) {
            if (it.isSuccess && it.response() != null) {
                mediatorLiveData.value = Resource.success(it.response())
            }else{
                mediatorLiveData.value = Resource.error(it.errorMessage, null)
            }
        }
        return mediatorLiveData
    }

    fun populateCardRequest(order: GatewayOrder, card: Card,
                            emiBankCode: String?, emiTenure: Int): Map<String, String>? {

        //For maestro, add the default values if empty
        if (CardUtil.isMaestroCard(card.cardNumber)) {
            if (card.date == null || card.date.isEmpty()) {
                card.date = "12/49"
            }
            if (card.cvv == null || card.cvv.isEmpty()) {
                card.date = "111"
            }
        }
        val fieldMap: MutableMap<String, String> = HashMap()
        fieldMap["order_id"] = order.paymentOptions.cardOptions.submissionData.orderID
        fieldMap["merchant_id"] = order.paymentOptions.cardOptions.submissionData.merchantID
        fieldMap["payment_method_type"] = "CARD"
        fieldMap["card_number"] = card.cardNumber
        fieldMap["card_exp_month"] = card.month
        fieldMap["card_exp_year"] = card.year
        fieldMap["card_security_code"] = card.cvv
        fieldMap["save_to_locker"] = if (card.canSaveCard()) "true" else "false"
        fieldMap["redirect_after_payment"] = "true"
        fieldMap["format"] = "json"
        if (card.cardHolderName != null) {
            fieldMap["name_on_card"] = card.cardHolderName
        }
        if (order.paymentOptions.emiOptions != null && emiBankCode != null) {
            fieldMap["is_emi"] = "true"
            fieldMap["emi_bank"] = emiBankCode
            fieldMap["emi_tenure"] = emiTenure.toString()
        }
        return fieldMap
    }

}