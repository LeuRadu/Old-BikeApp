package com.leuradu.android.bikeapp.utils;

import android.app.ProgressDialog;
import android.content.Context;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.leuradu.android.bikeapp.R;

/**
 * Created by radu on 21.03.2016.
 */
public class LoadingCallback<T> implements AsyncCallback<T> {

    Context mContext;
    ProgressDialog mProgressDialog;

    public LoadingCallback(Context context) {
        this(context, context.getString(R.string.loading));
    }

    public LoadingCallback(Context context, String loadingMess) {
        mContext = context;
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(loadingMess);
    }

    @Override
    public void handleResponse(T response) {
        mProgressDialog.dismiss();
    }

    @Override
    public void handleFault(BackendlessFault fault) {
        mProgressDialog.dismiss();
        Util.createErrorDialog(mContext, "Backendless Fault", fault.getMessage());
    }

    public void showProgressDialog()
    {
        mProgressDialog.show();
    }

}
