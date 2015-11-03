package com.sknutti.capstoneproject.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.sknutti.capstoneproject.AppointmentActivity;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment {

    public static DatePickerFragment newInstance(int year, int month, int day) {
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        final Calendar c = Calendar.getInstance();
        int year = args.getInt("year", c.get(Calendar.YEAR));
        int month = args.getInt("month", c.get(Calendar.MONTH));
        int day = args.getInt("day", c.get(Calendar.DAY_OF_MONTH));

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), (AppointmentActivity)getActivity(), year, month, day);
    }
}
