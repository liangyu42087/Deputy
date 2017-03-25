package deputy.android.com.deputyliang.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by liangyu42087 on 2017/3/24.
 */

public class GenericUtil {
    private static SimpleDateFormat sdf;

    private static final String TIME_STMAP_FORMAT = "yyyy-MM-dd HH:mm a";
    private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";
    public static String getFormattedTime(long timestamp){

        return new SimpleDateFormat(TIME_STMAP_FORMAT).format(timestamp);
    }

    public static String getISO_8601Format(long timestamp){
        return new SimpleDateFormat(ISO_8601_DATE_FORMAT).format(timestamp);
    }

    public static long getMillisecondsFromTime(String time){
        if(time == null || time.isEmpty()){
            return 0;
        }
        try {
            return new SimpleDateFormat(ISO_8601_DATE_FORMAT).parse(time).getTime();
        }catch(ParseException e){
            e.printStackTrace();
        }
        return 0;

    }
/*    public static String getFormattedAddress(Context context, double longitude, double latitude){
       if(geocoder == null) {
           geocoder = new Geocoder(context, Locale.getDefault());
       }
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            return address +" "+ city;
        }catch(IOException e){
            e.printStackTrace();;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }*/

}
