package com.rakbny.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.location.DetectedActivity;
import com.rakbny.R;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    boolean active = true;
    private static int SPLASH_TIME_OUT = 1000;
 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      //  AppController.getInstance().getPrefManager().clear();
        super.onCreate(savedInstanceState);


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
               Intent intent = null ;
                if (AppController.getInstance().getPrefManager().getUser() == null){
                     intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else if (

                        AppController.getInstance().getPrefManager().getUser().getType().equals("admin")){
                    intent = new Intent(getApplicationContext(), AdminPanel.class);
                    startActivity(intent);
                    finish();
                }
                else {

                    decativated(AppController.getInstance().getPrefManager().getUser().getBus_id());
                }


            }
        }, SPLASH_TIME_OUT);
    }

    public void decativated(final String bus_id){

        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Intent intent = null ;
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        intent = new Intent(getApplicationContext(), DeactivtedActivity.class);

                    }else {

                        if (AppController.getInstance().getPrefManager().getUser().getType().equals("driver") || AppController.getInstance().getPrefManager().getUser().getType().equals("supervisor")){
                            intent = new Intent(getApplicationContext(), Trace_Ride_Activity.class);
                        }
                        if (AppController.getInstance().getPrefManager().getUser().getType().equals("parent") || AppController.getInstance().getPrefManager().getUser().getType().equals("student")){
                            intent = new Intent(getApplicationContext(), FollowerActivity.class);
                        }
                    }
                    startActivity(intent);
                    finish();
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
                prams.put("request", String.valueOf(Config.DEACTIVATE));
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
}
