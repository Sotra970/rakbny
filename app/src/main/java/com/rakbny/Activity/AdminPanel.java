package com.rakbny.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
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
import com.google.android.gms.maps.model.LatLng;
import com.itextpdf.text.DocumentException;
import com.rakbny.Widget.FrameLayoutTouchListener;
import com.rakbny.data.Models.UserModel;
import com.rakbny.data.Models.serialModel;
import com.rakbny.R;
import com.rakbny.Utils.Utils;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;
import com.rakbny.fragments.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminPanel extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 262 ;
    ArrayList<serialModel> serials = new ArrayList<>();
    TextInputLayout inputLayout ;
    EditText input ;
    View input_container ;
    int code = 404 ;
    String deletedID ;

    private Bundle onActivityResultExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        if (savedInstanceState == null) no_storage_permission();
        input = (EditText) findViewById(R.id.input);
        inputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        input_container = findViewById(R.id.input_container);
    }

    public void create_student(final String count, final String bus_id , final String schoolname){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    String serials_txt  = "";
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONArray feedArr = new JSONArray(resObj.getString("response"));
                        for (int i=0 ; i<feedArr.length();i++){
                           int counter = i +1;
                            JSONObject currentElement =  feedArr.getJSONObject(i);
                            serialModel currentFeed = new serialModel();
                            currentFeed.setStudent(currentElement.getString("hash_Student"));
                            currentFeed.setF_parent(currentElement.getString("hash_first_parent"));
                            currentFeed.setS_parent(currentElement.getString("hash_sec_parent"));
                            serials.add(currentFeed);

                            serials_txt = serials_txt+"\r\n"+ "\r\n" +  "student "+counter + ": " + "\r\n";
                            serials_txt = serials_txt + "student "+ "serial: " + currentFeed.getStudent() + "\r\n";
                            serials_txt = serials_txt + "first parent " + "serial: " + currentFeed.getF_parent() + "\r\n";
                            serials_txt = serials_txt + "second parent " + "serial: " + currentFeed.getS_parent()+ "\r\n"+ "\r\n" ;



                        }

                        String pdfname = schoolname + "_"+"students" + "_serial's";
                        try {

                            viewPdf(Utils.createPdf(pdfname , pdfname , serials_txt , getApplicationContext()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (DocumentException e) {
                            e.printStackTrace();
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.CREATE_STUDENT));
                prams.put("count", count);
                prams.put("bus_id", bus_id);
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

    public void create_editor(final String school_name ,final String bus_id, final String type){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
            final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                        JSONObject resObj = new JSONObject(response);
                        if (resObj.getString("operation").equals("done")){
                            String received_hash = resObj.getString("response");

                                serialModel currentFeed = new serialModel();
                                currentFeed.setEditor(received_hash);

                            String pdfname = school_name +"-"+type + " serial";
                            try {

                                viewPdf(Utils.createPdf(pdfname , pdfname , received_hash , getApplicationContext()));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (DocumentException e) {
                                e.printStackTrace();
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
                        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    }

                    Log.e("volley", "error" + error.toString());
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> prams = new HashMap<>();
                    prams.put("request", String.valueOf(Config.CREATE_EDITOR));
                    prams.put("type", type);
                    prams.put("bus_id", bus_id);
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

    public void activate_bus(final String bus_id){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        Toast.makeText(getApplicationContext() , "bus activated" , Toast.LENGTH_LONG).show();
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.ACTIVATE_BUS));
                prams.put("bus_id", bus_id);
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

    public void deactivate_bus(final String bus_id){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        Toast.makeText(getApplicationContext() , "bus deactivated" , Toast.LENGTH_LONG).show();
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.DEACTIVATE_BUS));
                prams.put("bus_id", bus_id);
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

    public void delete_bus(final String bus_id){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                    Toast.makeText(getApplicationContext() , "bus deleted" , Toast.LENGTH_LONG).show();
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.DELETE_BUS));
                prams.put("bus_id", bus_id);
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


    public void delete_user(final String ID  , String type){
        String req = String.valueOf(Config.DELETE_USER);
        switch (type){
            case  "student":
                req = Config.DELETE_STUDENT;
                break;
            case  "driver":
            case  "supervisor":
                req = Config.DELETE_EDITOR;
                break;

        }
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final String finalReq = req;
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        Toast.makeText(getApplicationContext() , "users deleted" , Toast.LENGTH_LONG).show();
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", finalReq);
                prams.put("user_ids", ID);
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

    public void create_admin(){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        String received_hash = resObj.getString("response");

                        serialModel currentFeed = new serialModel();
                        currentFeed.setAdmin(received_hash);

                        String pdfname = "Admin" + " serial";
                        try {

                            viewPdf(Utils.createPdf(pdfname , pdfname , received_hash , getApplicationContext()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (DocumentException e) {
                            e.printStackTrace();
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.CREATE_ADMIN));
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



   public void create_school(final String SchoolName){
       findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
       final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
           @Override
           public void onResponse(String response) {
               try {
                   findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                   JSONObject resObj = new JSONObject(response);
                   if (resObj.getString("operation").equals("done")){
                       // TODO: 10/30/2016 display OK
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
                   findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
               }

               Log.e("volley", "error" + error.toString());
           }
       }){
           @Override
           protected Map<String, String> getParams() throws AuthFailureError {
               Map<String,String> prams = new HashMap<>();
               prams.put("request", String.valueOf(Config.CREATE_SCHOOL));
               prams.put("school_name", SchoolName);
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

    public void create_bus(final String bus_name , final String school_id ){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        // TODO: 10/30/2016 display OK
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.CREATE_BUS));
                prams.put("school_id", school_id );
                prams.put("bus_name", bus_name );
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
    public void delete_school(final String school_id ){
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        Toast.makeText(getApplicationContext(),"school deleted" , Toast.LENGTH_LONG).show();
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.DELETE_SCHOOL));
                prams.put("school_id", school_id );
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

    public void get_bus_location(final String bus_id){
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONObject res = new JSONObject(resObj.getString("response"));

                        try {
                            LatLng latLng = new LatLng(Double.valueOf(res.getString("lat")) ,Double.valueOf(res.getString("lung")) );
                            startActivity(new Intent(getApplicationContext(),adminn_bus_loc_map_activity.class).putExtra("location",latLng));

                        }catch (Exception e ){
                           Toast.makeText(getApplicationContext(),"The bus have not location",Toast.LENGTH_LONG).show();
                        }
                        // TODO: 10/30/2016 display location
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
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", String.valueOf(Config.GET_BUS_LOCATION));
                prams.put("bus_id", bus_id);
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

    public void create(View view) {
        code = 404;
        switch (view.getId()){
            case  R.id.create_student:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.CREATE_STUDENT);
                break;
            case  R.id.create_supervisor:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.CREATE_SUPERVISOR);
                break;
            case  R.id.create_driver:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.CREATE_DRIVER);
                break;
            case  R.id.create_bus:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class).putExtra("from",Config.CREATE_BUS),Config.CREATE_BUS);
                break;
            case  R.id.delete_school:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class).putExtra("from",Config.DELETE_SCHOOL),Config.DELETE_SCHOOL);
                break;
            case  R.id.bus_location:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.GET_BUS_LOCATION);
                break;
            case  R.id.create_home_route:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.CREATE_HOME_ROUTE);
                break;
            case  R.id.create_school_route:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.CREATE_SCHOOL_ROUTE);
                break;
            case  R.id.create_admin:
                create_admin();
                break;
            case  R.id.create_school:
                 inputLayout.setHint("Enter School Name ");
                input_container.setVisibility(View.VISIBLE);
                code = Config.CREATE_SCHOOL ;
                break;
            case  R.id.delete_user:
                startActivityForResult(new Intent(getApplicationContext(),UsersActivity.class).putExtra("type","any"),Config.GET_USERS);
                break;
            case  R.id.delete_student:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.DELETE_STUDENT_CODE);
                break;
            case  R.id.delete_parent:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.DELETE_PARENT_CODE);
                break;
            case  R.id.delete_driver:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.DELETE_DRIVER);
                break;
            case  R.id.delete_supervisor:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.DELETE_SUPERVISOR);
                break;
            case  R.id.delete_admin :
                inputLayout.setHint("Enter admins deletion password ");
                input_container.setVisibility(View.VISIBLE);
                code = Config.DELETE_USER;
                break;
            case  R.id.delete_bus:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.DELETE_BUS);
                break;
            case  R.id.deactivate_bus:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class),Config.DEACTIVATE_BUS);
                break;
            case  R.id.activate_bus:
                startActivityForResult(new Intent(getApplicationContext(),SchoolsActivity.class).putExtra("selection","deactivate"),Config.ACTIVATE_BUS);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            try{        Log.e("admin activity result " ,"request code "+ requestCode +"result code"+ resultCode + "extra id"+data.getExtras().get("ID"));}catch (NullPointerException e){}
            code = 404;
            onActivityResultExtra = new Bundle() ;
        if(requestCode==Config.CREATE_STUDENT && resultCode==Config.BUSES_ACTIVITY_OK){
            inputLayout.setHint("Enter Count");
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input_container.setVisibility(View.VISIBLE);
            code = Config.CREATE_STUDENT ;
            onActivityResultExtra = data.getExtras();
        }


        if(requestCode==Config.CREATE_SUPERVISOR && resultCode==Config.BUSES_ACTIVITY_OK){
                create_editor(data.getExtras().getString("school_name"),data.getExtras().getString("ID"),"supervisor");

        }
        if(requestCode==Config.CREATE_DRIVER && resultCode==Config.BUSES_ACTIVITY_OK){
                create_editor(data.getExtras().getString("school_name"),data.getExtras().getString("ID"),"driver");
        }
        if(requestCode==Config.GET_BUS_LOCATION && resultCode==Config.BUSES_ACTIVITY_OK){
            get_bus_location(data.getExtras().getString("ID"));

        }
        if(requestCode==Config.CREATE_HOME_ROUTE&& resultCode==Config.BUSES_ACTIVITY_OK){
           Intent intent = new Intent(getApplicationContext(),Trace_Ride_Activity.class);
            intent.putExtra(Trace_Ride_Activity.EXTRA_DIR,Trace_Ride_Activity.EXTRA_HOME_DIR);
            intent.putExtra(Trace_Ride_Activity.EXTRA_BUS_ID,data.getExtras().getString("ID"));
            startActivity(intent);

        }

        if(requestCode==Config.CREATE_SCHOOL_ROUTE && resultCode==Config.BUSES_ACTIVITY_OK){
            Intent intent = new Intent(getApplicationContext(),Trace_Ride_Activity.class);
            intent.putExtra(Trace_Ride_Activity.EXTRA_DIR,Trace_Ride_Activity.EXTRA_SCHOOL_DIR);
            intent.putExtra(Trace_Ride_Activity.EXTRA_BUS_ID,data.getExtras().getString("ID"));
            startActivity(intent);


        }


        if(requestCode==Config.CREATE_BUS && resultCode==Config.BUSES_ACTIVITY_OK){

            inputLayout.setHint("Enter Bus Name ");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input_container.setVisibility(View.VISIBLE);
            code = Config.CREATE_BUS ;
            onActivityResultExtra = data.getExtras();



        }
        if(requestCode==Config.DELETE_SCHOOL && resultCode==Config.BUSES_ACTIVITY_OK){

            create_dialog(Config.DELETE_SCHOOL,data.getExtras().getString("ID"));

        }
        if(requestCode==Config.DELETE_BUS && resultCode==Config.BUSES_ACTIVITY_OK){
            create_dialog(Config.DELETE_BUS,data.getExtras().getString("ID"));

        }

        if(requestCode==Config.DEACTIVATE_BUS && resultCode==Config.BUSES_ACTIVITY_OK){
            deactivate_bus(data.getExtras().getString("ID"));
        }
        if(requestCode==Config.ACTIVATE_BUS && resultCode==Config.BUSES_ACTIVITY_OK){
            activate_bus(data.getExtras().getString("ID"));
        }
        if (requestCode == Config.DELETE_SUPERVISOR && resultCode==Config.BUSES_ACTIVITY_OK ){
            startActivityForResult(new Intent(getApplicationContext(),UsersActivity.class).putExtra("type","supervisor").putExtra("bus_id",data.getExtras().getString("ID")),Config.GET_USERS);
        }

        if (requestCode == Config.DELETE_DRIVER && resultCode==Config.BUSES_ACTIVITY_OK ){
            startActivityForResult(new Intent(getApplicationContext(),UsersActivity.class).putExtra("type","driver").putExtra("bus_id",data.getExtras().getString("ID")),Config.GET_USERS);
        }

        if (requestCode == Config.DELETE_STUDENT_CODE && resultCode==Config.BUSES_ACTIVITY_OK ){
            startActivityForResult(new Intent(getApplicationContext(),UsersActivity.class).putExtra("type","student").putExtra("bus_id",data.getExtras().getString("ID")),Config.GET_USERS);
        }
        if (requestCode == Config.DELETE_PARENT_CODE && resultCode==Config.BUSES_ACTIVITY_OK ){
            startActivityForResult(new Intent(getApplicationContext(),UsersActivity.class).putExtra("type","parent").putExtra("bus_id",data.getExtras().getString("ID")),Config.GET_USERS);
        }
        if(requestCode==Config.GET_USERS && resultCode==Config.GET_USERS_OK){
            delete_user(data.getExtras().getString("IDs") ,data.getExtras().getString("type") );
        }

    }

    public void cancel(View view) {
        input_container.setVisibility(View.GONE);
    }

    public void submit(View view) {
        if (code == 404 ) return;
          switch (code){
              case Config.CREATE_STUDENT : create_student(input.getText().toString(),onActivityResultExtra.getString("ID"),onActivityResultExtra.getString("school_name"));
                  input_container.setVisibility(View.GONE);
                  input.setText("");
                  code = 404 ;
                  break;
              case Config.CREATE_SCHOOL : create_school(input.getText().toString());
                  input_container.setVisibility(View.GONE);
                  input.setText("");
                  code = 404 ;
                  break;
              case Config.DELETE_USER :
                  if (!input.getText().toString().equals("1234")){
                      inputLayout.setError("incorrect password");
                      inputLayout.setErrorEnabled(true);
                      return;
                  }
                  inputLayout.setErrorEnabled(false);
                  startActivityForResult(new Intent(getApplicationContext(),UsersActivity.class).putExtra("type","admin"),Config.GET_USERS);
                  input_container.setVisibility(View.GONE);
                  input.setText("");
                  code = 404 ;
                  break;
              case Config.CREATE_BUS :
                  create_bus(input.getText().toString(),onActivityResultExtra.getString("ID"));
                  input_container.setVisibility(View.GONE);
                  input.setText("");
                  code =404 ;
                  break;


          }
    }
    private void viewPdf(File myFile){
      Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(myFile), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
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
    private  void  create_dialog(final int action , final String ID ){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete users ?");
        builder.setCancelable(false);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (action == Config.DELETE_BUS){
                    delete_bus(ID);
                }
                if (action == Config.DELETE_SCHOOL){
                    delete_school(ID);
                }

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.RED);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.green));

    }

    private void no_storage_permission() {
        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {



            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    //  no_gps_permition();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(AdminPanel.this, "Permission needed to run your app", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
