package com.instamojo.android.models;

import androidx.annotation.NonNull;

import com.instamojo.android.helpers.CardUtil;

/**
 * Card object to hold the User card details.
 */
public class Card {

    private String cardHolderName;

    private String cardNumber;

    private String date;

    private String cvv;

    private boolean saveCard = false;

    /**
     * Constructor for Card.
     */
    public Card() {
    }

    public String getCardHolderName() {
        return cardHolderName;
    }


    public void setCardHolderName(@NonNull String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }


    public void setCardNumber(@NonNull String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getDate() {
        return date;
    }

    /**
     * @param date Must be NonNull and should be in MM/yy format.
     *             Add 12/49 as default date for Maestro Card.
     *             Else {@link NullPointerException} will be thrown while making Juspay Safe browser.
     */
    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(@NonNull String cvv) {
        this.cvv = cvv;
    }

    public String getMonth() {
        return this.date.split("/")[0];
    }

    public String getYear() {
        return this.date.split("/")[1];
    }

    public boolean canSaveCard() {
        return saveCard;
    }

    /**
     * Check if the card Name is Valid.
     *
     * @return True if not Null and not Empty. Else False.
     */
    public boolean isCardNameValid() {
        return this.cardHolderName != null && !this.cardHolderName.isEmpty();
    }

    /**
     * Checks if expiry date is valid
     */
    public boolean isDateValid() {
        if (date != null && !date.isEmpty()) {
            return !CardUtil.isDateInValid(this.date);
        }

        // Expiry is optional for MAESTRO card
        return CardUtil.isMaestroCard(this.cardNumber);
    }

    /**
     * Checks if CVV is valid
     */
    public boolean isCVVValid() {
        if (cvv != null && !cvv.isEmpty()) {
            return true;
        }

        // CVV is optional for MAESTRO card
        return CardUtil.isMaestroCard(this.cardNumber);
    }

    /**
     * Check if the Card Number is Valid using Luhn's algorithm.
     * Takes care of all the Edge cases. Requires atleast first four digits of the card.
     *
     * @return True if Valid. Else False.
     */
    public boolean isCardNumberValid() {
        return CardUtil.isCardNumberValid(cardNumber);
    }

    /**
     * Check if the all the card details are valid.
     * if False, use
     * {@link Card#isCardNameValid()},
     * {@link Card#isCardNumberValid()},
     * {@link Card#isCVVValid()},
     * {@link Card#isDateValid()}
     * to pinpoint the which field failed.
     *
     * @return True if Valid. Else False.
     */
    public boolean isCardValid() {
        return isCardNameValid() && isDateValid() && isCVVValid() && isCardNumberValid();
    }
}