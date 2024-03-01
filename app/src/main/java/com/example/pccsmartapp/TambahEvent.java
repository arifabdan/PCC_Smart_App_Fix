package com.example.pccsmartapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TambahEvent extends AppCompatActivity implements OnMapReadyCallback {
    private EditText namatxt, triptxt, deskripsi;
    private DatePicker tanggal;
    private Button tambahEvent;
    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private FusedLocationProviderClient fusedLocationClient;
    private Place selectedStartPlace;
    private Place selectedFinishPlace;
    private AtomicBoolean isMapReady = new AtomicBoolean(false);
    private List<Polyline> polylineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_event);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        namatxt = findViewById(R.id.namaevent);
        triptxt = findViewById(R.id.faseevent);
        deskripsi = findViewById(R.id.deskripsi);
        tanggal = findViewById(R.id.tanggalPicker);
        tambahEvent = findViewById(R.id.simpanevent);



        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCEC70VvQ3VeOESBOy3XpwSwVlTYYU_sbo");
        }

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragmentStart = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_start);
        autocompleteFragmentStart.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragmentStart.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Simpan tempat yang dipilih pada EditText start
                selectedStartPlace = place;
                Log.i(TAG, "Start Place: " + place.getName() + ", " + place.getId());


                // Proses jalur setelah kedua lokasi dipilih
                if (selectedStartPlace != null && selectedFinishPlace != null) {
                    processDirections(selectedStartPlace, selectedFinishPlace);
                }

            }

            @Override
            public void onError(@NonNull Status status) {
                // Tangani kesalahan
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        AutocompleteSupportFragment autocompleteFragmentFinish = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_finish);
        autocompleteFragmentFinish.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragmentFinish.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Simpan tempat yang dipilih pada EditText finish
                selectedFinishPlace = place;
                Log.i(TAG, "Finish Place: " + place.getName() + ", " + place.getId());

                // Proses jalur setelah kedua lokasi dipilih
                if (selectedStartPlace != null && selectedFinishPlace != null) {
                    processDirections(selectedStartPlace, selectedFinishPlace);
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // Tangani kesalahan
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        // Set listener untuk tombol tambahEvent
        tambahEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mendapatkan nilai dari elemen UI
                String eventName = namatxt.getText().toString().trim();
                String tripEvent = triptxt.getText().toString().trim();
                String eventDescription = deskripsi.getText().toString().trim();

                // Mendapatkan tanggal dari DatePicker
                int day = tanggal.getDayOfMonth();
                int month = tanggal.getMonth() + 1; // Januari dimulai dari 0
                int year = tanggal.getYear();

                // Format tanggal ke dalam string
                String eventDate = day + "/" + month + "/" + year;

                // Memeriksa apakah tempat sudah dipilih
                if (selectedStartPlace != null && selectedFinishPlace != null) {
                    // Mendapatkan nama tempat dari AutocompleteSupportFragment
                    String startPlace = selectedStartPlace.getName();
                    String finishPlace = selectedFinishPlace.getName();

                    // Memanggil metode untuk menyimpan data event ke Firebase Database
                    saveEventData(eventName, tripEvent ,startPlace, finishPlace, eventDescription, eventDate);
                } else {

                    Toast.makeText(TambahEvent.this, "Pilih tempat terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    // Metode untuk menyimpan data event ke Firebase Database
    private void saveEventData(String eventName, String tripEvent, String startPlace, String finishPlace, String description, String eventDate) {
        DatabaseReference eventRef = mDatabase.child("Event");
        String eventId = eventRef.push().getKey(); // Menghasilkan kunci acak yang unik

        Event event = new Event(eventId, eventName, tripEvent, startPlace, finishPlace, description, eventDate); // Memasukkan eventId ke objek Event

        mDatabase.child("Event").child(eventId).setValue(event)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(TambahEvent.this, "Tambah Event Berhasil", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TambahEvent.this, "Tambah Event Gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void processDirections(Place startPlace, Place finishPlace) {
        LatLng startLatLng = startPlace.getLatLng();
        LatLng finishLatLng = finishPlace.getLatLng();

        String startLocation = startLatLng.latitude + "," + startLatLng.longitude;
        String finishLocation = finishLatLng.latitude + "," + finishLatLng.longitude;

        DirectionsTask directionsTask = new DirectionsTask();
        directionsTask.execute(startLocation, finishLocation);
    }


    public void onMapReady(GoogleMap googleMap) {
        isMapReady.set(true);
        mMap = googleMap;

        // Proses jalur jika kedua lokasi sudah dipilih
        if (selectedStartPlace != null && selectedFinishPlace != null) {
            processDirections(selectedStartPlace, selectedFinishPlace);
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

                    Polyline segmentPolyline = mMap.addPolyline(polylineOptions);
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

                Polyline segmentPolyline = mMap.addPolyline(polylineOptions);
                polylineList.add(segmentPolyline);
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : coordinates) {
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

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
}