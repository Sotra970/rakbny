package com.rakbny.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rakbny.data.Models.GeneralModel;
import com.rakbny.R;


import java.util.ArrayList;

/**
 * Created by sotra on 9/25/2016.
 */
public class GeneralBasicAdapter extends  RecyclerView.Adapter<GeneralBasicAdapter.schoolViewHolder> {
    private Activity context ;
    private ArrayList<GeneralModel> data ;
    private LayoutInflater layoutInflater ;


    public GeneralBasicAdapter(Activity context, ArrayList<GeneralModel> data) {
        this.context = context;
        this.data = data;
        layoutInflater = LayoutInflater.from(context);
        Log.e("newAdapter","size" + data.size());
    }
    public GeneralModel getItem(int postion){
        return  data.get(postion);
    }


    @Override
    public GeneralBasicAdapter.schoolViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.general_basic_item,parent,false);
        schoolViewHolder schoolViewHolder = new schoolViewHolder(view);
        return schoolViewHolder;
    }

    @Override
    public void onBindViewHolder(GeneralBasicAdapter.schoolViewHolder holder, int position) {
        final GeneralModel currentElement =  data.get(position);
        holder.ID.setText(String.valueOf(currentElement.getId()));
        holder.name.setText(currentElement.getName());
        Log.e("onBindView","onb"  );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
    public void insertData(GeneralModel data){
        this.data.add(data);
        notifyDataSetChanged();
    }
    public void refresh(ArrayList<GeneralModel> data) {
        this.data = data ;
        notifyDataSetChanged();
    }

    public class  schoolViewHolder extends RecyclerView.ViewHolder {
        TextView ID,name ;

        public schoolViewHolder(View itemView) {
            super(itemView);
            ID = (TextView) itemView.findViewById(R.id.ID);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }

}
