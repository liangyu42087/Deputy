package deputy.android.com.deputyliang.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
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
    private Shift[] mShiftData;

    @Override
    protected void onHandleIntent(Intent intent) {

        /*
        Load api data.
        Load image
        Sync db by delete and bulkinsert
         */

        try {
            String response = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.SHIFTS_URL);
            JSONArray jsonArray = new JSONArray(response);
            ContentValues[] contentValuesArray = new ContentValues[jsonArray.length()];
            for(int i = 0 ; i < jsonArray.length(); i ++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Shift shift = DeputyJsonUtil.getShiftFromJson(jsonObject);
                try {
                    String imageUrl = shift.getImage();
                    Picasso.with(this).invalidate(imageUrl);
                    Bitmap bitmap = Picasso.with(this).load(imageUrl).resize(100, 100).get();
                    Uri uri = FileUtil.saveToInternalStorage(this, bitmap, String.valueOf(shift.get_id()));

                        /*
                        Set the image to our file path
                         */
                    shift.setImage(uri.toString());

                        /*
                        Write image to file
                         */
                }catch(IOException ioe){
                    Log.e(TAG, "Unable to load image", ioe);
                }

                contentValuesArray[i] = DeputyJsonUtil.getContentValuesFromShift(shift);
            }

                /*
                Remove existing row form database
                 */
            getContentResolver().delete(DeputyContract.ShiftEntry.CONTENT_URI, null, null);

                /*
                Bulk insert
                 */
            getContentResolver().bulkInsert(DeputyContract.ShiftEntry.CONTENT_URI, contentValuesArray);

            /*
            Send broadcast to mainActivity
             */
            sendBroadcast();
        }catch(IOException e){
            Log.e(TAG, "Unable to load shift from server", e);
        }catch (JSONException e){
            Log.e(TAG, "Unable to convert response to JSON", e);
        }

    }

    private void sendBroadcast() {
        Intent intent = new Intent(SYNC_COMPLETE_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
