package deputy.android.com.deputyliang.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.util.DeputyJsonUtil;
import deputy.android.com.deputyliang.util.FileUtil;
import deputy.android.com.deputyliang.util.NetworkUtils;

/**
 * Created by liangyu42087 on 2017/3/25.
 */

public class SyncService extends IntentService {

    private static final String TAG = "SyncService";
    public static final String SYNC_COMPLETE_BROADCAST = "SYNC_COMPLETE_BROADCAST";
    public SyncService() {
        super("SyncService");
    }
    private static final String UPDATE_SELECTION = DeputyContract.ShiftEntry._ID + " = ?";
    private Shift[] mShiftData;

    @Override
    protected void onHandleIntent(Intent intent) {

        /*
        Load api data.
        Compare with existing db data
        If no row exist
            get image and save to file.
            insert row
        If row exist
            check if image exist, if not get image and save to file
            update row.
         */

        try {
            Cursor cursor = getContentResolver().query(DeputyContract.ShiftEntry.CONTENT_URI, null, null, null, null, null);

            String response = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.SHIFTS_URL);
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0 ; i < jsonArray.length(); i ++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Shift shift = DeputyJsonUtil.getShiftFromJson(jsonObject);
                try {
                    String imageUrl = shift.getImage();
                    String id = String.valueOf(shift.get_id());

                    if(cursor.moveToPosition(i)){
                    /*
                    Cursor moved, row exist check if image exist;
                     */
                        String existingImage = cursor.getString(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_IMAGE));
                        if(existingImage == null || existingImage.isEmpty()){
                            //Image does not exist, get from URL and insert
                            String newImageUri =  getImageFromServerAndSaveToFile(id, shift.getImage());
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DeputyContract.ShiftEntry.COLUMN_IMAGE, newImageUri);
                            int rowUpdated = getContentResolver().update(DeputyContract.ShiftEntry.CONTENT_URI, contentValues, UPDATE_SELECTION, new String[]{id});
                            if(rowUpdated != 1){
                                Log.e(TAG, "Error updating row.");
                            }
                        }



                    }else{
                    /*
                    Unable to move cursor, no row exist. insert
                     */


                        String newImageUri = getImageFromServerAndSaveToFile(id, shift.getImage());
                        shift.setImage(newImageUri);
                        ContentValues contentValues = DeputyJsonUtil.getContentValuesFromShift(shift);
                        Uri uri =  getContentResolver().insert(DeputyContract.ShiftEntry.CONTENT_URI, contentValues );
                        if(uri == null){
                            Log.e(TAG, "Unable to insert row.");
                        }
                    }
                }catch(IOException ioe){
                    Log.e(TAG, "Unable to load image", ioe);
                }
            }
            sendBroadcast();
        }catch(IOException e){
            Log.e(TAG, "Unable to load shift from server", e);
        }catch (JSONException e){
            Log.e(TAG, "Unable to convert response to JSON", e);
        }

    }
    private String getImageFromServerAndSaveToFile(String id, String imageUrl) throws IOException{
        Picasso.with(this).invalidate(imageUrl);
        Bitmap bitmap = Picasso.with(this).load(imageUrl).resize(100, 100).get();
        Uri uri = FileUtil.saveToInternalStorage(this, bitmap, String.valueOf(id));
        return uri.toString();
    }



    private void sendBroadcast() {
        Intent intent = new Intent(SYNC_COMPLETE_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
