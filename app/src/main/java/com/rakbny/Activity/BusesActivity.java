package com.rakbny.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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
import com.rakbny.Adapter.GeneralBasicAdapter;
import com.rakbny.data.Models.GeneralModel;
import com.rakbny.R;
import com.rakbny.Utils.RecyclerViewTouchHelper;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BusesActivity extends AppCompatActivity {
    ArrayList<GeneralModel> buses = new ArrayList<>();
    SwipeRefreshLayout swipeRefresh ;
    RecyclerView Buses_list;
    GeneralBasicAdapter adapter ;
    GeneralModel extraFeed ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schools);
        extraFeed =(GeneralModel) getIntent().getExtras().get("feed");
        get_buses();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(ContextCompat.getDrawable(getApplicationContext(),R.drawable.tlogo2));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        Buses_list = (RecyclerView) findViewById(R.id.schools_list);
        setupRefreshLayout();
        recycleSetUP();
    }
    void recycleSetUP(){

        adapter = new GeneralBasicAdapter(BusesActivity.this,buses);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        Buses_list.setLayoutManager(layoutManager);
        Buses_list.setAdapter(adapter);


        Buses_list.addOnItemTouchListener(new RecyclerViewTouchHelper(getApplicationContext(), Buses_list, new RecyclerViewTouchHelper.recyclerViewTouchListner() {
            @Override
            public void onclick(View child, int postion) {
                GeneralModel selected = adapter.getItem(postion);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ID",selected.getId());
                resultIntent.putExtra("school_name",selected.getName());
                setResult(Config.BUSES_ACTIVITY_OK,resultIntent);
                supportFinishAfterTransition();
            }

            @Override
            public void onLongClick(View child, int postion) {

            }


        }));

    }

    private void setupRefreshLayout(){

        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_buses();
            }
        });
    }
    private void endLoading() {
        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(false);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            Buses_list.setAlpha(0);
            Buses_list.animate().alpha(1);
        }else {
            Buses_list.setVisibility(View.VISIBLE);
        }


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return false;
    }


    public void get_buses(){
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    buses.clear();
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONArray feedArr = new JSONArray(resObj.getString("response"));
                        for (int i=0 ; i<feedArr.length();i++){
                            JSONObject currentElement =  feedArr.getJSONObject(i);
                            GeneralModel currentFeed = new GeneralModel();
                            String id = currentElement.getString("bus_id");
                            String bus_name = currentElement.getString("bus_name");
                            currentFeed.setId(id);
                            // school name + bus_name
                            currentFeed.setName(extraFeed.getName()+" - "+ bus_name);
                            buses.add(currentFeed);
                        }
                        endLoading();
                        adapter.notifyDataSetChanged();
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
                prams.put("request", Config.GET_BUSES);
                prams.put("school_id", extraFeed.getId());
                if (getIntent().getExtras().get("selection")!= null)
                    prams.put("selection",getIntent().getExtras().getString("selection"));
                Log.e("get school pareams " , prams.toString());
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
