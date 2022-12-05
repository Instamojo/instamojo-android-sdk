package com.instamojo.android.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instamojo.android.R;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.models.GatewayOrder;
import com.instamojo.android.models.PaymentOptions;

/**
 * Fragment holds the available Payment options for the User
 */
public class PaymentOptionsFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = PaymentOptionsFragment.class.getSimpleName();
    private static final String FRAGMENT_NAME = "PaymentOptionsFragment";
    private PaymentDetailsActivity parentActivity;

    public PaymentOptionsFragment() {
        //empty as required
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_payment_instamojo, container, false);
        parentActivity = (PaymentDetailsActivity) getActivity();
        inflateXML(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        parentActivity.updateActionBarTitle(R.string.title_fragment_choose_payment_option);
    }

    @Override
    public void inflateXML(View view) {
        GatewayOrder order = parentActivity.getOrder();
        PaymentOptions paymentOptions = order.getPaymentOptions();
        View debitCardLayout = view.findViewById(R.id.debit_card_layout);
        View creditCardLayout = view.findViewById(R.id.credit_card_layout);
        View netBankingLayout = view.findViewById(R.id.net_banking_layout);
        View emiLayout = view.findViewById(R.id.emi_layout);
        View walletLayout = view.findViewById(R.id.wallet_layout);
        View upiLayout = view.findViewById(R.id.upi_layout);

        if (paymentOptions.getNetBankingOptions() == null) {
            Logger.d(TAG, "Hiding Net banking Layout");
            netBankingLayout.setVisibility(View.GONE);
        }

        if (paymentOptions.getCardOptions() == null) {
            Logger.d(TAG, "Hiding Debit and Credit Card Layout");
            debitCardLayout.setVisibility(View.GONE);
            creditCardLayout.setVisibility(View.GONE);
        }

        if (paymentOptions.getEmiOptions() == null) {
            Logger.d(TAG, "Hiding EMI Layout");
            emiLayout.setVisibility(View.GONE);
        }

        if (paymentOptions.getWalletOptions() == null) {
            Logger.d(TAG, "Hiding Wallet Layout");
            walletLayout.setVisibility(View.GONE);
        }

        if (paymentOptions.getUpiOptions() == null) {
            Logger.d(TAG, "Hiding UPISubmission layout");
            upiLayout.setVisibility(View.GONE);
        }

        debitCardLayout.setOnClickListener(this);
        creditCardLayout.setOnClickListener(this);
        netBankingLayout.setOnClickListener(this);
        emiLayout.setOnClickListener(this);
        walletLayout.setOnClickListener(this);
        upiLayout.setOnClickListener(this);
        Logger.d(this.getClass().getSimpleName(), "Inflated XML");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.wallet_layout) {
            Logger.d(TAG, "Starting Wallet Form");
            parentActivity.loadFragment(WalletFragment.newInstance(), true);

        } else if (id == R.id.net_banking_layout) {
            Logger.d(TAG, "Starting Net Banking Form");
            parentActivity.loadFragment(NetBankingFragment.newInstance(), true);

        } else if (id == R.id.emi_layout) {
            Logger.d(TAG, "Starting EMI Form");
            parentActivity.loadFragment(new EMIFragment(), true);

        } else if (id == R.id.upi_layout) {
            Logger.d(TAG, "Starting UPISubmission Form");
            parentActivity.loadFragment(new UPIFragment(), true);

        } else {
            Logger.d(TAG, "Starting CardFragment");
            if (id == R.id.debit_card_layout) {
                parentActivity.loadFragment(CardFragment.getCardForm(CardFragment.Mode.DebitCard), true);

            } else {
                parentActivity.loadFragment(CardFragment.getCardForm(CardFragment.Mode.CreditCard), true);
            }
        }
    }

    @Override
    public String getFragmentName() {
        return FRAGMENT_NAME;
    }
}
