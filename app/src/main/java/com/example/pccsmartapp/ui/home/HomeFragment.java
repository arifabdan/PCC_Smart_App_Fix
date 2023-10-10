package com.example.pccsmartapp.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pccsmartapp.MyService;
import com.example.pccsmartapp.R;


import com.example.pccsmartapp.databinding.FragmentHomeBinding;
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

public class HomeFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private FragmentHomeBinding binding;
    private GoogleMap map;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatabaseReference userLocationData, userRef;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private Button searchbutton;
    private String userRole;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Intent serviceI = new Intent(getActivity(), MyService.class);
        getActivity().startService(serviceI);


        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Places.initialize(requireContext(), "AIzaSyCuCk-V1xx4wjZuexn58sUO-sMxYjZX7qA");
        placesClient = Places.createClient(requireContext());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        searchbutton = root.findViewById(R.id.caribtn);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        userLocationData = FirebaseDatabase.getInstance().getReference("User_Location");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRole = FirebaseDatabase.getInstance().getReference("users").child(userId).child("role");

            userRole.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.getValue(String.class);
                        if (role != null) {
                            if (role.equals("Staff")) {
                                searchbutton.setVisibility(View.VISIBLE);
                                searchbutton.setEnabled(true);
                            } else if (role.equals("Anggota")) {
                                reqLocationUpdates();
                                searchbutton.setVisibility(View.GONE);
                                searchbutton.setEnabled(false);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        userLocationData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for (DataSnapshot lastlocation : snapshot.getChildren()){
                    String UID = lastlocation.getKey();
                    String username = lastlocation.child("username").getValue(String.class);
                    double latitude = lastlocation.child("latitude").getValue(Double.class);
                    double longitude = lastlocation.child("longitude").getValue(Double.class);

                    LatLng latLng = new LatLng(latitude, longitude);
                   if (latitude != 0 && longitude != 0){
                       MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(username);
                       googleMap.addMarker(markerOptions);
                   }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private void reqLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(500)
                .setFastestInterval(1000);


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
                                if (snapshot.exists()){
                                    String username = snapshot.child("username").getValue(String.class);
                                    if (username != null){
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

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {

        }

    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event) {
        double Ax = Math.abs(event.values[0]);
        double Ay = Math.abs(event.values[1]);
        double Az = Math.abs(event.values[2]);

        double gravityRes = calculateSVM(Ax, Ay, Az);

            if (gravityRes > 20.6) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    DatabaseReference userRole = FirebaseDatabase.getInstance().getReference("users").child(userId).child("role");
                    userRole.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String role = snapshot.getValue(String.class);
                                if (role != null && role.equals("Staff")) {
                                    searchbutton.setVisibility(View.VISIBLE);
                                    searchbutton.setEnabled(true);
                                    searchbutton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            getCurrentLocation();
                                        }
                                    });
                                    Toast.makeText(requireContext(), "Anggota Terjatuh", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle the error if needed
                        }
                    });
                }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double calculateSVM(double Ax, double Ay, double Az){
        double sumSVM = (Ax * Ax) + (Ay * Ay) + (Az * Az);
        return Math.sqrt(sumSVM);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    findNearestHospital();
                } else {
                    Toast.makeText(requireContext(), "Lokasi tidak tersedia.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
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
                if (snapshot.exists()){
                    DataSnapshot lastlocation = snapshot.getChildren().iterator().next();
                    double latitude = lastlocation.child("latitude").getValue(Double.class);
                    double longitude = lastlocation.child("longitude").getValue(Double.class);

                    LatLng latLng = new LatLng(latitude, longitude);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
                    placesClient.findCurrentPlace(request).addOnSuccessListener(findCurrentPlaceResponse -> {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

                                            Toast.makeText(requireContext(), "Tempat pengobatan terdekat: " + placeName + ", " + placeAddress, Toast.LENGTH_SHORT).show();

                                            map.addMarker(new MarkerOptions()
                                                    .position(placeLocation)
                                                    .title(placeName)
                                                    .snippet(placeAddress));
                                        } else {
                                            Toast.makeText(requireContext(), "Tidak ada tempat pengobatan ditemukan.", Toast.LENGTH_SHORT).show();
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Izin akses lokasi ditolak.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if (locationCallback != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}