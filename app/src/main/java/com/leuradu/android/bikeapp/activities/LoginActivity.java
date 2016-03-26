package com.leuradu.android.bikeapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.leuradu.android.bikeapp.fragments.LoginFragment;

/**
 * Created by radu on 22.03.2016.
 */
public class LoginActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }
}
