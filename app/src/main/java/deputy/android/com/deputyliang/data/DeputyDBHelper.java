package deputy.android.com.deputyliang.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by liangyu42087 on 2017/3/20.
 */

public class DeputyDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "deputy.db";
    private static final int DATABASE_VERSION = 1;

    public DeputyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_SHIFT_TABLE =
                "CREATE TABLE " + DeputyContract.ShiftEntry.TABLE_NAME + " (" +
                        DeputyContract.ShiftEntry._ID                               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        DeputyContract.ShiftEntry.COLUMN_SHIFT_ID               + " INTEGER NOT NULL, " +
                        DeputyContract.ShiftEntry.COLUMN_START                  + " TEXT NOT NULL," +
                        DeputyContract.ShiftEntry.COLUMN_START_LATITUDE           + " TEXT NOT NULL," +
                        DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE           + " TEXT NOT NULL," +
                        DeputyContract.ShiftEntry.COLUMN_END                        + " TEXT,"                  +
                        DeputyContract.ShiftEntry.COLUMN_END_LATITUDE           + " TEXT," +
                        DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE           + " TEXT," +
                        DeputyContract.ShiftEntry.COLUMN_IMAGE                   + " TEXT);";

        db.execSQL(SQL_CREATE_SHIFT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        We are on database version 1. No need to implement this method.
         */
    }
}
