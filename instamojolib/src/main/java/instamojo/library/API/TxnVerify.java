package instamojo.library.API;

import android.os.AsyncTask;

import java.io.IOException;

import instamojo.library.Callback;
import instamojo.library.REST.GET;

/**
 * Created by shardullavekar on 18/07/17.
 */

public class TxnVerify {

    public void get(String url, String token, String paymentID, Callback callback) {
        TxnVerifyasync txnVerifyasync = new TxnVerifyasync(url, token, paymentID, callback);
        txnVerifyasync.execute();
    }

    private class TxnVerifyasync extends AsyncTask<Void, Void, String> {
        Callback callback;

        String url, paymentId, token;

        public TxnVerifyasync(String url, String token, String paymentID, Callback callback) {
            this.callback = callback;
            this.paymentId = paymentID;
            this.url = url;
            this.token = token;
        }

        @Override
        protected String doInBackground(Void... params) {
            GET get = new GET();

            String response = null;

            try {
                response = get.getStatus(url + paymentId, token);
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
