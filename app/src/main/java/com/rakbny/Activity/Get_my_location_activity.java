package com.rakbny.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rakbny.R;
import com.rakbny.Utils.LatLngInterpolator;
import com.rakbny.Utils.LocationReq;
import com.rakbny.Utils.markerAnimator;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Get_my_location_activity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int MY_PERMISSIONS_REQUEST_GPS = 66;
    private GoogleMap mMap;
    private LatLng cuurent_location;
    LocationReq gpsTracker ;
    private boolean move_update = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null){
            no_gps_permition();
        }
        setContentView(R.layout.activity_get_my_location_activity);
        registerReceiver(mReceivedSMSReceiver , new IntentFilter("android.location.PROVIDERS_CHANGED"));
        checkGpsProvider();
        setupMap();
        gpsTracker = new LocationReq(getApplicationContext(), new LocationReq.update_location() {
            @Override
            public void update_location(Location location) {
                Log.e("can move" , move_update + "");
                if (move_update){
                    moveCamera(new LatLng(location.getLatitude(),location.getLongitude()));
                }else {
                    gpsTracker.disconnect();
                }

            }
        }, 5000);

    }
    private void setupMap(){

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setMyLocationEnabled(true);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
              LatLng mLatLng = new LatLng(cameraPosition.target.latitude , cameraPosition.target.longitude) ;
                 cuurent_location = mLatLng;;
            }
        });
        if (gpsTracker.getLatitude() !=-0 ){
            moveCamera(new LatLng(gpsTracker.getLongitude(),gpsTracker.getLatitude()));
        }

    }


    public void cancel_req(View view) {
        onBackPressed();
    }

    public void pin_it(View view) {
        update_station();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.b_exit, R.anim.b_enter);
    }

    public void update_station(){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                         AppController.getInstance().getPrefManager().update_station(cuurent_location.latitude , cuurent_location.longitude);
                            onBackPressed();
                            Toast.makeText(getApplicationContext(),"STATION UPDATED" , Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("volley","response"+response.toString() );

            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    //  getCategories();
                }
                if (error instanceof NoConnectionError) {
                    String message = getString(R.string.NoConnection);
                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.UPDATE_STATION));
                prams.put("lat", String.valueOf(cuurent_location.latitude));
                prams.put("lung", String.valueOf(cuurent_location.longitude));
                prams.put("station_id", AppController.getInstance().getPrefManager().getUser().getStation_id());

                return prams;
            }
        };
        int socketTimeout = 5000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                12,
                1);

        feedRequest.setRetryPolicy(policy);
        AppController.getInstance().addToRequestQueue(feedRequest);

    }
    private void moveCamera(LatLng current) {
        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(current)      // Sets the center of the map to Mountain View
                .zoom(15)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null);
        move_update = false ;
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mReceivedSMSReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        registerReceiver(mReceivedSMSReceiver , new IntentFilter("android.location.PROVIDERS_CHANGED"));
        super.onResume();
    }
    private final BroadcastReceiver mReceivedSMSReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED"))
            {
                Log.e("GpsProviderReciver", String.valueOf(intent.getAction()));
                checkGpsProvider();
            }
        }
    };
    private  void  checkGpsProvider(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();        }
    }

    private void no_gps_permition() {
        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {



            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);


        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GPS: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    //  no_gps_permition();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Get_my_location_activity.this, "Permission needed to run your app", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
