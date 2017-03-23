package deputy.android.com.deputyliang.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by liangyu42087 on 2017/3/20.
 */

public class DeputyContract {
    public static final String CONTENT_AUTHORITY = "deputy.android.com.deputyliang";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_SHIFT = "shift";
    public static final class ShiftEntry implements BaseColumns {
        // Shift content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SHIFT).build();

        /**
         * This is the name of the SQL table for shift.
         */
        public static final String TABLE_NAME = "shift";

    /*    *//**
         * This is the column name in the SQLiteDatabase for the retrieved id.
         *//*
        public static final String COLUMN_SHIFT_ID = "shift_id";*/
        /**
         * This is the column name in the SQLiteDatabase for the start time.
         */
        public static final String COLUMN_START = "start";

        /**
         * This is the column name in the SQLiteDatabase for the end time.
         */
        public static final String COLUMN_END = "end";

        /**
         * This is the column name in the SQLiteDatabase for the start longitude.
         */
        public static final String COLUMN_START_LONGITUDE = "start_longitude";

        /**
         * This is the column name in the SQLiteDatabase for the start latitude.
         */
        public static final String COLUMN_START_LATITUDE = "start_latitude";

        /**
         * This is the column name in the SQLiteDatabase for the end longitude.
         */
        public static final String COLUMN_END_LONGITUDE = "end_longitude";

        /**
         * This is the column name in the SQLiteDatabase for the end latitude.
         */
        public static final String COLUMN_END_LATITUDE = "end_latitude";

        /**
         * This is the column name in the SQLiteDatabase for the image.
         */
        public static final String COLUMN_IMAGE = "image";
    }


}
