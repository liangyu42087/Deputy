package deputy.android.com.deputyliang.network;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import deputy.android.com.deputyliang.R;

/**
 * Created by liangyu42087 on 2017/3/22.
 */

public class RequestListener implements Response.ErrorListener, Response.Listener  {
    private static RequestListener mInstance;
    private Context mContext;
    private RequestListener(Context context){
        mContext = context;
    }

    public static synchronized RequestListener getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestListener(context);
        }
        return mInstance;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(mContext.getApplicationContext(), mContext.getApplicationContext().getString(R.string.main_activity_volley_error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(Object response) {
        if(response instanceof JsonObjectRequest){

        }else if(response instanceof JsonArrayRequest){

        }
    }
}
