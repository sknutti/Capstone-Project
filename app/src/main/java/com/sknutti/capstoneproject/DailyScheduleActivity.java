package com.sknutti.capstoneproject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sknutti.capstoneproject.data.SchedulerContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

@SuppressLint("SimpleDateFormat")
public class DailyScheduleActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");

    public static final int APPOINTMENT_LIST_LOADER = 0;

    private RecyclerView mRecyclerView;
    private AppointmentListAdapter mAdapter;
    private Date mCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_schedule);

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

        registerForContextMenu(mRecyclerView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Spinner mScheduleSpinner = (Spinner) findViewById(R.id.spinner_toolbar);
        mScheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                interviewerId = mScheduleSpinner.getSelectedItemId();
                preferences.edit().putLong(BaseActivity.PREF_CURRENT_INTERVIEWER, mScheduleSpinner.getSelectedItemId()).apply();
                getSupportLoaderManager().restartLoader(APPOINTMENT_LIST_LOADER, null, DailyScheduleActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Utility.updateInterviewerSpinner(this, mScheduleSpinner);
        Utility.selectSpinnerItemByValue(mScheduleSpinner, preferences.getLong(PREF_CURRENT_INTERVIEWER, 0));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, AppointmentActivity.class);
            //get the selectedDay from the previous intent
            intent.putExtra("currentDate", getIntent().getStringExtra("selectedDay"));
            startActivity(intent);
        });

        getSupportLoaderManager().initLoader(APPOINTMENT_LIST_LOADER, null, this);
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, MonthScheduleActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Cursor cursor = mAdapter.getCursor();
        if (cursor == null) {
            //TODO: display a message to the user
            return;     // For some reason the requested item isn't available, do nothing
        }

        cursor.moveToPosition(mAdapter.getPosition());
        String datetime = formatter.format(new Date(Long.parseLong(cursor.getString(AppointmentActivity.COL_DATETIME))));
        menu.setHeaderTitle(cursor.getString(AppointmentActivity.COL_MEMBER_NAME) + " - " + datetime);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_appointment, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        RecyclerViewContextMenuInfo info = (RecyclerViewContextMenuInfo) item.getMenuInfo();

        int position;
        try {
            position = mAdapter.getPosition();
        } catch (Exception e) {
            Timber.d(e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }

        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);

        long id = cursor.getLong(AppointmentActivity.COL_I_APPT_ID);

        switch (item.getItemId()) {
            case R.id.menu_make_current_int_list:
                Timber.d("Toggle current appointment completed flag");
                ContentValues contentValues = new ContentValues();
                int completed = cursor.getInt(AppointmentActivity.COL_COMPLETED);
                contentValues.put(SchedulerContract.AppointmentEntry.COLUMN_COMPLETED, (completed == 1) ? 0 : 1);
                getContentResolver().update(
                        SchedulerContract.AppointmentEntry.CONTENT_URI,
                        contentValues,
                        SchedulerContract.AppointmentEntry.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)}
                );
                getSupportLoaderManager().restartLoader(APPOINTMENT_LIST_LOADER, null, this);
                break;
            case R.id.menu_delete_int_list:
                Timber.d("Deleting appointment");
                getContentResolver().delete(
                        SchedulerContract.InterviewListEntry.CONTENT_URI,
                        SchedulerContract.InterviewListEntry.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)}
                );
                getSupportLoaderManager().restartLoader(APPOINTMENT_LIST_LOADER, null, this);
                break;
            default:
                //TODO: display message to user
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                SchedulerContract.AppointmentEntry.buildAppointmentByDayAndInterviewer(mCurrentDate.getTime(), interviewerId),
                AppointmentActivity.APPOINTMENT_COLUMNS,
                null,
                null,
                SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " ASC");
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
