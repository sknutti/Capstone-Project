package com.sknutti.capstoneproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.sknutti.capstoneproject.data.SchedulerContract;
import com.sknutti.capstoneproject.dialogs.DatePickerFragment;
import com.sknutti.capstoneproject.dialogs.TimePickerFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import timber.log.Timber;

public class AppointmentActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener,
                    TimePickerDialog.OnTimeSetListener {

    DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy", Locale.US);
    DateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

    private Subscription apptSubscription;

    public static final String[] APPOINTMENT_COLUMNS = {
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry._ID,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_MEMBER_KEY,
            "m." + SchedulerContract.MemberEntry.COLUMN_NAME,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEW_LIST_KEY,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEWER_KEY,
            "i." + SchedulerContract.MemberEntry.COLUMN_NAME,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_DATETIME,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_LOCATION,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_COMPLETED,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_NOTES
    };

    public static final String[] INDIVIDUAL_APPOINTMENT_COLUMNS = {
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry._ID,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_MEMBER_KEY,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEW_LIST_KEY,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEWER_KEY,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_DATETIME,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_LOCATION,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_COMPLETED,
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_NOTES
    };

    public static final int COL_APPT_ID = 0;
    public static final int COL_MEMBER_KEY = 1;
    public static final int COL_MEMBER_NAME = 2;
    public static final int COL_INTERVIEW_LIST_KEY = 3;
    public static final int COL_INTERVIEWER_KEY = 4;
    public static final int COL_INTERVIEWER_NAME = 5;
    public static final int COL_DATETIME = 6;
    public static final int COL_LOCATION = 7;
    public static final int COL_COMPLETED = 8;
    public static final int COL_NOTES = 9;
    // for the individual appointment
    public static final int COL_I_APPT_ID = 0;
    public static final int COL_I_MEMBER_KEY = 1;
    public static final int COL_I_INTERVIEW_LIST_KEY = 2;
    public static final int COL_I_INTERVIEWER_KEY = 3;
    public static final int COL_I_DATETIME = 4;
    public static final int COL_I_LOCATION = 5;
    public static final int COL_I_COMPLETED = 6;
    public static final int COL_I_NOTES = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView id = (TextView) findViewById(R.id.appointment_id);
        TextView interviewListId = (TextView) findViewById(R.id.appointment_int_list_id);
        Spinner memberSpinner = (Spinner) findViewById(R.id.appointment_name_spinner);
        Spinner interviewerSpinner = (Spinner) findViewById(R.id.appointment_interviewer_spinner);
        TextView datePicker = (TextView) findViewById(R.id.appointment_date);
        TextView timePicker = (TextView) findViewById(R.id.appointment_time);
        TextView location = (TextView) findViewById(R.id.appointment_location);
        TextView notes = (TextView) findViewById(R.id.appointment_notes);
        CheckBox completed = (CheckBox) findViewById(R.id.appointment_is_completed);

        Cursor memberCursor = getContentResolver().query(
                SchedulerContract.MemberEntry.CONTENT_URI,
                MemberListActivity.MEMBER_COLUMNS,
                SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " = ? ",
                new String[]{"0"},
                SchedulerContract.MemberEntry.COLUMN_NAME + " ASC"
        );
        SimpleCursorAdapter memberAdapter = new SimpleCursorAdapter(
                this, // context
                android.R.layout.simple_spinner_item, // layout file
                memberCursor, // DB cursor
                new String[]{SchedulerContract.MemberEntry.COLUMN_NAME}, // data to bind to the UI
                new int[]{android.R.id.text1}, // views that'll represent the data from `fromColumns`
                0
        );
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memberSpinner.setAdapter(memberAdapter);

        Cursor interviewerCursor = getContentResolver().query(
                SchedulerContract.MemberEntry.CONTENT_URI,
                MemberListActivity.MEMBER_COLUMNS,
                SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " = ? ",
                new String[]{"1"},
                SchedulerContract.MemberEntry.COLUMN_NAME + " ASC"
        );
        SimpleCursorAdapter interviewerAdapter = new SimpleCursorAdapter(
                this, // context
                android.R.layout.simple_spinner_item, // layout file
                interviewerCursor, // DB cursor
                new String[]{SchedulerContract.MemberEntry.COLUMN_NAME}, // data to bind to the UI
                new int[]{android.R.id.text1}, // views that'll represent the data from `fromColumns`
                0
        );
        interviewerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interviewerSpinner.setAdapter(interviewerAdapter);

        RxView.clicks(datePicker).subscribe(aVoid -> {
            DatePickerFragment dialogFragment = new DatePickerFragment();
            dialogFragment.show(getSupportFragmentManager(), dialogFragment.getClass().getName());
        });

        RxView.clicks(timePicker).subscribe(aVoid -> {
            TimePickerFragment dialogFragment = new TimePickerFragment();
            dialogFragment.show(getSupportFragmentManager(), dialogFragment.getClass().getName());
        });

        Button cancel = (Button) findViewById(R.id.appointment_cancel);
        RxView.clicks(cancel).subscribe(aVoid -> {
            Intent intent = new Intent(AppointmentActivity.this, MonthScheduleActivity.class);
            startActivity(intent);
        });

        Button save = (Button) findViewById(R.id.appointment_save);
        RxView.clicks(save)
                .doOnNext(aVoid -> {
                    String dateTimeString = datePicker.getText().toString() + " " + timePicker.getText().toString();

                    long dateTime = 0L;
                    try {
                        SimpleDateFormat f = new SimpleDateFormat("MMM dd yyyy h:mma");
                        Date tempDate = f.parse(dateTimeString);
                        dateTime = tempDate.getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    ContentValues apptValues = new ContentValues();

                    String intListId;
                    if (interviewListId.getText().length() > 0) {
                        intListId = interviewListId.getText().toString();
                    } else {
                        intListId = "1";
                    }
                    apptValues.put(SchedulerContract.AppointmentEntry.COLUMN_INTERVIEW_LIST_KEY, intListId);
                    apptValues.put(SchedulerContract.AppointmentEntry.COLUMN_MEMBER_KEY, String.valueOf(memberSpinner.getSelectedItemId()));
                    apptValues.put(SchedulerContract.AppointmentEntry.COLUMN_INTERVIEWER_KEY, String.valueOf(interviewerSpinner.getSelectedItemId()));
                    apptValues.put(SchedulerContract.AppointmentEntry.COLUMN_DATETIME, dateTime);
                    apptValues.put(SchedulerContract.AppointmentEntry.COLUMN_LOCATION, location.getText().toString());
                    apptValues.put(SchedulerContract.AppointmentEntry.COLUMN_NOTES, notes.getText().toString());
                    apptValues.put(SchedulerContract.AppointmentEntry.COLUMN_COMPLETED, completed.isChecked() ? 1 : 0);

                    boolean result = false;
                    if (id.getText().length() == 0) {
                        Uri insertedUri = getContentResolver().insert(
                                SchedulerContract.AppointmentEntry.CONTENT_URI,
                                apptValues
                        );
//                        long memberId = ContentUris.parseId(insertedUri);
                        result = (insertedUri != null);
                    } else {
                        int numUpdated = getContentResolver().update(
                                SchedulerContract.AppointmentEntry.CONTENT_URI,
                                apptValues,
                                SchedulerContract.AppointmentEntry.TABLE_NAME + "." + SchedulerContract.AppointmentEntry.COLUMN_ID + " = ?",
                                new String[]{id.getText().toString()}
                        );
                        result = (numUpdated > 0);
                    }

                    if (result) {
                        Toast.makeText(this, "Saved Appointment Successfully!", Toast.LENGTH_SHORT).show();
                    }

                    Intent intent = new Intent(AppointmentActivity.this, MonthScheduleActivity.class);
                    startActivity(intent);
                })
                .subscribe();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long selectedAppointmentId = Long.parseLong(extras.getString("selectedAppointment"));
            toolbar.setTitle("Edit Appointment");
            apptSubscription = Observable.create(new Observable.OnSubscribe<Cursor>() {
                @Override
                public void call(Subscriber<? super Cursor> observer) {
                    try {
                        if (!observer.isUnsubscribed()) {
                            Cursor apptCursor = getContentResolver().query(
                                    SchedulerContract.AppointmentEntry.buildAppointmentUri(selectedAppointmentId),
                                    AppointmentActivity.INDIVIDUAL_APPOINTMENT_COLUMNS,
                                    SchedulerContract.AppointmentEntry.COLUMN_ID + " = ?",
                                    new String[]{String.valueOf(selectedAppointmentId)},
                                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " ASC"
                            );
                            observer.onNext(apptCursor);
                            observer.onCompleted();
                        }
                    } catch (Exception e) {
                        observer.onError(e);
                    }
                }
            }).subscribe(new Subscriber<Cursor>() {
                @Override
                public void onNext(Cursor cursor) {
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            id.setText(cursor.getString(COL_I_APPT_ID));
                            interviewListId.setText(cursor.getString(COL_I_INTERVIEW_LIST_KEY));
                            Utility.selectSpinnerItemByValue(memberSpinner, cursor.getLong(COL_I_MEMBER_KEY));
                            Utility.selectSpinnerItemByValue(interviewerSpinner, cursor.getLong(COL_I_INTERVIEWER_KEY));
                            datePicker.setText(dateFormat.format(new Date(cursor.getLong(COL_I_DATETIME))));
                            timePicker.setText(timeFormat.format(new Date(cursor.getLong(COL_I_DATETIME))));
                            location.setText(cursor.getString(COL_I_LOCATION));
                            completed.setChecked(cursor.getInt(COL_I_COMPLETED) == 1);
                            notes.setText(cursor.getString(COL_I_NOTES));
                        }
                        cursor.close();
                    }
                }

                @Override
                public void onError(Throwable error) {
                    System.err.println("Error: " + error.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Completed the appointment lookup...");
                }
            });
        } else {
            datePicker.setText(dateFormat.format(new Date(System.currentTimeMillis())));
            timePicker.setText(timeFormat.format(new Date(System.currentTimeMillis())));
        }

