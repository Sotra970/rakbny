package com.rakbny.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.rakbny.Adapter.StudentsAdapter;
import com.rakbny.R;
import com.rakbny.data.Models.Student_list_model;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class StudentsFragment extends Fragment {


    ArrayList<Student_list_model> students;
    SwipeRefreshLayout swipeRefresh ;
    RecyclerView students_list ;
    StudentsAdapter adapter ;
    View res_layout ;
    public StudentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        res_layout = inflater.inflate(R.layout.fragment_students, container, false);
        get_students();
        swipeRefresh = (SwipeRefreshLayout) res_layout.findViewById(R.id.swipeRefresh);
        students_list = (RecyclerView) res_layout.findViewById(R.id.students_list);

        recycleSetUP();
        res_layout.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_students();
                Log.e("students fragment","retry layout clicked");
            }
        });
        return res_layout;
    }




    void recycleSetUP(){
        students = new ArrayList<>();
        adapter = new StudentsAdapter(getActivity(), students,res_layout.findViewById(R.id.loadingSpinner));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        students_list.setLayoutManager(layoutManager);
        students_list.setAdapter(adapter);

    }



    public void get_students(){
        res_layout.findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);

        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    res_layout.findViewById(R.id.retry).setVisibility(View.GONE);
                    res_layout.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    students.clear();
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                        JSONArray feedArr = new JSONArray(resObj.getString("response"));
                        for (int i=0 ; i<feedArr.length();i++){
                            JSONObject currentElement =  feedArr.getJSONObject(i);
                            Student_list_model currentFeed = new Student_list_model();
                            currentFeed.setName(currentElement.getString("student_name"));
                            currentFeed.setId(currentElement.getString("student_id"));
                            currentFeed.setState(currentElement.getString("student_state"));
                            students.add(currentFeed);
                        }
                        adapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    Log.e("students list ","response"+e.toString() );
                }
                Log.e("students list ","response"+response.toString() );

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
                    Toast.makeText(getContext(), "" + message, Toast.LENGTH_LONG).show();
                    res_layout.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", Config.GET_STUDENTS);
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

}
