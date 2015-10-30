package com.sknutti.capstoneproject.dialogs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.sknutti.capstoneproject.R;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class AddIntListDialogFragment extends DialogFragment {

    public interface AddIntListDialogListener {
        void onClosed(String action, String id, String name, String startDate, String endDate, Boolean isCurrentList);
    }

    AddIntListDialogListener mListener;
    private DatePickerDialog startDatePickerDialog;
    private DatePickerDialog endDatePickerDialog;

    private TextView textId;
    private EditText text1;
    private EditText text2;
    private EditText text3;
    private CheckBox checkbox;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_interview_list, null);

        text1 = (EditText)dialogView.findViewById(R.id.int_list_name);
        text2 = (EditText)dialogView.findViewById(R.id.int_list_start_date);
        text3 = (EditText)dialogView.findViewById(R.id.int_list_end_date);
        checkbox = (CheckBox)dialogView.findViewById(R.id.int_list_is_current);

        builder.setView(dialogView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mListener != null) {

                            mListener.onClosed("add",
                                    "",     //placeholder for the interview list id in the edit dialog
                                    text1.getText().toString(),
                                    text2.getText().toString(),
                                    text3.getText().toString(),
                                    checkbox.isChecked());
                        }

                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

//    public void showDatePickerDialog(View v) {
//        DialogFragment newFragment = new DatePickerFragment();
//        newFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "datePicker");
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (AddIntListDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AddIntListDialogListener");
        }
    }

    @Override
    public void onDestroy() {
        textId = null;
        text1 = null;
        text2 = null;
        text3 = null;
        checkbox = null;
        super.onDestroy();
    }
}
