package com.instamojo.android.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
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

/**
 * A simple {@link Fragment} subclass. The {@link Fragment} to show Net Banking options to User.
 */
public class WalletFragment extends BaseFragment implements SearchView.OnQueryTextListener {

    private static final String TAG = WalletFragment.class.getSimpleName();
    private PaymentDetailsActivity parentActivity;
    private LinearLayout listContainer;
    private TextView headerTextView;

    /**
     * Creates a new Instance of Fragment.
     */
    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance() {
        return new WalletFragment();
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_form_instamojo, container, false);
        parentActivity = (PaymentDetailsActivity) getActivity();
        inflateXML(view);
        loadAllWallets();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        headerTextView.setText(R.string.choose_your_wallet);
        parentActivity.updateActionBarTitle(R.string.wallets);
        parentActivity.showSearchOption(getString(R.string.search_your_wallet), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        parentActivity.hideSearchOption();
    }

    @Override
    public void inflateXML(View view) {
        listContainer = view.findViewById(R.id.list_container);
        headerTextView = view.findViewById(R.id.header_text);
        Logger.d(TAG, "Inflated XML");
    }

    private void loadAllWallets() {
        loadWallets("");
    }

    private void loadWallets(String query) {
        for (final Wallet wallet : parentActivity.getOrder().getWalletOptions().getWallets()) {
            if (!wallet.getName().toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US))) {
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        loadWallets(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        loadWallets(newText);
        return false;
    }
}
