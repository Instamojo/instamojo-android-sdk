package com.instamojo.android.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instamojo.android.R;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.models.Order;
import com.instamojo.android.models.Wallet;

import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass. The {@link Fragment} to show Net Banking options to User.
 */
public class ListForm extends BaseFragment implements SearchView.OnQueryTextListener {

    private PaymentDetailsActivity parentActivity;
    private LinearLayout listContainer;
    private TextView headerTextView;
    private Mode mode;

    /**
     * Creates a new Instance of Fragment.
     */
    public ListForm() {
        // Required empty public constructor
    }

    public static ListForm getListFormFragment(Mode mode) {
        ListForm form = new ListForm();
        form.mode = mode;
        return form;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_form_instamojo, container, false);
        parentActivity = (PaymentDetailsActivity) getActivity();
        inflateXML(view);
        loadList("");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mode == Mode.NetBanking) {
            headerTextView.setText(R.string.choose_your_bank);
            parentActivity.updateActionBarTitle(R.string.net_banking);
        } else {
            headerTextView.setText(R.string.choose_your_wallet);
            parentActivity.updateActionBarTitle(R.string.wallets);
        }
        parentActivity.setShowSearch(true, this, mode);
    }

    @Override
    public void onPause() {
        super.onPause();
        parentActivity.setShowSearch(false, this, mode);
    }

    @Override
    public void inflateXML(View view) {
        listContainer = (LinearLayout) view.findViewById(R.id.list_container);
        headerTextView = (TextView) view.findViewById(R.id.header_text);
        Logger.logDebug(this.getClass().getSimpleName(), "Inflated XML");
    }

    private void loadBanks(String queryText) {
        for (final Map.Entry<String, String> bank : parentActivity.getOrder().getNetBankingOptions().getBanks().entrySet()) {
            if (!bank.getKey().toLowerCase(Locale.US).contains(queryText.toLowerCase(Locale.US))) {
                continue;
            }
            View bankView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_instamojo, listContainer, false);
            ((TextView) bankView.findViewById(R.id.item_name)).setText(bank.getKey());
            bankView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    Order order = parentActivity.getOrder();
                    bundle.putString(Constants.URL, order.getNetBankingOptions().getUrl());
                    bundle.putString(Constants.POST_DATA, order.getNetBankingOptions().
                            getPostData(bank.getValue()));
                    parentActivity.startPaymentActivity(bundle);
                }
            });

            listContainer.addView(bankView);
        }
    }

    private void loadWallets(String queryText) {
        for (final Wallet wallet : parentActivity.getOrder().getWalletOptions().getWallets()) {
            if (!wallet.getName().toLowerCase(Locale.US).contains(queryText.toLowerCase(Locale.US))) {
                continue;
            }
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_instamojo, listContainer, false);
            ((TextView) itemView.findViewById(R.id.item_name)).setText(wallet.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    Order order = parentActivity.getOrder();
                    bundle.putString(Constants.URL, order.getWalletOptions().getUrl());
                    bundle.putString(Constants.POST_DATA, order.getWalletOptions().
                            getPostData(wallet.getWalletID()));
                    parentActivity.startPaymentActivity(bundle);
                }
            });

            listContainer.addView(itemView);
        }
    }

    private void loadList(String queryText) {
        listContainer.removeAllViews();
        if (mode == Mode.Wallet) {
            loadWallets(queryText);
        } else {
            loadBanks(queryText);
        }

        Logger.logDebug(this.getClass().getSimpleName(), "Loaded list matching Query text - " + queryText);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        loadList(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        loadList(newText);
        return false;
    }

    public enum Mode {
        Wallet,
        NetBanking
    }
}
