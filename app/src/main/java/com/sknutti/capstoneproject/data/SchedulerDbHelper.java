package com.sknutti.capstoneproject.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SchedulerDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "interviewScheduler.db";


    public SchedulerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MEMBER_TABLE = "CREATE TABLE " + SchedulerContract.MemberEntry.TABLE_NAME + " (" +
                SchedulerContract.MemberEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SchedulerContract.MemberEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SchedulerContract.MemberEntry.COLUMN_MOBILE + " TEXT, " +
                SchedulerContract.MemberEntry.COLUMN_EMAIL + " TEXT, " +
                SchedulerContract.MemberEntry.COLUMN_IS_INTERVIEWER + " INTEGER NOT NULL DEFAULT 0" +
                " );";

        final String SQL_CREATE_INTERVIEW_LIST_TABLE = "CREATE TABLE " + SchedulerContract.InterviewListEntry.TABLE_NAME + " (" +
                SchedulerContract.InterviewListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SchedulerContract.InterviewListEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SchedulerContract.InterviewListEntry.COLUMN_START_DATE + " TEXT, " +
                SchedulerContract.InterviewListEntry.COLUMN_END_DATE + " TEXT, " +
                SchedulerContract.InterviewListEntry.COLUMN_IS_CURRENT_LIST + " INTEGER NOT NULL DEFAULT 0" +
                " );";

        final String SQL_CREATE_MEMBER_INTERVIEW_LIST_TABLE = "CREATE TABLE " + SchedulerContract.MemberInterviewListEntry.TABLE_NAME + " (" +
                SchedulerContract.MemberInterviewListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SchedulerContract.MemberInterviewListEntry.COLUMN_MEMBER_KEY + " INTEGER NOT NULL, " +
                SchedulerContract.MemberInterviewListEntry.COLUMN_INTERVIEW_LIST_KEY + " INTEGER NOT NULL, " +

                " FOREIGN KEY (" + SchedulerContract.MemberInterviewListEntry.COLUMN_MEMBER_KEY + ") REFERENCES " +
                SchedulerContract.MemberEntry.TABLE_NAME + " (" + SchedulerContract.MemberEntry._ID + "), " +
                " FOREIGN KEY (" + SchedulerContract.MemberInterviewListEntry.COLUMN_INTERVIEW_LIST_KEY + ") REFERENCES " +
                SchedulerContract.InterviewListEntry.TABLE_NAME + " (" + SchedulerContract.InterviewListEntry._ID + ") " +
                " );";


        final String SQL_CREATE_APPOINTMENT_TABLE = "CREATE TABLE " + SchedulerContract.AppointmentEntry.TABLE_NAME + " (" +
                SchedulerContract.AppointmentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SchedulerContract.AppointmentEntry.COLUMN_MEMBER_KEY + " INTEGER NOT NULL, " +
                SchedulerContract.AppointmentEntry.COLUMN_INTERVIEW_LIST_KEY + " INTEGER NOT NULL, " +
                SchedulerContract.AppointmentEntry.COLUMN_INTERVIEWER_KEY + " INTEGER NOT NULL, " +
                SchedulerContract.AppointmentEntry.COLUMN_COMPLETED + " INTEGER NOT NULL, " +
                SchedulerContract.AppointmentEntry.COLUMN_DATETIME + " INTEGER NOT NULL, " +
                SchedulerContract.AppointmentEntry.COLUMN_LOCATION + " TEXT NOT NULL, " +
                SchedulerContract.AppointmentEntry.COLUMN_NOTES + " TEXT NOT NULL," +

                " FOREIGN KEY (" + SchedulerContract.AppointmentEntry.COLUMN_MEMBER_KEY + ") REFERENCES " +
                SchedulerContract.MemberEntry.TABLE_NAME + " (" + SchedulerContract.MemberEntry._ID + "), " +
                " FOREIGN KEY (" + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEW_LIST_KEY + ") REFERENCES " +
                SchedulerContract.InterviewListEntry.TABLE_NAME + " (" + SchedulerContract.InterviewListEntry._ID + "), " +
                " FOREIGN KEY (" + SchedulerContract.AppointmentEntry.COLUMN_INTERVIEWER_KEY + ") REFERENCES " +
                SchedulerContract.MemberEntry.TABLE_NAME + " (" + SchedulerContract.MemberEntry._ID + "));";


        sqLiteDatabase.execSQL(SQL_CREATE_MEMBER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_INTERVIEW_LIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MEMBER_INTERVIEW_LIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_APPOINTMENT_TABLE);

        final String SQL_INSERT_INITIAL_INTERVIEW_LIST = "INSERT INTO " + SchedulerContract.InterviewListEntry.TABLE_NAME + " (" +
                SchedulerContract.InterviewListEntry.COLUMN_NAME + ", " +
                SchedulerContract.InterviewListEntry.COLUMN_START_DATE + ", " +
                SchedulerContract.InterviewListEntry.COLUMN_END_DATE + ", " +
                SchedulerContract.InterviewListEntry.COLUMN_IS_CURRENT_LIST +
                ") VALUES ('Current', 'Nov 1 2015', 'Dec 31 2015', 1);";

        sqLiteDatabase.execSQL(SQL_INSERT_INITIAL_INTERVIEW_LIST);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
