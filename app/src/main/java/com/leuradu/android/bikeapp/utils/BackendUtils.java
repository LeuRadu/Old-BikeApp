package com.leuradu.android.bikeapp.utils;

import android.content.Context;
import android.widget.Toast;

import com.backendless.Backendless;
import com.leuradu.android.bikeapp.R;

/**
 * Created by radu on 30.03.2016.
 */
public class BackendUtils {

    private static Context mContext;

    public static void logout(Context context) {
        mContext = context;
        LoadingCallback<Void> callback = createLogoutCallback(context);
        callback.showProgressDialog();
        Backendless.UserService.logout(callback);
    }

    private static LoadingCallback<Void> createLogoutCallback(Context context) {
        return new LoadingCallback<Void>(context,
//                TODO: use this to fix deprecated getResource methods
                context.getString(R.string.logout_wait)){
            @Override
            public void handleResponse(Void response) {
                super.handleResponse(response);
                Toast.makeText(mContext, "Log out successful", Toast.LENGTH_LONG).show();
            }
        };
    }
}
