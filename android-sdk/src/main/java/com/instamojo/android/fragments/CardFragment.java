package com.instamojo.android.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.instamojo.android.R;
import com.instamojo.android.activities.BaseActivity;
import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.helpers.CardType;
import com.instamojo.android.helpers.CardUtil;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.helpers.ObjectMapper;
import com.instamojo.android.helpers.Validators;
import com.instamojo.android.models.Card;
import com.instamojo.android.models.CardOptions;
import com.instamojo.android.models.CardPaymentResponse;
import com.instamojo.android.models.GatewayOrder;
import com.instamojo.android.network.ImojoService;
import com.instamojo.android.network.ServiceGenerator;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass. The {@link Fragment} to get Debit Card details from user.
 */
public class CardFragment extends BaseFragment implements View.OnClickListener, View.OnKeyListener {

    private static final String TAG = CardFragment.class.getSimpleName();
    private static final String MONTH_YEAR_SEPARATOR = "/";
    private static final String FRAGMENT_NAME = "Card Form";

    private MaterialEditText cardNumberBox, cvvBox, dateBox;
    private List<MaterialEditText> editTexts;
    private PaymentDetailsActivity parentActivity;
    private Mode mode;
    private int mSelectedTenure;
    private String mSelectedBankCode;
    private int cardNumberSelection;
    private CardTextWatcher textWatcher;

    /**
     * Creates a new instance of Fragment
     */
    public CardFragment() {
        // Required empty public constructor
    }

    public static CardFragment getCardForm(Mode mode) {
        CardFragment form = new CardFragment();
        form.mode = mode;
        return form;
    }

    public static CardFragment getCardForm(Mode mode, int tenure, String bankCode) {
        CardFragment form = new CardFragment();
        form.mode = mode;
        form.mSelectedBankCode = bankCode;
        form.mSelectedTenure = tenure;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
        cardNumberBox = view.findViewById(R.id.card_number_box);
        cardNumberBox.setNextFocusDownId(R.id.card_date_box);
        cardNumberBox.addTextChangedListener(textWatcher = new CardTextWatcher());
        cardNumberBox.setOnKeyListener(this);
        cardNumberBox.addValidator(new Validators.EmptyFieldValidator());
        cardNumberBox.addValidator(new Validators.CardValidator());
        dateBox = view.findViewById(R.id.card_date_box);
        dateBox.setNextFocusDownId(R.id.cvv_box);
        cvvBox = view.findViewById(R.id.cvv_box);
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

                applyDateText(dateBox, this, modifiedDate);
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

        Button checkOutButton = view.findViewById(R.id.checkout);
        String checkoutText = "Pay ₹" + parentActivity.getOrder().getOrder().getAmount();
        checkOutButton.setText(checkoutText);
        checkOutButton.setOnClickListener(this);

        editTexts = Arrays.asList(cardNumberBox, dateBox, cvvBox);
        Logger.d(TAG, "Inflated XML");
    }

    private void applyDateText(MaterialEditText dateBox, TextWatcher textWatcher, String modifiedDate) {
        dateBox.removeTextChangedListener(textWatcher);
        dateBox.setText(modifiedDate);
        dateBox.setSelection(modifiedDate.length());
        dateBox.addTextChangedListener(textWatcher);
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
        cardNumberSelection = Math.min(cardNumberSelection, text.length());
        editText.setSelection(cardNumberSelection);
        editText.addTextChangedListener(watcher);
    }

    private void clearOptionalValidators() {
        cvvBox.clearValidators();
        dateBox.clearValidators();
        cvvBox.setError(null);
        dateBox.setError(null);
    }

    private void addOptionalValidators() {
        boolean dateValidatorAdded = false;
        boolean emptyFieldValidatorAdded = false;
        List<METValidator> validators = dateBox.getValidators();
        if (validators != null) {
            for (METValidator validator : validators) {
                if (validator instanceof Validators.DateValidator) {
                    dateValidatorAdded = true;
                }
                if (validator instanceof Validators.EmptyFieldValidator) {
                    emptyFieldValidatorAdded = true;
                }
            }
        }
        if (!emptyFieldValidatorAdded) {
            dateBox.addValidator(new Validators.EmptyFieldValidator());
        }
        if (!dateValidatorAdded) {
            dateBox.addValidator(new Validators.DateValidator());
        }

        boolean emptyFieldValidatorAddedCvv = false;
        if (cvvBox.getValidators() != null) {
            for (METValidator validator : cvvBox.getValidators()) {
                if (validator instanceof Validators.EmptyFieldValidator) {
                    emptyFieldValidatorAddedCvv = true;
                }
            }
            if (!emptyFieldValidatorAddedCvv) {
                cvvBox.addValidator(new Validators.EmptyFieldValidator());
            }
        }
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

        Logger.d(TAG, "Checking Out");
        checkOut(card);
    }

