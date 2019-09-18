# Instamojo Android SDK 

## Table of Contents

   * [Overview](#overview)
   * [Payment flow using SDK](#payment-flow-using-sdk)
   * [Sample Application](#sample-application)
   * [SDK Installation](#sdk-installation)
     * [SDK Dependency](#sdk-dependency)
     * [SDK Permissions](#sdk-permissions)
     * [Proguard rules](#proguard-rules)
   * [Fetch Order ID from backend server](#fetch-order-id-from-backend-server)
   * [Simple Integration](#simple-integration)
     * [Initializing SDK](#initializing-sdk)
     * [Initiating Payment](#initiating-payment)
     * [Receiving response from the SDK](#receiving-response-from-the-sdk)
   * [Using Custom UI](#using-custom-ui)
    
##***Note:SDK currently does not support MarketPlace Integration. MarketPlace API Documentation is available [here](https://docs.instamojo.com/v2/docs)***

## Overview
This SDK allows you to integrate payments via Instamojo into your Android app. It currently supports following modes of payments:

1. Credit / Debit Cards
2. EMI
3. Net Banking
4. Wallets
5. UPI

This SDK also comes pre-integrated with Juspay Safe Browser, which makes payments on mobile easier, resulting in faster transaction time and improvement in conversions.

1. 1-click OTP: Auto-processing Bank SMS OTP for 1-Click experience.
2. Network optimizations: Smart 2G connection handling to reduce page load times.
3. Input & Keyboard Enhancements: Displays right keyboard with password viewing option.
4. Smooth User Experience: Aids the natural flow of users with features like Automatic Focus, Scroll/Zoom, Navigation buttons.

## Payment flow using SDK
The section describes how the payment flow probably looks like, when you integrate with this SDK. Note that, this is just for reference and you are free to make changes to this flow that works well for you.

- When the buyer clicks on Buy button, your app makes a call to your backend to initiate a transaction in your system.
- Your backend systems creates a payment order and returns the `orderID` to the app. See the [sample app](https://github.com/Instamojo/instamojo-android-sdk/tree/readme_update/sample-app) and [sample backend](https://github.com/Instamojo/sample-sdk-server) for more details.
- The app then initiates a payment via Instamojo SDK using the `orderID`.
- If `orderID` is valid, the user is shown the available payment modes. This will take him via the payment process as per mode selected.
- Once a payment is completed, a callback is received in your android activity with the `payment_id`, `transaction_id` and `payment_status`.
- Your app shows the success / failure screen based on the payment status (either `Credit` or `Failure`).
- For more details on the payment, your app may make a request to your backend servers with the `transaction_id`, which then returns the details after fetching the same from Instamojo servers.


## Sample Application 
A sample app that uses the latest SDK code is provided under `/sample-app` folder. You can either use it as a base for your project or have a look at the integration in action.
Check out the documentation of the sample app [here](https://github.com/Instamojo/instamojo-android-sdk/tree/master/sample-app/Readme.md).

## SDK Installation
### SDK Dependency
The SDK currently supports Android Version >= ICS 4.0.3 (15). Just add the following to your application’s `build.gradle` file, inside the dependencies section.
```groovy
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
The following are automatically added to your app manifest as required by the SDK.
```xml
//General permissions 
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

//required for Juspay to read the OTP from the SMS sent to the device
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
```

You can remove the SMS permission from your app (as per new [Google's Guidelines](https://support.google.com/googleplay/android-developer/answer/9047303))
```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" tools:node="remove" />
<uses-permission android:name="android.permission.READ_SMS" tools:node="remove" />
<uses-permission android:name="android.permission.RECEIVE_SMS" tools:node="remove" />
```

### Proguard rules
If you are using Proguard for code obfuscation, add following rules in the proguard configuration file `proguard-rules.pro`.
```
# Rules for Instamojo SDK
-keep class com.instamojo.android.**{*;}
```

## Fetch Order ID from backend server
For initiating a payment, you should first create a payment order in your backend server and send the orderID to the app.
Please check this [documentation](https://github.com/Instamojo/sample-sdk-server/blob/master/Readme.md) 
on how to create a payment order on your backend server using your Instamojo client credentials.


## Simple Integration
### Initializing SDK
Add the following code snippet inside the `onCreate()` method of your activity class.
```java
@Override
public void onCreate() {
    super.onCreate();
    Instamojo.getInstance().initialize(this, Instamojo.Environment.TEST);
    ...
}
```

### Implement the callback interface
Implement the `Instamojo.InstamojoPaymentCallback` callback interface in your activity class.
```java
public class MainActivity extends AppCompatActivity implements Instamojo.InstamojoPaymentCallback {
    ...
    
    @Override
    public void onInstamojoPaymentComplete(String orderID, String transactionID, String paymentID, String paymentStatus) {
        ...
    }

    @Override
    public void onPaymentCancelled() {
        ...
    }

    @Override
    public void onInitiatePaymentFailure(String errorMessage) {
        ...
    }
}

```
### Initiating Payment
Now you can simply call `initiatePayment` with the `orderID` and the callback instance (which is the activity itself) to initiate a payment.
```java
Instamojo.getInstance().initiatePayment(this, orderID, this);
```

### Receiving response from the SDK
Once the payment is initiated from your activity, it can receive the various SDK responses in the callback methods.

1. Payment through SDK is complete. The payment can be either a success or a failure
```java
@Override
public void onInstamojoPaymentComplete(String orderID, String transactionID, String paymentID, String paymentStatus) {
    Log.d(TAG, "Payment complete. Order ID: " + orderID + ", Transaction ID: " + transactionID
            + ", Payment ID:" + paymentID + ", Status: " + paymentStatus);
}
```

2. Payment is cancelled by the user
```java
@Override
public void onPaymentCancelled() {
    Log.d(TAG, "Payment cancelled");
}
```

3. There was a error while initiating a payment (eg: the orderID is invalid)
```java
@Override
public void onInitiatePaymentFailure(String errorMessage) {
    Log.d(TAG, "Initiate payment failed");
    showToast("Initiating payment failed. Error: " + errorMessage);
}
```

## Using Custom UI
We know that every application is unique. If you choose to create your own UI to collect payment information, Instamojo SDK has the necessary APIs to achieve this.
Check out the [CustomUIActivity](https://github.com/Instamojo/instamojo-android-sdk/blob/master/sample-app/src/main/java/com/instamojo/androidsdksample/CustomUIActivity.java) activity with in the sample app to find out how this can be achieved.

## Integrate the SDK using DevsupportAI (DEPRECATED and uses SDK version 1.2.6)
Find the integration documentation for SDK v1.2.6 using DevsupportAI [here](https://docs.instamojo.com/v1.1/page/devsupport-ai-android-integration).

## I have queries regarding the SDK Integration
If you still have queries regarding SDK integration, please send a mail to our support id: [support@instamojo.com](mailto:support@instamojo.com). We will respond ASAP.
