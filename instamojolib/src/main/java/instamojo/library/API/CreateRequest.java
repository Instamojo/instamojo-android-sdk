package instamojo.library.API;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.IOException;

import instamojo.library.Callback;
import instamojo.library.Config;
import instamojo.library.REST.Post;

/**
 * Created by shardullavekar on 15/11/17.
 */

public class CreateRequest {
    Context context;
    String redirectUrl;
    public CreateRequest(Context context) {
        this.context = context;
        String env = Config.getEnv(this.context);
        if (TextUtils.equals(env, Config.PROD)) {
            redirectUrl = "https://www.instamojo.com/integrations/android/redirect/";
        }
        else if (TextUtils.equals(env, Config.TEST)){
            redirectUrl = "https://test.instamojo.com/integrations/android/redirect/";
        }
        else {
            return;
        }
    }

    public void post(String url, String token, String amount, String email, String phone, String purpose, String buyer_name, String webhook, boolean send_sms, boolean send_email, Callback callback) {
        CreateRequestAsynch createRequestAsynch = new CreateRequestAsynch(url, token, amount, email, phone, purpose, buyer_name, webhook, send_sms, send_email, callback);
        createRequestAsynch.execute();
    }

    private class CreateRequestAsynch extends AsyncTask<Void, Void, String> {
        Callback callback;

        String url, amount, email, phone, purpose, buyer_name, webhook, token;

        boolean send_sms, send_email;

        public CreateRequestAsynch (String url, String token, String amount, String email, String phone, String purpose, String buyer_name, String webhook, boolean send_sms, boolean send_email, Callback callback) {
            this.url = url;
            this.amount = amount;
            this.email = email;
            this.phone = phone;
            this.purpose = purpose;
            this.buyer_name = buyer_name;
            this.webhook = webhook;
            this.callback = callback;
            this.token = token;
            this.send_email = send_email;
            this.send_sms = send_sms;
        }

        protected String doInBackground(Void... params) {
            Post post = new Post();
            String response = null;
            try {
                response = post.createRequest(url, redirectUrl, webhook, token, buyer_name, email, phone, purpose, amount, send_email, send_sms);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onResponse(s);
        }
    }
}
