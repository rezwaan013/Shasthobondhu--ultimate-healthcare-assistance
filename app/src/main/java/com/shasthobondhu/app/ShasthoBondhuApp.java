package com.shasthobondhu.app;

import android.app.Application;
import com.shasthobondhu.app.util.ThemeManager;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ShasthoBondhuApp extends Application {
    @Override
    public void onCreate() {
        ThemeManager.applySavedTheme(this);
        super.onCreate();
    }
}
