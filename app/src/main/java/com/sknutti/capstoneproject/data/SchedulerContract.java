package com.sknutti.capstoneproject.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class SchedulerContract {
    public static final String CONTENT_AUTHORITY = "com.sknutti.capstoneproject";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MEMBER = "member";
    public static final String PATH_INT_LIST = "interview_list";
    public static final String PATH_MEMBER_INT_LIST = "member_interview_list";
    public static final String PATH_APPOINTMENT = "appointment";


    public static final class MemberEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMBER).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBER;


        public static final String TABLE_NAME = "member";

        // member id - assigned automatically
        public static final String COLUMN_ID = "_id";
        // The name of the member
        public static final String COLUMN_NAME = "name";
        // The mobile number for the member
        public static final String COLUMN_MOBILE = "mobile";
        // The email for the member
        public static final String COLUMN_EMAIL = "email";
        // Flag to set if the member is also an interviewer
        public static final String COLUMN_IS_INTERVIEWER = "is_interviewer";


        public static Uri buildMemberUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMemberIsInterviewer() {
            return CONTENT_URI.buildUpon().appendPath("interviewer").build();
        }
    }

    public static final class InterviewListEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INT_LIST).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INT_LIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INT_LIST;


        public static final String TABLE_NAME = "interview_list";

        // interview list id - assigned automatically
        public static final String COLUMN_ID = "_id";
        // The name of the interview list
        public static final String COLUMN_NAME = "name";
        // The start date for the interview list -- used only for display
        public static final String COLUMN_START_DATE = "start_date";
        // The end date for the interview list -- used only for display
        public static final String COLUMN_END_DATE = "end_date";
        // Flag to specify the current interview list being used
        public static final String COLUMN_IS_CURRENT_LIST = "is_current_list";


        public static Uri buildInterviewListUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class MemberInterviewListEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEMBER_INT_LIST).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBER_INT_LIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMBER_INT_LIST;


        public static final String TABLE_NAME = "member_interview_list";

        // Foreign key to the member
        public static final String COLUMN_MEMBER_KEY = "member_id";
        // Foreign key to the interview list
        public static final String COLUMN_INTERVIEW_LIST_KEY = "interview_list_id";


        public static Uri buildMemberInterviewListUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMemberByInterviewList(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static String getInterviewListFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }


    public static final class AppointmentEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_APPOINTMENT).build();


        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPOINTMENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPOINTMENT;


        public static final String TABLE_NAME = "appointment";


        // appointment id - assigned automatically
        public static final String COLUMN_ID = "_id";
        // Foreign key to the member
        public static final String COLUMN_MEMBER_KEY = "member_id";
        // Foreign key to the interview list
        public static final String COLUMN_INTERVIEW_LIST_KEY = "interview_list_id";
        // Foreign key to the interviewer
        public static final String COLUMN_INTERVIEWER_KEY = "interviewer_id";
        // Flag to mark if the interview was completed
        public static final String COLUMN_COMPLETED = "completed";
        // DateTime, stored as string
        public static final String COLUMN_DATETIME = "date_time";
        // The location of the interview
        public static final String COLUMN_LOCATION = "location";
        // Notes about the interview, e.g. purpose, talking points, etc
        public static final String COLUMN_NOTES = "notes";


        public static Uri buildAppointmentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


        public static Uri buildAppointmentByMonthAndInterviewer(long date, long interviewer) {
            return CONTENT_URI.buildUpon().appendPath("month").appendPath(Long.toString(date))
                    .appendPath("int").appendPath(Long.toString(interviewer)).build();
        }

        public static Uri buildAppointmentByWeekAndInterviewer(long date, long interviewer) {
            return CONTENT_URI.buildUpon().appendPath("week").appendPath(Long.toString(date))
                    .appendPath("int").appendPath(Long.toString(interviewer)).build();
        }

        public static Uri buildAppointmentByDayAndInterviewer(long date, long interviewer) {
            return CONTENT_URI.buildUpon().appendPath("day").appendPath(Long.toString(date))
                    .appendPath("int").appendPath(Long.toString(interviewer)).build();
        }


        public static String getInterviewerFromUri(Uri uri) {
            return uri.getPathSegments().get(4);
        }


        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }


        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_DATETIME);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
    }
}
