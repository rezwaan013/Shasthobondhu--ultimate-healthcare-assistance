package com.shasthobondhu.app;

import android.app.Application;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shasthobondhu.app.databinding.ActivityMainBinding;
import androidx.navigation.NavController;
import androidx.navigation.NavigationUI;
import dagger.hilt.android.AndroidApp;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@HiltAndroidApp
public class ShasthoBondhuApp extends Application {
}
