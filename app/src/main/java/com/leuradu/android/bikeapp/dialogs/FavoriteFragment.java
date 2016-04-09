package com.leuradu.android.bikeapp.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.controller.Controller;
import com.leuradu.android.bikeapp.repository.BackendManager;
import com.leuradu.android.bikeapp.utils.Validator;
import com.skobbler.ngx.SKCoordinate;

/**
 * Created by radu on 09.04.2016.
 */
public class FavoriteFragment extends DialogFragment {

    public static final String ARG_LAT = "lat";
    public static final String ARG_LON = "lon";

    private Controller controller;

    private EditText mTextName;
    private EditText mTextDescription;
    private Button mButtonAdd;
    private Button mButtonCancel;

    private double lat;
    private double lon;

    public static FavoriteFragment newInstance(SKCoordinate point, Controller controller) {
        Bundle args = new Bundle();

        args.putDouble(ARG_LAT, point.getLatitude());
        args.putDouble(ARG_LON, point.getLongitude());
        FavoriteFragment fragment = new FavoriteFragment();
        fragment.setController(controller);
        fragment.setArguments(args);
        return fragment;
    }

    public void setController(Controller c) {
        controller = c;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        lat = getArguments().getDouble(ARG_LAT);
        lon = getArguments().getDouble(ARG_LON);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_favorite,null);

        mTextName = (EditText) v.findViewById(R.id.text_name);
        mTextDescription = (EditText) v.findViewById(R.id.text_description);
        mButtonAdd = (Button) v.findViewById(R.id.button_add);
        mButtonCancel = (Button) v.findViewById(R.id.button_cancel);

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mTextName.getText().toString();
                String description = mTextDescription.getText().toString();
                if (!Validator.isNameValid(getActivity(), name) ||
                    !Validator.isTextValid(getActivity(),description)){
                    return;
                }
                controller.saveFavorite(lon, lat, name, description);
                dismiss();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }
}
