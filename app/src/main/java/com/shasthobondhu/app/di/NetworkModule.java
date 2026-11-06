package com.shasthobondhu.app.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shasthobondhu.app.BuildConfig;
import com.shasthobondhu.app.data.remote.FirebaseService;
import com.shasthobondhu.app.data.remote.PasswordResetApi;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String PLACEHOLDER_BASE_URL = "https://shasthobondhu.invalid/";

    @Provides
    @Singleton
    public FirebaseService provideFirebaseService() {
        return new FirebaseService();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        String baseUrl = BuildConfig.PASSWORD_RESET_BASE_URL;
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = PLACEHOLDER_BASE_URL;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    public PasswordResetApi providePasswordResetApi(Retrofit retrofit) {
        return retrofit.create(PasswordResetApi.class);
    }
}
