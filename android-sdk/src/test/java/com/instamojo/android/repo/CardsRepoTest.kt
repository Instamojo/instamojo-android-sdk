package com.instamojo.android.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.instamojo.android.helpers.mock
import com.instamojo.android.models.*
import com.instamojo.android.network.ApiResponse
import com.instamojo.android.network.ImojoService
import com.instamojo.android.network.Resource
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito

@RunWith(JUnit4::class)
class CardsRepoTest {


    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val imojoService = Mockito.mock(ImojoService::class.java)

    var cardsRepo : CardsRepo?= null

    val submissionUrl = "https://dummySubmissionurl.com"
    val bankCode = "203"
    val emiTenure = 12

    @Before
    fun setUp() {
        cardsRepo = CardsRepo((imojoService))
    }

    @Test
    fun checkout() {
        val observer = mock<Observer<Resource<CardPaymentResponse>>>()
        val mediatorLiveData = MediatorLiveData<ApiResponse<CardPaymentResponse>>()

        val order = Mockito.mock(GatewayOrder::class.java)
        val card = Mockito.mock(Card::class.java)

        Mockito.`when`(card.cardNumber).thenReturn("4242 42424 42424")
        Mockito.`when`(card.cardHolderName).thenReturn("dummy name")
        Mockito.`when`(card.cvv).thenReturn("435")
        Mockito.`when`(card.month).thenReturn("01")
        Mockito.`when`(card.year).thenReturn("2023")
        Mockito.`when`(card.canSaveCard()).thenReturn(true)

        val paymentOptions = Mockito.mock(PaymentOptions::class.java)
        Mockito.`when`(order.paymentOptions).thenReturn(paymentOptions)

        val cardOptions = Mockito.mock(CardOptions::class.java)
        Mockito.`when`(paymentOptions.cardOptions).thenReturn(cardOptions)

        val submissionData = Mockito.mock(SubmissionData::class.java)
        Mockito.`when`(cardOptions.submissionData).thenReturn(submissionData)
        Mockito.`when`(submissionData.orderID).thenReturn("123")
        Mockito.`when`(submissionData.merchantID).thenReturn("234")

        val emiOption = Mockito.mock(EMIOptions::class.java)
        Mockito.`when`(paymentOptions.emiOptions).thenReturn(emiOption)

        Mockito.`when`(imojoService.collectCardPayment(submissionUrl,cardsRepo?.populateCardRequest(order,card,bankCode,emiTenure)))
                .thenReturn(mediatorLiveData)

        val cardPaymentResponse = Mockito.mock(CardPaymentResponse::class.java)
        val apiResponse = mock<ApiResponse<CardPaymentResponse>>()
        Mockito.`when`(apiResponse.isSuccess).thenReturn(true)
        Mockito.`when`(apiResponse.response()).thenReturn(cardPaymentResponse)
        cardsRepo?.checkout(submissionUrl,order,card,bankCode,emiTenure)
                ?.observeForever(observer)
        mediatorLiveData.value = apiResponse
        Mockito.verify(observer).onChanged(Resource.success(cardPaymentResponse))
    }

}