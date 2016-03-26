package com.leuradu.android.bikeapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.leuradu.android.bikeapp.fragments.RegistrationFragment;

/**
 * Created by radu on 19.03.2016.
 */
public class RegistrationActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, RegistrationActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new RegistrationFragment();
    }
}
