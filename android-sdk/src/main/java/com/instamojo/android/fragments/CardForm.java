package com.instamojo.android.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.instamojo.android.R;
import com.instamojo.android.activities.BaseActivity;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.callbacks.JusPayRequestCallback;
import com.instamojo.android.helpers.CardValidator;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.helpers.Validators;
import com.instamojo.android.models.Card;
import com.instamojo.android.network.Request;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass. The {@link Fragment} to get Debit Card details from user.
 */
public class CardForm extends BaseFragment implements View.OnClickListener {

    private static final String MONTH_YEAR_SEPARATOR = "/";
    private static final String FRAGMENT_NAME = "Card Form";

    private MaterialEditText cardNumberBox, cvvBox, dateBox;
    private List<MaterialEditText> editTexts;
    private PaymentDetailsActivity parentActivity;
    private Mode mode;

    /**
     * Creates a new instance of Fragment
     */
    public CardForm() {
        // Required empty public constructor
    }

    public static CardForm getCardForm(Mode mode) {
        CardForm form = new CardForm();
        form.mode = mode;
        return form;
    }

    private static boolean isEditBoxesValid(List<MaterialEditText> editTexts) {
        boolean allValid = true;
        for (MaterialEditText editText : editTexts) {
            allValid = editText.validate() && allValid;
        }
        return allValid;
    }

    @Override
    public String getFragmentName() {
        return FRAGMENT_NAME;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_form_instamojo, container, false);
        parentActivity = (PaymentDetailsActivity) getActivity();
        inflateXML(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        int title = R.string.title_fragment_debit_card_form;
        if (mode == Mode.CreditCard) {
            title = R.string.title_fragment_credit_card_form;
        } else if (mode == Mode.EMI) {
            title = R.string.emi_on_credit_card;
        }
        parentActivity.updateActionBarTitle(title);
    }

    @Override
    public void inflateXML(View view) {
        cardNumberBox = (MaterialEditText) view.findViewById(R.id.card_number_box);
        cardNumberBox.setNextFocusDownId(R.id.card_date_box);
        cardNumberBox.addTextChangedListener(new CardTextWatcher());
        cardNumberBox.addValidator(new Validators.EmptyFieldValidator());
        cardNumberBox.addValidator(new Validators.CardValidator());
        dateBox = (MaterialEditText) view.findViewById(R.id.card_date_box);
        dateBox.setNextFocusDownId(R.id.cvv_box);
        cvvBox = (MaterialEditText) view.findViewById(R.id.cvv_box);
        dateBox.addTextChangedListener(new TextWatcher() {

            private int previousLength = 0, currentLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = dateBox.getText().toString().trim().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentLength = dateBox.getText().toString().trim().length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String date = s.toString().trim().replaceAll(" ", "");
                String modifiedDate = date;

                boolean backPressed = previousLength > currentLength;
                if (backPressed) {
                    if (date.length() == 3) {
                        modifiedDate = date.substring(0, 2);
                    }
                } else {
                    if (date.length() == 2) {
                        modifiedDate = date + MONTH_YEAR_SEPARATOR;
                    } else if (previousLength == 2) {
                        modifiedDate = date.substring(0, 2) + MONTH_YEAR_SEPARATOR + date.substring(2, date.length());
                    }
                }

                applyText(dateBox, this, modifiedDate);
                if (modifiedDate.length() == 5 && dateBox.validate()) {
                    cvvBox.requestFocus();
                }
            }
        });

        cardNumberBox.post(new Runnable() {
            @Override
            public void run() {
                cardNumberBox.requestFocus();
                InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (lManager != null) {
                    lManager.showSoftInput(cardNumberBox, InputMethodManager.SHOW_IMPLICIT);
                }

            }
        });

        Button checkOutButton = (Button) view.findViewById(R.id.checkout);
        String checkoutText = "Pay ₹" + parentActivity.getOrder().getAmount();
        checkOutButton.setText(checkoutText);
        checkOutButton.setOnClickListener(this);

