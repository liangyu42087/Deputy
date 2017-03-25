package deputy.android.com.deputyliang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import deputy.android.com.deputyliang.adapter.ShiftAdapter;
import deputy.android.com.deputyliang.data.DeputyAsyncHandler;
import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.util.NetworkUtils;
import deputy.android.com.deputyliang.network.VolleyRequestQueue;
import deputy.android.com.deputyliang.service.SyncService;
import deputy.android.com.deputyliang.util.GenericUtil;

public class MainActivity extends AppCompatActivity implements ShiftAdapter.ShiftAdapterOnClickHandler, DeputyAsyncHandler.AsyncListener{

    private RecyclerView mRecyclerView;
    private ShiftAdapter mShiftAdapter;
    private TextView mEmptyMessageDisplay;
    private String TAG = "MainActivity";
    private Shift[] mShiftData;
    private Intent mServiceIntent;

    private static final int QUERY_TOKEN = 105;
    private static final String SERVICE_STARTED_KEY = "SERVICE_STARTED_KEY";
    private static final int ADD_SHIFT_REQUEST = 1;
    private boolean mServiceStarted = false;

    private DeputyAsyncHandler mAsyncHandler;

    private static final String PROJECTION[] = {DeputyContract.ShiftEntry._ID,
            DeputyContract.ShiftEntry.COLUMN_START,
            DeputyContract.ShiftEntry.COLUMN_END,
            DeputyContract.ShiftEntry.COLUMN_IMAGE};

    private static final String SORT_ORDER = DeputyContract.ShiftEntry._ID + " DESC";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_shifts);
        mEmptyMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mShiftAdapter = new ShiftAdapter(this, this);

        mRecyclerView.setAdapter(mShiftAdapter);

        mAsyncHandler = new DeputyAsyncHandler(getContentResolver(), this);

        updateValuesFromBundle(savedInstanceState);

        mServiceIntent = new Intent(this, SyncService.class);
        if(!mServiceStarted) {
            mServiceStarted = true;
            startService(mServiceIntent);
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Sync complete reload list
                populateShiftData();
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(SyncService.SYNC_COMPLETE_BROADCAST));

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(SERVICE_STARTED_KEY)) {
                mServiceStarted = savedInstanceState.getBoolean(SERVICE_STARTED_KEY);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SERVICE_STARTED_KEY, mServiceStarted);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateShiftData();
    }

    private void populateShiftData(){
        mAsyncHandler.startQuery(QUERY_TOKEN, null, DeputyContract.ShiftEntry.CONTENT_URI, PROJECTION, null, null, SORT_ORDER);
    }

    @Override
    public void onAsyncComplete(int token, int result, Uri uri, Cursor cursor) {
            if(token == QUERY_TOKEN && cursor != null){
                mShiftData = new Shift[cursor.getCount()];
                for(int i = 0; i < cursor.getCount(); i ++){
                    if(!cursor.moveToNext()){
                        break;
                    }
                    Shift shift = new Shift();
                    shift.set_id(cursor.getInt(cursor.getColumnIndex(DeputyContract.ShiftEntry._ID)));
                    shift.setStart(cursor.getLong(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_START)));
                    shift.setEnd(cursor.getLong(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_END)));
                    shift.setImage(cursor.getString(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_IMAGE)));
                    mShiftData[i] = shift;
                }
                cursor.close();
                mShiftAdapter.setShiftData(mShiftData);
            }
    }
    @Override
    public void onClick(Shift shift) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.SHIFT_ID_KEY, shift.get_id());
        startActivity(intent);
    }

    public void addNewShifts(View view){
        if(mShiftData != null && mShiftData.length > 0){
            Shift shift = mShiftData[0];
            if(shift.getEnd() <= 0){
                //Incomplete shifts
                Toast.makeText(this, getString(R.string.incomplete_shift), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        VolleyRequestQueue.getInstance(this).getRequestQueue().cancelAll(TAG);
    }
}
