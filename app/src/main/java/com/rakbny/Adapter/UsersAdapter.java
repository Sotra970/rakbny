package com.rakbny.Adapter;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SelectableHolder;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.rakbny.R;
import com.rakbny.data.Models.UserModel;
import com.rakbny.data.Models.UserModel;

import java.util.ArrayList;

/**
 * Created by sotra on 9/25/2016.
 */
public class UsersAdapter extends BaseAdapter{
    private Activity context ;
    private ArrayList<UserModel> data ;
    private LayoutInflater layoutInflater ;
    private  ArrayList selectedItems  = new ArrayList();

    public UsersAdapter(Activity context, ArrayList<UserModel> data)  {
        this.context = context;
        this.data = data;
        layoutInflater = LayoutInflater.from(context);
        Log.e("newAdapter","size" + data.size());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    public UserModel getItem(int postion){
        return  data.get(postion);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View itemView = layoutInflater.inflate(R.layout.users_item,parent,false);

        TextView ID,name  , bus , number , type ;
        View busLAyout ;

        ID = (TextView) itemView.findViewById(R.id.ID);
        name = (TextView) itemView.findViewById(R.id.name);
        bus = (TextView) itemView.findViewById(R.id.bus);
        number = (TextView) itemView.findViewById(R.id.phone);
        type = (TextView) itemView.findViewById(R.id.type);
        busLAyout =  itemView.findViewById(R.id.busLAyout);

        final UserModel currentElement =  data.get(position);
        ID.setText(String.valueOf(currentElement.getId()));
        name.setText(currentElement.getName());
        if (currentElement.getBus_id().equals("null")){
            busLAyout.setVisibility(View.GONE);
        }else {
           bus.setText(currentElement.getSchool()+"  " + currentElement.getBus_id());

        }

       number.setText(currentElement.getPhone());
       type.setText(currentElement.getType());
        Log.e("onBindView","onb" + currentElement.getPhone() );
        if (isSelected(data.get(position).getId())){
            itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAccent));
        }

        return itemView;
    }

    public ArrayList getSelectedItems() {
        return selectedItems;
    }

    public Boolean isSelected(String id) {
        for (int i = 0; i < selectedItems.size(); i++){
            if (selectedItems.get(i).equals(id)) return true;
    }

        return false;
    }

    public void addSelectedItems(String id) {
        this.selectedItems.add(id);
    }


    public void insertData(UserModel data){
        this.data.add(data);
        notifyDataSetChanged();
    }
    public void refresh(ArrayList<UserModel> data) {
        this.data = data ;
        notifyDataSetChanged();
    }

    public void removeSelectedItems(String id) {
        selectedItems.remove(id);
    }




}
