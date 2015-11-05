package com.sknutti.capstoneproject.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sknutti.capstoneproject.AppointmentActivity;
import com.sknutti.capstoneproject.R;
import com.sknutti.capstoneproject.data.SchedulerContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class WidgetRemoteViewsService  extends RemoteViewsService {

    DateFormat f = new SimpleDateFormat("MMM dd yyyy h:mma", Locale.US);

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        SchedulerContract.AppointmentEntry.CONTENT_URI,
                        AppointmentActivity.APPOINTMENT_COLUMNS,
                        SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " > ?",
                        new String[]{String.valueOf(System.currentTimeMillis())},
                        SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " ASC"
                );
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDataSetChanged() {

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);

                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }

                Date date = new Date(data.getLong(AppointmentActivity.COL_DATETIME));
                views.setTextViewText(R.id.widget_appt_datetime, f.format(date));
                views.setTextViewText(R.id.widget_appt_member, data.getString(AppointmentActivity.COL_MEMBER_NAME));
                views.setTextViewText(R.id.widget_appt_interviewer, data.getString(AppointmentActivity.COL_INTERVIEWER_NAME));
                views.setTextViewText(R.id.widget_empty, "");
                long apptId = data.getLong(AppointmentActivity.COL_APPT_ID);

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("selectedAppointment", String.valueOf(apptId));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(AppointmentActivity.COL_APPT_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
