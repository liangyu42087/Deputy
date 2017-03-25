package deputy.android.com.deputyliang.util;

import android.content.ContentValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;

/**
 * Created by liangyu42087 on 2017/3/25.
 */

public class DeputyJsonUtil {

    public static final String ID = "id";
    public static final String START = "start";
    public static final String END = "end";
    public static final String STARTLATITUDE = "startLatitude";
    public static final String STARTLONGITUDE = "startLongitude";
    public static final String ENDLATITUDE = "endLatitude";
    public static final String ENDLONGITUDE = "endLongitude";
    public static final String IMAGE = "image";

    public static ContentValues[] getContentValueArrayFromJsonArray( JSONArray jsonArray) throws JSONException {

        ContentValues[] contentValues = new ContentValues[jsonArray.length()];


        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            int id = jsonObject.getInt(ID);
            long start = GenericUtil.getMillisecondsFromTime(jsonObject.getString(START));
            long end = GenericUtil.getMillisecondsFromTime(jsonObject.getString(END));
            double startLatitude = (jsonObject.getString(STARTLATITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(STARTLATITUDE)) ;
            double startLongitude = (jsonObject.getString(STARTLONGITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(STARTLONGITUDE));

            double endLatitude = (jsonObject.getString(ENDLATITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(ENDLATITUDE)) ;
            double endLongitude = (jsonObject.getString(ENDLONGITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(ENDLONGITUDE));
            String image = jsonObject.getString(IMAGE);


            ContentValues cv = new ContentValues();
            cv.put(DeputyContract.ShiftEntry._ID, id);
            cv.put(DeputyContract.ShiftEntry.COLUMN_START, start);
            cv.put(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE, startLatitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE, startLongitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_END, end);
            cv.put(DeputyContract.ShiftEntry.COLUMN_END_LATITUDE, endLatitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE, endLongitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_IMAGE, image);
            contentValues[i] = cv;
        }
        return contentValues;
    }

    public static Shift getShiftFromJson(JSONObject jsonObject)  throws JSONException{
        int id = jsonObject.getInt(ID);
        long start = GenericUtil.getMillisecondsFromTime(jsonObject.getString(START));
        long end = GenericUtil.getMillisecondsFromTime(jsonObject.getString(END));
        double startLatitude = (jsonObject.getString(STARTLATITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(STARTLATITUDE)) ;
        double startLongitude = (jsonObject.getString(STARTLONGITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(STARTLONGITUDE));

        double endLatitude = (jsonObject.getString(ENDLATITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(ENDLATITUDE)) ;
        double endLongitude = (jsonObject.getString(ENDLONGITUDE).isEmpty()) ? 0 : Double.parseDouble(jsonObject.getString(ENDLONGITUDE));
        String image = jsonObject.getString(IMAGE);

        Shift shift = new Shift();
        shift.set_id(id);
        shift.setStart(start);
        shift.setEnd(end);
        shift.setStartLongitude(startLongitude);
        shift.setStartLatitude(startLatitude);
        shift.setEndLongitude(endLongitude);
        shift.setEndLatitude(endLatitude);
        shift.setImage(image);
        return shift;
    }
    public static ContentValues getContentValuesFromShift(Shift shift){
        ContentValues cv = new ContentValues();
        cv.put(DeputyContract.ShiftEntry._ID, shift.get_id());
        cv.put(DeputyContract.ShiftEntry.COLUMN_START, shift.getStart());
        cv.put(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE, shift.getStartLatitude());
        cv.put(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE, shift.getStartLongitude());
        cv.put(DeputyContract.ShiftEntry.COLUMN_END, shift.getEnd());
        cv.put(DeputyContract.ShiftEntry.COLUMN_END_LATITUDE, shift.getEndLatitude());
        cv.put(DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE, shift.getEndLongitude());
        cv.put(DeputyContract.ShiftEntry.COLUMN_IMAGE, shift.getImage());
        return cv;
    }

}
