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

public class TambahEvent extends AppCompatActivity implements OnMapReadyCallback {
    private EditText namatxt, start, finish, deskripsi;
    private DatePicker tanggal;
    private Button tambahTujuan, tambahEvent;
    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private FusedLocationProviderClient fusedLocationClient;
    private Place selectedStartPlace;
    private Place selectedFinishPlace;
    private List<Place> daftarTujuan = new ArrayList<>();

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
                    saveEventData(eventName, startPlace, finishPlace, eventDescription, eventDate);
                } else {

                    Toast.makeText(TambahEvent.this, "Pilih tempat terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    // Metode untuk menyimpan data event ke Firebase Database
    private void saveEventData (String eventName, String startPlace, String finishPlace, String description, String eventDate){
        DatabaseReference eventRef = mDatabase.child("Events");
        String eventId = eventRef.push().getKey();

        Event event = new Event(eventName, startPlace, finishPlace, description, eventDate);

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
        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        List<LatLng> coordinates = new ArrayList<>();

        for (int i = 0; i < directionsResult.routes.length; i++) {
            com.google.maps.model.LatLng[] path = directionsResult.routes[i].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);
            for (com.google.maps.model.LatLng latLng : path) {
                LatLng coordinate = new LatLng(latLng.lat, latLng.lng);
                coordinates.add(coordinate);
            }
        }

        currentPolyline = mMap.addPolyline(new PolylineOptions().addAll(coordinates));

        // Membuat array LatLng dari List<LatLng>
        com.google.maps.model.LatLng[] latLngArray = new com.google.maps.model.LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            LatLng latLng = coordinates.get(i);
            latLngArray[i] = new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude);
        }

        // Membuat permintaan Elevation API
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyARwUqqEC-Fn9rHk9RWm5pr1SBSAPL1yeM")
                .build();
        PendingResult<ElevationResult[]> pendingResult = ElevationApi.getByPoints(geoApiContext, latLngArray);

        try {
            ElevationResult[] elevationResults = pendingResult.await();
            if (elevationResults != null) {
                for (int i = 0; i < elevationResults.length; i++) {
                    ElevationResult elevationResult = elevationResults[i];
                    double elevation = elevationResult.elevation;

                    // Mengatur warna polyline berdasarkan elevasi
                    int color = getColorByElevation(elevation);
                    currentPolyline.setColor(color);
                }
            }
        } catch (Exception e) {
            Log.e("ElevationApi", "Error occurred: " + e.getMessage());
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : currentPolyline.getPoints()) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
    private int getColorByElevation(double elevation) {
        // Menentukan logika pemetaan warna berdasarkan elevasi
        // Misalnya, jika elevasi di bawah 100 meter, warna polyline akan merah
        if (elevation < 600) {
            return Color.RED;
        } else if (elevation < 700) {
            return Color.YELLOW;
        } else {
            return Color.GREEN;
        }
    }
}