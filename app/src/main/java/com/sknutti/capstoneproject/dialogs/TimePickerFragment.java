package com.sknutti.capstoneproject.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import com.sknutti.capstoneproject.AppointmentActivity;

import java.util.Calendar;


public class TimePickerFragment extends DialogFragment {

    public static TimePickerFragment newInstance(int hour, int minute, String ampm) {
        Bundle args = new Bundle();
        args.putInt("hour", hour);
        args.putInt("minute", minute);
        args.putString("ampm", ampm);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();

        final Calendar c = Calendar.getInstance();
        String ampm = args.getString("ampm", "");
        int hour = args.getInt("hour", c.get(Calendar.HOUR_OF_DAY));
        if (hour == 12 && ampm.equals("AM")) {
            hour = 0;
        }
        if (hour > 0 && hour < 12 && ampm.equals("PM")) {
            hour = hour + 12;
        }

        int minute = args.getInt("minute", c.get(Calendar.MINUTE));

        // Create a new instance of DatePickerDialog and return it
        return new TimePickerDialog(getActivity(), (AppointmentActivity)getActivity(), hour, minute, DateFormat.is24HourFormat(getActivity()));
    }
}
