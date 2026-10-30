package com.shasthobondhu.app.di;

import com.shasthobondhu.app.data.remote.FirebaseService;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    @Provides
    @Singleton
    public FirebaseService provideFirebaseService() {
        return new FirebaseService();
    }
}
