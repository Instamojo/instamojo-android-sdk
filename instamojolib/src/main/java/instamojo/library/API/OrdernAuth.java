package instamojo.library.API;

import android.os.AsyncTask;

import java.io.IOException;

import instamojo.library.Callback;
import instamojo.library.REST.Post;

/**
 * Created by shardullavekar on 18/07/17.
 */

public class OrdernAuth {
    public OrdernAuth() {

    }

    public void post(String url, String email, String phone, String name, String amount, String purpose, Callback callback) {
        OrdernAuthasynch authasynch = new OrdernAuthasynch(url, email, phone, name, amount, purpose, callback);
        authasynch.execute();
    }

    private class OrdernAuthasynch extends AsyncTask<Void, Void, String> {
        Callback callback;

        String url, amount, email, phone, purpose, name;

        public OrdernAuthasynch(String url, String email, String phone, String name, String amount, String purpose, Callback callback) {
            this.callback = callback;
            this.email = email;
            this.phone = phone;
            this.name = name;
            this.purpose = purpose;
            this.amount = amount;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            Post post = new Post();

            String response = null;

            try {
                response = post.postOrdernAuth(url, name, email, phone, purpose, amount);
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

