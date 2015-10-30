package com.sknutti.capstoneproject.dialogs;

import android.app.Activity;
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


public class AddMemberDialogFragment extends DialogFragment {

    public interface AddMemberDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
        void onClosed(String action, String id, String name, String mobile, String email, Boolean isInterviewer);
    }

    AddMemberDialogListener mListener;
    private TextView textId;
    private EditText text1;
    private EditText text2;
    private EditText text3;
    private CheckBox checkbox;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_member, null);

        EditText text1 = (EditText)dialogView.findViewById(R.id.member_name);
        EditText text2 = (EditText)dialogView.findViewById(R.id.member_mobile);
        EditText text3 = (EditText)dialogView.findViewById(R.id.member_email);
        CheckBox checkbox = (CheckBox)dialogView.findViewById(R.id.member_is_interviewer);

        builder.setView(dialogView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mListener != null) {
                            mListener.onClosed("add",
                                    "",     //plpaceholder for the member id in the edit dialog
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
                        mListener.onDialogNegativeClick(AddMemberDialogFragment.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (AddMemberDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement AddMemberDialogListener");
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
