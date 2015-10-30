package com.sknutti.capstoneproject.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.sknutti.capstoneproject.MemberListActivity;
import com.sknutti.capstoneproject.MemberListAdapter;
import com.sknutti.capstoneproject.R;
import com.sknutti.capstoneproject.data.SchedulerContract;


public class EditMemberDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MEMBER_LIST_LOADER = 0;
    private MemberListAdapter mMemberListAdapter;
    private long mMemberId;

    public interface EditMemberDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
        void onClosed(String action, String id, String name, String mobile, String email, Boolean isInterviewer);
    }

    EditMemberDialogListener mListener;
    private TextView textId;
    private EditText text1;
    private EditText text2;
    private EditText text3;
    private CheckBox checkbox;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mMemberListAdapter = new MemberListAdapter(getContext(), null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_member, null);

        TextView header = (TextView)dialogView.findViewById(R.id.member_dialog_header);
        header.setText(R.string.edit_member);
        textId = (TextView)dialogView.findViewById(R.id.member_id);
        text1 = (EditText)dialogView.findViewById(R.id.member_name);
        text2 = (EditText)dialogView.findViewById(R.id.member_mobile);
        text3 = (EditText)dialogView.findViewById(R.id.member_email);
        checkbox = (CheckBox)dialogView.findViewById(R.id.member_is_interviewer);

        builder.setView(dialogView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mListener != null) {

                            mListener.onClosed("edit",
                                    textId.getText().toString(),
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
                        mListener.onDialogNegativeClick(EditMemberDialogFragment.this);
                    }
                });

        ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(MEMBER_LIST_LOADER, null, this);

        return builder.create();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = SchedulerContract.MemberEntry.COLUMN_NAME + " ASC";
        Uri uri = SchedulerContract.MemberEntry.CONTENT_URI;

        return new CursorLoader(getContext(),
                uri,
                MemberListActivity.MEMBER_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMemberListAdapter.swapCursor(data);
        textId.setText(data.getString(MemberListActivity.COL_MEMBER_ID));
        text1.setText(data.getString(MemberListActivity.COL_NAME));
        text2.setText(data.getString(MemberListActivity.COL_MOBILE));
        text3.setText(data.getString(MemberListActivity.COL_EMAIL));
        checkbox.setChecked(data.getInt(MemberListActivity.COL_IS_INTERVIEWER) == 1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMemberListAdapter.swapCursor(null);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (EditMemberDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditMemberDialogListener");
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
