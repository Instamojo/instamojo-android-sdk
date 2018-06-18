package instamojo.library.REST;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by shardullavekar on 03/07/17.
 */

public class Post {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public String postOrdernAuth(String url, String name, String email, String phone, String purpose, String amount) throws IOException {

        Request request = new Request.Builder()
                .url(url + "?action=new_transaction&name=" + name + "&email=" + email + "&phone=" + phone + "&amount=" + amount
                + "&purpose=" + purpose + "&client_type=Android")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String createOrder(String url, String accessToken, String id) throws IOException {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(JSON, requestJson.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String createRequest(String url, String redirectURL, String webhook, String accessToken, String name, String email, String phone, String purpose, String amount, boolean send_email, boolean send_sms) throws IOException {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("purpose", purpose);
            requestJson.put("amount", amount);
            requestJson.put("buyer_name", name);
            requestJson.put("email", email);
            requestJson.put("phone", phone);
            requestJson.put("webhook", webhook);
            requestJson.put("redirect_url", redirectURL);
            requestJson.put("purpose", purpose);
            requestJson.put("send_email", send_email);
            requestJson.put("send_sms", send_sms);
            requestJson.put("allow_repeated_payments", "False");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(JSON, requestJson.toString());

        Request request = new Request.Builder()
                              .url(url)
                              .post(requestBody)
                              .addHeader("Authorization", "Bearer " + accessToken)
                              .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
