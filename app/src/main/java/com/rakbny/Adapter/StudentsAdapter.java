package com.rakbny.Adapter;

import android.app.Activity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import com.rakbny.data.Models.Student_list_model;
import com.rakbny.R;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sotra on 9/25/2016.
 */
public class StudentsAdapter extends  RecyclerView.Adapter<StudentsAdapter.schoolViewHolder> {
    private Activity context ;
    private ArrayList<Student_list_model> data ;
    private LayoutInflater layoutInflater ;
    View loading ;


    public StudentsAdapter(Activity context, ArrayList<Student_list_model> data,View loading)  {
        this.context = context;
        this.data = data;
        layoutInflater = LayoutInflater.from(context);
        Log.e("newAdapter","size" + data.size());
        this.loading = loading ;
    }
    public Student_list_model getItem(int postion){
        return  data.get(postion);
    }


    @Override
    public StudentsAdapter.schoolViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.student_list_item,parent,false);
        schoolViewHolder schoolViewHolder = new schoolViewHolder(view);
        return schoolViewHolder;
    }

    @Override
    public void onBindViewHolder(StudentsAdapter.schoolViewHolder holder, int position) {
        final Student_list_model currentElement =  data.get(position);
        holder.ID.setText(String.valueOf(position+1));
        holder.name.setText(currentElement.getName());
        if (currentElement.getState().equals("1")){
            holder.checkBox.setChecked(true);
        }
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((CheckBox) view).isChecked();
                if (checked){
                    sendstate("1",currentElement.getId(),(CheckBox)view);
                }else {
                    sendstate("0",currentElement.getId(),(CheckBox) view);
                }
            }
        });
        Log.e("onBindView","onb"  );
    }
    private void  reverseCheck(final String state , CheckBox view ){
        if (state.equals("0")){
            view.setChecked(true);
        }else {
            view.setChecked(false);
        }
        loading.setVisibility(View.GONE);
    }

    private void  Check(final String state , CheckBox view ){
        if (state.equals("1")){
            view.setChecked(true);
        }else {
            view.setChecked(false);
        }
    }
    private void sendstate(final String state , final String student_id, final CheckBox view ) {
        loading.setVisibility(View.VISIBLE);
        final StringRequest feedRequest = new StringRequest(Request.Method.POST, Config.OPERATION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getString("operation").equals("done")){
                       loading.setVisibility(View.GONE);
                        Toast.makeText(context,"operation done" , Toast.LENGTH_SHORT).show();
                    }else {
                        reverseCheck(state,view);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    reverseCheck(state,view);
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
                    String message = context.getString(R.string.NoConnection);
                    Toast.makeText(context, "" + message, Toast.LENGTH_LONG).show();
                    reverseCheck(state,view);
                }

                Log.e("volley", "error" + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> prams = new HashMap<>();
                prams.put("request", Config.STUDENT_ATTEND);
                prams.put("student_id",student_id);
                prams.put("state",state);
                prams.put("bus_id",AppController.getInstance().getPrefManager().getUser().getBus_id());
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

    @Override
    public int getItemCount() {
        return data.size();
    }
    public void insertData(Student_list_model data){
        this.data.add(data);
        notifyDataSetChanged();
    }
    public void refresh(ArrayList<Student_list_model> data) {
        this.data = data ;
        notifyDataSetChanged();
    }

    public class  schoolViewHolder extends RecyclerView.ViewHolder {
        TextView ID,name ;
        AppCompatCheckBox checkBox ;
        public schoolViewHolder(View itemView) {
            super(itemView);
            ID = (TextView) itemView.findViewById(R.id.ID);
            name = (TextView) itemView.findViewById(R.id.name);
            checkBox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

}
