package com.sknutti.capstoneproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.sknutti.capstoneproject.data.SchedulerContract;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
//    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
//    @Bind(R.id.nav_view) NavigationView drawer;
//    @Bind(R.id.main_content) ViewGroup content;
//
//    @BindColor(R.color.status_bar) int statusBarColor;

    protected CaldroidFragment caldroidFragment;
    private Subscription shareSubscription;
    private Subscription shareApptSubscription;
    public static final String DATE_MODE_MONTH = "MONTH";
    public static final String DATE_MODE_DAY = "DAY";
    public static final String PREF_CURRENT_INT_LIST = "currentInterviewList";
    public static final String PREF_CURRENT_INTERVIEWER = "currentInterviewer";
    DateFormat f = new SimpleDateFormat("MMM dd yyyy h:mma", Locale.US);

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private TextView mNextAppointment;
    private boolean postLollipop;
    public static long interviewerId = 0L;
    public static int mCurrentYear = 0;
    public static int mCurrentMonth = 0;
    public static int mCurrentDay = 1;
    public static String mCurrentMode = DATE_MODE_MONTH;

    private static Handler mHandler;
    private static SimpleCursorAdapter interviewerAdapter;
    private static Cursor interviewerCursor;
    private SharedPreferences preferences;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mNextAppointment = (TextView) findViewById(R.id.next_appointment);
    }

    @Override
    public void onResume() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (caldroidFragment != null) {
                    caldroidFragment.refreshView();
                }
            }
        };

        super.onResume();
    }

    @Override
    public void onPause() {
        if (shareSubscription != null && !shareSubscription.isUnsubscribed()) {
            shareSubscription.unsubscribe();
        }
        if (shareApptSubscription != null && !shareApptSubscription.isUnsubscribed()) {
            shareApptSubscription.unsubscribe();
        }
        if (mHandler != null) {
            mHandler = null;
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.month_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_share:
                shareSubscription = Observable.create(new Observable.OnSubscribe<Cursor>() {
                    @Override
                    public void call(Subscriber<? super Cursor> observer) {
                        try {
                            long interviewerId = preferences.getLong(BaseActivity.PREF_CURRENT_INTERVIEWER, 0);

                            String selection;
                            String[] selectionArgs;
                            if (interviewerId > 0) {
                                selection = SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " = ? AND " +
                                            SchedulerContract.MemberEntry.COLUMN_ID + " = ?";
                                selectionArgs = new String[]{"1", String.valueOf(interviewerId)};
                            } else {
                                selection = SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " = ? ";
                                selectionArgs = new String[]{"1"};
                            }
                            Cursor cursor = getContentResolver().query(
                                    SchedulerContract.MemberEntry.CONTENT_URI,
                                    MemberListActivity.MEMBER_COLUMNS,
                                    selection,
                                    selectionArgs,
                                    SchedulerContract.MemberEntry.COLUMN_NAME + " ASC"
                            );
                            observer.onNext(cursor);
                            observer.onCompleted();
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
                                ArrayList<String> emails = new ArrayList<>();
                                if (cursor != null) {
                                    if (cursor.moveToFirst()) {
                                        do {
                                            emails.add(cursor.getString(MemberListActivity.COL_EMAIL));
                                        } while (cursor.moveToNext());
                                    }
                                    cursor.close();
                                }

                                shareApptSubscription = Observable.create(new Observable.OnSubscribe<Cursor>() {
                                    @Override
                                    public void call(Subscriber<? super Cursor> observer) {
                                        try {
                                            long interviewerId = preferences.getLong(BaseActivity.PREF_CURRENT_INTERVIEWER, 0);

                                            Cursor cursor = getContentResolver().query(
                                                    SchedulerContract.AppointmentEntry.buildAppointmentByWeekAndInterviewer(System.currentTimeMillis(), interviewerId),
                                                    AppointmentActivity.APPOINTMENT_COLUMNS,
                                                    null,
                                                    null,
                                                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " ASC"
                                            );
                                            observer.onNext(cursor);
                                            observer.onCompleted();
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
                                                Intent email = new Intent(Intent.ACTION_SEND);
                                                email.putExtra(Intent.EXTRA_SUBJECT, "Schedule for upcoming week");
                                                email.setType("message/rfc822");
                                                DateFormat f = new SimpleDateFormat("MMM dd yyyy h:mma", Locale.US);
                                                StringBuffer emailMessage = new StringBuffer("<p>Here is the upcoming schedule...</p>");
                                                if (cursor != null) {
                                                    if (cursor.moveToFirst()) {
                                                        do {
                                                            emailMessage.append("<p>");
                                                            emailMessage.append(f.format(new Date(cursor.getLong(AppointmentActivity.COL_DATETIME))));
                                                            emailMessage.append(" - ");
                                                            emailMessage.append(cursor.getString(AppointmentActivity.COL_INTERVIEWER_NAME));
                                                            emailMessage.append(" -> ");
                                                            emailMessage.append(cursor.getString(AppointmentActivity.COL_MEMBER_NAME));
                                                            emailMessage.append("</p>");
                                                        } while (cursor.moveToNext());
                                                    } else {
                                                        emailMessage = new StringBuffer("<p>There are no appointments scheduled for this week.</p>");
                                                    }
                                                    cursor.close();
                                                }
                                                email.putExtra(Intent.EXTRA_EMAIL, emails.toArray(new String[emails.size()]));
                                                email.putExtra(Intent.EXTRA_TEXT, emailMessage.toString());
                                                startActivity(Intent.createChooser(email, "Choose an Email client :"));
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

                            @Override
                            public void onError(Throwable error) {
                                System.err.println("Error: " + error.getMessage());
                            }

                            @Override
                            public void onCompleted() {
                                System.out.println("Completed the appointment lookup...");
                            }
                        });
                break;
            case R.id.action_settings:
                break;
            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
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
//            case R.id.nav_interview_list:
//                intent = new Intent(this, InterviewListActivity.class);
//                break;
            case R.id.nav_member_list:
                intent = new Intent(this, MemberListActivity.class);
                break;
//            case R.id.nav_notifications:
//                intent = new Intent(this, MonthScheduleActivity.class);
//                break;
//            case R.id.nav_settings:
//                intent = new Intent(this, SettingsActivity.class);
//                break;
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

            if (monthOrDay.equals(DATE_MODE_MONTH)) {
                clearCalendar(dateTime);
                uri = SchedulerContract.AppointmentEntry.buildAppointmentByMonthAndInterviewer(dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), interviewerId);
            } else {
                uri = SchedulerContract.AppointmentEntry.buildAppointmentByDayAndInterviewer(dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), interviewerId);
            }

            Cursor cursor = getContentResolver().query(
                    uri,
                    AppointmentActivity.APPOINTMENT_COLUMNS,
                    null,
                    null,
                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " ASC"
            );
            observer.onNext(cursor);
            observer.onCompleted();
        }
    }

    protected void updateCalendar(Cursor cursor) {
        boolean gotNextAppointment = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (!gotNextAppointment) {
                        Date datetime = new Date(cursor.getLong(cursor.getColumnIndex(SchedulerContract.AppointmentEntry.COLUMN_DATETIME)));
                        Date now = new Date(System.currentTimeMillis());
                        if (datetime.after(now)) {
                            gotNextAppointment = true;
                            StringBuffer appointment = new StringBuffer();
                            appointment.append(f.format(datetime));
                            appointment.append("\n\t\t\t\t");
                            appointment.append(cursor.getString(AppointmentActivity.COL_MEMBER_NAME));
                            appointment.append(" is meeting with ");
                            appointment.append(cursor.getString(AppointmentActivity.COL_INTERVIEWER_NAME));
                            mNextAppointment.setText(appointment.toString());
                        }
                    }
                    long millis = cursor.getLong(cursor.getColumnIndex(SchedulerContract.AppointmentEntry.COLUMN_DATETIME));
                    try {
                        Date date = new Date(millis);
                        caldroidFragment.setTextColorForDate(R.color.colorTextHasAppts, date);
                    } catch (Exception e) {
                        Timber.w(e, "Error creating date for display on calendar - " + millis);
                    }
                } while (cursor.moveToNext());
                Message message = mHandler.obtainMessage(0, "refresh");
                message.sendToTarget();
            } else {
                mNextAppointment.setText("No Appointments Scheduled.");
                clearCalendar(LocalDateTime.of(mCurrentYear, mCurrentMonth, mCurrentDay, 0, 0));
            }
            cursor.close();
        }
    }

    private void clearCalendar(LocalDateTime dateTime) {
        long start = dateTime.with(firstDayOfMonth()).minusDays(7).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = dateTime.with(lastDayOfMonth()).plusDays(14).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Date date = new Date(start);
        while (date.getTime() <= end) {
            caldroidFragment.clearTextColorForDate(date);
            date = new Date(date.getTime() + 86400000);
        }
        Message message = mHandler.obtainMessage(0, "refresh");
        message.sendToTarget();
    }
}
