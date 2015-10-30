package com.sknutti.capstoneproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.roomorama.caldroid.CaldroidFragment;
import com.sknutti.capstoneproject.data.SchedulerContract;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
//    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
//    @Bind(R.id.nav_view) NavigationView drawer;
//    @Bind(R.id.main_content) ViewGroup content;
//
//    @BindColor(R.color.status_bar) int statusBarColor;

    protected CaldroidFragment caldroidFragment;
    private Subscription apptSubscription;
    public static final String DATE_MODE_MONTH = "MONTH";
    public static final String DATE_MODE_DAY = "DAY";

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Spinner mScheduleSpinner;
    private boolean postLollipop;
    public static long interviewerId = 0L;
    public static int mCurrentYear = 0;
    public static int mCurrentMonth = 0;
    public static int mCurrentDay = 1;
    public static String mCurrentMode = DATE_MODE_MONTH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        postLollipop = getResources().getBoolean(R.bool.postLollipop);
        if(postLollipop){
            mToolbar.setElevation(15);
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);


        //now manage the mScheduleSpinner
        mScheduleSpinner = (Spinner) findViewById(R.id.spinner_toolbar);
        Cursor interviewerCursor = getContentResolver().query(
                SchedulerContract.MemberEntry.CONTENT_URI,
                MemberListActivity.MEMBER_COLUMNS,
                SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " = ? ",
                new String[]{"1"},
                SchedulerContract.MemberEntry.COLUMN_NAME + " ASC"
        );
        MatrixCursor extras = new MatrixCursor(new String[]{"_id", "name" });
        extras.addRow(new String[] { "0", "All Schedules" });
        Cursor[] cursors = { extras, interviewerCursor };
        Cursor extendedCursor = new MergeCursor(cursors);
        SimpleCursorAdapter interviewerAdapter = new SimpleCursorAdapter(
                this, // context
                R.layout.layout_drop_title, // layout file
                extendedCursor, // DB cursor
                new String[]{SchedulerContract.MemberEntry.COLUMN_NAME}, // data to bind to the UI
                new int[]{android.R.id.text1}, // views that'll represent the data from `fromColumns`
                0
        );
        interviewerAdapter.setDropDownViewResource(R.layout.layout_drop_list);
        mScheduleSpinner.setAdapter(interviewerAdapter);

        mScheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //TODO: set current interviewer in shared prefs
                String interviewer = mScheduleSpinner.getItemAtPosition(position).toString();
                interviewerId = mScheduleSpinner.getSelectedItemId();

                apptSubscription = Observable.create(new Observable.OnSubscribe<Cursor>() {
                    @Override
                    public void call(Subscriber<? super Cursor> observer) {
                        try {
                            createAppointmentLookupQuery(mCurrentMode, observer);
                        } catch (Exception e) {
                            observer.onError(e);
                        }
                    }
                }).subscribe(new Subscriber<Cursor>() {
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

                System.out.println("Interviewer: " + interviewer);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        //TODO: figure out where to tap into the nav drawer header to set the current list info
//        TextView intListName = (TextView) mNavigationView.findViewById(R.id.int_list_name);
//        TextView intListDateRange = (TextView) mNavigationView.findViewById(R.id.int_list_date_range);
    }

    @Override
    public void onPause() {
        if (!apptSubscription.isUnsubscribed()) {
            apptSubscription.unsubscribe();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_schedule:
                intent = new Intent(this, MonthScheduleActivity.class);
                break;
            case R.id.nav_interview_list:
                intent = new Intent(this, InterviewListActivity.class);
                break;
            case R.id.nav_member_list:
                intent = new Intent(this, MemberListActivity.class);
                break;
            case R.id.nav_notifications:
                intent = new Intent(this, MonthScheduleActivity.class);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            default:
                intent = new Intent(this, MonthScheduleActivity.class);
        }
        startActivity(intent);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    protected void createAppointmentLookupQuery(String monthOrDay, Subscriber<? super Cursor> observer) {
        if (!observer.isUnsubscribed()) {
            Uri uri;
            LocalDateTime dateTime;
            if (mCurrentYear == 0) {
                dateTime = LocalDateTime.now();
            } else {
                dateTime = LocalDateTime.of(mCurrentYear, mCurrentMonth, mCurrentDay, 0, 0);
            }

            if (monthOrDay.equals("MONTH")) {
                long start = dateTime.with(firstDayOfMonth()).minusDays(7).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long end = dateTime.with(lastDayOfMonth()).plusDays(14).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                Date date = new Date(start);
                do {
                    caldroidFragment.clearTextColorForDate(date);
                    date = new Date(date.getTime() + 86400000);
                } while (date.getTime() <= end);
                caldroidFragment.refreshView();

                uri = SchedulerContract.AppointmentEntry.buildAppointmentByMonthAndInterviewer(dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), interviewerId);
            } else {
                uri = SchedulerContract.AppointmentEntry.buildAppointmentByDayAndInterviewer(dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), interviewerId);
            }

            Cursor apptCursor = getContentResolver().query(
                    uri,
                    AppointmentActivity.APPOINTMENT_COLUMNS,
                    null,
                    null,
                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " ASC"
            );
            observer.onNext(apptCursor);
            observer.onCompleted();
        }
    }

    protected void updateCalendar(Cursor cursor) {
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
}
