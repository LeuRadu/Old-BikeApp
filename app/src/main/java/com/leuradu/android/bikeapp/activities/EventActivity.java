package com.leuradu.android.bikeapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.leuradu.android.bikeapp.controller.Controller;
import com.leuradu.android.bikeapp.fragments.EventFragment;
import com.skobbler.ngx.SKCoordinate;

/**
 * Created by radu on 09.04.2016.
 */
public class EventActivity extends SingleFragmentActivity {

    private static SKCoordinate mPoint;
    private static Controller controller;

    public static Intent newIntent(Context context, SKCoordinate point, Controller c) {
        mPoint = point;
        controller = c;
        Intent intent = new Intent(context, EventActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return EventFragment.newInstance(mPoint,controller);
    }
}