    private void checkOut(Card card) {
        parentActivity.hideKeyboard();
        changeEditBoxesState(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(R.layout.please_wait_dialog_instamojo);
        final AlertDialog dialog = builder.create();
        dialog.show();

        final GatewayOrder order = parentActivity.getOrder();
        Map<String, String> cardPaymentRequest = ObjectMapper.populateCardRequest(order, card,
                mSelectedBankCode, mSelectedTenure);

        ImojoService service = ServiceGenerator.getImojoService();
        final CardOptions cardOptions = order.getPaymentOptions().getCardOptions();
        Call<CardPaymentResponse> orderCall = service.collectCardPayment(cardOptions.getSubmissionURL(),
                cardPaymentRequest);
        Logger.d(TAG, cardOptions.getSubmissionURL());
        orderCall.enqueue(new Callback<CardPaymentResponse>() {
            @Override
            public void onResponse(Call<CardPaymentResponse> call, final Response<CardPaymentResponse> response) {

                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            changeEditBoxesState(true);
                            dialog.dismiss();

                            final Bundle bundle = new Bundle();
                            bundle.putString(Constants.URL, response.body().getUrl());
                            bundle.putString(Constants.MERCHANT_ID, cardOptions.getSubmissionData().getMerchantID());
                            bundle.putString(Constants.ORDER_ID, cardOptions.getSubmissionData().getOrderID());
                            parentActivity.startPaymentActivity(bundle);

                        } else {
                            Toast.makeText(parentActivity, R.string.error_message_juspay,
                                    Toast.LENGTH_SHORT).show();
                            if (response.errorBody() != null) {
                                try {
                                    Logger.e(TAG, "Error response from card checkout call - " +
                                            response.errorBody().string());

                                } catch (IOException e) {
                                    Logger.e(TAG, "Error reading error response: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<CardPaymentResponse> call, final Throwable t) {
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(parentActivity, R.string.error_message_juspay,
                                Toast.LENGTH_SHORT).show();
                        Logger.e(TAG, "Card checkout failed due to - " + t.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.checkout) {
            prepareCheckOut();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                cardNumberBox.removeTextChangedListener(textWatcher);
                return true;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                cardNumberBox.addTextChangedListener(textWatcher);
                return true;
            }
        }
        return false;
    }

    enum Mode {
        DebitCard,
        CreditCard,
        EMI
    }

    private class CardTextWatcher implements TextWatcher {

        private int drawable = 0;
        private int previousLength = 0, currentLength = 0;
        private CardType cardType = CardType.UNKNOWN;

        public CardTextWatcher() {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            previousLength = cardNumberBox.getText().toString().trim().length();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String cardNumber = cardNumberBox.getText().toString();
            cardNumber = cardNumber.replaceAll(" ", "");

            // TODO this will be triggered for every text change which may not be required
            // Do this only for few characters not for entire card number
            cardType = CardUtil.getCardType(cardNumber);
            drawable = cardType.getImageResource();

            if (cardType == CardType.UNKNOWN || cardType == CardType.MAESTRO) {
                clearOptionalValidators();
            } else {
                addOptionalValidators();
            }

            if (cardNumber.isEmpty()) {
                drawable = R.drawable.ic_accepted_cards;
            }

            cardNumberBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);

            if (cardNumber.length() == cardType.getNumberLength()) {
                dateBox.requestFocus();
            }
            currentLength = cardNumberBox.getText().toString().trim().length();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (currentLength < previousLength) {
                return;
            }
            String cardNumber = s.toString().trim();
            cardNumberSelection = cardNumberBox.getSelectionStart();
            if (cardNumber.length() < 4) {
                return;
            }

            String modifiedCard;
            if (currentLength > previousLength) {
                List<String> data = getCardNumberArray(cardNumber);
                modifiedCard = "";
                CardType cardType = CardUtil.getCardType(cardNumber);
                switch (cardType) {
                    case VISA:
                    case MASTER_CARD:
                    case DISCOVER:
                    case RUPAY:
                        for (int index = 0; index < data.size(); index++) {
                            modifiedCard = modifiedCard + data.get(index);
                            if (index == 3 || index == 7 || index == 11) {
                                modifiedCard = modifiedCard + " ";
                                cardNumberSelection++;
                            }
                        }

                        cvvBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cardType.getCvvLength())});
                        break;
                    case AMEX:
                        for (int index = 0; index < data.size(); index++) {
                            modifiedCard = modifiedCard + data.get(index);
                            if (index == 3 || index == 10) {
                                modifiedCard = modifiedCard + " ";
                                cardNumberSelection++;
                            }
                        }
                        cvvBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cardType.getCvvLength())});
                        break;
                    case DINERS_CLUB:
                        for (int index = 0; index < data.size(); index++) {
                            modifiedCard = modifiedCard + data.get(index);
                            if (index == 3 || index == 9) {
                                modifiedCard = modifiedCard + " ";
                                cardNumberSelection++;
                            }
                        }
                        cvvBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cardType.getCvvLength())});
                        break;
                    default:
                        modifiedCard = cardNumber;
                }
            } else {
                modifiedCard = cardNumber;
            }
            applyText(cardNumberBox, this, modifiedCard);
        }
    }

    public static List<String> getCardNumberArray(String cardNumber) {
        List<String> data = new ArrayList<>(Arrays.asList(cardNumber.replaceAll(" ", "").split("")));
        data.removeAll(Collections.singleton(""));
        return data;
    }

}
