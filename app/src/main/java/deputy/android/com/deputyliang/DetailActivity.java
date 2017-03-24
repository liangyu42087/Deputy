package deputy.android.com.deputyliang;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import deputy.android.com.deputyliang.data.DeputyAsyncHandler;
import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.service.LocationService;
import deputy.android.com.deputyliang.util.GenericUtil;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, DeputyAsyncHandler.AsyncListener, OnMapReadyCallback{
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

        setupLocationService();

        mAsyncHandler = new DeputyAsyncHandler(getContentResolver(), this);

        updateValuesFromBundle(savedInstanceState);

        Intent intent = getIntent();
        if(intent.hasExtra(SHIFT_ID_KEY) && mShift == null){
            int id = intent.getIntExtra(SHIFT_ID_KEY, -1);
            Uri uri = DeputyContract.ShiftEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
            mAsyncHandler.startQuery(QUERY_TOKEN, null, uri, PROJECTION, SELECTION, new String[]{String.valueOf(id)}, null);
        }
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
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
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
            String formattedAddress = getFormattedAddress( startLongitude, startLatitude);
            if(formattedAddress != null){
                tvStartLocation.setText(formattedAddress);
            }

            long endTime = mShift.getEnd();
            double endLatitude = mShift.getEndLatitude();
            double endLongitude = mShift.getEndLongitude();

            if(endTime > 0){
                String formattedEndTime = GenericUtil.getFormattedTime(startTime);
                tvEndTime.setText(formattedEndTime);
                String formattedEndAddress = getFormattedAddress( startLongitude, startLatitude);
                if(formattedAddress != null) {
                    tvEndLocation.setText(formattedAddress);
                }
            }
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
        if(token == INSERT_TOKEN && uri != null){
            String id = uri.getLastPathSegment();
            try{
                mShift.set_id(Integer.parseInt(id));
                success = true;
            }catch(NumberFormatException nfe){
                Log.e(TAG, "Cannot convert id to int", nfe);
            }
            Log.d(TAG, "Insert successful");
        }else if(token == UPDATE_TOKEN && result > 0){
            success = true;
            Log.d(TAG, "Update successful");
        } else if(token == QUERY_TOKEN && cursor != null){
            success = true;
            Log.d(TAG, "Query successful");
            if(cursor.moveToNext()){
                mShift = new Shift();
                mShift.set_id(cursor.getInt(cursor.getColumnIndex(DeputyContract.ShiftEntry._ID)));
                mShift.setStart(cursor.getLong(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_START)));
                mShift.setStartLongitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE)));
                mShift.setStartLatitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE)));
                mShift.setEnd(cursor.getLong(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_END)));
                mShift.setEndLatitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_END_LATITUDE)));
                mShift.setEndLongitude(cursor.getDouble(cursor.getColumnIndex(DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE)));
                cursor.close();
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
            cv.put(DeputyContract.ShiftEntry.COLUMN_START, currentTimeInMilli);
            cv.put(DeputyContract.ShiftEntry.COLUMN_START_LATITUDE, latitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_START_LONGITUDE, longitude);
            mShift = new Shift();
            mShift.setStartLatitude(latitude);
            mShift.setStartLongitude(longitude);
            mShift.setStart(currentTimeInMilli);

           mAsyncHandler.startInsert(INSERT_TOKEN, null, DeputyContract.ShiftEntry.CONTENT_URI, cv);
        }else{
            cv.put(DeputyContract.ShiftEntry.COLUMN_END, currentTimeInMilli);
            cv.put(DeputyContract.ShiftEntry.COLUMN_END_LATITUDE, latitude);
            cv.put(DeputyContract.ShiftEntry.COLUMN_END_LONGITUDE, longitude);
            mShift.setEnd(currentTimeInMilli);
            mShift.setEndLatitude(latitude);
            mShift.setEndLongitude(longitude);
            String id = String.valueOf(mShift.get_id());
            mAsyncHandler.startUpdate(UPDATE_TOKEN, null, DeputyContract.ShiftEntry.CONTENT_URI, cv, SELECTION, new String[]{id});
        }

        //Post to API
        //Update UI

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
