package deputy.android.com.deputyliang.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liangyu42087 on 2017/3/22.
 */

public class VolleyRequestQueue {
/*    public static final String BASE_URL = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc";
    public static final String BUSINESS_URL = BASE_URL + "/business";
    public static final String START_SHIFT_URL = BASE_URL + "/shift/start";
    public static final String END_SHIFT_URL = BASE_URL + "/shift/end";
    public static final String SHIFTS_URL = BASE_URL + "/shifts";*/
    public static final String PARAM_TOKEN = "Deputy 863407acb9e207441ec9e80d0debd4fe4489aa9b";
    public static final String PARAM_AUTHORIZATION = "Authorization";

    public static final String POST_TIME_KEY = "time";
    public static final String POST_LATITUDE_KEY = "latitude";
    public static final String POST_LONGITUDE_KEY = "longitude";


    private static Map<String, String> PARAM_HEADER;

    private static VolleyRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;
    private ImageLoader mImageLoader;
    private JsonObjectRequest mJsonObjectRequest;

    private VolleyRequestQueue(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }
    public static synchronized VolleyRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static Map<String, String> getHeaderParameter(){
        if(PARAM_HEADER == null){
            PARAM_HEADER = new HashMap<String, String>();
            PARAM_HEADER.put(PARAM_AUTHORIZATION, PARAM_TOKEN);
        }
        return PARAM_HEADER;
    }
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
