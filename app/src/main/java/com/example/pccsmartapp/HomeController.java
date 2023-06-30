package com.example.pccsmartapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeController implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    GoogleMap googleMap;
    MapView mapView;
    Marker marker;
    private View homeView;
    private Context context;

    public HomeController(Context context){
        this.context = context;
    }
    public void homeFuction() {
        this.homeView = homeView;
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    saveLocation(location);
                    updateLocation(location);
                }
            }
        };

        mapView = ((AppCompatActivity) context).findViewById(R.id.maps);
        mapView.getMapAsync(this);

        if (checkLocPermiss()) {
            startLocUpdates();
        } else {
            requestLocPermiss();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap Map) {
        googleMap = Map;
    }

    private Boolean checkLocPermiss() {
        int permissionState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocPermiss() {
        ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission
                .ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void startLocUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);

        if (checkLocPermiss()) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, null);

        }
    }
    private void saveLocation(Location location){
        if (firebaseAuth.getCurrentUser() != null){
            String uid = firebaseAuth.getCurrentUser().getUid();
            if (isAnggota(uid)) {
                DatabaseReference userLoc = databaseReference.child("locations").child(uid);
                userLoc.child("latitude").setValue(location.getLatitude());
                userLoc.child("longitude").setValue(location.getLongitude());
                userLoc.child("timestamp").setValue(System.currentTimeMillis());
            }
        }
    }

    private boolean isAnggota(String uid){
        return uid.equals("uid_anggota");
    }

    private void updateLocation(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (marker == null){
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            marker = googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        } else {
            marker.setPosition(latLng);
        }
    }

    public void setHomeView(View homeView, Context context){
        this.homeView = homeView;
        this.context = context;
        initView();
    }

    private void initView(){
        mapView = ((AppCompatActivity) context).findViewById(R.id.maps);
        mapView.getMapAsync(this);
    }
}

