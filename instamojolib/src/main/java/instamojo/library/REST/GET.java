package instamojo.library.REST;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by shardullavekar on 18/10/17.
 */

public class GET {
    OkHttpClient client = new OkHttpClient();

    public String getStatus(String url, String accessToken) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String getToken(String url) throws IOException {
        Request request = new Request.Builder()
                          .url(url)
                          .build();
        Response response = client.newCall(request).execute();
        return response.body().string();

    }
}
