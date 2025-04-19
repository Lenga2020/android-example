package com.lena.android.api;

import androidx.annotation.NonNull;

import com.lena.android.App;
import com.lena.android.utils.VerifyUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitManager {
    private final static boolean ENABLE_DEBUG = false;

    private static boolean instanced = false;
    private volatile static RetrofitManager retrofitManager = null;

    private final OkHttpClient mOkHttpClient;
    private Retrofit mDefaultRetrofit;

    private String cachedBaseURL;

    private RetrofitManager() {
        synchronized (RetrofitManager.class) {
            if (instanced || null != retrofitManager) {
                final String error = String.format(Locale.getDefault(), "instantiate multiple %s(constructor).", this.getClass().getName());
                throw new RuntimeException(error);
            }
            instanced = true;

            final boolean isDebug = ENABLE_DEBUG && App.app != null && !App.app.isReleaseMode();
            final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(35, TimeUnit.SECONDS)
                    .readTimeout(35, TimeUnit.SECONDS)
                    .writeTimeout(35, TimeUnit.SECONDS)
                    .addInterceptor(this::addHeaders);
            if (isDebug) {
                final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                clientBuilder.addInterceptor(loggingInterceptor);
            }
            mOkHttpClient = clientBuilder.build();
        }
    }

    public static RetrofitManager getInstance() {
        if (retrofitManager == null) {
            synchronized (RetrofitManager.class) {
                if (retrofitManager == null) {
                    retrofitManager = new RetrofitManager();
                }
            }
        }
        return retrofitManager;
    }

    public void initDefaultRetrofit(final String baseUrl) {
        if (VerifyUtil.aValidBaseUrl(baseUrl)) {
            if (null == mDefaultRetrofit || !baseUrl.equals(cachedBaseURL)) {
                mDefaultRetrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .client(mOkHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            cachedBaseURL = baseUrl;
        } else {
            mDefaultRetrofit = null;
        }
    }

    public Retrofit getRetrofit() {
        return mDefaultRetrofit;
    }

    public Retrofit getRetrofit(String baseUrl) {
        if (VerifyUtil.aValidBaseUrl(baseUrl)) {
            return new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(mOkHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return null;
    }

    private Response addHeaders(@NonNull final Interceptor.Chain chain) throws IOException {
        final Request baseRequest = chain.request();
        final Request request = baseRequest.newBuilder()
                .header("User-Agent", getAppName())
                .header("Authorization", getToken())
                .build();
        return chain.proceed(request);
    }

    private String getAppName() {
        return App.app != null ? App.app.getApplicationId() : "Android";
    }

    private String getToken() {
        return String.format(Locale.getDefault(), "Bearer %s",  null == App.app? "": App.app.getApplicationId());
    }
}
