package com.leuradu.android.bikeapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.controller.Controller;
import com.leuradu.android.bikeapp.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by radu on 11.04.2016.
 */
public class EventInfoFragment extends DialogFragment {

    public static final String ARG_ID = "id";

    private Controller ctr;
    private Event mEvent;

    private TextView mTextTitle;
    private TextView mTextDescr;
    private TextView mTextDate;
    private TextView mTextTime;
    private TextView mTextUser;
    private Button mButtonOk;

    public static EventInfoFragment newInstance(Controller controller, int id) {
        Bundle args = new Bundle();

        args.putInt(ARG_ID, id);
        EventInfoFragment fragment = new EventInfoFragment();
        fragment.setController(controller);
        fragment.setArguments(args);
        return fragment;
    }

    public void setController(Controller c) {
        ctr = c;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_event_info, null);

        int id = getArguments().getInt(ARG_ID);
        mEvent = ctr.getEvent(id);

        mTextTitle = (TextView) v.findViewById(R.id.text_title);
        mTextDescr = (TextView) v.findViewById(R.id.text_description);
        mTextDate = (TextView) v.findViewById(R.id.text_date);
        mTextTime = (TextView) v.findViewById(R.id.text_time);
        mTextUser = (TextView) v.findViewById(R.id.text_user);
        mButtonOk = (Button) v.findViewById(R.id.button_ok);

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setData();

        return new AlertDialog.Builder(getActivity())
                .setView(v).create();
    }

    private void setData() {
        mTextTitle.setText(mEvent.getName());
        mTextDescr.setText(mEvent.getDescription());

        Date date = mEvent.getDate();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String month = new SimpleDateFormat("MMM").format(c.getTime());
        int year = c.get(Calendar.YEAR);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String dateString = "Date: " + day + " " + month + " " + year;
        String timeString = "Time: " + hour + ":" + minute;

        mTextDate.setText(dateString);
        mTextTime.setText(timeString);
        mTextUser.setText("Added by " + mEvent.getUserId());
    }
}
