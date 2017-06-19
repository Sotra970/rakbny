package com.rakbny.data.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.rakbny.data.Models.UserModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MyPreferenceManager {

    private String TAG = MyPreferenceManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "sabaya";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_BUS_ID = "user_bus_id";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_STATION_ID = "staion_id";
    private static final String KEY_STATION_LAT = "lat";
    private static final String KEY_STATION_LUNG = "lung";
    private static final String KEY_DESTANCE_1_KM = "1km";
    private static final String KEY_DESTANCE_2_KM = "2km";
    private static final String KEY_DESTANCE_100_M = "100m";


    // Constructor
    public MyPreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void storeUser(UserModel user) {
        editor.clear();
        editor.commit();

            editor.putString(KEY_USER_ID, user.getId());
            editor.putString(KEY_USER_NAME, user.getName());
            editor.putString(KEY_USER_EMAIL, user.getEmail());
            editor.putString(KEY_USER_TYPE , user.getType());
            editor.putString(KEY_USER_PHONE , user.getPhone());
            editor.putString(KEY_USER_BUS_ID , user.getBus_id());
            editor.putString(KEY_USER_TOKEN , user.getToken());
        try {
            editor.putString(KEY_STATION_ID , user.getStation_id());
            editor.putFloat(KEY_STATION_LAT , user.getLat().floatValue());
            editor.putFloat(KEY_STATION_LUNG , user.getLung().floatValue());

        }catch (NullPointerException e){
            Log.e(TAG, "User shared preferences.err "+e.toString() );

        }

        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getName() + " ," + user.getType() );
    }

    public void update_station(Double lat , Double lung) {
        editor.putFloat(KEY_STATION_LAT , lat.floatValue());
        editor.putFloat(KEY_STATION_LUNG ,lung.floatValue());
        editor.commit();


    }
    public  boolean distance_1_KM_notify(){
        Date date = new Date() ;
        DateFormat dateFormat = new SimpleDateFormat("HHmmss") ;
              String   timestamp = dateFormat.format(date);
       String dis_1_km =  pref.getString(KEY_DESTANCE_1_KM, null);
        if (dis_1_km != null){
            try {
                long diffInMillis =  (dateFormat.parse(timestamp)).getTime() -  (dateFormat.parse(dis_1_km)).getTime() ;
                Log.e("diffInMillis" , "1km "+diffInMillis +"");
                diffInMillis/=1000*60*60;
                Log.e("diffInMillis" ,"1km "+ diffInMillis +"");
                if (diffInMillis >3){
                    editor.putString(KEY_DESTANCE_1_KM, timestamp);
                    editor.commit();
                    return true ;
                }else {
                    return false ;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            Log.e("diffInMillis" ,"1km "+ "null");
            editor.putString(KEY_DESTANCE_1_KM, timestamp);
            editor.commit();

        }
        return true ;
    }

    public  boolean distance_2_KM_notify(){
        Date date = new Date() ;
        DateFormat dateFormat = new SimpleDateFormat("HHmmss") ;
        String   timestamp = dateFormat.format(date);
        String dis_1_km =  pref.getString(KEY_DESTANCE_2_KM, null);
        if (dis_1_km != null){
            try {
                long diffInMillis =  (dateFormat.parse(timestamp)).getTime() -  (dateFormat.parse(dis_1_km)).getTime() ;
                Log.e("diffInMillis" ,"2km "+ diffInMillis +"");
                diffInMillis/=1000*60*60;
                Log.e("diffInMillis" ,"2km "+ diffInMillis +"");
                if (diffInMillis >3){
                    editor.putString(KEY_DESTANCE_2_KM, timestamp);
                    editor.commit();
                    return true ;
                }else {
                    return false ;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            Log.e("diffInMillis" ,"2km "+ "null");
            editor.putString(KEY_DESTANCE_2_KM, timestamp);
            editor.commit();

        }
        return true ;
    }

    public  boolean distance_100_M_notify(){
        Date date = new Date() ;
        DateFormat dateFormat = new SimpleDateFormat("HHmmss") ;
        String   timestamp = dateFormat.format(date);
        String dis_1_km =  pref.getString(KEY_DESTANCE_100_M, null);
        if (dis_1_km != null){
            try {
                long diffInMillis =  (dateFormat.parse(timestamp)).getTime() -  (dateFormat.parse(dis_1_km)).getTime() ;
                Log.e("diffInMillis" , "100m "+diffInMillis +"");
                diffInMillis/=1000*60*60;
                Log.e("diffInMillis" ,"100m "+ diffInMillis +"");
                if (diffInMillis >3){
                    editor.putString(KEY_DESTANCE_100_M, timestamp);
                    editor.commit();
                    return true ;
                }else {
                    return false ;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            Log.e("diffInMillis" ,"100m "+ "null");
            editor.putString(KEY_DESTANCE_100_M, timestamp);
            editor.commit();

        }
        return true ;
    }

    public UserModel getUser() {
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, name,type,phone , email , bus_id , token , staion_id;
            double lat ,  lung;
            id = pref.getString(KEY_USER_ID, null);
            name = pref.getString(KEY_USER_NAME, null);
            type = pref.getString(KEY_USER_TYPE, null);
            phone = pref.getString(KEY_USER_PHONE, null);
            email = pref.getString(KEY_USER_EMAIL, null);
            token = pref.getString(KEY_USER_TOKEN, null);
            bus_id = pref.getString(KEY_USER_BUS_ID, null);
            staion_id = pref.getString(KEY_STATION_ID, null);
            lat = pref.getFloat(KEY_STATION_LAT, 0);
            lung = pref.getFloat(KEY_STATION_LUNG, 0);

            UserModel user = new UserModel(id,name,email,phone,type,bus_id,token,lat,lung,staion_id);
            return user;
        }
        return null;
    }
    public void clear() {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(getUser().getBus_id() );
            FirebaseMessaging.getInstance().unsubscribeFromTopic("station"+getUser().getStation_id());
            Log.e(TAG, "unsubsidised from topic " + getUser().getBus_id() );

        }catch (Exception e){

        }

        editor.clear();
        editor.commit();
    }
}
