package com.instamojo.android.views.cards

import androidx.arch.core.util.Function
import androidx.lifecycle.*
import com.instamojo.android.models.Card
import com.instamojo.android.models.CardPaymentResponse
import com.instamojo.android.models.GatewayOrder
import com.instamojo.android.network.Resource
import com.instamojo.android.repo.CardsRepo

class CardsViewModel(val cardsRepo: CardsRepo) : ViewModel() {

    val checkoutLiveData = MutableLiveData<CardCheckout>()

    fun checkout(): LiveData<Resource<CardPaymentResponse>> {
        return Transformations.switchMap(checkoutLiveData, Function {
            cardsRepo.checkout(it.SubmissionURL,it.order,it.card,it.mSelectedBankCode,it.mSelectedTenure)
        })
    }

    fun onCheckout(SubmissionURL: String, order: GatewayOrder, card: Card, mSelectedBankCode: String, mSelectedTenure: Int) {
        checkoutLiveData.value = CardCheckout(SubmissionURL,order,card,mSelectedBankCode,mSelectedTenure)
    }

    class CardsViewModelFactory internal constructor(private val cardsRepo: CardsRepo) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CardsViewModel(cardsRepo) as T
        }
    }

    class CardCheckout(val SubmissionURL: String,val order: GatewayOrder,val card: Card,
                       val mSelectedBankCode: String,val mSelectedTenure: Int)

}