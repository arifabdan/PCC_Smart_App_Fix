package com.example.pccsmartapp;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    public void onMessageReceived(RemoteMessage remoteMessage){
        String notifikasi = remoteMessage.getData().get("notifikasi");
        Intent intent = new Intent(this, LihatAnggota2.class);
        intent.putExtra("notifikasi", notifikasi);
        startActivity(intent);
    }
}
