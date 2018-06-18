package instamojo.library.API;

import android.os.AsyncTask;

import java.io.IOException;

import instamojo.library.Callback;
import instamojo.library.REST.Post;

/**
 * Created by shardullavekar on 15/11/17.
 */

public class CreateOrder {

    public CreateOrder() {

    }

    public void post(String url, String token, String paymentId, Callback callback) {
        CreateOrderAsync createOrderAsync = new CreateOrderAsync(callback);
        createOrderAsync.execute(new String[]{url, token, paymentId});
    }

    private class CreateOrderAsync extends AsyncTask<String, Void, String> {
        Callback callback;
        public CreateOrderAsync(Callback callback) {
            this.callback = callback;
        }
        @Override
        protected String doInBackground(String... params) {
            Post post = new Post();
            String response = null;
            try {
                response = post.createOrder(params[0], params[1], params[2]);
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
