package com.example.pccsmartapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
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
    private Button searchbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_anggota2);

        Intent serviceI = new Intent(this, MyService.class);
        startService(serviceI);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Places.initialize(this, "AIzaSyCuCk-V1xx4wjZuexn58sUO-sMxYjZX7qA");
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        searchbutton = findViewById(R.id.caribtn);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        userLocationData = FirebaseDatabase.getInstance().getReference("User_Location");

        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the search button click
                findNearestHospital();
            }
        });
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
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(username);
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

    public void onFallDetected() {
        Toast.makeText(this, "Anggota Terjatuh.", Toast.LENGTH_SHORT).show();
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    findNearestHospital();
                } else {
                    Toast.makeText(this, "Lokasi tidak tersedia.", Toast.LENGTH_SHORT).show();
                }
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

    @SuppressLint("MissingPermission")
    private void findNearestHospital() {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.TYPES
        );

        FirebaseDatabase.getInstance().getReference("Users_Location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot lastlocation = snapshot.getChildren().iterator().next();
                    double latitude = lastlocation.child("latitude").getValue(Double.class);
                    double longitude = lastlocation.child("longitude").getValue(Double.class);

                    LatLng latLng = new LatLng(latitude, longitude);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
                    placesClient.findCurrentPlace(request).addOnSuccessListener(findCurrentPlaceResponse -> {
                        if (ContextCompat.checkSelfPermission(LihatAnggota2.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                                placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
                                    FindCurrentPlaceResponse currentPlaceResponse = response;
                                    List<PlaceLikelihood> placeLikelihoods = currentPlaceResponse.getPlaceLikelihoods();

                                    if (!placeLikelihoods.isEmpty()) {
                                        PlaceLikelihood likelihood = placeLikelihoods.get(0);
                                        Place currentPlace = likelihood.getPlace();

                                        String placeName = currentPlace.getName();
                                        String placeAddress = currentPlace.getAddress();
                                        LatLng placeLocation = currentPlace.getLatLng();

                                        Toast.makeText(LihatAnggota2.this, "Tempat pengobatan terdekat: " + placeName + ", " + placeAddress, Toast.LENGTH_SHORT).show();

                                        map.addMarker(new MarkerOptions()
                                                .position(placeLocation)
                                                .title(placeName)
                                                .snippet(placeAddress));
                                    } else {
                                        Toast.makeText(LihatAnggota2.this, "Tidak ada tempat pengobatan ditemukan.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(e -> {
                                    if (e instanceof ApiException) {
                                        ApiException apiException = (ApiException) e;
                                        int statusCode = apiException.getStatusCode();
                                    }
                                });
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
