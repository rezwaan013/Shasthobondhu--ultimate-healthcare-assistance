package com.shasthobondhu.app;

import android.app.Application;
import com.shasthobondhu.app.di.PresenceEntryPoint;
import com.shasthobondhu.app.util.SessionManager;
import com.shasthobondhu.app.util.ThemeManager;
import dagger.hilt.android.HiltAndroidApp;
import dagger.hilt.android.EntryPointAccessors;

@HiltAndroidApp
public class ShasthoBondhuApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
        PresenceEntryPoint entryPoint = EntryPointAccessors.fromApplication(
                this, PresenceEntryPoint.class);
        if (!SessionManager.isLocalMode(this)) {
            entryPoint.presenceRepository().startPresenceForCurrentUser();
        }
    }
}
