package com.sknutti.capstoneproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.sknutti.capstoneproject.data.SchedulerContract;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;


public class AppointmentListAdapter extends RecyclerView.Adapter<AppointmentListAdapter.AppointmentListAdapterViewHolder> {
    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;
    private int position;

    public class AppointmentListAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mInterviewerInitials;
        public final TextView mMemberName;
        public final TextView mApptTime;
        public final CheckBox mCompleted;

        public AppointmentListAdapterViewHolder(View view) {
            super(view);
            mInterviewerInitials = (TextView) view.findViewById(R.id.list_item_interviewer_initials);
            mMemberName = (TextView) view.findViewById(R.id.list_item_member_name);
            mApptTime = (TextView) view.findViewById(R.id.list_item_appt_time);
            mCompleted = (CheckBox) view.findViewById(R.id.list_item_completed);

            RxView.clicks(view).subscribe(aVoid -> {
                Intent intent = new Intent(mContext, AppointmentActivity.class);
                mCursor.moveToPosition(getAdapterPosition());
                intent.putExtra("selectedAppointment", mCursor.getString(AppointmentActivity.COL_APPT_ID));
                mContext.startActivity(intent);
            });
            RxView.clicks(mCompleted).subscribe(aVoid -> {
                ContentValues contentValues = new ContentValues();
                long id = mCursor.getLong(AppointmentActivity.COL_I_APPT_ID);
                int completed = mCursor.getInt(AppointmentActivity.COL_I_COMPLETED);
                contentValues.put(SchedulerContract.AppointmentEntry.COLUMN_COMPLETED, (completed == 1) ? 0 : 1);
                mContext.getContentResolver().update(
                        SchedulerContract.AppointmentEntry.CONTENT_URI,
                        contentValues,
                        SchedulerContract.AppointmentEntry.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)}
                );
            });
            RxView.longClicks(view).subscribe(aVoid -> {
                setPosition(getAdapterPosition());
                view.showContextMenu();
            });
        }
    }


    public AppointmentListAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public AppointmentListAdapter.AppointmentListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_appointment, parent, false);
            view.setFocusable(true);
            return new AppointmentListAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(AppointmentListAdapter.AppointmentListAdapterViewHolder holder, int position) {
        if (mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                String memberName = mCursor.getString(AppointmentActivity.COL_MEMBER_NAME);
                String interviewerName = mCursor.getString(AppointmentActivity.COL_INTERVIEWER_NAME);
                String date = mCursor.getString(AppointmentActivity.COL_DATETIME);
                Instant instant = Instant.ofEpochMilli(Long.parseLong(date));
                LocalDateTime dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mma");

                holder.mCompleted.setChecked(mCursor.getInt(AppointmentActivity.COL_COMPLETED) == 1);
                holder.mApptTime.setText(dateTime.format(timeFormatter));
                holder.mMemberName.setText(memberName);
                holder.mInterviewerInitials.setText(Utility.getInitialsFromFullName(interviewerName));
                holder.mInterviewerInitials.setBackground(mContext.getResources().getDrawable(R.drawable.ic_dark_blank_circle));
            }
        }
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        if (mEmptyView != null) {
            mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
