package com.sknutti.capstoneproject.data;


import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import static org.threeten.bp.temporal.TemporalAdjusters.firstDayOfMonth;
import static org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth;

public class SchedulerProvider extends ContentProvider{
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SchedulerDbHelper mOpenHelper;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    static final int APPOINTMENT = 100;
    static final int APPOINTMENT_BY_ID = 101;
    static final int APPOINTMENT_BY_MONTH_AND_INTERVIEWER = 102;
    static final int APPOINTMENT_BY_DAY_AND_INTERVIEWER = 103;
    static final int INTERVIEW_LIST = 200;
    static final int MEMBER = 300;
    static final int MEMBER_IS_INTERVIEWER = 301;
    static final int MEMBER_INTERVIEW_LIST = 400;
    static final int MEMBER_BY_INTERVIEW_LIST = 401;

    private static final SQLiteQueryBuilder sMemberByInterviewListQueryBuilder;
    static {
        sMemberByInterviewListQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        // member_interview_list INNER JOIN member ON member_interview_list.member_id = member._id
        //  INNER JOIN interview_list ON member_interview_list.interview_list_id = interview_list._id
        sMemberByInterviewListQueryBuilder.setTables(
                SchedulerContract.MemberInterviewListEntry.TABLE_NAME + " INNER JOIN " +
                        SchedulerContract.MemberEntry.TABLE_NAME +
                        " ON " + SchedulerContract.MemberInterviewListEntry.TABLE_NAME +
                        "." + SchedulerContract.MemberInterviewListEntry.COLUMN_MEMBER_KEY +
                        " = " + SchedulerContract.MemberEntry.TABLE_NAME +
                        "." + SchedulerContract.MemberEntry._ID +
                        " INNER JOIN " +
                        SchedulerContract.InterviewListEntry.TABLE_NAME +
                        " ON " + SchedulerContract.MemberInterviewListEntry.TABLE_NAME +
                        "." + SchedulerContract.MemberInterviewListEntry.COLUMN_INTERVIEW_LIST_KEY +
                        " = " + SchedulerContract.InterviewListEntry.TABLE_NAME +
                        "." + SchedulerContract.InterviewListEntry._ID);
    }

    private static final SQLiteQueryBuilder sAppointmentQueryBuilder;
    static {
        sAppointmentQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        // appointment INNER JOIN member m ON appointment.member_id = m._id
        //  INNER JOIN interview_list ON appointment.interview_list_id = interview_list._id
        //  INNER JOIN member i ON appointment.interviewer_id = i._id
        sAppointmentQueryBuilder.setTables(
                SchedulerContract.AppointmentEntry.TABLE_NAME + " INNER JOIN " +
                        SchedulerContract.MemberEntry.TABLE_NAME +
                        " m ON " + SchedulerContract.AppointmentEntry.TABLE_NAME +
                        "." + SchedulerContract.AppointmentEntry.COLUMN_MEMBER_KEY +
                        " = m." + SchedulerContract.MemberEntry._ID +
                        " INNER JOIN " +
                        SchedulerContract.InterviewListEntry.TABLE_NAME +
                        " ON " + SchedulerContract.AppointmentEntry.TABLE_NAME +
                        "." + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEW_LIST_KEY +
                        " = " + SchedulerContract.InterviewListEntry.TABLE_NAME +
                        "." + SchedulerContract.InterviewListEntry._ID +
                        " INNER JOIN " +
                        SchedulerContract.MemberEntry.TABLE_NAME +
                        " i ON " + SchedulerContract.AppointmentEntry.TABLE_NAME +
                        "." + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEWER_KEY +
                        " = i." + SchedulerContract.MemberEntry._ID);
    }

    //appointment.date_time >= ? AND appointment.date_time <= ?
    private static final String sAppointmentByDateSelection =
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." +
                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " >= ? AND " +
                    SchedulerContract.AppointmentEntry.TABLE_NAME + "." +
                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " <= ? ";

