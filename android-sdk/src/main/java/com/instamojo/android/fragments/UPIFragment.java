package com.instamojo.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.instamojo.android.R;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.helpers.MoneyUtil;
import com.instamojo.android.helpers.Validators;
import com.instamojo.android.models.GatewayOrder;
import com.instamojo.android.models.UPIOptions;
import com.instamojo.android.models.UPIStatusResponse;
import com.instamojo.android.models.UPISubmissionResponse;
import com.instamojo.android.network.ImojoService;
import com.instamojo.android.network.Resource;
import com.instamojo.android.network.ServiceGenerator;
import com.instamojo.android.repo.UPIRepo;
import com.instamojo.android.views.upi.UPIViewModel;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass. The {@link Fragment} to get Virtual Private Address from user.
 */

public class UPIFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = UPIFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "UPISubmission Form";
    private static final long DELAY_CHECK = 2000;
    public static final String UPI_RESULT = "instamojo:upi:result";

    private MaterialEditText virtualAddressBox;
    private PaymentDetailsActivity parentActivity;
    private View preVPALayout, postVPALayout, verifyPayment;
    private UPISubmissionResponse upiSubmissionResponse;
    private Handler handler = new Handler();
    private String mUPIStatusURL;

    private UPIViewModel upiViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upi_instamojo, container, false);
        parentActivity = (PaymentDetailsActivity) getActivity();
        inflateXML(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        upiViewModel = ViewModelProviders.of(this,
                new UPIViewModel.UPIViewModelFactory(new UPIRepo(ServiceGenerator.getImojoService()))).get(UPIViewModel.class);
        upiViewModel.getUPIStatus()
                .observe(getViewLifecycleOwner(), new Observer<Resource<UPIStatusResponse>>() {
                    @Override
                    public void onChanged(Resource<UPIStatusResponse> upiStatusResponseResource) {
                        if(upiStatusResponseResource.getStatus() == Resource.SUCCESS){
                            int statusCode = upiStatusResponseResource.getData().getStatusCode();
                            if (statusCode != Constants.PENDING_PAYMENT) {
                                returnResult();
                            } else {
                                retryUPIStatusCheck();
                            }
                        }
                    }
                });

        upiViewModel.collectUPIPayment()
                .observe(getViewLifecycleOwner(), new Observer<Resource<UPISubmissionResponse>>() {
                    @Override
                    public void onChanged(Resource<UPISubmissionResponse> upiSubmissionResponseResource) {
                        if(upiSubmissionResponseResource.getStatus() == Resource.SUCCESS){
                            UPISubmissionResponse upiSubmissionResponse = upiSubmissionResponseResource.getData();
                            if (upiSubmissionResponse.getStatusCode() != Constants.PENDING_PAYMENT) {
                                virtualAddressBox.setEnabled(true);
                                verifyPayment.setEnabled(true);
                                Toast.makeText(getContext(), "please try again...", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            preVPALayout.setVisibility(View.GONE);
                            postVPALayout.setVisibility(View.VISIBLE);

                            UPIFragment.this.upiSubmissionResponse = upiSubmissionResponse;
                            mUPIStatusURL = upiSubmissionResponse.getStatusCheckURL();
                            checkUpiPaymentStatus();
                        }else if(upiSubmissionResponseResource.getStatus() == Resource.ERROR){
                            virtualAddressBox.setEnabled(true);
                            verifyPayment.setEnabled(true);
                            Toast.makeText(getContext(), upiSubmissionResponseResource.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }

    @Override
    public void onResume() {
        super.onResume();
        int title = R.string.title_fragment_upi;
        parentActivity.updateActionBarTitle(title);
    }

    @Override
    public String getFragmentName() {
        return FRAGMENT_NAME;
    }

    @Override
    public void inflateXML(View view) {
        virtualAddressBox = view.findViewById(R.id.virtual_address_box);
        virtualAddressBox.addValidator(new Validators.EmptyFieldValidator());
        virtualAddressBox.addValidator(new Validators.VPAValidator());
        preVPALayout = view.findViewById(R.id.pre_vpa_layout);
        postVPALayout = view.findViewById(R.id.post_vpa_layout);
        verifyPayment = view.findViewById(R.id.verify_payment);
        verifyPayment.setOnClickListener(this);

        // Automatically open soft keyboard for VPA field (on display of this fragment).
        virtualAddressBox.post(new Runnable() {
            @Override
            public void run() {
                virtualAddressBox.requestFocus();
                InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (lManager != null) {
                    lManager.showSoftInput(virtualAddressBox, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (!virtualAddressBox.validate()) {
            return;
        }

        virtualAddressBox.setEnabled(false);
        verifyPayment.setEnabled(false);

        GatewayOrder gatewayOrder = parentActivity.getOrder();
        UPIOptions upiOptions = gatewayOrder.getPaymentOptions().getUpiOptions();
        upiViewModel.onNewCollectPaymentRequest(upiOptions.getSubmissionURL(), virtualAddressBox.getText().toString());
    }

    private void checkUpiPaymentStatus() {
        upiViewModel.onNewUPIStatusRequest(mUPIStatusURL);
    }

    private void returnResult() {
        Bundle bundle = MoneyUtil.createBundleFromOrder(parentActivity.getOrder().getOrder().getId(),parentActivity.getOrder().getOrder().getTransactionID(),upiSubmissionResponse.getPaymentID());
        Logger.d(TAG, "Payment complete. Finishing activity...");
        getParentFragmentManager().setFragmentResult(UPI_RESULT,bundle);

    }

    public void retryUPIStatusCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUpiPaymentStatus();
            }
        }, DELAY_CHECK);
    }

}
