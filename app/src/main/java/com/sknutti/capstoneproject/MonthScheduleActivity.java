package com.sknutti.capstoneproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@SuppressLint("SimpleDateFormat")
public class MonthScheduleActivity extends BaseActivity {

    private Subscription apptSubscription;
    private Subscription monthChangeSubscription;
    private SharedPreferences preferences;
    private Spinner mScheduleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_month_schedule);

        final SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");

        // set the current interview list as the default list
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString(BaseActivity.PREF_CURRENT_INT_LIST, null) == null) {
            preferences.edit().putString(BaseActivity.PREF_CURRENT_INT_LIST, "1").apply();
        }

        caldroidFragment = new CaldroidFragment();
        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");
        } else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidLight);
            caldroidFragment.setArguments(args);
        }

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.cal, caldroidFragment);
        t.commit();

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                mCurrentDay = cal.get(Calendar.DAY_OF_MONTH);
                mCurrentMode = BaseActivity.DATE_MODE_DAY;

                Intent intent = new Intent(MonthScheduleActivity.this, DailyScheduleActivity.class);
                intent.putExtra("selectedDay", formatter.format(date));
                startActivity(intent);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                if (month != mCurrentMonth && mCurrentMonth != 0) {
                    mCurrentYear = year;
                    mCurrentMonth = month;
                    mCurrentMode = BaseActivity.DATE_MODE_MONTH;

                    monthChangeSubscription = Observable.create(new Observable.OnSubscribe<Cursor>() {
                        @Override
                        public void call(Subscriber<? super Cursor> observer) {
                            try {
                                createAppointmentLookupQuery(BaseActivity.DATE_MODE_MONTH, observer);
                            } catch (Exception e) {
                                observer.onError(e);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Cursor>() {
                        @Override
                        public void onNext(Cursor cursor) {
                            updateCalendar(cursor);
                        }

                        @Override
                        public void onError(Throwable error) {
                            System.err.println("Error: " + error.getMessage());
                        }

                        @Override
                        public void onCompleted() {
                            System.out.println("Sequence complete.");
                        }
                    });
                }
                //this is to keep the observable from running on activity creation...
                if (mCurrentMonth == 0) {
                    mCurrentYear = year;
                    mCurrentMonth = month;
                }
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Toast.makeText(getApplicationContext(),
                        "Long click " + formatter.format(date),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
//                if (caldroidFragment.getLeftArrowButton() != null) {
//                    Toast.makeText(getApplicationContext(),
//                            "Caldroid view is created", Toast.LENGTH_SHORT)
//                            .show();
//                }
            }
        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);

        mScheduleSpinner = (Spinner) findViewById(R.id.spinner_toolbar);
        mScheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putLong(BaseActivity.PREF_CURRENT_INTERVIEWER, mScheduleSpinner.getSelectedItemId()).apply();
                interviewerId = mScheduleSpinner.getSelectedItemId();
                refreshCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MonthScheduleActivity.this, AppointmentActivity.class);
            startActivity(intent);
        });
    }

   @Override
    public void onResume() {
        super.onResume();

        Utility.updateInterviewerSpinner(this, mScheduleSpinner);
        Utility.selectSpinnerItemByValue(mScheduleSpinner, preferences.getLong(PREF_CURRENT_INTERVIEWER, 0));
        refreshCalendar();
    }

    @Override
    public void onPause() {
        if (apptSubscription != null && !apptSubscription.isUnsubscribed()) {
            apptSubscription.unsubscribe();
        }
        if (monthChangeSubscription != null && !monthChangeSubscription.isUnsubscribed()) {
            monthChangeSubscription.unsubscribe();
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }
    }

    private void refreshCalendar() {
        apptSubscription = Observable.create(new Observable.OnSubscribe<Cursor>() {
            @Override
            public void call(Subscriber<? super Cursor> observer) {
                try {
                    createAppointmentLookupQuery(BaseActivity.DATE_MODE_MONTH, observer);
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Cursor>() {
                    @Override
                    public void onNext(Cursor cursor) {
                        updateCalendar(cursor);
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
    }

}
