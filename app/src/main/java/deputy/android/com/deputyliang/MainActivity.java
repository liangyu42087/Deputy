package deputy.android.com.deputyliang;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import deputy.android.com.deputyliang.adapter.ShiftAdapter;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.network.VolleyRequestQueue;
import deputy.android.com.deputyliang.testing.ShiftTestUtil;

public class MainActivity extends AppCompatActivity implements ShiftAdapter.ShiftAdapterOnClickHandler, Response.ErrorListener, Response.Listener, ImageLoader.ImageListener{

    private RecyclerView mRecyclerView;
    private ShiftAdapter mShiftAdapter;
    private TextView mEmptyMessageDisplay;
    private String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_shifts);
        mEmptyMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mShiftAdapter = new ShiftAdapter(this);

        mRecyclerView.setAdapter(mShiftAdapter);

        mShiftAdapter.setShiftData(ShiftTestUtil.generateArrayOfFakeShift());
        populateActionBar();
    }

    /*
    Populate actionbar with value returned by https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc
     */
    private void populateActionBar(){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, VolleyRequestQueue.BUSINESS_URL, null, this, this){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return VolleyRequestQueue.getHeaderParameter();
            }
        };
        jsonObjectRequest.setTag(TAG);
        VolleyRequestQueue.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public void onResponse(Object response) {
        try {
            if (response instanceof JSONObject) {
                //Insert into DB.
                JSONObject jsonObject = (JSONObject) response;
                getSupportActionBar().setTitle(jsonObject.getString("name"));
            } else if (response instanceof JSONObject[]) {

            }
        }catch (JSONException e){
            Toast.makeText(this, getString(R.string.main_activity_volley_error), Toast.LENGTH_LONG).show();
            Log.e(TAG, getString(R.string.main_activity_volley_error), e);
        }
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        if(response.getBitmap() != null){

        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, getString(R.string.main_activity_volley_error), Toast.LENGTH_LONG).show();
        Log.e(TAG, getString(R.string.main_activity_volley_error), error);
    }

    @Override
    public void onClick(Shift shift) {
       //
    }

    public void addNewShifts(View view){
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }
    private void showShiftDataView() {
        mEmptyMessageDisplay.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.GONE);
        /* Then, show the error */
        mEmptyMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        VolleyRequestQueue.getInstance(this).getRequestQueue().cancelAll(TAG);
    }
}
