package com.sknutti.capstoneproject;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sknutti.capstoneproject.data.SchedulerContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.Subscription;
import timber.log.Timber;

public class DailyScheduleActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Subscription apptSubscription;
    private Subscription dayChangeSubscription;
    final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

    private static final int APPOINTMENT_LIST_LOADER = 0;

    private RecyclerView mRecyclerView;
    private AppointmentListAdapter mAdapter;
    private Date mCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_schedule);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(v -> finish());
        try {
            mCurrentDate = formatter.parse(getIntent().getStringExtra("selectedDay"));
        } catch (ParseException e) {
            Timber.w(e, "Error parsing date when opening daily schedule");
        }

        TextView listHeader = (TextView) findViewById(R.id.daily_date);
        listHeader.setText(formatter.format(mCurrentDate));

        View emptyView = findViewById(R.id.rv_appointment_list_empty);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_daily_schedule);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AppointmentListAdapter(this, emptyView);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        getSupportLoaderManager().initLoader(APPOINTMENT_LIST_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();

//        apptSubscription = Observable.create(new Observable.OnSubscribe<Cursor>() {
//            @Override
//            public void call(Subscriber<? super Cursor> observer) {
//                try {
//                    createAppointmentLookupQuery(BaseActivity.DATE_MODE_DAY, observer);
//                } catch (Exception e) {
//                    observer.onError(e);
//                }
//            }
//        }).subscribe(new Subscriber<Cursor>() {
//            @Override
//            public void onNext(Cursor cursor) {
//                updateDailySchedule(cursor);
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                System.err.println("Error: " + error.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("Completed the appointment lookup...");
//            }
//        });
    }

    @Override
    public void onPause() {
//        if (!apptSubscription.isUnsubscribed()) {
//            apptSubscription.unsubscribe();
//        }
//        if (!dayChangeSubscription.isUnsubscribed()) {
//            dayChangeSubscription.unsubscribe();
//        }
        super.onPause();
    }

    private void updateDailySchedule(Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long millis = cursor.getLong(cursor.getColumnIndex(SchedulerContract.AppointmentEntry.COLUMN_DATETIME));
                    caldroidFragment.setTextColorForDate(R.color.colorTextHasAppts, new Date(millis));
                    cursor.moveToNext();
                } while (cursor.moveToNext());
                caldroidFragment.refreshView();
            }
            cursor.close();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " ASC";
        Uri uri = SchedulerContract.AppointmentEntry.buildAppointmentByDayAndInterviewer(mCurrentDate.getTime(), interviewerId);

        return new CursorLoader(this,
                uri,
                AppointmentActivity.APPOINTMENT_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
//        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