        editTexts = Arrays.asList(cardNumberBox, dateBox, cvvBox);
        Logger.logDebug(this.getClass().getSimpleName(), "Inflated XML");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ((BaseActivity) getActivity()).hideKeyboard();
        }
    }

    private void applyText(MaterialEditText editText, TextWatcher watcher, String text) {
        editText.removeTextChangedListener(watcher);
        editText.setText(text);
        editText.setSelection(editText.getText().toString().length());
        editText.addTextChangedListener(watcher);
    }

    private void clearOptionalValidators() {
        cvvBox.clearValidators();
        dateBox.clearValidators();
        cvvBox.setError(null);
        dateBox.setError(null);
    }

    private void addOptionalValidators() {
        dateBox.addValidator(new Validators.EmptyFieldValidator());
        dateBox.addValidator(new Validators.DateValidator());
        cvvBox.addValidator(new Validators.EmptyFieldValidator());
    }

    private void changeEditBoxesState(boolean enable) {
        cardNumberBox.setEnabled(enable);
        dateBox.setEnabled(enable);
        cvvBox.setEnabled(enable);
    }

    private void prepareCheckOut() {
        if (!isEditBoxesValid(editTexts)) {
            return;
        }

        Card card = new Card();
        String cardNumber = cardNumberBox.getText().toString().trim();
        cardNumber = cardNumber.replaceAll(" ", "");
        card.setCardNumber(cardNumber);
        String date = dateBox.getText().toString().trim();
        if (date.isEmpty()) {
            card.setDate("12/49");
        } else {
            if (!dateBox.validateWith(new Validators.DateValidator())) {
                return;
            } else {
                card.setDate(date);
            }
        }

        String cvv = cvvBox.getText().toString().trim();
        if (cvv.isEmpty()) {
            card.setCvv("111");
        } else {
            card.setCvv(cvv);
        }

        Logger.logDebug(this.getClass().getSimpleName(), "Checking Out");
        checkOut(card);
    }

    private void checkOut(Card card) {
        parentActivity.hideKeyboard();
        changeEditBoxesState(false);
        final ProgressDialog dialog = ProgressDialog.show(parentActivity, "", getString(R.string.please_wait), true, false);
        Request request = new Request(parentActivity.getOrder(), card, new JusPayRequestCallback() {
            @Override
            public void onFinish(final Bundle bundle, final Exception error) {
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeEditBoxesState(true);
                        dialog.dismiss();
                        if (error != null) {
                            Toast.makeText(parentActivity, R.string.error_message_juspay,
                                    Toast.LENGTH_SHORT).show();
                            Logger.logError(this.getClass().getSimpleName(), "Card checkout failed due to - " + error.getMessage());
                            return;
                        }
                        parentActivity.startPaymentActivity(bundle);
                    }
                });
            }
        });
        request.execute();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.checkout) {
            prepareCheckOut();
        }
    }

    enum Mode {
        DebitCard,
        CreditCard,
        EMI
    }

    private class CardTextWatcher implements TextWatcher {

        private int limit = -1;
        private int drawable = 0;
        private int previousLength = 0, currentLength = 0;

        public CardTextWatcher() {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            previousLength = cardNumberBox.getText().toString().trim().length();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String card = cardNumberBox.getText().toString();
            card = card.replaceAll(" ", "");
            if (card.length() == 4) {
                drawable = CardValidator.getCardDrawable(card);
                limit = CardValidator.validateCardTypeWithoutLengthForLimit(card);
                if (CardValidator.maestroCard(card)) {
                    clearOptionalValidators();
                } else {
                    addOptionalValidators();
                }
            } else if (card.length() < 4) {
                drawable = R.drawable.ic_accepted_cards;
                clearOptionalValidators();
            }

            cardNumberBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);

            if (card.length() == limit) {
                dateBox.requestFocus();
            }
            currentLength = cardNumberBox.getText().toString().trim().length();
        }

        @Override
        public void afterTextChanged(Editable s) {
            String card = s.toString().trim();
            if (card.length() < 4) {
                return;
            }

            String modifiedCard;
            if (currentLength > previousLength) {
                String[] data = card.replaceAll(" ", "").split("");
                modifiedCard = "";
                if (CardValidator.masterCardWithoutLength(card) || CardValidator.visaCardWithoutLength(card)
                        || CardValidator.discoverCardWithoutLength(card)) {
                    for (int index = 1; index < data.length; index++) {
                        modifiedCard = modifiedCard + data[index];
                        if (index == 4 || index == 8 || index == 12) {
                            modifiedCard = modifiedCard + " ";
                        }
                    }
                } else if (CardValidator.amexCardWithoutLength(card)) {
                    for (int index = 1; index < data.length; index++) {
                        modifiedCard = modifiedCard + data[index];
                        if (index == 4 || index == 11) {
                            modifiedCard = modifiedCard + " ";
                        }
                    }
                } else if (CardValidator.dinnersClubIntWithoutLength(card)) {
                    for (int index = 1; index < data.length; index++) {
                        modifiedCard = modifiedCard + data[index];
                        if (index == 4 || index == 10) {
                            modifiedCard = modifiedCard + " ";
                        }
                    }
                } else {
                    modifiedCard = card;
                }
            } else {
                modifiedCard = card;
            }

            applyText(cardNumberBox, this, modifiedCard);
        }
    }

}
