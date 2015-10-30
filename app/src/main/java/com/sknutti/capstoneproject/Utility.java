package com.sknutti.capstoneproject;

import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Spinner;

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
}
