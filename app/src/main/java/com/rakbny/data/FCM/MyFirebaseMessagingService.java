package com.rakbny.data.FCM;

/**
 * Created by Sotraa on 5/27/2016.
 */

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
       //testing incoming messeage
        Log.e("incoming message",remoteMessage.getData().toString());

//        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        Log.d(TAG, "Notification Message data: " + remoteMessage.getData());
//        Log.d(TAG, "Notification Message status : " + remoteMessage.getData().get("status"));
       //handel the message
       new recivedMesseageHandel(getApplicationContext(),remoteMessage.getData());

    }
    // [END receive_message]



}