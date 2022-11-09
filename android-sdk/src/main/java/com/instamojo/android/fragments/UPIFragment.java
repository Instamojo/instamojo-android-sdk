package com.instamojo.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.instamojo.android.network.ServiceGenerator;
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

    private MaterialEditText virtualAddressBox;
    private PaymentDetailsActivity parentActivity;
    private View preVPALayout, postVPALayout, verifyPayment;
    private UPISubmissionResponse upiSubmissionResponse;
    private Handler handler = new Handler();
    private String mUPIStatusURL;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upi_instamojo, container, false);
        parentActivity = (PaymentDetailsActivity) getActivity();
        inflateXML(view);
        int title = R.string.title_fragment_upi;
        parentActivity.updateActionBarTitle(title);
        return view;
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

        ImojoService imojoService = ServiceGenerator.getImojoService();

        // TODO this fragment should receive UPI options only
        GatewayOrder gatewayOrder = parentActivity.getOrder();
        UPIOptions upiOptions = gatewayOrder.getPaymentOptions().getUpiOptions();

        Call<UPISubmissionResponse> upiPaymentCall =
                imojoService.collectUPIPayment(upiOptions.getSubmissionURL(), virtualAddressBox.getText().toString());

        upiPaymentCall.enqueue(new Callback<UPISubmissionResponse>() {
            @Override
            public void onResponse(Call<UPISubmissionResponse> call, final Response<UPISubmissionResponse> response) {
                if (response.isSuccessful()) {
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UPISubmissionResponse upiSubmissionResponse = response.body();
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
                        }
                    });

                } else {
                    String error = "Oops. Some error occurred. Please try again..";
                    if (response.code() == 400) {
                        try {
                            String errorBody = response.errorBody().string();
                            Logger.d(TAG, errorBody);
                            JSONObject responseObject = new JSONObject(errorBody);
                            JSONObject errors = responseObject.getJSONObject("errors");

                            virtualAddressBox.setEnabled(true);
                            verifyPayment.setEnabled(true);
                            error = errors.getString("virtual_address");

                        } catch (IOException | JSONException e) {
                            Logger.e(TAG, "Error while handling UPI error response - " + e.getMessage());
                        }
                    }

                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UPISubmissionResponse> call, Throwable t) {
                Logger.e(TAG, "Error while making UPI Submission request - " + t.getMessage());
                Toast.makeText(getContext(), "Oops. Some error occurred. Please try again..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUpiPaymentStatus() {
        ImojoService imojoService = ServiceGenerator.getImojoService();
        Call<UPIStatusResponse> upiStatusCall = imojoService.getUPIStatus(mUPIStatusURL);
        upiStatusCall.enqueue(new Callback<UPIStatusResponse>() {
            @Override
            public void onResponse(Call<UPIStatusResponse> call, Response<UPIStatusResponse> response) {
                if (response.isSuccessful()) {
                    int statusCode = response.body().getStatusCode();
                    if (statusCode != Constants.PENDING_PAYMENT) {
                        // Stop polling for status. Return to activity
                        returnResult(statusCode);
                    } else {
                        // Keep trying
                        retryUPIStatusCheck();
                    }

                } else {
                    Logger.e(TAG, "Error response while fetching UPI status");
                }
            }

            @Override
            public void onFailure(Call<UPIStatusResponse> call, Throwable t) {
                Logger.e(TAG, "Failed to fetch UPI status. Error: " + t.getMessage());
            }
        });
    }

    private void returnResult(int statusCode) {
        Bundle bundle = MoneyUtil.createBundleFromOrder(parentActivity.getOrder().getOrder().getId(),parentActivity.getOrder().getOrder().getTransactionID(),upiSubmissionResponse.getPaymentID());
        Logger.d(TAG, "Payment complete. Finishing activity...");
        if(statusCode == Constants.PAYMENT_SUCCEDED) {
            parentActivity.onUPIResponse(bundle, Activity.RESULT_OK);
        }else{
            parentActivity.onUPIResponse(bundle, Activity.RESULT_CANCELED);
        }
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
