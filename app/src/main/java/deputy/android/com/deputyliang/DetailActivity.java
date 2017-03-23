package deputy.android.com.deputyliang;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.net.URL;
import java.util.Date;

import deputy.android.com.deputyliang.data.DeputyAsyncHandler;
import deputy.android.com.deputyliang.data.DeputyContract;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.service.LocationService;
import deputy.android.com.deputyliang.util.GenericUtil;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, DeputyAsyncHandler.AsyncListener, OnMapReadyCallback{
    private static final String TAG = "DetailActivity";
    private static final int INSERT_TOKEN = 103;
    private static final int UPDATE_TOKEN = 104;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY";
    private static final String LOCATION_KEY = "LOCATION_KEY";
    private static final String SHIFT_KEY = "SHIFT_KEY";
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 11;




    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private TextView tvStartTime;
    private TextView tvStartLocation;
    private TextView tvEndTime;
    private TextView tvEndLocation;
    private Button btnShift;
    private MapFragment fragmentMap;

    private DeputyAsyncHandler mAsyncHandler;

    private Shift mShift;

    private boolean mRequestingLocationUpdates = false;


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
        }
    }

    private void updateUI(){
        if(mShift != null){
            long startTime = mShift.getStart();
            double startLatitude = mShift.getStartLatitude();
            double startLongitude = mShift.getStartLongitude();

            String formattedTime = GenericUtil.getFormattedTime(startTime);
            tvStartTime.setText(formattedTime);
            String formattedAddress = GenericUtil.getFormattedAddress(this, startLongitude, startLatitude);
            if(formattedAddress != null){
                tvStartLocation.setText(formattedAddress);
            }
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
    public void onAsyncComplete(int token, int result, Uri uri) {
        if(token == INSERT_TOKEN && uri != null){
            Log.d(TAG, "Insert successful");
        }else{

        }
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
        }

        //Post to API
        //Update UI
        updateUI();
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

        //Set start location
        if(mShift != null){
            if(mShift.getStartLatitude() != 0){
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mShift.getStartLatitude(), mShift.getStartLongitude()))
                        .title(getString(R.string.start_location)));
            }
            if(mShift.getEndLatitude() != 0){
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mShift.getEndLatitude(), mShift.getEndLongitude()))
                        .title(getString(R.string.start_location)));
            }

        }



    }
}
