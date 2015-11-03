package com.sknutti.capstoneproject;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.sknutti.capstoneproject.data.SchedulerContract;

public class Utility {

    public static String getInitialsFromFullName(String fullName) {
        String initials = "";
        String[] parts = fullName.split(" ");
        int numParts = parts.length;

        if (numParts == 2) {
            initials = parts[0].substring(0,1) + parts[1].substring(0,1);
        } else if (numParts < 2) {
            initials = parts[0].substring(0,1);
        } else if (numParts > 2) {
            initials = parts[0].substring(0,1) + parts[numParts-1].substring(0,1);
        }
        return initials;
    }

    public static void selectSpinnerItemByValue(Spinner spnr, long value) {
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) spnr.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if(adapter.getItemId(position) == value) {
                spnr.setSelection(position);
                return;
            }
        }
    }

    public static void updateInterviewerSpinner(Context context, Spinner spinner) {
        //now manage the mScheduleSpinner
        Cursor cursor = context.getContentResolver().query(
                SchedulerContract.MemberEntry.CONTENT_URI,
                MemberListActivity.MEMBER_COLUMNS,
                SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " = ? ",
                new String[]{"1"},
                SchedulerContract.MemberEntry.COLUMN_NAME + " ASC"
        );
        MatrixCursor extras = new MatrixCursor(new String[]{"_id", "name" });
        extras.addRow(new String[]{"0", "All Schedules"});
        Cursor[] cursors = { extras, cursor };
        Cursor extendedCursor = new MergeCursor(cursors);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context, // context
                R.layout.layout_drop_title, // layout file
                extendedCursor, // DB cursor
                new String[]{SchedulerContract.MemberEntry.COLUMN_NAME}, // data to bind to the UI
                new int[]{android.R.id.text1}, // views that'll represent the data from `fromColumns`
                0
        );
        adapter.setDropDownViewResource(R.layout.layout_drop_list);
        spinner.setAdapter(adapter);
    }
}
