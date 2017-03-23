package deputy.android.com.deputyliang;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.data.DeputyDBHelper;
import deputy.android.com.deputyliang.data.DeputyProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by liangyu42087 on 2017/3/20.
 */
@RunWith(AndroidJUnit4.class)
public class TestDeputyProvider {

    /* Context used to access various parts of the system */
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    /**
     * We want to start each test clean, so we delete all entries in the tasks directory to do so.
     */
    @Before
    public void setUp() {
        /* Use TaskDbHelper to get access to a writable database */
        DeputyDBHelper dbHelper = new DeputyDBHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DeputyContract.ShiftEntry.TABLE_NAME, null, null);
    }


    //================================================================================
    // Test ContentProvider Registration
    //================================================================================


    /**
     * This test checks to make sure that the content provider is registered correctly in the
     * AndroidManifest file. If it fails, you should check the AndroidManifest to see if you've
     * added a <provider/> tag and that you've properly specified the android:authorities attribute.
     */
    @Test
    public void testProviderRegistry() {

        /*
         * A ComponentName is an identifier for a specific application component, such as an
         * Activity, ContentProvider, BroadcastReceiver, or a Service.
         *
         * Two pieces of information are required to identify a component: the package (a String)
         * it exists in, and the class (a String) name inside of that package.
         *
         * We will use the ComponentName for our ContentProvider class to ask the system
         * information about the ContentProvider, specifically, the authority under which it is
         * registered.
         */
        String packageName = mContext.getPackageName();
        String shiftProviderClassName = DeputyProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, shiftProviderClassName);

        try {

            /*
             * Get a reference to the package manager. The package manager allows us to access
             * information about packages installed on a particular device. In this case, we're
             * going to use it to get some information about our ContentProvider under test.
             */
            PackageManager pm = mContext.getPackageManager();

            /* The ProviderInfo will contain the authority, which is what we want to test */
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = packageName;

            /* Make sure that the registered authority matches the authority from the Contract */
            String incorrectAuthority =
                    "Error: DeputyProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority);

        } catch (PackageManager.NameNotFoundException e) {
            String providerNotRegisteredAtAll =
                    "Error: DeputyProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);
        }
    }


    //================================================================================
    // Test UriMatcher
    //================================================================================


    private static final Uri TEST_SHIFT = DeputyContract.ShiftEntry.CONTENT_URI;
    // Content URI for a single task with id = 1
    private static final Uri TEST_SHIFT_WITH_ID = TEST_SHIFT.buildUpon().appendPath("1").build();


    /**
     * This function tests that the UriMatcher returns the correct integer value for
     * each of the Uri types that the ContentProvider can handle. Uncomment this when you are
     * ready to test your UriMatcher.
     */
    @Test
    public void testUriMatcher() {

        /* Create a URI matcher that the TaskContentProvider uses */
        UriMatcher testMatcher = DeputyProvider.buildUriMatcher();

        /* Test that the code returned from our matcher matches the expected TASKS int */
        String uriDoesNotMatch = "Error: The TEST_SHIFT URI was matched incorrectly.";
        int actualShiftMatchCode = testMatcher.match(TEST_SHIFT);
        int expectedShiftMatchCode = DeputyProvider.CODE_SHIFT;
        assertEquals(uriDoesNotMatch,
                actualShiftMatchCode,
                expectedShiftMatchCode);

        /* Test that the code returned from our matcher matches the expected TASK_WITH_ID */
        String uriWithIdDoesNotMatch =
                "Error: The TEST_SHIFT_WITH_ID URI was matched incorrectly.";
        int actualShiftWithIdCode = testMatcher.match(TEST_SHIFT_WITH_ID);
        int expectedShiftWithIdCode = DeputyProvider.CODE_SHIFT_WITH_ID;
        assertEquals(uriWithIdDoesNotMatch,
                actualShiftWithIdCode,
                expectedShiftWithIdCode);
    }


    //================================================================================
    // Test Insert
    //================================================================================


    /**
     * Tests inserting a single row of data via a ContentResolver
     */
    @Test
    public void testInsert() {

        /* Create values to insert */
        ContentValues shiftValues = new ContentValues();
        //shiftValues.put(DeputyContract.ShiftEntry.COLUMN_SHIFT_ID, "42");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START, "2017-01-17T06:35:57+00:00");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE, "0.00000");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE, "0.00000");
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = contentResolver.insert(DeputyContract.ShiftEntry.CONTENT_URI, shiftValues);
        String insertProviderFailed = "Unable to insert item through Provider";
        assertTrue(insertProviderFailed, uri != null);
    }


    //================================================================================
    // Test Query (for tasks directory)
    //================================================================================


    /**
     * Inserts data, then tests if a query for the tasks directory returns that data as a Cursor
     */
    @Test
    public void testQuery() {

        /* Get access to a writable database */
        DeputyDBHelper dbHelper = new DeputyDBHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues shiftValues = new ContentValues();
       // shiftValues.put(DeputyContract.ShiftEntry.COLUMN_SHIFT_ID, "42");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START, "2017-01-17T06:35:57+00:00");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE, "0.00000");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE, "0.00000");

        /* Insert ContentValues into database and get a row ID back */
        long rowId = database.insert(
                /* Table to insert values into */
                DeputyContract.ShiftEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                shiftValues);

        String insertFailed = "Unable to insert directly into the database";
        assertTrue(insertFailed, rowId != -1);

        /* We are done with the database, close it now. */
        database.close();

        /* Perform the ContentProvider query */
        Cursor cursor = mContext.getContentResolver().query(DeputyContract.ShiftEntry.CONTENT_URI, null, null, null, null);


        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, cursor != null);

        /* We are done with the cursor, close it now. */
        cursor.close();
    }


    //================================================================================
    // Test Delete (for a single item)
    //================================================================================


    /**
     * Tests updating single row
     */
    @Test
    public void testUpdate() {
        /* Access writable database */
        DeputyDBHelper dbHelper = new DeputyDBHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues shiftValues = new ContentValues();
        //shiftValues.put(DeputyContract.ShiftEntry.COLUMN_SHIFT_ID, "42");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START, "2017-01-17T06:35:57+00:00");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE, "0.00000");
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE, "0.00000");

           /* Insert ContentValues into database and get a row ID back */
        long rowId = database.insert(
                /* Table to insert values into */
                DeputyContract.ShiftEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                shiftValues);

        String insertFailed = "Unable to insert directly into the database";
        assertTrue(insertFailed, rowId != -1);

        /* We are done with the database, close it now. */
        database.close();

        ContentResolver contentResolver = mContext.getContentResolver();

        /*
        Update ContentValues
         */
        shiftValues.put(DeputyContract.ShiftEntry.COLUMN_END, "2017-01-16T18:42:12+00:00");

        //String selection = DeputyContract.ShiftEntry.COLUMN_SHIFT_ID + " = ?";
        String [] selectionArgs = {"42"};

        int updatedRows = contentResolver.update(DeputyContract.ShiftEntry.CONTENT_URI, shiftValues, selection, selectionArgs);

        String updatedFailed = "Unable to update item in the database";
        assertTrue(updatedFailed, updatedRows != 0);

    }

}
