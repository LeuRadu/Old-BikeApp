package com.leuradu.android.bikeapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.leuradu.android.bikeapp.App;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.utils.Util;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.map.SKMapFragment;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;

import java.io.File;
import java.io.IOException;

/**
 * Created by radu on 26.03.2016.
 */
public class StartActivity extends AppCompatActivity implements SKPrepareMapTextureListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        prepareMaps();
        copyResources();
    }

    private void prepareMaps() {
        final SKPrepareMapTextureThread prepThread =
                new SKPrepareMapTextureThread(this, App.getResourcesDirPath(), "SKMaps.zip", this);
        prepThread.start();
    }

    @Override
    public void onMapTexturesPrepared(boolean b) {

        SKMapViewStyle mapViewStyle = new SKMapViewStyle(App.getResourcesDirPath() + "daystyle/", "daystyle.json");
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();
        initMapSettings.setMapDetailLevel(SKMapsInitSettings.SK_MAP_DETAIL_LIGHT);
        initMapSettings.setCurrentMapViewStyle(mapViewStyle);
        initMapSettings.setMapResourcesPath(App.getResourcesDirPath());
        initMapSettings.setMapsPath(App.getResourcesDirPath());

        final SKAdvisorSettings advisorSettings = new SKAdvisorSettings();
        advisorSettings.setAdvisorConfigPath(App.getResourcesDirPath() + "/Advisor");
        advisorSettings.setResourcePath(App.getResourcesDirPath() + "/Advisor/Languages");
        advisorSettings.setLanguage(SKAdvisorSettings.SKAdvisorLanguage.LANGUAGE_EN);
        advisorSettings.setAdvisorVoice("en");
        initMapSettings.setAdvisorSettings(advisorSettings);

        SKMaps.getInstance().initializeSKMaps(this, initMapSettings);

        startMap();
    }

    private void startMap() {
        finish();
        startActivity(MapActivity.newIntent(this));
    }

    private void copyResources() {
        new Thread() {
            public void run() {
                copyResource("GPXTRacks");
                copyResource("Graphs");
            }
        }.start();
    }

    private void copyResource(String s) {
        try {
            String tracksPath = App.getResourcesDirPath() + s;
            File tracksDir = new File(tracksPath);
            if (!tracksDir.exists()) {
                tracksDir.mkdirs();
            }
            Util.copyAssetsToFolder(getAssets(), s, App.getResourcesDirPath() + s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
