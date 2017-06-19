package com.rakbny.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.itextpdf.text.DocumentException;
import com.rakbny.R;
import com.rakbny.Utils.Utils;
import com.rakbny.data.Models.serialModel;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class Contact_Us extends AppCompatActivity {
EditText phone , name , issue ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact__us);
        phone = (EditText) findViewById(R.id.phone_em);
        name = (EditText) findViewById(R.id.name_em);
        issue = (EditText) findViewById(R.id.issue_em);
    }

    public void send_mail(View view) {
        final String subject =  name.getText().toString().toString()+ "  -  "+
                phone.getText().toString().toString() ;
        final String messsage =
              "Name: " +  name.getText().toString().toString()+ "\r\n"+
                      "phone Number:   " +    phone.getText().toString().toString() + "\r\n"+
                issue.getText().toString().toString() + "\r\n" ;
            findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
            final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                        JSONObject resObj = new JSONObject(response);
                        if (resObj.getString("operation").equals("done")){
                                        Toast.makeText(getApplicationContext(),"your message have sent ",Toast.LENGTH_LONG).show();
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
                    prams.put("request", String.valueOf(Config.SEND_MESSAGE));
                    prams.put("subject", subject);
                    prams.put("message", messsage);
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
