package com.leuradu.android.bikeapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.backendless.Backendless;
import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.utils.BackendSettings;

public class MainActivity extends AppCompatActivity {

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Backendless.initApp(this, BackendSettings.APPLICATION_ID, BackendSettings.ANDROID_SECRET_KEY,
                BackendSettings.VERSION);

        mButton = (Button) findViewById(R.id.button_main_register);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(MapActivity.newIntent(MainActivity.this));
            }
        });
    }


}
