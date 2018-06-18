package instamojo.library;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shardullavekar on 05/09/17.
 */

public class InstamojoPay extends BroadcastReceiver {
    String amountstr, email, phone, name, description, purpose, webhook;
    boolean send_sms, send_email;
    JSONObject payment;
    Activity activity;
    InstapayListener listener;

    public void start(Activity activity, JSONObject payment, InstapayListener listener){
        this.activity = activity;
        this.payment = payment;
        this.listener = listener;
        initInstamojo();
    }

    private void initInstamojo() {

        try {
            amountstr = payment.getString("amount");

            purpose = payment.getString("purpose");

            email = payment.getString("email");

            phone = payment.getString("phone");

            name = payment.getString("name");

            Intent intent = new Intent(activity, Instamojo.class);

            if (payment.has("webhook")) {
                webhook = payment.getString("webhook");
                intent.putExtra("webhook", webhook);
            }

            if (payment.has("send_sms")) {
                send_sms = payment.getBoolean("send_sms");
                intent.putExtra("send_sms", send_sms);
            }

            if (payment.has("send_email")) {
                send_email = payment.getBoolean("send_email");
                intent.putExtra("send_email", send_email);
            }

            intent.putExtra("email", email);
            intent.putExtra("phone", phone);
            intent.putExtra("purpose", purpose);
            intent.putExtra("amount", amountstr);
            intent.putExtra("name", name);


            activity.startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
            listener.onFailure(Config.FAILED, "One of the params in JSON is missing");
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int code = intent.getIntExtra("code", Config.FAILED);
        if (code == Config.SUCCESS) {
            listener.onSuccess(intent.getStringExtra("response"));
        }
        else {
            listener.onFailure(Config.FAILED, intent.getStringExtra("response"));
        }
    }
}
