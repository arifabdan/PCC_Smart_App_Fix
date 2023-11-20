package com.example.pccsmartapp;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService{

    private static final String TAG = "FirebaseService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle the received message and trigger the notification.
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Add your custom logic to handle the data payload and trigger a notification.
            // You can use a NotificationManager to display a notification to the user.
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
