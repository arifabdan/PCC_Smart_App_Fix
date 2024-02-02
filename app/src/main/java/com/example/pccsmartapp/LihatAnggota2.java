package com.example.pccsmartapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class LihatAnggota2 extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private GoogleMap map;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatabaseReference userLocationData;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private double THRESHOLD_FALL = 20.6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_anggota);

        FirebaseApp.initializeApp(this);
        Intent serviceI = new Intent(this, MessagingService.class);
        startService(serviceI);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Places.initialize(this, "AIzaSyCuCk-V1xx4wjZuexn58sUO-sMxYjZX7qA");
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        userLocationData = FirebaseDatabase.getInstance().getReference("User_Location");

    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                DatabaseReference userData = FirebaseDatabase.getInstance().getReference("users").child(userId);

                userData.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String userRole = snapshot.child("role").getValue(String.class);

                            // Cek apakah pengguna adalah "anggota" dan percepatan di bawah ambang batas
                            if ("Anggota".equals(userRole) && acceleration < THRESHOLD_FALL) {
                                // Jatuh terdeteksi untuk anggota, lakukan sesuatu (misalnya, kirim notifikasi ke staff)
                                sendNotificationToStaff();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        userLocationData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                googleMap.clear();

                for (DataSnapshot lastlocation : snapshot.getChildren()) {
                    String UID = lastlocation.getKey();
                    String username = lastlocation.child("username").getValue(String.class);
                    double latitude = lastlocation.child("latitude").getValue(Double.class);
                    double longitude = lastlocation.child("longitude").getValue(Double.class);

                    LatLng latLng = new LatLng(latitude, longitude);
                    if (latitude != 0 && longitude != 0) {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sepeda);
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(username).icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        googleMap.addMarker(markerOptions);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                                    // Menambahkan kondisi untuk membatasi pengambilan data lokasi hanya untuk anggota
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

    private void sendNotificationToStaff() {
        // Buat Intent untuk membuka aktivitas tertentu ketika notifikasi diklik
        Intent intent = new Intent(this, LihatAnggota1.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Bangun notifikasi
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "YourChannelId")
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                .setContentTitle("Anggota Terdeteksi Jatuh")
                .setContentText("Notifikasi: Anggota terdeteksi jatuh!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Dapatkan NotificationManager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Tampilkan notifikasi
        notificationManager.notify(123, builder.build());
    }

    protected void onResume() {
        super.onResume();
        reqLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
