package com.rakbny.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rakbny.R;
import com.rakbny.Utils.LatLngInterpolator;
import com.rakbny.Utils.Utils;
import com.rakbny.Utils.markerAnimator;
import com.rakbny.Widget.FrameLayoutTouchListener;
import com.rakbny.data.FCM.showNotificationHandel;
import com.rakbny.data.Models.UserModel;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;
import com.rakbny.fragments.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FollowerActivity extends AppCompatActivity implements OnMapReadyCallback {
    String not_rode_the_bus = "not rode the bus yet";
    String rode_the_bus = "Have rode the bus";
    TextView student_attendance_txt ,distance_txt ;
    int DistanceColor ;
    UserModel user ;
    LatLng cuurent_bus_location;
    private BroadcastReceiver broadcastReceiver;
    private Marker bus_marker;
    private GoogleMap mMap;
    Point settings_point ;
    View  profile_icon ;
    private Marker station_marker;
   boolean move_marker = true ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);
        setupMap();
        profile_icon = findViewById(R.id.profile_icon);
        int x  = (int) (profile_icon.getX()+(int) (profile_icon.getMeasuredWidth()/2));
        int Y  = (int) (profile_icon.getY()+(int) (profile_icon.getMeasuredHeight()/2));
         settings_point = new Point(x,Y);
        Log.e("fCM id", FirebaseInstanceId.getInstance().toString());
        cuurent_bus_location = new LatLng(0,0);
         user = AppController.getInstance().getPrefManager().getUser() ;
        //parents section
        if (user.getType().equals("parent")){
            get_student_state();
        }
            student_attendance_txt = (TextView) findViewById(R.id.student_attendance_txt);
        distance_txt = (TextView) findViewById(R.id.distance_txt);


        /**
         * Broadcast receiver  scenarios

         * */
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("broadcast", "receive broadcast");
                Log.e("broadcast", intent.getAction() + "");
                // checking for type intent filter
                if (intent.getAction().equals(Config.UPDATE_BUS_LOCATION)) {
                    Log.d("broadcast", Config.UPDATE_BUS_LOCATION  );
                    Bundle extra = intent.getExtras();
                    cuurent_bus_location = (LatLng) extra.get(Config.LOCATION);
                    binding_distance(extra.getDouble(Config.DISTANCE));
                    binding_bus_location();

                }

                if (intent.getAction().equals(Config.UPDATE_CHILD_STATE)) {
                    Log.d("broadcast", Config.UPDATE_CHILD_STATE );
                    Bundle data = intent.getExtras();
                    int state = Integer.parseInt(data.getString("state"));
                    Log.d("broadcast", Config.UPDATE_CHILD_STATE + "state " + state );
                    if (state == 0){
                        not_rode_the_bus();
                    }else if(state == 1){
                        rode_the_bus();
                        new showNotificationHandel(context);

                    }
                }


                if (intent.getAction().equals(Config.UPDATE_STATION)) {
                    Log.d("broadcast", Config.UPDATE_CHILD_STATE );
                 checkStation();
                }



            }
        };
    }

    private void create_bus_marker(){

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_pin);

        MarkerOptions markerOptions = new MarkerOptions().position(cuurent_bus_location).icon(icon);

        bus_marker = mMap.addMarker(markerOptions);
        Log.e("bus marker " ,"postion "+ bus_marker.getPosition());

    }

    private void create_station_marker(){

       try {
           BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_home_pin);

           MarkerOptions markerOption = new MarkerOptions().position(new LatLng(user.getLat(),user.getLung())).icon(icon);
           station_marker= mMap.addMarker(markerOption);
           Log.e("station marker " ,"postion "+ markerOption.getPosition());
       }catch (Exception e){

       }

    }
    private void binding_bus_location(){
        Log.e("binding bus location","cuurent location" + cuurent_bus_location.toString());
        try{

            if ( bus_marker.getPosition() == null ){
                bus_marker.setPosition(cuurent_bus_location);

            }else{
                new markerAnimator().animateMarkerToICS(bus_marker, cuurent_bus_location, new LatLngInterpolator.Spherical(), 700);
            }
            if (move_marker){
                moveCamera(cuurent_bus_location);
                move_marker = false ;
            }


        }catch (Exception e){
            Log.e("binding bus location","exeption " + e.toString());
        }

    }


    private void binding_distance(double distance){
        distance_txt.setText(formatDistance(distance));
        distance_txt.setTextColor(DistanceColor);
    }

    private void rode_the_bus(){
        student_attendance_txt.setText(rode_the_bus);
        student_attendance_txt.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
    }

    private void not_rode_the_bus(){
        student_attendance_txt.setText(not_rode_the_bus);
        student_attendance_txt.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.danger));
    }
    private String formatDistance(Double distance) {
        String unit = "m";
        DistanceColor = ContextCompat.getColor(getApplicationContext(),R.color.green) ;
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";

        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
           DistanceColor = ContextCompat.getColor(getApplicationContext(),R.color.danger);
            return String.format("%4.3f%s", distance, unit);
        }

        return String.format("%4d%s", distance.intValue(), unit);
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
    }

    public void bus_location_on_click(View view) {
        get_bus_location();
        move_marker = true ;
    }

    public void station_location_on_click(View view) {


        try{
            if (user.getLat() != 0.0 )

            moveCamera(new LatLng(user.getLat(),user.getLung()));
            else noStationAlert();

        }catch (Exception e){
         noStationAlert();
        }
    }

    private void noStationAlert() {
        //// TODO: 11/8/2016 show dailog you dont have location go to setting
        AlertDialog.Builder builder = new AlertDialog.Builder(FollowerActivity.this);
        builder.setTitle("No Station Found");
        builder.setMessage(" you don't set your station yet , please  go to settings and add your station or update ");
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class) ;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    intent.putExtra("point",settings_point);
                }
                startActivity(intent);
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void get_student_state() {
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.student_attendance).setVisibility(View.VISIBLE);

                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")) {
                        int state = resObj.getInt("state");
                        if (state == 0) {
                            not_rode_the_bus();
                        } else if (state == 1) {
                            rode_the_bus();
                        }
                    }

                } catch (JSONException e) {
                    Log.e("students list ", "response" + e.toString());
                }
                Log.e("students list ", "response" + response.toString());

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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> prams = new HashMap<>();
                prams.put("request", Config.GET_CHILD_STATE);
                prams.put("parent_id", user.getId());
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
    // after map sync get bus location from db
    public void get_bus_location(){
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONObject res = new JSONObject(resObj.getString("response"));

                        cuurent_bus_location = new LatLng(res.getDouble("lat"),res.getDouble("lung"));
                       binding_bus_location();

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
                prams.put("request", String.valueOf(Config.GET_BUS_LOCATION));
                prams.put("bus_id", user.getBus_id());
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


    protected void onResume() {
        super.onResume();
        Log.d("broadcast","register");

        // register new news feed  notification broadcast receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Config.UPDATE_BUS_LOCATION));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Config.UPDATE_CHILD_STATE));
        try {
            checkStation();
        }catch (Exception e){
            Log.e("on resume ex" , e.toString());
        }

    }

    private void checkStation() {

        if (station_marker.getPosition().longitude != AppController.getInstance().getPrefManager().getUser().getLung() ) {
            user = AppController.getInstance().getPrefManager().getUser();
            new markerAnimator().animateMarkerToICS(station_marker, new LatLng(user.getLat() , user.getLung()), new LatLngInterpolator.Spherical(), 700);
        }
    }

    @Override
    protected void onPause() {
        Log.d("broadcast","unregister");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setBuildingsEnabled(true);
        load_roads();
        create_bus_marker();
        create_station_marker();
        get_bus_location();

    }

    private void load_roads() {

        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONObject res =new JSONObject( resObj.getString("response") );
                        JSONArray home  = new JSONArray(res.getString("home"));
                        JSONArray school  = new JSONArray(res.getString("school"));

                        LatLng[] home_route = new LatLng[home.length()];
                        for (int i=0; i<home.length() ; i++){
                            JSONObject point = home.getJSONObject(i);
                            Double lat = (Double) point.get("lat");
                            Double lung = (Double) point.get("lung");
                            LatLng latlung = new LatLng(lat,lung);
                            home_route[i]= latlung;
                        }

                        PolylineOptions home_rout_poly = new PolylineOptions();
                        home_rout_poly.geodesic(true);
                        home_rout_poly.color(ContextCompat.getColor(getApplicationContext(),R.color.blue));
                        home_rout_poly.add(home_route);
                        home_rout_poly.width(10);
                        mMap.addPolyline(home_rout_poly);

                        LatLng[] school_route = new LatLng[school.length()];
                        for (int i=0; i<school.length() ; i++){
                            JSONObject point = school.getJSONObject(i);
                            Double lat = (Double) point.get("lat");
                            Double lung = (Double) point.get("lung");
                            LatLng latlung = new LatLng(lat,lung);
                            school_route[i]= latlung;
                        }

                        PolylineOptions school_rout_poly = new PolylineOptions();
                        school_rout_poly.geodesic(true);
                        school_rout_poly.color(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
                        school_rout_poly.add(school_route);
                        school_rout_poly.width(10);
                        mMap.addPolyline(school_rout_poly);





                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("load route","response"+response.toString() );

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
                prams.put("request", String.valueOf(Config.GET_ROADS));
                prams.put("bus_id", user.getBus_id());
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
    public void go_to_settings(View view) {
        FrameLayoutTouchListener menu_container = (FrameLayoutTouchListener) findViewById(R.id.menu_container);
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class) ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Point point = new Point(menu_container.getTouchedx(),menu_container.getTouchedy());
            intent.putExtra("point",point);
        }
        startActivity(intent);
    }

    private void setupMap(){

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

}