//        if (savedInstanceState != null) {
//            savedInstanceState.getInt(STATE_SCORE);
//        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save the user's current game state
//        savedInstanceState.putInt(STATE_SCORE, mCurrentScore);
//        savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(savedInstanceState);
//    }

    @Override
    public void onPause() {
        if (!apptSubscription.isUnsubscribed()) {
            apptSubscription.unsubscribe();
        }
        super.onPause();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = (new StringBuilder()
                // Month is 0 based so add 1
                .append(year).append("-").append(month + 1).append("-")
                .append(day)).toString();
        Timber.w("Date = " + date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = null;
        try {
            newDate = format.parse(date);
        } catch (ParseException e) {
            Timber.w(e, "Error parsing date from datepicker dialog");
        }
        ((TextView) findViewById(R.id.appointment_date)).setText(dateFormat.format(newDate));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hour = String.valueOf(hourOfDay);
        String min = String.format("%02d", minute);
        String ampm = "AM";
        if (hourOfDay > 12) {
            hour = String.valueOf(hourOfDay - 12);
            ampm = "PM";
        }
        String time = (new StringBuilder().append(hour).append(":").append(min).append("").append(ampm)).toString();
        Timber.w("Time = " + time);
        ((TextView) findViewById(R.id.appointment_time)).setText(time);
    }
}
