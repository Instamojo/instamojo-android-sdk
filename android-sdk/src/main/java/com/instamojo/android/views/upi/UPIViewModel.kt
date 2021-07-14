package com.instamojo.android.views.upi

import androidx.arch.core.util.Function
import androidx.lifecycle.*
import com.instamojo.android.models.UPIStatusResponse
import com.instamojo.android.models.UPISubmissionResponse
import com.instamojo.android.network.Resource
import com.instamojo.android.repo.CardsRepo
import com.instamojo.android.repo.UPIRepo

class UPIViewModel(val upiRepo: UPIRepo) : ViewModel() {

    private val submissionUrlChangeLiveData = MutableLiveData<String>()

    private val paymentRequestChangeLiveData = MutableLiveData<PaymentRequest>()

    fun collectUPIPayment(): LiveData<Resource<UPISubmissionResponse>> {
        return Transformations.switchMap(paymentRequestChangeLiveData, Function {
            upiRepo.collectUPIPayment(it.submissionUrl,it.vpa)
        })
    }

    fun getUPIStatus(): LiveData<Resource<UPIStatusResponse>> {
        return Transformations.switchMap(submissionUrlChangeLiveData, Function {
            upiRepo.getUPIStatus(it)
        })
    }

    fun onNewUPIStatusRequest(submissionUrl: String){
        submissionUrlChangeLiveData.value = submissionUrl
    }

    fun onNewCollectPaymentRequest(submissionUrl: String, vpa: String){
        paymentRequestChangeLiveData.value = PaymentRequest(submissionUrl,vpa)
    }

    class UPIViewModelFactory internal constructor(private val upiRepo: UPIRepo) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UPIViewModel(upiRepo) as T
        }
    }

    class PaymentRequest(val submissionUrl: String,val vpa: String)

}