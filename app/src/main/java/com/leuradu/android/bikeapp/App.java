package com.leuradu.android.bikeapp;

import android.app.Application;

import com.backendless.Backendless;
import com.leuradu.android.bikeapp.utils.BackendSettings;
import com.leuradu.android.bikeapp.utils.Util;

import java.io.File;
import java.io.IOException;

/**
 * Created by radu on 26.03.2016.
 */
public class App extends Application {

    private static String mResourcesDirPath;

    @Override
    public void onCreate() {
        super.onCreate();
        mResourcesDirPath = Util.determineStoragePath(getApplicationContext()) + "/SKMaps/";
        Backendless.initApp(this, BackendSettings.APPLICATION_ID, BackendSettings.ANDROID_SECRET_KEY,
                BackendSettings.VERSION);
    }

    public static String getResourcesDirPath() {
        return mResourcesDirPath;
    }
}
