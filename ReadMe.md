# Instamojo SDK Integration Documentation   

##Overview
This SDK allows you to integrate payments via Instamojo into your Android app. It currently supports following modes of payments:

1. Credit / Debit Cards
2. Netbanking

This SDK also comes pre-integrated with Juspay Safe Browser, which makes payments on mobile easier, resulting in faster transaction time and improvement in conversions.

1. 1-click OTP: Auto-processing Bank SMS OTP for 1-Click experience.
2. Network optimizations: Smart 2G connection handling to reduce page load times.
3. Input & Keyboard Enhancements: Displays right keyboard with password viewing option.
4. Smooth User Experience: Aids the natural flow of users with features like Automatic Focus, Scroll/Zoom, Navigation buttons.

#Payment flow Via SDK
The section describes how the payment flow probably looks like, when you integrate with this SDK. Note that, this is just for reference and you are free to make changes to this flow that works well for you.

- When the buyer clicks on Buy button, your app makes a call to your backend to initiate a transaction in your system.
- Your backend systems create a transaction (uniquely identified by transaction_id). If required, it also obtains an access token from Instamojo servers. It passes back order details (like, buyer details, amount, transaction id etc.) and a valid Instamojo’s access token.
- Your Android app creates a new Order (from the SDK), by passing it order details and access token.
- If order is valid, the user is shown the payment modes, which will take him via the payment process as per mode selected.
- Once a payment is completed, a callback is called in your Android app with your transaction_id.
- Your Android app makes a request to your backend servers with the transaction_id.
- Your backend servers checks the status of the transaction by making a request to Instamojo’ servers. Your backend servers updates the status in the database, and passes back the result (successful / failure) to your app.
- Your app shows the success / failure screen based on the result if received from your backend.

