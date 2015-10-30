package com.sknutti.capstoneproject;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sknutti.capstoneproject.data.SchedulerContract;
import com.sknutti.capstoneproject.dialogs.AddMemberDialogFragment;
import com.sknutti.capstoneproject.dialogs.EditMemberDialogFragment;

import timber.log.Timber;

public class MemberListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        AddMemberDialogFragment.AddMemberDialogListener,
        EditMemberDialogFragment.EditMemberDialogListener {
    private RecyclerView mRecyclerView;
    private MemberListAdapter mMemberListAdapter;

    private static final int MEMBER_LIST_LOADER = 0;

    public static final String[] MEMBER_COLUMNS = {
            SchedulerContract.MemberEntry._ID,
            SchedulerContract.MemberEntry.COLUMN_NAME,
            SchedulerContract.MemberEntry.COLUMN_MOBILE,
            SchedulerContract.MemberEntry.COLUMN_EMAIL,
            SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER
    };

    public static final int COL_MEMBER_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_MOBILE = 2;
    public static final int COL_EMAIL = 3;
    public static final int COL_IS_INTERVIEWER = 4;

    public interface Callback {
        void onItemSelected(Uri uri, MemberListAdapter.MemberListAdapterViewHolder vh);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_member_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        View emptyView = findViewById(R.id.rv_member_list_empty);
        mRecyclerView.setHasFixedSize(true);

        mMemberListAdapter = new MemberListAdapter(this, emptyView);
        mRecyclerView.setAdapter(mMemberListAdapter);

        registerForContextMenu(mRecyclerView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new AddMemberDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), dialogFragment.getClass().getName());
            }
        });

        getSupportLoaderManager().initLoader(MEMBER_LIST_LOADER, null, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Cursor cursor = mMemberListAdapter.getCursor();
        if (cursor == null) {
            //TODO: display a message to the user
            return;     // For some reason the requested item isn't available, do nothing
        }

        cursor.moveToPosition(mMemberListAdapter.getPosition());
        menu.setHeaderTitle(cursor.getString(MemberListActivity.COL_NAME));

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_member, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        RecyclerViewContextMenuInfo info = (RecyclerViewContextMenuInfo) item.getMenuInfo();

        int position;
        try {
            position = mMemberListAdapter.getPosition();
        } catch (Exception e) {
            Timber.d(e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }

        Cursor cursor = mMemberListAdapter.getCursor();
        cursor.moveToPosition(position);

        Uri number;
        Intent actionIntent;
        String title = getResources().getString(R.string.chooser_title);
        Intent chooser;

        switch (item.getItemId()) {
            case R.id.menu_call_member:
                Timber.d("Calling member");
                number = Uri.parse(cursor.getString(MemberListActivity.COL_MOBILE));
                actionIntent = new Intent(Intent.ACTION_DIAL, number);
                chooser = Intent.createChooser(actionIntent, title);
                // Verify the intent will resolve to at least one activity
                if (actionIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                break;
            case R.id.menu_text_member:
                Timber.d("Texting member");
                number = Uri.parse(cursor.getString(MemberListActivity.COL_MOBILE));
                actionIntent = new Intent(Intent.ACTION_SENDTO, number);
                chooser = Intent.createChooser(actionIntent, title);
                // Verify the intent will resolve to at least one activity
                if (actionIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                break;
            case R.id.menu_email_member:
                Timber.d("Emailing member");
                number = Uri.parse(cursor.getString(MemberListActivity.COL_EMAIL));
                actionIntent = new Intent(Intent.ACTION_SEND, number);
                chooser = Intent.createChooser(actionIntent, title);
                // Verify the intent will resolve to at least one activity
                if (actionIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
                break;
            case R.id.menu_delete_member:
                Timber.d("Deleting member");
                long id = cursor.getLong(MemberListActivity.COL_MEMBER_ID);
                getContentResolver().delete(
                        SchedulerContract.MemberEntry.CONTENT_URI,
                        SchedulerContract.MemberEntry.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)}
                );
                getSupportLoaderManager().restartLoader(MEMBER_LIST_LOADER, null, this);
                break;
            default:
                //TODO: display message to user
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                SchedulerContract.MemberEntry.CONTENT_URI,
                MEMBER_COLUMNS,
                null,
                null,
                SchedulerContract.MemberEntry.COLUMN_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMemberListAdapter.swapCursor(data);
//        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMemberListAdapter.swapCursor(null);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // save the data
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User cancelled, so we don't need to do anything
    }

    @Override
    public void onClosed(String action, String id, String name, String mobile, String email, Boolean isInterviewer) {

        ContentValues memberValues = new ContentValues();

        memberValues.put(SchedulerContract.MemberEntry.COLUMN_NAME, name);
        memberValues.put(SchedulerContract.MemberEntry.COLUMN_MOBILE, mobile);
        memberValues.put(SchedulerContract.MemberEntry.COLUMN_EMAIL, email);
        memberValues.put(SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER, isInterviewer ? 1 : 0);

        if (action.equals("add")) {
            Uri insertedUri = getContentResolver().insert(
                    SchedulerContract.MemberEntry.CONTENT_URI,
                    memberValues
            );
            long memberId = ContentUris.parseId(insertedUri);

            //Add new member to current interview list
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String interviewListId = preferences.getString(BaseActivity.PREF_CURRENT_INT_LIST, "1");
            ContentValues milValues = new ContentValues();
            milValues.put(SchedulerContract.MemberInterviewListEntry.COLUMN_MEMBER_KEY, memberId);
            milValues.put(SchedulerContract.MemberInterviewListEntry.COLUMN_INTERVIEW_LIST_KEY, interviewListId);
            Uri insertedUri2 = getContentResolver().insert(
                    SchedulerContract.MemberInterviewListEntry.CONTENT_URI,
                    milValues
            );
        } else {
            int numUpdated = getContentResolver().update(
                    SchedulerContract.MemberEntry.CONTENT_URI,
                    memberValues,
                    SchedulerContract.MemberEntry.TABLE_NAME + "." + SchedulerContract.MemberEntry.COLUMN_ID + " = ?",
                    new String[]{id}
            );
        }

        getSupportLoaderManager().restartLoader(MEMBER_LIST_LOADER, null, this);
    }
}
