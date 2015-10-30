package com.sknutti.capstoneproject;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Toast;

import com.sknutti.capstoneproject.data.SchedulerContract;
import com.sknutti.capstoneproject.dialogs.AddIntListDialogFragment;
import com.sknutti.capstoneproject.dialogs.EditIntListDialogFragment;

import timber.log.Timber;

public class InterviewListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        AddIntListDialogFragment.AddIntListDialogListener,
        EditIntListDialogFragment.EditIntListDialogListener {
    private RecyclerView mRecyclerView;
    private InterviewListAdapter mInterviewListAdapter;

    private static final int INTERVIEW_LIST_LOADER = 0;

    public static final String[] INTERVIEW_LIST_COLUMNS = {
            SchedulerContract.InterviewListEntry._ID,
            SchedulerContract.InterviewListEntry.COLUMN_NAME,
            SchedulerContract.InterviewListEntry.COLUMN_START_DATE,
            SchedulerContract.InterviewListEntry.COLUMN_END_DATE,
            SchedulerContract.InterviewListEntry.COLUMN_IS_CURRENT_LIST
    };

    public static final int COL_INT_LIST_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_START_DATE = 2;
    public static final int COL_END_DATE = 3;
    public static final int COL_IS_CURRENT_LIST = 4;

    public interface Callback {
        void onItemSelected(Uri uri, MemberListAdapter.MemberListAdapterViewHolder vh);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_interview_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        View emptyView = findViewById(R.id.rv_interview_list_empty);
        mRecyclerView.setHasFixedSize(true);

        mInterviewListAdapter = new InterviewListAdapter(this, emptyView);
        mRecyclerView.setAdapter(mInterviewListAdapter);

        registerForContextMenu(mRecyclerView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new AddIntListDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), dialogFragment.getClass().getName());
            }
        });

        getSupportLoaderManager().initLoader(INTERVIEW_LIST_LOADER, null, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Cursor cursor = mInterviewListAdapter.getCursor();
        if (cursor == null) {
            //TODO: display a message to the user
            return;     // For some reason the requested item isn't available, do nothing
        }

        cursor.moveToPosition(mInterviewListAdapter.getPosition());
        menu.setHeaderTitle(cursor.getString(MemberListActivity.COL_NAME));

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_int_list, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        RecyclerViewContextMenuInfo info = (RecyclerViewContextMenuInfo) item.getMenuInfo();

        int position;
        try {
            position = mInterviewListAdapter.getPosition();
        } catch (Exception e) {
            Timber.d(e.getLocalizedMessage(), e);
            return super.onContextItemSelected(item);
        }

        Cursor cursor = mInterviewListAdapter.getCursor();
        cursor.moveToPosition(position);
        long id = cursor.getLong(COL_INT_LIST_ID);

        switch (item.getItemId()) {
            case R.id.menu_make_current_int_list:
                Timber.d("Making interview list current");
                // Loop through list of interview lists and set is_current to false(0)
                Cursor ilCursor = getContentResolver().query(
                        SchedulerContract.InterviewListEntry.CONTENT_URI,
                        InterviewListActivity.INTERVIEW_LIST_COLUMNS,
                        null,
                        null,
                        SchedulerContract.InterviewListEntry.COLUMN_NAME + " ASC");
                if (ilCursor != null && ilCursor.getCount() > 0) {
                    if (ilCursor.moveToFirst()) {
                        do {
                            ContentValues milValues = new ContentValues();
                            milValues.put(SchedulerContract.InterviewListEntry.COLUMN_IS_CURRENT_LIST, "0");
                            getContentResolver().update(
                                    SchedulerContract.InterviewListEntry.CONTENT_URI,
                                    milValues,
                                    SchedulerContract.InterviewListEntry.COLUMN_ID + " = ?",
                                    new String[]{String.valueOf(ilCursor.getLong(COL_INT_LIST_ID))}
                            );
                        } while (ilCursor.moveToNext());
                    }
                    ilCursor.close();
                }

                // then set this list to current
                ContentValues milValues = new ContentValues();
                milValues.put(SchedulerContract.InterviewListEntry.COLUMN_IS_CURRENT_LIST, "1");
                getContentResolver().update(
                        SchedulerContract.InterviewListEntry.CONTENT_URI,
                        milValues,
                        SchedulerContract.InterviewListEntry.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)}
                );
                break;
            case R.id.menu_delete_int_list:
                Timber.d("Deleting interview list");
                if (id == 1) {
                    Toast.makeText(getApplicationContext(),
                            "Cannot delete the Current list",
                            Toast.LENGTH_SHORT).show();
                } else {
                    getContentResolver().delete(
                            SchedulerContract.InterviewListEntry.CONTENT_URI,
                            SchedulerContract.InterviewListEntry.COLUMN_ID + " = ?",
                            new String[]{String.valueOf(id)}
                    );
                    getSupportLoaderManager().restartLoader(INTERVIEW_LIST_LOADER, null, this);
                }
                break;
            default:
                //TODO: display message to user
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = SchedulerContract.InterviewListEntry.COLUMN_NAME + " ASC";
        Uri uri = SchedulerContract.InterviewListEntry.CONTENT_URI;

        return new CursorLoader(this,
                uri,
                INTERVIEW_LIST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInterviewListAdapter.swapCursor(data);
//        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInterviewListAdapter.swapCursor(null);
    }

    @Override
    public void onClosed(String action, String id, String name, String startDate, String endDate, Boolean isCurrentList) {

        ContentValues intListValues = new ContentValues();

        intListValues.put(SchedulerContract.InterviewListEntry.COLUMN_NAME, name);
        intListValues.put(SchedulerContract.InterviewListEntry.COLUMN_START_DATE, startDate);
        intListValues.put(SchedulerContract.InterviewListEntry.COLUMN_END_DATE, endDate);
        intListValues.put(SchedulerContract.InterviewListEntry.COLUMN_IS_CURRENT_LIST, isCurrentList ? 1 : 0);

        // Loo[ through all interview lists and set to false if the new list is being set to the current list
        if (isCurrentList) {
            Cursor cursor = getContentResolver().query(
                    SchedulerContract.InterviewListEntry.CONTENT_URI,
                    InterviewListActivity.INTERVIEW_LIST_COLUMNS,
                    null,
                    null,
                    SchedulerContract.InterviewListEntry.COLUMN_NAME + " ASC");
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        ContentValues milValues = new ContentValues();
                        milValues.put(SchedulerContract.InterviewListEntry.COLUMN_IS_CURRENT_LIST, "0");
                        getContentResolver().update(
                                SchedulerContract.InterviewListEntry.CONTENT_URI,
                                milValues,
                                SchedulerContract.InterviewListEntry.COLUMN_ID + " = ?",
                                new String[]{String.valueOf(cursor.getLong(COL_INT_LIST_ID))}
                        );
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        if (action.equals("add")) {
            Uri insertedUri = getContentResolver().insert(
                    SchedulerContract.InterviewListEntry.CONTENT_URI,
                    intListValues
            );
            long interviewListId = ContentUris.parseId(insertedUri);

            // Add all members to this new interview list
            Cursor cursor = getContentResolver().query(
                    SchedulerContract.MemberEntry.CONTENT_URI,
                    MemberListActivity.MEMBER_COLUMNS,
                    null,
                    null,
                    SchedulerContract.MemberEntry.COLUMN_NAME + " ASC");
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        ContentValues milValues = new ContentValues();
                        milValues.put(SchedulerContract.MemberInterviewListEntry.COLUMN_MEMBER_KEY, cursor.getLong(MemberListActivity.COL_MEMBER_ID));
                        milValues.put(SchedulerContract.MemberInterviewListEntry.COLUMN_INTERVIEW_LIST_KEY, interviewListId);
                        Uri insertedUri2 = getContentResolver().insert(
                                SchedulerContract.MemberInterviewListEntry.CONTENT_URI,
                                milValues
                        );
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } else {
            int numUpdated = getContentResolver().update(
                    SchedulerContract.InterviewListEntry.CONTENT_URI,
                    intListValues,
                    SchedulerContract.InterviewListEntry.TABLE_NAME + "." + SchedulerContract.InterviewListEntry.COLUMN_ID + " = ?",
                    new String[]{id}
            );
        }

        getSupportLoaderManager().restartLoader(INTERVIEW_LIST_LOADER, null, this);
    }
}
