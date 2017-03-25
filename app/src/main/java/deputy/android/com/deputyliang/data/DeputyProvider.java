package deputy.android.com.deputyliang.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by liangyu42087 on 2017/3/20.
 */

public class DeputyProvider extends ContentProvider {
    public static final int CODE_SHIFT = 100;
    public static final int CODE_SHIFT_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DeputyDBHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        /*
         * Uri Matcher to match uri.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DeputyContract.CONTENT_AUTHORITY;

        /* This URI is content://deputy.android.com.deputyliang/shift/ */
        matcher.addURI(authority, DeputyContract.ShiftEntry.TABLE_NAME, CODE_SHIFT);

        /*
         * This URI is content://deputy.android.com.deputyliang/shift/42
         */
        matcher.addURI(authority, DeputyContract.ShiftEntry.TABLE_NAME + "/#", CODE_SHIFT_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DeputyDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            case CODE_SHIFT_WITH_ID: {

                String id = uri.getLastPathSegment();

                String[] selectionArguments = new String[]{id};

                cursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        DeputyContract.ShiftEntry.TABLE_NAME,
                        projection,
                        DeputyContract.ShiftEntry._ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case CODE_SHIFT: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        DeputyContract.ShiftEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_SHIFT:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DeputyContract.ShiftEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Get access to the deputy database (to write new data to)
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case CODE_SHIFT:
                // Insert new values into the database
                // Inserting values into tasks table
                long id = db.insert(DeputyContract.ShiftEntry.TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(DeputyContract.ShiftEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case CODE_SHIFT:
                numRowsUpdated = mOpenHelper.getWritableDatabase().update(
                        DeputyContract.ShiftEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;

        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {

            case CODE_SHIFT:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        DeputyContract.ShiftEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }
    @Override
    public String getType(Uri uri) {
        /*
            We are not implementing this method.
         */
        return null;
    }

}
