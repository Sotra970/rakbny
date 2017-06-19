package com.rakbny.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;
import com.rakbny.R;
import com.rakbny.Utils.LatLngInterpolator;
import com.rakbny.Utils.LocationReq;
import com.rakbny.Utils.markerAnimator;
import com.rakbny.Widget.FrameLayoutTouchListener;
import com.rakbny.data.Models.UserModel;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;
import com.rakbny.fragments.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trace_Ride_Activity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_HOME_DIR = "home";
    public static final String EXTRA_SCHOOL_DIR = "school";
    public static final String EXTRA_DIR = "dir";
    public static final String EXTRA_BUS_ID = "bus_id";
    private static final int MY_PERMISSIONS_REQUEST_GPS= 603;
    private String BUS_ID ;
    private String DIR ;
    private Button start_trip ,end_trip;
    BottomSheetBehavior bottomSheetBehavior ;
    private  boolean isCameraUpdated = false ;


    /**
     * The API context used for the Roads and Geocoding web service APIs.
     */
    private GeoApiContext mContext;



    /**
     * Define the number of data points to re-send at the start of subsequent requests. This helps
     * to influence the API with prior data, so that paths can be inferred across multiple requests.
     * You should experiment with this value for your use-case.
     */
    private static final int PAGINATION_OVERLAP = 5;
    List<SnappedPoint> mSnappedPoints;

    //GPSTracker gpsTracker ;
    LocationReq gpsTracker ;
    LocationReq routegpsTracker ;
    List <com.google.maps.model.LatLng> mCapturedLocations = new ArrayList<>();
    PolylineOptions route_tracker_line_Options, snaplineOptions;

    Marker currentPosIcon ;
    private GoogleMap mMap;
    private View mProgressBar;
    View stop , cancel , track ,fab_container , home_dir , school_dir;
    ImageButton students_check_list;
    ImageButton current;
    private int uodate_live_bus_location_time = 15000; // 15sec
    private int uodate_bus_route_traking_time = 10000; //10sec
    Drawable drawable ;
    private Polyline route_tracker_line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_trace__ride);
        if (savedInstanceState == null){
            no_gps_permition();
        }
        drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_my_location_black_36dp);
        mProgressBar =  findViewById(R.id.progress_bar);
        stop = findViewById(R.id.stop);
        cancel =findViewById(R.id.Cancel);
        track=findViewById(R.id.start_tracking);
        fab_container=findViewById(R.id.fab_container);
        home_dir=findViewById(R.id.home_dir);
        school_dir=findViewById(R.id.school_dir);
        students_check_list = (ImageButton)findViewById(R.id.students_check_list);
        current =(ImageButton) findViewById(R.id.current_location);
        start_trip =(Button) findViewById(R.id.start_bus_tracking);
        end_trip =(Button) findViewById(R.id.end_bus_tracking);
        mContext = new GeoApiContext().setApiKey(getString(R.string.google_maps_web_services_key));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.student_list));
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });



        //setting bus_id
        BUS_ID = AppController.getInstance().getPrefManager().getUser().getBus_id() ;
        try {
           Bundle extras = getIntent().getExtras();
            BUS_ID = extras.getString(EXTRA_BUS_ID);
           DIR = extras.getString(EXTRA_DIR);
            students_check_list.setVisibility(View.GONE);
            findViewById(R.id.student_list).setVisibility(View.GONE);
            findViewById(R.id.trace_ride_splash_layout).setVisibility(View.GONE);
//       after map ready and type is admin
//         onTrackClicked();

        }catch (NullPointerException e){
            Log.e("from admin ","tracking route" + e.toString());
        }



        // set up map
        setupMap();

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
                findViewById(R.id.stop).setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    findViewById(R.id.Cancel).animate().scaleX(0).scaleY(0).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator());
                }else {
                    findViewById(R.id.Cancel).setScaleX(0);
                    findViewById(R.id.Cancel).setScaleY(0);
                }
            }
        });

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_dir();
                Toast.makeText(getApplicationContext(),"start dir" , Toast.LENGTH_SHORT).show();
            }
        });

        home_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DIR = "home";
                hide_dir();
                Toast.makeText(getApplicationContext(),"home dir" , Toast.LENGTH_SHORT).show();
            }
        });
        school_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               DIR = "school" ;
                hide_dir();
                Toast.makeText(getApplicationContext(),"school dir" , Toast.LENGTH_SHORT).show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routegpsTracker.disconnect();
                findViewById(R.id.stop).setVisibility(View.GONE);
                mMap.clear();
               createCurrentPosMarker();
                BUS_ID = null ;
                DIR = null ;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    findViewById(R.id.Cancel).animate().scaleX(0).scaleY(0).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator());
                }else {
                    findViewById(R.id.Cancel).setScaleX(0);
                    findViewById(R.id.Cancel).setScaleY(0);
                }
            }

        });

            setupCurrentPostion();
        registerReceiver(mReceivedSMSReceiver , new IntentFilter("android.location.PROVIDERS_CHANGED"));
        checkGpsProvider();

    }


    private void sendLocation(final double latitude, final double longitude) {

        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                   try {
                    JSONObject resObj = new JSONObject(response);
                 if (resObj.getString("operation").equals("done")){
                    if (resObj.getString("response").equals("not_permitted")) {
                        gpsTracker.disconnect();
                    }
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
                prams.put("request", Config.SEND_BUS_LOCATION);
                prams.put("type", AppController.getInstance().getPrefManager().getUser().getType());
                prams.put("bus_id", AppController.getInstance().getPrefManager().getUser().getBus_id());
                prams.put("lat", String.valueOf(latitude));
                prams.put("lung", String.valueOf(longitude));
                Log.e("update loc", "params: " + prams.toString());
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

    private void setupCurrentPostion() {
        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationReq locationReq ;
                if (AppController.getInstance().getPrefManager().getUser().getType().equals("admin")){
                    locationReq  = routegpsTracker ;
                }else {
                    locationReq = gpsTracker ;
                }
                if (locationReq.getLatitude() != 0 ){
                    LatLng currentLoc = new LatLng(locationReq.getLatitude(), locationReq.getLongitude());
                    new markerAnimator().animateMarkerToICS(currentPosIcon, currentLoc, new LatLngInterpolator.Spherical(), 1200);

                    drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.blue), PorterDuff.Mode.SRC_ATOP);
                    current.setImageDrawable(drawable);
                        moveCamera(currentLoc);
              }else {
                    drawable.clearColorFilter();
                    current.setImageDrawable(drawable);
                }
            }
        });
    }

    private void onCurrentLocationUpdated(Location location) {
        if (gpsTracker.getLatitude() != 0 ){
            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            //currentPosIcon.setRotation((float) SphericalUtil.computeHeading(currentPosIcon.getPosition(),currentLoc));
            new markerAnimator().animateMarkerToICS(currentPosIcon, currentLoc, new LatLngInterpolator.Spherical(), 1200);

            drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.blue), PorterDuff.Mode.SRC_ATOP);
            current.setImageDrawable(drawable);

            if (!isCameraUpdated){
                moveCamera(currentLoc);
                isCameraUpdated = true ;
            }

        }else {
            drawable.clearColorFilter();
            current.setImageDrawable(drawable);
        }
    }

    private void moveCamera(LatLng current) {
        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(current)      // Sets the center of the map to Mountain View
                .zoom(16)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null);

    }


    //setup map function

    private void onTrackClicked() {
        if (DIR == null){
            Log.e("Dir",DIR);
            return;
        }
      //  setupRoutegpsTracker
        mCapturedLocations.clear();
        route_tracker_line_Options = new PolylineOptions();
        route_tracker_line_Options.width(10);
        route_tracker_line_Options.visible(true);
        route_tracker_line_Options.color(Color.RED);
        route_tracker_line_Options.geodesic(true);
        mMap.addPolyline(route_tracker_line_Options);
        //gps tracker to implement location change listener

        routegpsTracker = new LocationReq(getApplicationContext(), new LocationReq.update_location() {
            @Override
            public void update_location(Location location) {
                Log.e("bearing",location.getBearing() +"" );
                oRouteGpsTrackerOnLocationChange(location);

            }
        },uodate_bus_route_traking_time);
        if (routegpsTracker.getLatitude()!=0){
            LatLng cuurent = new LatLng(routegpsTracker.getLatitude(),routegpsTracker.getLongitude());
            route_tracker_line_Options.add(cuurent);
            mMap.addPolyline(route_tracker_line_Options);
            mCapturedLocations.add(new com.google.maps.model.LatLng(cuurent.latitude,cuurent.longitude));

        }
        if (AppController.getInstance().getPrefManager().getUser().getType().equals("admin")){
            createCurrentPosMarker();
        }
        ///////////////
        stop.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            cancel.animate().scaleX(1).scaleY(1).setDuration(100);
        }else {
            cancel.setScaleX(1);
           cancel.setScaleY(1);
        }


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
        if (AppController.getInstance().getPrefManager().getUser().getType().equals("admin")){
            onTrackClicked();
        }


        // just for debguing
      /*  mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            try{
                Log.e("latlung",latLng.toString());
                route_tracker_line_Options.add(latLng);
                mMap.addPolyline(route_tracker_line_Options);
                mCapturedLocations.add(new com.google.maps.model.LatLng(latLng.latitude,latLng.longitude));

            }catch (Exception e){}
            }
        });
*/

        get_stations();
    }



    //onlocation change update path
    private void oRouteGpsTrackerOnLocationChange(Location location) {
     LatLng   coordinates  = new LatLng(location.getLatitude(),location.getLongitude());
        Log.e("RouteTracker_Loc_Change",coordinates.latitude+"  "+ coordinates.longitude + "");
        mCapturedLocations.add(new com.google.maps.model.LatLng(coordinates.latitude,coordinates.longitude));
        route_tracker_line_Options.add(coordinates);
        Log.e( "points","route_tracker_line_Options " + route_tracker_line_Options.getPoints());
        mMap.addPolyline(route_tracker_line_Options);

        if (AppController.getInstance().getPrefManager().getUser().getType().equals("admin")){
            LatLng currentLoc = new LatLng(routegpsTracker.getLatitude(), routegpsTracker.getLongitude());
            new markerAnimator().animateMarkerToICS(currentPosIcon, currentLoc, new LatLngInterpolator.Spherical(), 1200);
        }
    }

    private void stop(){
        new mTaskSnapToRoads().execute("") ;
    }

    public void student_check_list(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


    public void start_bus_tracking(View view){
       load_roads();

    }

    public void end_bus_tracking(View view) {
        clear_trip();
        enterReveal();
        gpsTracker.disconnect();
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

    public class mTaskSnapToRoads extends AsyncTask<String, Void, List<SnappedPoint>> {
                @Override
                protected void onPreExecute() {
                    mProgressBar.setVisibility(View.VISIBLE);

                }

                @Override
                protected List<SnappedPoint> doInBackground(String... params) {
                    try {
                        return snapToRoads(mContext);
                    } catch (final Exception ex) {
                        toastException(ex);
                        ex.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(List<SnappedPoint> snappedPoints) {
                    mSnappedPoints = snappedPoints;
                    mProgressBar.setVisibility(View.INVISIBLE);
                    com.google.android.gms.maps.model.LatLng[] mapPoints =
                            new com.google.android.gms.maps.model.LatLng[mSnappedPoints.size()];
                    int i = 0;
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                    for (SnappedPoint point : mSnappedPoints) {
                        mapPoints[i] = new com.google.android.gms.maps.model.LatLng(point.location.lat,
                                point.location.lng);
                        bounds.include(mapPoints[i]);
                        i += 1;
                    }
                    snaplineOptions = new PolylineOptions().add(mapPoints).color(Color.BLUE) ;
                    mMap.clear();
                    mMap.addPolyline(snaplineOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0));
                    ArrayList <String> routpoints = new ArrayList<>();
                    for ( com.google.android.gms.maps.model.LatLng point : mapPoints){
                        HashMap pairs  = new HashMap();
                        pairs.put("lat",point.latitude);
                        pairs.put("lung",point.longitude);
                        routpoints.add(pairs.toString());
                    }
                    /// uploads route
                      upload_route(routpoints);
                }

            }

    private void upload_route(final ArrayList route) {
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){

                      Toast.makeText(getApplicationContext(),"Operation Success",Toast.LENGTH_LONG).show();


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
                prams.put("request", String.valueOf(Config.CREATE_ROUTE));
                prams.put("dir", DIR);
                prams.put("bus_id", BUS_ID);
                prams.put("route", route.toString().replaceAll("\\s",""));
                Log.e("update route" , prams.toString());
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

    ;

    private List<SnappedPoint> snapToRoads(GeoApiContext context) throws Exception {

        ArrayList< List <com.google.maps.model.LatLng>> mCapturedLocationsContainer = new ArrayList<>();
        int limit = -1 ;
        int j = -1 ;
        for (int i=0 ;i < mCapturedLocations.size();i++){
            if (i>limit) {
                j++ ;
                limit = limit+100;
                mCapturedLocationsContainer.add(new ArrayList<com.google.maps.model.LatLng>());
            }
            mCapturedLocationsContainer.get(j).add(mCapturedLocations.get(i));
        }


        List<SnappedPoint> snappedPoints = new ArrayList<>();

        for (int ind=0;ind<mCapturedLocationsContainer.size();ind++){
            int offset = 0;
            while (offset < mCapturedLocationsContainer.get(ind).size()) {
                // Calculate which points to include in this request. We can't exceed the APIs
                // maximum and we want to ensure some overlap so the API can infer a good location for
                // the first few points in each request.
                if (offset > 0) {
                    offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points
                }
                int lowerBound = offset;
                int upperBound =  mCapturedLocationsContainer.get(ind).size();

                // Grab the data we need for this page.
                com.google.maps.model.LatLng[] page = mCapturedLocationsContainer.get(ind)
                        .subList(lowerBound, upperBound)
                        .toArray(new com.google.maps.model.LatLng[upperBound - lowerBound]);

                // Perform the request. Because we have interpolate=true, we will get extra data points
                // between our originally requested path. To ensure we can concatenate these points, we
                // only start adding once we've hit the first new point (i.e. skip the overlap).
                SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();

                boolean passedOverlap = false;
                for (SnappedPoint point : points) {
                    if (offset == 0 || point.originalIndex >= PAGINATION_OVERLAP) {
                        passedOverlap = true;
                    }
                    if (passedOverlap) {
                        snappedPoints.add(point);
                    }
                }
                Log.e("snap to roads " ,  snappedPoints.size() +"  " +  mCapturedLocationsContainer.get(ind).size()  +"");
                offset = upperBound;
            }
        }
        Log.e("snap array list size ",snappedPoints.size() +"");
        Log.e("mloc container size ",mCapturedLocationsContainer.size() +"");
        return snappedPoints;
    }
    /** Helper for toasting exception messages on the UI thread. */
    private void toastException(final Exception ex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        try{
            gpsTracker.disconnect();

        }catch (Exception e){}
        try {
            routegpsTracker.disconnect();
        }catch (Exception e){}
        super.onDestroy();
    }


    void exitReveal() {
        // previously visible view
        final View myView = findViewById(R.id.star_tip_container);
        //dp
        float dp12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

        float deltaY =  fab_container.getY() - start_trip.getY();
        float deltax =  end_trip.getX() - start_trip.getX() - fab_container.getPaddingRight() ;

        ObjectAnimator translateY = ObjectAnimator.ofFloat(start_trip, "translationY", deltaY);
        translateY.setDuration(400);
        ObjectAnimator translatex = ObjectAnimator.ofFloat(start_trip, "translationX", deltax);
        translatex.setDuration(550);
        ObjectAnimator scalex = ObjectAnimator.ofFloat(start_trip, "scaleX", (float) .66);
        scalex.setDuration(200);
        ObjectAnimator scaley = ObjectAnimator.ofFloat(start_trip, "scaleY", (float).66);
        scaley.setDuration(200);
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translatex,translateY,scalex,scaley);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
             start_trip.setVisibility(View.GONE);
                end_trip.setVisibility(View.VISIBLE);
            }
        });




        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() / 2;
        int cy = myView.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = myView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
            anim.setDuration(150);
            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animatorSet.start();
                    myView.setVisibility(View.GONE);
                }
            });

            // start the animation
            anim.start();
        }else {
            animatorSet.start();
            myView.setVisibility(View.GONE);
        }

    }





    void enterReveal() {
        // previously invisible view
        final View myView = findViewById(R.id.star_tip_container);



        start_trip.setVisibility(View.VISIBLE);


        float deltaY =  fab_container.getY() - start_trip.getY();
        Log.e("dimn" , "fa x " +end_trip.getX()  +"  start trip x " + end_trip.getX() );
        float deltax =  end_trip.getX() - start_trip.getX() ;

        ObjectAnimator translateY = ObjectAnimator.ofFloat(start_trip, "translationY", -deltaY );
        translateY.setDuration(500);
        ObjectAnimator translatex = ObjectAnimator.ofFloat(start_trip, "translationX",0 );
        translatex.setDuration(400);
        ObjectAnimator scalex = ObjectAnimator.ofFloat(start_trip, "scaleX", 1);
        scalex.setDuration(400);
        ObjectAnimator scaley = ObjectAnimator.ofFloat(start_trip, "scaleY", 1 );
        scaley.setDuration(400);
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translatex,translateY,scalex,scaley);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                end_trip.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // make the view visible and start the animation
                myView.setVisibility(View.VISIBLE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                    // get the center for the clipping circle
                    int cx = myView.getMeasuredWidth() / 2;
                    int cy = myView.getMeasuredHeight() / 2;
                    // get the final radius for the clipping circle
                    int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;

                    // create the animator for this view (the start radius is zero)



                    Animator anim;
                    anim = ViewAnimationUtils.createCircularReveal(myView,  cx, cy, 0, finalRadius);

                    anim.setDuration(250);

                    anim.start();
                }
            }
        });
        animatorSet.start();





    }



    void show_dir() {

        school_dir.setVisibility(View.VISIBLE);
        home_dir.setVisibility(View.VISIBLE);
        //dp
         float dp52 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getResources().getDisplayMetrics());


        float deltaX =  fab_container.getX() - dp52;

        ObjectAnimator schoolX = ObjectAnimator.ofFloat(school_dir, "translationX",  deltaX );
        schoolX.setDuration(300);
        ObjectAnimator homeX = ObjectAnimator.ofFloat(home_dir, "translationX", deltaX*2 );
        homeX.setDuration(400);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(schoolX,homeX);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animatorSet.start();
    }

    void hide_dir() {


        //dp
        float dp52 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52, getResources().getDisplayMetrics());


        float deltaX =  fab_container.getX() ;

        ObjectAnimator schoolX = ObjectAnimator.ofFloat(school_dir, "translationX",  deltaX );
        schoolX.setDuration(195);
        schoolX.setStartDelay(200);
        ObjectAnimator homeX = ObjectAnimator.ofFloat(home_dir, "translationX", deltaX);
        homeX.setDuration(400);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(schoolX,homeX);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                school_dir.setVisibility(View.GONE);
                home_dir.setVisibility(View.GONE);
                onTrackClicked();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onTrackClicked();
            }
        });
        animatorSet.start();

    }
    private void clear_trip() {
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){

                        Toast.makeText(getApplicationContext(),"Trip ended ",Toast.LENGTH_LONG).show();


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
                prams.put("request", String.valueOf(Config.CLEAR_TRIP));
                prams.put("bus_id", AppController.getInstance().getPrefManager().getUser().getBus_id());
                Log.e("clear cuur and att" , prams.toString());
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

    private   void load_roads() {
        findViewById(R.id.loading_raoad_progressBar).setVisibility(View.VISIBLE);
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
                        start_trip();




                    }

                } catch (JSONException e) {
                    start_trip();
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
                prams.put("bus_id", AppController.getInstance().getPrefManager().getUser().getBus_id());
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

        private  void start_trip(){
          findViewById(R.id.loading_raoad_progressBar).setVisibility(View.GONE);
            exitReveal();
            gpsTracker = new LocationReq(getApplicationContext(), new LocationReq.update_location() {
                @Override
                public void update_location(Location location) {
                    onCurrentLocationUpdated(location);
                    sendLocation(location.getLatitude(),location.getLongitude());
                    Log.e("RouteTracker_Loc_Change",location.getLatitude()+"  "+ location.getLatitude() + "");
                }
            },uodate_live_bus_location_time);
           createCurrentPosMarker();
        }


    private void createCurrentPosMarker() {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_pin);
        MarkerOptions markerOptions = null;
        try {
             markerOptions = new MarkerOptions().position(new LatLng(gpsTracker.getLatitude(),gpsTracker.getLongitude())).icon(icon);
        }catch (NullPointerException e ){
             markerOptions = new MarkerOptions().position(new LatLng(routegpsTracker.getLatitude(),routegpsTracker.getLongitude())).icon(icon);
        }

        currentPosIcon = mMap.addMarker(markerOptions);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else {
            super.onBackPressed();
        }

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
                    Toast.makeText(Trace_Ride_Activity.this, "Permission needed to run your app", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void get_stations(){
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_home_pin);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONArray feedArr = new JSONArray(resObj.getString("response"));
                        for (int i=0 ; i<feedArr.length();i++){
                            JSONObject currentElement =  feedArr.getJSONObject(i);
                            if (!currentElement.getString("lat").equals("null")){
                                LatLng temp =  new LatLng(Double.valueOf(currentElement.getString("lat")),Double.valueOf(currentElement.getString("lung")));

                                mMap.addMarker(

                                        new MarkerOptions().position(temp).icon(icon)
                                );
                            }

                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("get stations","response"+response.toString() );

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
                prams.put("request", Config.GET_STATIONS);
                prams.put("bus_id",AppController.getInstance().getPrefManager().getUser().getBus_id());
                Log.e("get stations " , " prams " + prams.toString());
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

}