## Sample Application 
Yes, we have a Sample app that is integrated with SDK. You can either use to a Base for your project or have a look at the integration in action.
Check out the Documentation of the Sample App [here](https://github.com/Instamojo/android-sdk-sample-app/blob/master/ReadMe.md).

## Installation   [ ![Download](https://api.bintray.com/packages/dev-accounts/maven/sdks/images/download.svg) ](https://bintray.com/dev-accounts/maven/sdks/_latestVersion)
### Include SDK
The SDK currently supports Android Version >= ICS 4.0.3(14). Just add the following to your application’s `build.gradle` file, inside the dependencies section.
```
repositories {
    mavenCentral()
    maven {
        url "https://s3-ap-southeast-1.amazonaws.com/godel-release/godel/"
    }
}

dependencies {
    compile 'com.instamojo:android-sdk:+'
}

```

### SDK Permissions
The following are the minimum set of permissions required by the SDK. Add the following set of permissions in the application’s Manifest file above the `<application>` tag.
```
//General permissions 
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />

//required for Juspay to read the OTP from the SMS sent to the device
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
```

### Proguard rules
If you are using Proguard for code obfuscation, add following rules in the proguard configuration file `proguard-rules.pro`
```
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-keep public class com.instamojo.android.network.JavaScriptInterface
-keep public class * implements com.instamojo.android.network.JavaScriptInterface
-keepclassmembers class com.instamojo.android.network.JavaScriptInterface{
    <methods>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# Keep source file names, line numbers for easier debugging
-keepattributes SourceFile,LineNumberTable

-keepattributes Signature
-dontwarn com.squareup.**
-dontwarn okio.**

# OkHttp
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# apache http
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient

# Juspay rules
-keep class in.juspay.** {*;}
-dontwarn in.juspay.**

# support class
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
```

## Generating Access Token
A valid access token should be generated on your server using your `Client ID` and `Client Secret` and the token is then passed on to the application.
Access token will be valid for 10 hours after generation. Please check this [documentation](https://github.com/Instamojo/android-sdk-sample-app/blob/master/sample-sdk-server/Readme.md#generating-access-token) 
on how to generate Access Token using the credentials.

## What is Transaction ID
Well, transaction ID is a unique ID for an Order. Using this transaction ID, you can fetch Order status, get order details, and even initiate a refund for the Order attached to that transaction ID.

The transaction ID should be unique for every Order.

## Simple Integration
### Initializing SDK
Add the following `android:name="com.instamojo.android.InstamojoApplication"` key to `<application>` tag in manifest tag
```XML

    <application
            android:name="com.instamojo.android.InstamojoApplication"
            ..... >
    </application>        
```

What if there is a custom `Application` class already?
Then, add the following line to `onCreate()` method of that custom application class.
```Java
    @Override
        public void onCreate() {
            super.onCreate();
            Instamojo.initialize(this);
            ...
        }
```

### Initiating Payment
To initiate a Payment, the following mandatory fields are required by the SDK.

1. Name of the buyer (Max 100 characters)&nbsp;
2. Email of the buyer (Max 75 characters)&nbsp;
3. Purpose of the transaction (Max 255 characters)&nbsp;
4. Phone number of the buyer &nbsp;
5. Transaction amount (Min of Rs. 9 and limited to 2 decimal points)&nbsp;
6. Access Token &nbsp;
7. Transaction ID (Max 64 characters)&nbsp;

With all the mandatory fields mentioned above, a `Order` object can created.
``` Java
Order order = new Order(accessToken, transactionID, name, email, phone, amount, purpose);
```

`Order` object must be validated locally before creating Order with Instamojo.
Add the following code snippet to validate the `Order` object.
```Java
//Validate the Order
        if (!order.isValid()){
            //oops order validation failed. Pinpoint the issue(s).

            if (!order.isValidName()){
                Log.e("App", "Buyer name is invalid");
            }

            if (!order.isValidEmail()){
                Log.e("App", "Buyer email is invalid");
            }

            if (!order.isValidPhone()){
                Log.e("App", "Buyer phone is invalid");
            }

            if (!order.isValidAmount()){
                Log.e("App", "Amount is invalid");
            }

            if (!order.isValidDescription()){
                Log.e("App", "description is invalid");
            }

            if (!order.isValidTransactionID()){
                Log.e("App", "Transaction ID is invalid");
            }

            if (!order.isValidRedirectURL()){
                Log.e("App", "Redirection URL is invalid");
            }

            return;
        }

        //Validation is successful. Proceed
```

Once `Order` is validated. Add the following snippet to create an order with Instamojo.
``` Java
// Good time to show progress dialog to user
Request request = new Request(order, new OrderRequestCallBack() {
                    @Override
                    public void onFinish(Order order, Exception error) {
                        //dismiss the dialog if showed
                        
                        // Make sure the follwoing code is called on UI thread to show Toasts or to 
                        //update UI elements 
                        if (error != null) {
                            if (error instanceof Errors.ConnectionError) {
                                  Log.e("App", "No internet connection");
                            } else if (error instanceof Errors.ServerError) {
                                  Log.e("App", "Server Error. Try again");
                            } else if (error instanceof Errors.AuthenticationError){
                                  Log.e("App", "Access token is invalid or expired");
                            } else if (error instanceof Errors.ValidationError){
                                  // Cast object to validation to pinpoint the issue
                                  Errors.ValidationError validationError = (Errors.ValidationError) error;
                                  if (!validationError.isValidTransactionID()) {
                                         Log.e("App", "Transaction ID is not Unique");
                                         return;
                                  }
                                  if (!validationError.isValidRedirectURL()) {
                                         Log.e("App", "Redirect url is invalid");
                                         return;
                                  }
                                  if (!validationError.isValidPhone()) {
                                         Log.e("App", "Buyer's Phone Number is invalid/empty");
                                         return;
                                  }
                                  if (!validationError.isValidEmail()) {
                                         Log.e("App", "Buyer's Email is invalid/empty");
                                         return;
                                  }
                                  if (!validationError.isValidAmount()) {
                                         Log.e("App", "Amount is either less than Rs.9 or has more than two decimal places");
                                         return;
                                  }
                                  if (!validationError.isValidName()) {
                                         Log.e("App", "Buyer's Name is required");
                                         return;
                                  }
                            } else {
                                  Log.e("App", error.getMessage());
                            }
                        return;
                        }

                        startPreCreatedUI(order);
                    }
                });

                request.execute();
            }
        });
```

### Displaying Payment Forms
This SDK comes by default with payment forms (Cards and Netbanking) that can be used to collect payment details from the buyer.

Add the following code snippet to your application's activity/fragment to use Pre-created UI.
``` Java
private void startPreCreatedUI(Order order){
        //Using Pre created UI
        Intent intent = new Intent(getBaseContext(), PaymentDetailsActivity.class);
        intent.putExtra(Constants.ORDER, order);
        startActivityForResult(intent, Constants.REQUEST_CODE);
}
```

And you can call this once the order is created and validated, as per the above step:
```Java
startPreCreatedUI(order);
```

### Receiving Payment result in the main activity
Add the following code snippet in the main activity.
Note that TransactionID, OrderID, and paymentID maybe null. Please do a null check before proceeding.
``` Java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && data != null) {
                    String orderID = data.getStringExtra(Constants.ORDER_ID);
                    String transactionID = data.getStringExtra(Constants.TRANSACTION_ID);
                    String paymentID = data.getStringExtra(Constants.PAYMENT_ID);
        
                    // Check transactionID, orderID, and orderID for null before using them to check the Payment status.
                    if (orderID != null && transactionID != null && paymentID != null) {
                         //Check for Payment status with Order ID or Transaction ID
                    } else {
                         //Oops!! Payment was cancelled
                    }
        }
}
```

## Using Custom Created UI
We know that every application is unique. If you choose to create your own UI to collect Payment information, SDK has necessary APIs to achieve this.
Use `CustomUIActivity` activity, which uses SDK APIs to collect Payment Information, to extend and modify as per your needs.
You can change the name of the activity to anything you like. Best way to do in Android Studio is by refactoring the name of the activity.

### Changing the Caller method
Replace `startPreCreatedUI` method wih the following one.
```Java
private void startCustomUI(Order order) {
        //Custom UI Implementation
        Intent intent = new Intent(getBaseContext(), CustomUIActivity.class);
        intent.putExtra(Constants.ORDER, order);
        startActivityForResult(intent, Constants.REQUEST_CODE);
}
```

### Fetching `order` object in the `CustomUIActivity`
To fetch the passed `order` object in the `CustomUIActivity`. Use the following snippet.
```Java
final Order order = getIntent().getParcelableExtra(Constants.ORDER);
```

### Collecting Card Details
#### Validating Card Option
Always validate whether the current order has card payment enabled. You can check for `null` for the card options.
```Java
if (order.getCardOptions() == null) {
   //seems like card payment is not enabled. Make the necessary UI Changes.
} else{
   // Card payment is enabled.
}
```

#### Creating and validating `Card` deatils
Once the user has typed in all the card details and ready to proceed, you can create the `Card` object.
```Java
Card card = new Card();
card.setCardNumber(cardNumber.getText().toString());
card.setDate(cardExpiryDate.getText().toString());
card.setCardHolderName(cardHoldersName.getText().toString());
card.setCvv(cvv.getText().toString());

//Validate the card now
if (!card.isCardValid()) {

   if (!card.isCardNameValid()) {
        Log.e("App", "Card Holders Name is invalid");
   }

   if (!card.isCardNumberValid()) {
        Log.e("App", "Card Number is invalid");
   }

   if (!card.isDateValid()) {
        Log.e("App", "Expiry date is invalid");
   }

   if (!card.isCVVValid()) {
        Log.e("App", "CVV is invalid");
   }

   //return so that user can correct card details
   return;
}
```

#### Generating Juspay Bundle using Card 
Once the card details are validated, You need to generate JusPay Bundle with the card details given
```Java
//Good time to show progress dialog while the bundle is generated
Request request = new Request(order, card, new JusPayRequestCallback() {
            @Override
            public void onFinish(final Bundle bundle, final Exception error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Dismiss the dialog here if showed.
                        
                        // Make sure the follwoing code is called on UI thread to show Toasts or to 
                        //update UI elements
                        if (error != null) {
                             if (error instanceof Errors.ConnectionError){
                                    Log.e("App", "No internet connection");
                             } else if (error instanceof Errors.ServerError){
                                    Log.e("App", "Server Error. Try again");
                             } else {
                                    Log.e("App", error.getMessage());
                             }
                             return;
                        }
                        
                        // Everything is fine. Pass the bundle to start payment Activity
                        startPaymentActivity(bundle);
                    }
                });
            }
        });
request.execute();

```

### Collecting Netbanking Details
#### Validating Netbanking Option
Similar to Card Options, Netbanking options can be disabled. Check Netbanking Options for `null`
```Java
if (order.getNetBankingOptions() == null) {
   //seems like Netbanking option is not enabled. Make the necessary UI Changes.
} else{
   // Netbanking is enabled.
}
```

#### Displaying available Banks
The Bank and Its code set can be fetched from `order` itself.
```Java
order.getNetBankingOptions().getBanks();
```
The above code snippet will return a `HashMap<String, String>` with key as bank name and value as bank code.
Use an android Spinner or List view to display the available banks and collect the bank code of the bank user selects.

#### Generating Juspay Bundle using Bank code
Once the bank code is collected, You can generate the Juspay Bundle using the following snippet.
```Java
//User selected a Bank. Hence proceed to Juspay
Bundle bundle = new Bundle();
bundle.putString(Constants.URL, order.getNetBankingOptions().getUrl());
bundle.putString(Constants.POST_DATA, order.getNetBankingOptions().getPostData(order.getAuthToken(), bankCode));

//Pass the bundle to start payment Activity
startPaymentActivity(bundle)
```

### Starting the payment Activity using the bundle
Add the following method to the activity which will start the Payment Activity with the Juspay Bundle.
```Java
private void startPaymentActivity(Bundle bundle) {
        // Start the payment activity
        //Do not change this unless you know what you are doing
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra(Constants.PAYMENT_BUNDLE, bundle);
        startActivityForResult(intent, Constants.REQUEST_CODE);
 }
```

### Passing the result back to main Activity
Paste the following snippet to pass the result to main activity.
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //send back the result to Main activity
        if (requestCode == Constants.REQUEST_CODE) {
            setResult(resultCode);
            setIntent(data);
            finish();
        }
}
```

## Integration with Test Environment
To do the integration in a test environment, add the following code snippet at any point in the code.
```Java
Instamojo.setBaseUrl("https://test.instamojo.com/");
```
You can remove this line to use production environment, before releasing the app.

## Verbose logging
Detailed logs can very useful during SDK Integration, especially while debugging any issues in integration. To enable verbose logging, add the following code snippet at any point in the code:
``` Java
Instamojo.setLogLevel(Log.DEBUG);
```
Log level defaults to `Log.WARN`.

Once the application is ready to be pushed to the Play Store, simply remove the line of code.