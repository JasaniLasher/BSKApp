package com.example.bskapp.com.example.bskapp.dataapi;

import android.content.Context;

import com.example.bskapp.Helper;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AzureServiceAdapter {
    private String mMobileBackendUrl = "http://bskapitest.azurewebsites.net";
    private Context mContext;
    private MobileServiceClient mClient;
    private static AzureServiceAdapter mInstance = null;

    private AzureServiceAdapter(Context context) {
        mContext = context;
        try {
            mClient = new MobileServiceClient(mMobileBackendUrl,mContext);
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {

                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

                    httpClient.addInterceptor(new Interceptor() {

                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            Request original = chain.request();
                            String url = original.url().url().toString();
                            String tenantID = Helper.ResolveTenant(url);
                            Request request = original.newBuilder()
                                    .header("TenantID", tenantID)
                                    .method(original.method(), original.body())
                                    .build();

                            return chain.proceed(request);
                        }
                    });

                    OkHttpClient client = httpClient
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .connectionPool(new ConnectionPool(5,3,TimeUnit.MINUTES))
                            .build();
                    return client;
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void Initialize(Context context) {
        if (mInstance == null) {
            mInstance = new AzureServiceAdapter(context);
        } else {
            throw new IllegalStateException("AzureServiceAdapter is already initialized");
        }
    }

    public static AzureServiceAdapter getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("AzureServiceAdapter is not initialized");
        }
        return mInstance;
    }

    public MobileServiceClient getClient() {
        return mClient;
    }

}
