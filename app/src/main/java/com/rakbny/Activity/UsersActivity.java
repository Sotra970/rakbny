package com.rakbny.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SelectableHolder;
import com.rakbny.Adapter.GeneralBasicAdapter;
import com.rakbny.Adapter.UsersAdapter;
import com.rakbny.R;
import com.rakbny.Utils.RecyclerViewTouchHelper;
import com.rakbny.data.Models.GeneralModel;
import com.rakbny.data.Models.UserModel;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersActivity extends AppCompatActivity {
    private ArrayList<UserModel> users;
    SwipeRefreshLayout swipeRefresh ;
    ListView users_list ;
    UsersAdapter adapter ;
    boolean ischildSelected = false ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        get_users();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(ContextCompat.getDrawable(getApplicationContext(),R.drawable.tlogo2));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        users_list = (ListView) findViewById(R.id.users_list);
        setupRefreshLayout();
        recycleSetUP();
        
    }
    void recycleSetUP(){
        users = new ArrayList<>();
        adapter = new UsersAdapter(UsersActivity.this,users);
        users_list.setAdapter(adapter);

        users_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View child, int postion, long l) {
                UserModel selected = adapter.getItem(postion);



                if (!ischildSelected){
                    getSupportActionBar().setDisplayUseLogoEnabled(false);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    findViewById(R.id.users_toolbar_bg).setBackgroundColor(Color.parseColor("#757575"));
                    findViewById(R.id.delete_user_action).setVisibility(View.VISIBLE);
                    ischildSelected= true ;
                }
                if (!adapter.isSelected(selected.getId())){
                    child.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
                    adapter.addSelectedItems(selected.getId());
                }else {
                    child.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),android.R.color.white));
                    adapter.removeSelectedItems(selected.getId());
                }

            }
        });

    }

    private void setupRefreshLayout(){

        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_users();
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
            users_list.setAlpha(0);
            users_list.animate().alpha(1);
        }else {
            users_list.setVisibility(View.VISIBLE);
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

    public void get_users(){
        users = new ArrayList<>();
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    users.clear();
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONArray feedArr = new JSONArray(resObj.getString("response"));
                        for (int i=0 ; i<feedArr.length();i++){
                            JSONObject currentElement =  feedArr.getJSONObject(i);
                            UserModel currentFeed = new UserModel();
                            currentFeed.setName(currentElement.getString("name"));
                            currentFeed.setSchool(currentElement.getString("school"));
                            currentFeed.setType(currentElement.getString("type"));
                            currentFeed.setId(currentElement.getString("id"));
                            currentFeed.setPhone(currentElement.getString("phone"));

                            if (!currentElement.getString("st_bus").equals("null"))
                                currentFeed.setBus_id(currentElement.getString("st_bus"));
                                else
                                currentFeed.setBus_id(currentElement.getString("ed_bus"));

                            Log.e("volley","user"+currentFeed.getPhone() );
                            users.add(currentFeed);
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
                prams.put("request", Config.GET_USERS_DATA);
                String  type  = getIntent().getExtras().getString("type");
                prams.put("type", type );
                if (!type.equals("admin") && !type.equals("any") )
                prams.put("bus_id", getIntent().getExtras().getString("bus_id"));
                Log.e("get users " , " prams " + prams.toString());
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

    public void delete_user_action_on_click(View view) {

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
                Log.e("ids", "ll");
                Intent resultIntent = new Intent();
                String ids = adapter.getSelectedItems().toString() ;
                ids =  ids.replaceAll("\\[" ,"(") .replaceAll("\\]" , ")");
                Log.e("ids", ids);
                resultIntent.putExtra("IDs", ids);
                resultIntent.putExtra("type", getIntent().getExtras().getString("type"));
                setResult(Config.GET_USERS_OK, resultIntent);
                supportFinishAfterTransition();

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.RED);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.green));

    }
}
