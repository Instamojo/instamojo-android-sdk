package com.instamojo.android.network;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.instamojo.android.BuildConfig;
import com.instamojo.android.Instamojo;
import com.instamojo.android.helpers.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static final String TAG = ServiceGenerator.class.getSimpleName();
    private static final String PRODUCTION_BASE_URL = "https://api.instamojo.com/";
    private static final String TEST_BASE_URL = "https://test.instamojo.com/";

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(new DefaultHeadersInterceptor())
            .build();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit;

    static {
        initialize(Instamojo.Environment.TEST);
    }

    public static void initialize(Instamojo.Environment environment) {
        String baseUrl = (environment == Instamojo.Environment.PRODUCTION) ? PRODUCTION_BASE_URL : TEST_BASE_URL;
        Logger.d(TAG, "Using base URL: " + baseUrl);
        retrofit = builder.baseUrl(baseUrl).build();
    }

    public static ImojoService getImojoService() {
        return retrofit.create(ImojoService.class);
    }

    private static class DefaultHeadersInterceptor implements Interceptor {
        private String userAgent;
        private String referer;

        @Override
        public Response intercept(Chain chain) throws IOException {
            return chain.proceed(chain.request()
                    .newBuilder()
                    .header("User-Agent", getUserAgent())
                    .header("Referer", getReferer())
                    .build());
        }

        private String getUserAgent() {
            if (userAgent == null || userAgent.isEmpty()) {
                userAgent = "instamojo-android-sdk/" + BuildConfig.VERSION_NAME
                        + " android/" + Build.VERSION.RELEASE
                        + " " + Build.BRAND + "/" + Build.MODEL;
            }

            return this.userAgent;
        }

        private String getReferer() {
            if (referer == null || referer.isEmpty()) {
                Context appContext = Instamojo.getInstance().getContext();

                if (appContext == null) {
                    return "";
                }

                referer = "android-app://" + appContext.getPackageName();

                try {
                    referer += "/" + appContext.getPackageManager()
                            .getPackageInfo(appContext.getPackageName(), 0).versionName;

                } catch (PackageManager.NameNotFoundException e) {
                    Logger.e(TAG, "Unable to get version of the current application.");
                }
            }

            return referer;
        }
    }
}
