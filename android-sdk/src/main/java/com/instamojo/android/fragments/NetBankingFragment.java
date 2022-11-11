package com.instamojo.android.fragments;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.instamojo.android.R;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.adapters.BankListAdapter;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.models.Bank;
import com.instamojo.android.models.GatewayOrder;
import com.instamojo.android.models.NetBankingOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment to show Net Banking options to User.
 */
public class NetBankingFragment extends BaseFragment implements SearchView.OnQueryTextListener {

    private static final String TAG = NetBankingFragment.class.getSimpleName();
    private PaymentDetailsActivity parentActivity;
    private ListView listView;
    private TextView headerTextView;

    /**
     * Creates a new Instance of Fragment.
     */
    public NetBankingFragment() {
        // Required empty public constructor
    }

    public static NetBankingFragment newInstance() {
        return new NetBankingFragment();
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_form_instamojo, container, false);
        parentActivity = (PaymentDetailsActivity) getActivity();
        inflateXML(view);
        loadAllBanks();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        headerTextView.setText(R.string.choose_your_bank);
        parentActivity.updateActionBarTitle(R.string.net_banking);
        parentActivity.showSearchOption(getString(R.string.search_your_bank), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        parentActivity.hideSearchOption();
    }

    @Override
    public void inflateXML(View view) {
        listView = view.findViewById(R.id.list_container);
        headerTextView = view.findViewById(R.id.header_text);
        Logger.d(TAG, "Inflated XML");
    }

    private void loadAllBanks() {
        loadBanks("");
    }

    private void loadBanks(String query) {
        final GatewayOrder order = parentActivity.getOrder();
        final NetBankingOptions netBankingOptions = order.getPaymentOptions().getNetBankingOptions();

        final List<Bank> filteredBanks = new ArrayList<>();
        for (final Bank bank : netBankingOptions.getBanks()) {
            if (bank.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredBanks.add(bank);
            }
        }

        Collections.sort(filteredBanks);

        BankListAdapter adapter = new BankListAdapter(getActivity(), filteredBanks);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.URL, netBankingOptions.getSubmissionURL());
                bundle.putString(Constants.POST_DATA, netBankingOptions.getPostData(filteredBanks.get(position).getId()));
                parentActivity.startPaymentActivity(bundle);
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        loadBanks(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        loadBanks(newText);
        return false;
    }
}
