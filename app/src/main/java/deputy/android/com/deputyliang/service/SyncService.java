package deputy.android.com.deputyliang.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import deputy.android.com.deputyliang.R;
import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.network.VolleyRequestQueue;
import deputy.android.com.deputyliang.util.CursorUtil;
import deputy.android.com.deputyliang.util.DeputyJsonUtil;
import deputy.android.com.deputyliang.util.FileUtil;
import deputy.android.com.deputyliang.util.GenericUtil;
import deputy.android.com.deputyliang.util.NetworkUtils;

/**
 * Created by liangyu42087 on 2017/3/25.
 */

public class SyncService extends IntentService implements Response.Listener, Response.ErrorListener{

    private static final String TAG = "SyncService";
    public static final String SYNC_COMPLETE_BROADCAST = "SYNC_COMPLETE_BROADCAST";
    public SyncService() {
        super("SyncService");
    }
    private static final String UPDATE_SELECTION = DeputyContract.ShiftEntry._ID + " = ?";
    private Shift[] mShiftData;

    @Override
    protected void onHandleIntent(Intent intent) {


        Cursor cursor = null;
        try {

            cursor = getContentResolver().query(DeputyContract.ShiftEntry.CONTENT_URI, null, null, null, null, null);
            String response = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.SHIFTS_URL);
            JSONArray jsonArray = new JSONArray(response);
            serverToAppSync(cursor, jsonArray);
            boolean serverUpdated = appToServerSync(cursor, jsonArray);
            if(!serverUpdated) {
                sendBroadcast();
            }
        }catch(IOException e){
            Log.e(TAG, "Unable to load shift from server", e);
        }catch (JSONException e){
            Log.e(TAG, "Unable to convert response to JSON", e);
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }

    }
    /*
    Sync from app to server, used when app inserted a row without internet connection.
     */
    private boolean appToServerSync(Cursor cursor, JSONArray jsonArray) throws JSONException{
        boolean serverUpdated = false;

        for(int i = 0; i < cursor.getCount(); i ++){
            if(cursor.moveToPosition(i)){
                Shift shift = CursorUtil.getShiftFromCursor(cursor);
                if(i >= jsonArray.length()){
                    /*
                    Entire record does not exist, insert start and end shift
                     */
                    postToApi(NetworkUtils.START_SHIFT_URL, shift.getStart(), shift.getStartLatitude(), shift.getStartLongitude());
                    postToApi(NetworkUtils.END_SHIFT_URL, shift.getEnd(), shift.getEndLatitude(), shift.getEndLongitude());

                    serverUpdated = true;
                }else{
                    /*
                    Record exist, check end time
                     */
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    long end = GenericUtil.getMillisecondsFromTime(jsonObject.getString(DeputyJsonUtil.END));
                    if(end == 0 && shift.getEnd() != 0){
                        /*
                        No end time
                         */
                        postToApi(NetworkUtils.END_SHIFT_URL, shift.getEnd(), shift.getEndLatitude(), shift.getEndLongitude());
                        serverUpdated = true;
                    }
                }
            }else{
                break;
            }
        }
        return serverUpdated;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        /*
        Unfortunate that we got an error.
         */
        sendBroadcast();
    }

    @Override
    public void onResponse(Object response) {
        sendBroadcast();
    }

    private void postToApi(String url, long time, double latitude, double longitude){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(VolleyRequestQueue.POST_TIME_KEY, GenericUtil.getISO_8601Format(time));
            jsonObject.put(VolleyRequestQueue.POST_LATITUDE_KEY, latitude);
            jsonObject.put(VolleyRequestQueue.POST_LONGITUDE_KEY, longitude);
        }catch(JSONException e){
            Log.e(TAG, getString(R.string.detail_activity_json_error), e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, null, null){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return VolleyRequestQueue.getHeaderParameter();
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyRequestQueue.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /*
    Sync from server to application.
     */
    private void serverToAppSync(Cursor cursor, JSONArray jsonArray)throws IOException, JSONException{
        /*
        Get api data
        Compare with existing db data
        If no row exist
            get image and save to file.
            insert row
        If row exist
            check if image exist, if not get image and save to file
            update row.
         */
        for(int i = 0 ; i < jsonArray.length(); i ++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Shift shift = DeputyJsonUtil.getShiftFromJson(jsonObject);

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
