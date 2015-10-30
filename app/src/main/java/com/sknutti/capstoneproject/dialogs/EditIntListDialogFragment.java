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

import com.sknutti.capstoneproject.InterviewListActivity;
import com.sknutti.capstoneproject.MemberListAdapter;
import com.sknutti.capstoneproject.R;
import com.sknutti.capstoneproject.data.SchedulerContract;


public class EditIntListDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INT_LIST_LOADER = 0;
    private MemberListAdapter mInterviewListAdapter;
    private long mInterviewListId;

    public interface EditIntListDialogListener {
        void onClosed(String action, String id, String name, String startDate, String endDate, Boolean isCurrentList);
    }

    EditIntListDialogListener mListener;
    private TextView textId;
    private EditText text1;
    private EditText text2;
    private EditText text3;
    private CheckBox checkbox;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mInterviewListAdapter = new MemberListAdapter(getContext(), null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_interview_list, null);

        TextView header = (TextView)dialogView.findViewById(R.id.int_list_dialog_header);
        header.setText(R.string.edit_interview_list);
        textId = (TextView)dialogView.findViewById(R.id.int_list_id);
        text1 = (EditText)dialogView.findViewById(R.id.int_list_name);
        text2 = (EditText)dialogView.findViewById(R.id.int_list_start_date);
        text3 = (EditText)dialogView.findViewById(R.id.int_list_end_date);
        checkbox = (CheckBox)dialogView.findViewById(R.id.int_list_is_current);

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

                    }
                });

        ((AppCompatActivity) getContext()).getSupportLoaderManager().initLoader(INT_LIST_LOADER, null, this);

        return builder.create();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = SchedulerContract.InterviewListEntry.COLUMN_NAME + " ASC";
        Uri uri = SchedulerContract.InterviewListEntry.CONTENT_URI;

        return new CursorLoader(getContext(),
                uri,
                InterviewListActivity.INTERVIEW_LIST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInterviewListAdapter.swapCursor(data);
        textId.setText(data.getString(InterviewListActivity.COL_INT_LIST_ID));
        text1.setText(data.getString(InterviewListActivity.COL_NAME));
        text2.setText(data.getString(InterviewListActivity.COL_START_DATE));
        text3.setText(data.getString(InterviewListActivity.COL_END_DATE));
        checkbox.setChecked(data.getInt(InterviewListActivity.COL_IS_CURRENT_LIST) == 1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInterviewListAdapter.swapCursor(null);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (EditIntListDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement EditIntListDialogListener");
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
