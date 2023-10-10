package com.example.pccsmartapp;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MyService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MyServiceChannel";
    private DatabaseReference databaseReference;
    private LocationManager locationManager;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            databaseReference.setValue(location);
        }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    public void onProviderEnabled(String provider) {
    }


    public void onProviderDisabled(String provider) {
    }
    };
    public void onCreate(){
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users_Location");
        Log.d(TAG, "service created");
    }


    public int onStartCommand (Intent intent, int flags, int startID){
        Log.d(TAG, "service started");
        createNotificationChannel();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return  null;
    }

    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    10F,
                    (android.location.LocationListener) locationListener
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates((android.location.LocationListener) locationListener);
    }
    private void showNotification(){

    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("My Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp);
        return builder.build();
    }

}
