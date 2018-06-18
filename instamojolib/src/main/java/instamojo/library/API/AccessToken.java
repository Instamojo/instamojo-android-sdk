package instamojo.library.API;

import android.os.AsyncTask;

import java.io.IOException;

import instamojo.library.Callback;
import instamojo.library.REST.GET;

/**
 * Created by shardullavekar on 04/12/17.
 */

public class AccessToken {

    public AccessToken() {

    }

    public void get(String url, Callback callback) {
        AccessTokenAsyn accessTokenAsyn = new AccessTokenAsyn(url, callback);
        accessTokenAsyn.execute();
    }

    private class AccessTokenAsyn extends AsyncTask<Void, Void, String> {
        String url; Callback callback;
        public AccessTokenAsyn(String url, Callback callback) {
            this.url = url;
            this.callback = callback;
        }
        @Override
        protected String doInBackground(Void... params) {
            GET get = new GET();
            String token = null;
            try {
                token = get.getToken(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            callback.onResponse(s);
        }
    }
}
