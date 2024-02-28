package com.example.pccsmartapp;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LihatAnggota1 extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatabaseReference userLocationData;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private double THRESHOLD_FALL=20.6;
    private String currentStatus = "Normal";



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        setContentView(R.layout.activity_lihat_anggota);
        FirebaseApp.initializeApp(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        userLocationData = FirebaseDatabase.getInstance().getReference("User_Location");
        userLocationData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    if (uid != null) {
                        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("User_Location").child(uid);
                        statusRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot statusSnapshot) {
                                // Method ini akan dipanggil saat ada perubahan pada status di bawah simpul dengan UID tertentu
                                String newStatus = statusSnapshot.child("status").getValue(String.class);
                                if (newStatus != null && newStatus.equals("Jatuh")) {
                                    // Dapatkan username dari snapshot user
                                    String username = userSnapshot.child("username").getValue(String.class);
                                    if (username != null) {
                                        // Dapatkan koordinat geografis pengguna yang terdeteksi jatuh
                                        Double latitude = userSnapshot.child("latitude").getValue(Double.class);
                                        Double longitude = userSnapshot.child("longitude").getValue(Double.class);
                                        if (latitude != null && longitude != null) {
                                            // Pindahkan kamera ke lokasi pengguna yang terdeteksi jatuh
                                            LatLng location = new LatLng(latitude, longitude);
                                            focusCameraOnLocation(location);
                                        }
                                        // Lakukan sesuatu dengan username, misalnya tampilkan notifikasi
                                        showNotification("WARNING!", username + " telah terjatuh");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void focusCameraOnLocation(LatLng location) {
        if (map != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location)
                    .zoom(15) // Sesuaikan level zoom yang diinginkan
                    .build();

            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float Ax = event.values[0];
            float Ay = event.values[1];
            float Az = event.values[2];

            double acceleration = Math.sqrt(Ax * Ax + Ay * Ay + Az * Az);


            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                userDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userRole = dataSnapshot.child("role").getValue(String.class);

                            // Hanya lanjut jika peran pengguna adalah "Anggota"
                            if ("Anggota".equals(userRole)) {
                                if (acceleration > THRESHOLD_FALL) {
                                    if (!currentStatus.equals("Jatuh")) {
                                        currentStatus = "Jatuh";
                                        updateStatusInFirebase(currentStatus);
                                    }
                                } else {
                                    if (!currentStatus.equals("Normal")) {
                                        currentStatus = "Normal";
                                        updateStatusInFirebase(currentStatus);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "staff_channel";
        CharSequence channelName = "Staff Channel";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Tampilkan notifikasi
        notificationManager.notify(notificationId, builder.build());
    }
    private void updateStatusInFirebase(String status) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userLocationData = FirebaseDatabase.getInstance().getReference("User_Location").child(userId);

            userLocationData.child("status").setValue(status);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        userLocationData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                googleMap.clear();

                for (DataSnapshot lastLocation : snapshot.getChildren()) {
                    String username = lastLocation.child("username").getValue(String.class);
                    Double latitude = lastLocation.child("latitude").getValue(Double.class);
                    Double longitude = lastLocation.child("longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        LatLng latLng = new LatLng(latitude, longitude);
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sepeda);
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(username).icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        googleMap.addMarker(markerOptions);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle batal permintaan
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void reqLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setFastestInterval(100);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        DatabaseReference userLocationData = FirebaseDatabase.getInstance().getReference("User_Location").child(userId);
                        DatabaseReference userData = FirebaseDatabase.getInstance().getReference("users").child(userId);
                        userData.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String userRole = snapshot.child("role").getValue(String.class);
                                    if ("Anggota".equals(userRole)) {
                                        String username = snapshot.child("username").getValue(String.class);
                                        if (username != null) {
                                            userLocationData.child("username").setValue(username);
                                            userLocationData.child("latitude").setValue(location.getLatitude());
                                            userLocationData.child("longitude").setValue(location.getLongitude());
                                            if (userMarker == null) {
                                                userMarker = map.addMarker(new MarkerOptions()
                                                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                                        .title(username));
                                            } else {
                                                userMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }



    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void addMarkerUsername(String username, LatLng latLng) {
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(username));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Izin akses lokasi ditolak.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        reqLocationUpdates();
    }

    protected void onPause() {
        super.onPause();
        // Hentikan pendaftaran listener saat aplikasi dihentikan atau di-background
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
