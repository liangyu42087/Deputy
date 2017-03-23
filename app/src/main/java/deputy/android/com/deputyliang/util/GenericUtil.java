package deputy.android.com.deputyliang.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by liangyu42087 on 2017/3/24.
 */

public class GenericUtil {
    private static SimpleDateFormat sdf;
    private static Geocoder geocoder;
    private static final String TIME_STMAP_FORMAT = "yyyy-MM-dd HH:mm a";
    public static String getFormattedTime(long timestamp){
        if(sdf == null){
            sdf  = new SimpleDateFormat(TIME_STMAP_FORMAT);
        }
        return sdf.format(timestamp);
    }

    public static String getFormattedAddress(Context context, double longitude, double latitude){
       if(geocoder == null) {
           geocoder = new Geocoder(context, Locale.getDefault());
       }
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            return address + city;
        }catch(IOException e){
            e.printStackTrace();;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
