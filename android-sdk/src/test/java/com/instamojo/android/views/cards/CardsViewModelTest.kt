package com.instamojo.android.views.cards

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.instamojo.android.helpers.mock
import com.instamojo.android.models.Card
import com.instamojo.android.models.CardPaymentResponse
import com.instamojo.android.models.GatewayOrder
import com.instamojo.android.network.Resource
import com.instamojo.android.repo.CardsRepo
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito

@RunWith(JUnit4::class)
class CardsViewModelTest {


    @get: Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val cardsRepo = Mockito.mock(CardsRepo::class.java)

    var cardsViewModel : CardsViewModel? = null

    val submissionUrl = "https://dummySubmissionurl.com"
    val bankCode = "203"
    val emiTenure = 12


    @Before
    fun setUp() {
     cardsViewModel = CardsViewModel(cardsRepo)
    }

    @Test
    fun onCheckout() {
        val observer = mock<Observer<Resource<CardPaymentResponse>>>()
        cardsViewModel?.checkout()?.observeForever(observer)

        val order = Mockito.mock(GatewayOrder::class.java)
        val card = Mockito.mock(
                Card::class.java)
        cardsViewModel?.onCheckout(submissionUrl,order,card,bankCode,emiTenure)
        Mockito.verify(cardsRepo,Mockito.times(1))
                .checkout(submissionUrl,order,card,bankCode,emiTenure)
    }

}