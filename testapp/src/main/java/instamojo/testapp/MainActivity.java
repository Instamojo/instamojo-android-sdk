package instamojo.testapp;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import instamojo.library.InstamojoPay;
import instamojo.library.InstapayListener;

public class MainActivity extends AppCompatActivity {
    Button pay;
    InstapayListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        pay = (Button) this.findViewById(R.id.pay);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = "nd.dhokia@gmail.com";
                String phone = "9033630540";
                String amount = "10";
                String purpose = "official";
                String buyername = "tester";
                callInstamojoPay(email, phone, amount, purpose, buyername);
            }
        });
    }

    private void callInstamojoPay(String email, String phone, String amount, String purpose, String buyername) {
        final Activity activity = this;
        InstamojoPay instamojoPay = new InstamojoPay();
        IntentFilter filter = new IntentFilter("ai.devsupport.instamojo");
        registerReceiver(instamojoPay, filter);
        JSONObject pay = new JSONObject();
        try {
            pay.put("email", email);
            pay.put("phone", phone);
            pay.put("purpose", purpose);
            pay.put("amount", amount);
            pay.put("name", buyername);
            pay.put("send_sms", true);
            pay.put("send_email", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initListener();
        instamojoPay.start(activity, pay, listener);
    }

    private void initListener() {
        listener = new InstapayListener() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onFailure(int code, String reason) {
                Toast.makeText(getApplicationContext(), "Failed: " + reason, Toast.LENGTH_LONG)
                        .show();
            }
        };
    }

}
