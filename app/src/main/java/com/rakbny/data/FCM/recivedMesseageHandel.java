package com.rakbny.data.FCM;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.rakbny.Activity.LoginActivity;
import com.rakbny.Utils.LocationReq;
import com.rakbny.Utils.Utils;
import com.rakbny.data.Models.UserModel;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;
import com.rakbny.data.app.MyPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Sotraa on 6/15/2016.
 */
public class recivedMesseageHandel {

    public recivedMesseageHandel(Context context,Map<String,String> currentElemnt){
        Log.e("fcm mess",currentElemnt.toString());
        MyPreferenceManager pref = AppController.getInstance().getPrefManager();;
        UserModel userModel =  pref.getUser() ;

    try {

        if (currentElemnt.get("topic").equals(userModel.getBus_id()) ){
            LatLng station = new LatLng(AppController.getInstance().getPrefManager().getUser().getLat(),AppController.getInstance().getPrefManager().getUser().getLung());
            if(!userModel.getType().equals("driver")  && currentElemnt.get("feed_type").equals("bus_feed") ){

                LatLng bus_location = new LatLng(Double.valueOf(currentElemnt.get("lat")) , Double.valueOf(currentElemnt.get("lung"))) ;
                Double distance = SphericalUtil.computeDistanceBetween(station, bus_location);
                Intent intent = new Intent ();
                intent.setAction(Config.UPDATE_BUS_LOCATION);
                intent.putExtra(Config.LOCATION,bus_location);
                intent.putExtra(Config.DISTANCE,distance);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                if (1000<distance&&distance < 2000 && AppController.getInstance().getPrefManager().distance_2_KM_notify() ){
                    new showNotificationHandel(context,String.valueOf(distance.intValue()+"m"));
                }else
                if (100<distance&&distance < 1000 && AppController.getInstance().getPrefManager().distance_1_KM_notify() ){
                    new showNotificationHandel(context,String.valueOf(distance.intValue()+"m"));
                }else
                if (distance < 100 && AppController.getInstance().getPrefManager().distance_100_M_notify() ){
                    new showNotificationHandel(context,"bus has arrived");
                }

            }
        }

        if (currentElemnt.get("topic").equals("logout") ){
            Log.e("tokens " , currentElemnt.get("token") + " user token " + userModel.getToken());
            if (currentElemnt.get("user_id").equals(userModel.getId()) && !currentElemnt.get("token").equals(userModel.getToken()) ){
                Log.e("tokens " ,"not match");
                pref.clear();
                Intent intent = new Intent(context, LoginActivity.class);
                ComponentName cn = intent.getComponent();
                Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
                context.startActivity(mainIntent);

            }
        }


        if (currentElemnt.get("topic").equals(userModel.getBus_id()) ){
            if(userModel.getType().equals("parent")  && currentElemnt.get("feed_type").equals("state_feed") ){

                Intent intent = new Intent ();
                intent.setAction(Config.UPDATE_CHILD_STATE);
                intent.putExtra("state",currentElemnt.get("state"));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            }
        }


        if (currentElemnt.get("topic").equals("station"+userModel.getStation_id()) ){
            if( currentElemnt.get("feed_type").equals("update_station") ){

                Intent intent = new Intent ();
                intent.setAction(Config.UPDATE_STATION);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                AppController.getInstance().getPrefManager().update_station(Double.valueOf(currentElemnt.get("lat")),Double.valueOf(currentElemnt.get("lung")));


            }
        }


    }catch (Exception e){
        Log.e("receive fcm exception",e.toString());
    }



    }
}
