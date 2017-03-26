package deputy.android.com.deputyliang;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import deputy.android.com.deputyliang.data.DeputyAsyncHandler;
import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.service.SyncService;
import deputy.android.com.deputyliang.util.CursorUtil;
import deputy.android.com.deputyliang.util.NetworkUtils;
import deputy.android.com.deputyliang.network.VolleyRequestQueue;
import deputy.android.com.deputyliang.util.GenericUtil;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, DeputyAsyncHandler.AsyncListener,
        OnMapReadyCallback, Response.ErrorListener, LoaderManager.LoaderCallbacks<Shift> {
    public static final String SHIFT_ID_KEY = "SHIFT_ID_KEY";
    private static final String TAG = "DetailActivity";
    private static final int INSERT_TOKEN = 103;
    private static final int UPDATE_TOKEN = 104;
    private static final int QUERY_TOKEN = 106;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";

    private static final String LOCATION_KEY = "LOCATION_KEY";
    private static final String SHIFT_KEY = "SHIFT_KEY";
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 11;
    private static final String SELECTION = DeputyContract.ShiftEntry._ID + " = ?";

    private static final int ADDRESS_SEARCH_LOADER = 22;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Geocoder geocoder;
    private Location mLocation;
    private TextView tvStartTime;
    private TextView tvStartLocation;
    private TextView tvEndTime;
    private TextView tvEndLocation;
    private Button btnShift;
    private MapFragment fragmentMap;

    private DeputyAsyncHandler mAsyncHandler;

    private Shift mShift;

    private GoogleMap mGoogleMap;

    private boolean mRequestingLocationUpdates = false;

    private Intent mServiceIntent;

    private static final String PROJECTION[] = {DeputyContract.ShiftEntry._ID,
            DeputyContract.ShiftEntry.COLUMN_START,
            DeputyContract.ShiftEntry.COLUMN_END,
            DeputyContract.ShiftEntry.COLUMN_START_LATITUDE,
            DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE,
            DeputyContract.ShiftEntry.COLUMN_END_LATITUDE,
            DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvStartTime = (TextView) findViewById(R.id.tv_startTime);
        tvStartLocation = (TextView) findViewById(R.id.tv_startLocation);
        tvEndTime = (TextView) findViewById(R.id.tv_endTime);
        tvEndLocation = (TextView) findViewById(R.id.tv_endLocation);
        btnShift = (Button) findViewById(R.id.btn_shift);
        fragmentMap = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        fragmentMap.getMapAsync(this);

        /*
        Setup location service.
         */
        setupLocationService();

        mAsyncHandler = new DeputyAsyncHandler(getContentResolver(), this);

        updateValuesFromBundle(savedInstanceState);

        Intent intent = getIntent();
        if(intent.hasExtra(SHIFT_ID_KEY) && mShift == null){
            /*
            Request from mainactivity list click, retrieve the id passed and query table.
             */
            int id = intent.getIntExtra(SHIFT_ID_KEY, -1);
            Uri uri = DeputyContract.ShiftEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            mAsyncHandler.startQuery(QUERY_TOKEN, null, uri, PROJECTION, SELECTION, new String[]{String.valueOf(id)}, null);
        }

        mServiceIntent = new Intent(this, SyncService.class);
    }

    private void setupLocationService(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }

            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if(savedInstanceState.keySet().contains(SHIFT_KEY)){
                mShift = savedInstanceState.getParcelable(SHIFT_KEY);
            }

            updateUI();
            updateMap();
        }
    }

    private void updateMap(){
        if(mGoogleMap == null){
            return;
        }
        //Set start location
        if(mShift != null){
            if(mShift.getStartLatitude() != 0){
                LatLng latLng = new LatLng(mShift.getStartLatitude(), mShift.getStartLongitude());
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.start_location)));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
            }
            if(mShift.getEndLatitude() != 0){
                LatLng latLng = new LatLng(mShift.getEndLatitude(), mShift.getEndLongitude());
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.start_location)));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
            }

        }
    }
    private void updateUI(){
        if(mShift != null){
            long startTime = mShift.getStart();
            double startLatitude = mShift.getStartLatitude();
            double startLongitude = mShift.getStartLongitude();

            String formattedTime = GenericUtil.getFormattedTime(startTime);
            tvStartTime.setText(formattedTime);
            /*String formattedAddress = getFormattedAddress( startLongitude, startLatitude);
            if(formattedAddress != null){
                tvStartLocation.setText(formattedAddress);
            }*/

            long endTime = mShift.getEnd();
            double endLatitude = mShift.getEndLatitude();
            double endLongitude = mShift.getEndLongitude();

            if(endTime > 0){
                String formattedEndTime = GenericUtil.getFormattedTime(endTime);
                tvEndTime.setText(formattedEndTime);
               /* String formattedEndAddress = getFormattedAddress( endLongitude, endLatitude);
                if(formattedAddress != null) {
                    tvEndLocation.setText(formattedAddress);
                }*/

            }

            queryForAddressUsingLatLong(mShift);
            if(endTime > 0 && startTime > 0){
                btnShift.setVisibility(View.INVISIBLE);
            }else if(startTime > 0){
                btnShift.setVisibility(View.VISIBLE);
                btnShift.setText(getString(R.string.end_shift));
            }else{
                btnShift.setVisibility(View.VISIBLE);
                btnShift.setText(getString(R.string.start_shift));
            }
        }else{
            btnShift.setVisibility(View.VISIBLE);
            btnShift.setText(getString(R.string.start_shift));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        outState.putParcelable(LOCATION_KEY, mLocation);
        outState.putParcelable(SHIFT_KEY, mShift);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAsyncComplete(int token, int result, Uri uri, Cursor cursor) {
        boolean success = false;
        if(token == QUERY_TOKEN && cursor != null){
            success = true;
            Log.d(TAG, "Query successful");
            if(cursor.moveToNext()){
                mShift = CursorUtil.getShiftFromCursor(cursor);
                cursor.close();
            }
        } else if(token == INSERT_TOKEN && uri != null){
            if(mShift != null){
                String id =  uri.getLastPathSegment();
                try {
                    mShift.set_id(Integer.parseInt(id));
                }catch(NumberFormatException nfe){
                    nfe.printStackTrace();
                    Log.e(TAG, "Unable to conver lastpathsegment to integer : " + id, nfe);
                }
            }
        }
        updateUI();
        updateMap();
    }

    public void updateShift(View view){
        //Update database
        ContentValues cv = new ContentValues();
        double latitude = 0;
        double longitude = 0;
        if(mLocation != null){
           latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
        }
        long currentTimeInMilli = new Date().getTime();
        if(mShift == null){
            /*
            start shift.
             */
            cv.put(DeputyContract.ShiftEntry.COLUMN_START, currentTimeInMilli);
            cv.put(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE, latitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE, longitude);

            mShift = new Shift();
            mShift.setStartLatitude(latitude);
            mShift.setStartLongitude(longitude);
            mShift.setStart(currentTimeInMilli);

            mAsyncHandler.startInsert(INSERT_TOKEN, null, DeputyContract.ShiftEntry.CONTENT_URI, cv); //Insert to db for instance access as the sync is quite slow
            postToApi(NetworkUtils.START_SHIFT_URL, currentTimeInMilli, latitude, longitude);
            startService(mServiceIntent);
        }else{
            cv.put(DeputyContract.ShiftEntry.COLUMN_END, currentTimeInMilli);
            cv.put(DeputyContract.ShiftEntry.COLUMN_END_LATITUDE, latitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE, longitude);

            String id = String.valueOf(mShift.get_id());
            mAsyncHandler.startUpdate(UPDATE_TOKEN, null, DeputyContract.ShiftEntry.CONTENT_URI, cv, SELECTION, new String[]{id});
            mShift.setEnd(currentTimeInMilli);
            mShift.setEndLatitude(latitude);
            mShift.setEndLongitude(longitude);
            postToApi(NetworkUtils.END_SHIFT_URL, currentTimeInMilli, latitude, longitude);
            startService(mServiceIntent);
        }
        updateUI();
        updateMap();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, getString(R.string.detail_activity_volley_error), error);
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

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, null, this){
          @Override
          public Map<String, String> getHeaders() throws AuthFailureError {
              return VolleyRequestQueue.getHeaderParameter();
          }
      };
      jsonObjectRequest.setTag(TAG);
      VolleyRequestQueue.getInstance(this).addToRequestQueue(jsonObjectRequest);
  }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection to play service suspended error code: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Connection Failed.
        Log.e(TAG, "Connection to play service failed error: " + connectionResult.getErrorMessage());

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    protected void onStart() {
        if(mGoogleApiClient != null)
            mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    protected void startLocationUpdates() {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        updateMap();
    }


    @Override
    public Loader<Shift> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Shift>(this) {

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }
                forceLoad();
            }

            @Override
            public Shift loadInBackground() {
                Shift shift = args.getParcelable(SHIFT_KEY);
                double startLongitude = shift.getStartLongitude();
                double startLatitude = shift.getStartLatitude();
                double endLongitude = shift.getEndLongitude();
                double endLatitude = shift.getEndLatitude();

                String startAddress = (startLatitude != 0) ? getFormattedAddress(startLongitude, startLatitude) : "";
                String endAddress = (endLatitude != 0) ? getFormattedAddress(endLongitude, endLatitude) : "";
                shift.setStartAddress(startAddress);
                shift.setEndAddress(endAddress);
                return shift;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Shift> loader, Shift data) {
        tvStartLocation.setText(data.getStartAddress());
        tvEndLocation.setText(data.getEndAddress());

    }

    @Override
    public void onLoaderReset(Loader<Shift> loader) {
        //We not using this
    }

    private void queryForAddressUsingLatLong(Shift shift){
        Bundle queryBundle = new Bundle();
        queryBundle.putParcelable(SHIFT_KEY, shift);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> locationSearchLoader = loaderManager.getLoader(ADDRESS_SEARCH_LOADER);
        if (locationSearchLoader == null) {
            loaderManager.initLoader(ADDRESS_SEARCH_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(ADDRESS_SEARCH_LOADER, queryBundle, this);
        }
    }

    private String getFormattedAddress(double longitude, double latitude ){
        if(geocoder == null) {
            geocoder = new Geocoder(this, Locale.getDefault());
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
    }
}
