package com.leuradu.android.bikeapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.controller.Controller;
import com.leuradu.android.bikeapp.dialogs.DatePickerFragment;
import com.leuradu.android.bikeapp.dialogs.TimePickerFragment;
import com.leuradu.android.bikeapp.repository.BackendManager;
import com.leuradu.android.bikeapp.utils.Validator;
import com.skobbler.ngx.SKCoordinate;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by radu on 09.04.2016.
 */
public class EventFragment extends Fragment {

    private static final int REQUEST_DATE = 1;
    private static final int REQUEST_TIME = 2;
    private static final String ARG_LAT = "lat";
    private static final String ARG_LON = "lon";
    private static final String TAG_DATE = "date";

    private Date mDate;
    private Controller controller;

    private EditText mTextName;
    private EditText mTextDescr;
    private Button mButtonDate;
    private Button mButtonTime;
    private Button mButtonAdd;
    private Button mButtonCancel;

    public static EventFragment newInstance(SKCoordinate point, Controller controller) {
        Bundle args = new Bundle();

        args.putDouble(ARG_LON, point.getLongitude());
        args.putDouble(ARG_LAT, point.getLatitude());
        EventFragment fragment = new EventFragment();
        fragment.setController(controller);
        fragment.setArguments(args);

        return fragment;
    }

    public void setController(Controller c) {
        controller = c;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_add_event, null);

        mTextName = (EditText) v.findViewById(R.id.text_name);
        mTextDescr = (EditText) v.findViewById(R.id.text_description);
        mButtonDate = (Button) v.findViewById(R.id.button_date);
        mButtonTime = (Button) v.findViewById(R.id.button_time);
        mButtonAdd = (Button) v.findViewById(R.id.button_add);
        mButtonCancel = (Button) v.findViewById(R.id.button_cancel);

        mDate = new Date();

        mButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment fragment = DatePickerFragment.newInstance(mDate);
                fragment.setTargetFragment(EventFragment.this, REQUEST_DATE);
                fragment.show(fm, TAG_DATE);
            }
        });

        mButtonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                TimePickerFragment fragment = TimePickerFragment.newInstance(mDate);
                fragment.setTargetFragment(EventFragment.this, REQUEST_TIME);
                fragment.show(fm, TAG_DATE);
            }
        });

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mTextName.getText().toString();
                String descr = mTextDescr.getText().toString();
                double lon = getArguments().getDouble(ARG_LON);
                double lat = getArguments().getDouble(ARG_LAT);

                if (isValidInput(name, descr)) {
                    controller.saveEvent(lon, lat, name, descr, mDate);
                }
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Date date;
        switch (requestCode) {
            case REQUEST_DATE:
                date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                modifyDate(date);
                updateDate();
                break;
            case REQUEST_TIME:
                date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_DATE);
                modifyTime(date);
                updateTime();
                break;
        }
    }

    private void modifyDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(mDate);
        newCalendar.set(year, month, day);

        mDate = newCalendar.getTime();
    }

    private void modifyTime(Date date) {
        Calendar time = Calendar.getInstance();
        time.setTime(date);

        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        mDate = calendar.getTime();
    }

    private void updateDate() {
        String date = DateFormat.format("EEEE, MMM d, yyyy", mDate).toString();
        mButtonDate.setText(date);
    }

    private void updateTime() {
        String time = DateFormat.format("hh:mm", mDate).toString();
        mButtonTime.setText(time);
    }

    private boolean isValidInput(String name, String descr) {
        Context context = getActivity();
        return (Validator.isNameValid(context,name) &&
                Validator.isTextValid(context,descr));
    }
}