    //appointment.date_time >= ? AND appointment.date_time <= ?
    // AND appointment.interviewer_id = ?
    private static final String sAppointmentByDateAndInterviewerSelection =
            SchedulerContract.AppointmentEntry.TABLE_NAME + "." +
                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " >= ? AND " +
                    SchedulerContract.AppointmentEntry.TABLE_NAME + "." +
                    SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " <= ? AND " +
                    SchedulerContract.AppointmentEntry.TABLE_NAME + "." +
                    SchedulerContract.AppointmentEntry.COLUMN_INTERVIEWER_KEY + " = ? ";

    //member_interview_list.interview_list_id = ?
    private static final String sMemberByInterviewListSelection =
            SchedulerContract.MemberInterviewListEntry.TABLE_NAME + "." +
                    SchedulerContract.MemberInterviewListEntry.COLUMN_INTERVIEW_LIST_KEY + " = ?";

    private Cursor getAppointmentByMonthAndInterviewer(Uri uri, String[] projection, String sortOrder) {
        String interviewer = SchedulerContract.AppointmentEntry.getInterviewerFromUri(uri);
        String date = SchedulerContract.AppointmentEntry.getDateFromUri(uri);

        Instant instant = Instant.ofEpochMilli(Long.parseLong(date));
        LocalDateTime initial = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        long start = initial.with(firstDayOfMonth()).minusDays(7).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = initial.with(lastDayOfMonth()).plusDays(14).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String[] selectionArgs;
        String selection;

        if (Integer.parseInt(interviewer) == 0) {
            selection = sAppointmentByDateSelection;
            selectionArgs = new String[]{String.valueOf(start), String.valueOf(end)};
        } else {
            selectionArgs = new String[]{String.valueOf(start), String.valueOf(end), interviewer};
            selection = sAppointmentByDateAndInterviewerSelection;
        }

        return sAppointmentQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAppointmentByDayAndInterviewer(Uri uri, String[] projection, String sortOrder) {
        String interviewer = SchedulerContract.AppointmentEntry.getInterviewerFromUri(uri);
        String date = SchedulerContract.AppointmentEntry.getDateFromUri(uri);

        LocalDateTime dayBeginning = Instant.ofEpochMilli(Long.parseLong(date)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime dayEnding = dayBeginning.withHour(23).withMinute(59);
        long start = dayBeginning.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = dayEnding.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String[] selectionArgs;
        String selection;

        if (Integer.parseInt(interviewer) == 0) {
            selection = sAppointmentByDateSelection;
            selectionArgs = new String[]{String.valueOf(start), String.valueOf(end)};
        } else {
            selectionArgs = new String[]{String.valueOf(start), String.valueOf(end), interviewer};
            selection = sAppointmentByDateAndInterviewerSelection;
        }

        return sAppointmentQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMemberByInterviewList(Uri uri, String[] projection, String sortOrder) {
        String interviewListId = SchedulerContract.MemberInterviewListEntry.getInterviewListFromUri(uri);

        return sMemberByInterviewListQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMemberByInterviewListSelection,
                new String[]{interviewListId},
                null,
                null,
                sortOrder
        );
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SchedulerContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, SchedulerContract.PATH_APPOINTMENT, APPOINTMENT);
        matcher.addURI(authority, SchedulerContract.PATH_APPOINTMENT + "/#", APPOINTMENT_BY_ID);
        matcher.addURI(authority, SchedulerContract.PATH_APPOINTMENT + "/month/*/int/*", APPOINTMENT_BY_MONTH_AND_INTERVIEWER);
        matcher.addURI(authority, SchedulerContract.PATH_APPOINTMENT + "/day/*/int/*", APPOINTMENT_BY_DAY_AND_INTERVIEWER);
        matcher.addURI(authority, SchedulerContract.PATH_INT_LIST, INTERVIEW_LIST);
        matcher.addURI(authority, SchedulerContract.PATH_MEMBER, MEMBER);
        matcher.addURI(authority, SchedulerContract.PATH_MEMBER + "/isInt", MEMBER_IS_INTERVIEWER);
        matcher.addURI(authority, SchedulerContract.PATH_MEMBER_INT_LIST + "/#", MEMBER_BY_INTERVIEW_LIST);
        matcher.addURI(authority, SchedulerContract.PATH_MEMBER_INT_LIST, MEMBER_INTERVIEW_LIST);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new SchedulerDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case APPOINTMENT_BY_MONTH_AND_INTERVIEWER:
                return SchedulerContract.AppointmentEntry.CONTENT_TYPE;
            case APPOINTMENT_BY_DAY_AND_INTERVIEWER:
                return SchedulerContract.AppointmentEntry.CONTENT_TYPE;
            case APPOINTMENT_BY_ID:
                return SchedulerContract.AppointmentEntry.CONTENT_TYPE;
            case APPOINTMENT:
                return SchedulerContract.AppointmentEntry.CONTENT_TYPE;
            case INTERVIEW_LIST:
                return SchedulerContract.InterviewListEntry.CONTENT_TYPE;
            case MEMBER_IS_INTERVIEWER:
                return SchedulerContract.MemberEntry.CONTENT_TYPE;
            case MEMBER:
                return SchedulerContract.MemberEntry.CONTENT_TYPE;
            case MEMBER_BY_INTERVIEW_LIST:
                return SchedulerContract.MemberInterviewListEntry.CONTENT_TYPE;
            case MEMBER_INTERVIEW_LIST:
                return SchedulerContract.MemberInterviewListEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "appointment/month/*/int/*"
            case APPOINTMENT_BY_MONTH_AND_INTERVIEWER:
                retCursor = getAppointmentByMonthAndInterviewer(uri, projection, sortOrder);
                break;
            // "appointment/day/*/int/*"
            case APPOINTMENT_BY_DAY_AND_INTERVIEWER:
                retCursor = getAppointmentByDayAndInterviewer(uri, projection, sortOrder);
                break;
            // appointment/*
            case APPOINTMENT_BY_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SchedulerContract.AppointmentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // appointment
            case APPOINTMENT:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SchedulerContract.AppointmentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // interview_list
            case INTERVIEW_LIST:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SchedulerContract.InterviewListEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // member/isInt
            case MEMBER_IS_INTERVIEWER:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SchedulerContract.MemberEntry.TABLE_NAME,
                        projection,
                        SchedulerContract.MemberEntry.TABLE_NAME + "." + SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " = ?",
                        new String[]{String.valueOf(1)},
                        null,
                        null,
                        sortOrder
                );
                break;
            // member
            case MEMBER:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SchedulerContract.MemberEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // member_interview_list/#
            case MEMBER_BY_INTERVIEW_LIST:
                retCursor = getMemberByInterviewList(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case APPOINTMENT: {
                long _id = db.insert(SchedulerContract.AppointmentEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SchedulerContract.AppointmentEntry.buildAppointmentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MEMBER: {
                long _id = db.insert(SchedulerContract.MemberEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SchedulerContract.MemberEntry.buildMemberUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case INTERVIEW_LIST: {
                long _id = db.insert(SchedulerContract.InterviewListEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SchedulerContract.InterviewListEntry.buildInterviewListUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MEMBER_INTERVIEW_LIST: {
                long _id = db.insert(SchedulerContract.MemberInterviewListEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SchedulerContract.MemberInterviewListEntry.buildMemberInterviewListUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case APPOINTMENT:
                rowsDeleted = db.delete(
                        SchedulerContract.AppointmentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEMBER:
                rowsDeleted = db.delete(
                        SchedulerContract.MemberEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INTERVIEW_LIST:
                rowsDeleted = db.delete(
                        SchedulerContract.InterviewListEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEMBER_INTERVIEW_LIST:
                rowsDeleted = db.delete(
                        SchedulerContract.MemberInterviewListEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case APPOINTMENT:
                rowsUpdated = db.update(SchedulerContract.AppointmentEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MEMBER:
                rowsUpdated = db.update(SchedulerContract.MemberEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case INTERVIEW_LIST:
                rowsUpdated = db.update(SchedulerContract.InterviewListEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MEMBER_INTERVIEW_LIST:
                rowsUpdated = db.update(SchedulerContract.MemberInterviewListEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // This is a method to assist the testing framework in running smoothly.
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
