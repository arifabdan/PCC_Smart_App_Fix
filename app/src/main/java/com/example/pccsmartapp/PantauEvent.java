package com.example.pccsmartapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PantauEvent extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private String eventId;
    private DatabaseReference userLocationData;
    private GoogleMap map;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Marker userMarker;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private AtomicBoolean isMapReady = new AtomicBoolean(false);
    private List<Polyline> polylineList = new ArrayList<>();
    private double THRESHOLD_FALL=20.6;
    private String currentStatus = "Normal";

    private String SelectedEventId,SelectedTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantau_event);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        if (intent != null) {
            SelectedEventId = intent.getStringExtra("SelectedEventId");
            SelectedTripId = intent.getStringExtra("SelectedTripId");
        }


        FirebaseApp.initializeApp(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        userLocationData = FirebaseDatabase.getInstance().getReference().child("Event").child(SelectedEventId).child("Trip").child(SelectedTripId).child("peserta");
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
            DatabaseReference userLocationData = FirebaseDatabase.getInstance().getReference().child("Event").child(eventId).child("peserta").child(userId);

            userLocationData.child("status").setValue(status);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void processDirections(String startLocation, String finishLocation) {
        DirectionsTask directionsTask = new DirectionsTask();
        directionsTask.execute(startLocation, finishLocation);
    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("Event").child(SelectedEventId).child("Trip").child(SelectedTripId);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String startLocation = snapshot.child("start").getValue(String.class);
                    String finishLocation = snapshot.child("finish").getValue(String.class);

                    // Proses permintaan jalur menggunakan lokasi start dan finish
                    if (startLocation != null && finishLocation != null) {
                        processDirections(startLocation, finishLocation);
                    } else {
                        Log.e(TAG, "Start atau finish location tidak valid");
                    }
                } else {
                    Log.e(TAG, "Event dengan ID tersebut tidak ditemukan");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Gagal mengambil data dari Firebase: " + error.getMessage());
            }
        });

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
                        DatabaseReference userLocationData = FirebaseDatabase.getInstance().getReference("Event").child(eventId).child("peserta").child(userId);
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
    private class DirectionsTask extends AsyncTask<String, Void, DirectionsResult> {
        @Override
        protected DirectionsResult doInBackground(String... locations) {
            String startLocation = locations[0];
            String destinationLocation = locations[1];

            // Membuat objek GeoApiContext dengan kunci API Google Maps
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBa2hl7Po3V-ALGMmIdbV_mUdDIS82sHUw")
                    .build();

            // Membuat permintaan Directions API
            DirectionsApiRequest directionsApiRequest = DirectionsApi.getDirections(geoApiContext,
                    startLocation, destinationLocation);
            directionsApiRequest.mode(TravelMode.WALKING);

            try {
                // Mengirim permintaan ke API dan menerima respons
                DirectionsResult directionsResult = directionsApiRequest.await();

                // Mengembalikan respons rute ke onPostExecute
                return directionsResult;
            } catch (Exception e) {
                Log.e("DirectionsTask", "Error occurred: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(DirectionsResult directionsResult) {
            if (directionsResult != null) {
                showRouteOnMap(directionsResult);
            } else {
                Log.e("DirectionsTask", "Directions API request failed.");
            }
        }
    }

    private void showRouteOnMap(DirectionsResult directionsResult) {
        if (!isMapReady.get()) {
            Log.e("DirectionsTask", "Map is not ready yet.");
            return;
        }

        clearPolylines();

        List<LatLng> coordinates = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        try {
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyDkKvzMxiKerYMBZuUr4sFulKIb_ieIV7c")
                    .build();

            com.google.maps.model.LatLng[] latLngArray = getLatLngArray(directionsResult);
            ElevationResult[] elevationResults = ElevationApi.getByPoints(geoApiContext, latLngArray).await();

            for (int i = 0; i < directionsResult.routes.length; i++) {
                com.google.maps.model.LatLng[] path = directionsResult.routes[i].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);

                for (int j = 0; j < path.length; j++) {
                    LatLng coordinate = new LatLng(path[j].lat, path[j].lng);
                    coordinates.add(coordinate);

                    if (elevationResults != null && j < elevationResults.length) {
                        double elevation = elevationResults[j].elevation;
                        int color = getColorByElevation(elevation);
                        colors.add(color);
                    } else {
                        colors.add(Color.GRAY);
                    }
                }
            }

            int start = 0;
            int end = 0;
            int zIndex = 1;

            List<LatLng> segment = new ArrayList<>();
            int currentColor = colors.get(0);

            for (int i = 0; i < coordinates.size(); i++) {
                if (colors.get(i) == currentColor) {
                    segment.add(coordinates.get(i));
                } else {
                    PolylineOptions polylineOptions = new PolylineOptions().addAll(segment);
                    polylineOptions.color(currentColor);
                    polylineOptions.zIndex(zIndex++);
                    polylineOptions.width(8);

                    Polyline segmentPolyline = map.addPolyline(polylineOptions);
                    polylineList.add(segmentPolyline);

                    segment.clear();
                    segment.add(coordinates.get(i));
                    currentColor = colors.get(i);
                }
            }

            if (!segment.isEmpty()) {
                PolylineOptions polylineOptions = new PolylineOptions().addAll(segment);
                polylineOptions.color(currentColor);
                polylineOptions.zIndex(zIndex++);
                polylineOptions.width(8);

                Polyline segmentPolyline = map.addPolyline(polylineOptions);
                polylineList.add(segmentPolyline);
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : coordinates) {
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

        } catch (Exception e) {
            Log.e("ElevationApi", "Error occurred: " + e.getMessage());
        }
    }

    private void clearPolylines() {
        for (Polyline polyline : polylineList) {
            polyline.remove();
        }
        polylineList.clear();
    }

    private com.google.maps.model.LatLng[] getLatLngArray(DirectionsResult directionsResult) {
        List<com.google.maps.model.LatLng> latLngList = new ArrayList<>();

        for (int i = 0; i < directionsResult.routes.length; i++) {
            com.google.maps.model.LatLng[] path = directionsResult.routes[i].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);

            for (int j = 0; j < path.length; j++) {
                Log.d("PathPoint", "Lat: " + path[j].lat + ", Lng: " + path[j].lng);
                latLngList.add(path[j]);
            }
        }

        return latLngList.toArray(new com.google.maps.model.LatLng[0]);
    }

    private int getColorByElevation(double elevation) {
        // Menentukan warna berdasarkan kisaran elevasi
        if (elevation < 100) {
            return Color.rgb(255, 0, 0); // Merah
        } else if (elevation < 200) {
            return Color.rgb(255, 128, 0); // Oranye
        } else if (elevation < 300) {
            return Color.rgb(255, 255, 0); // Kuning
        } else if (elevation < 400) {
            return Color.rgb(128, 255, 0); // Hijau Terang
        } else if (elevation < 500) {
            return Color.rgb(0, 255, 0); // Hijau
        } else if (elevation < 600) {
            return Color.rgb(0, 255, 128); // Hijau Laut
        } else if (elevation < 700) {
            return Color.rgb(0, 128, 255); // Biru Terang
        } else if (elevation < 800) {
            return Color.rgb(0, 0, 255); // Biru
        } else if (elevation < 900) {
            return Color.rgb(128, 0, 255); // Ungu
        } else {
            return Color.rgb(255, 0, 255); // Magenta
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