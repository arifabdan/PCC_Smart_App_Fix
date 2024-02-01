package com.example.pccsmartapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Date;
import java.util.List;

public class TambahEvent extends AppCompatActivity {
    private EditText namatxt, start, finish, deskripsi;
    private DatePicker tanggal;
    private Button tambahEvent;
    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_event);
        // Inisialisasi Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inisialisasi elemen UI
        namatxt = findViewById(R.id.namaevent);
        start = findViewById(R.id.Start);
        finish = findViewById(R.id.tujuan);
        deskripsi = findViewById(R.id.deskripsi);
        tanggal = findViewById(R.id.tanggalPicker);
        tambahEvent = findViewById(R.id.simpanevent);

        String StartLoc = start.getText().toString();
        String FinishLoc = finish.getText().toString();

        processDirections(StartLoc, FinishLoc);

        // Set listener untuk tombol tambahEvent
        tambahEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mendapatkan nilai dari elemen UI
                String eventName = namatxt.getText().toString().trim();
                String startTime = start.getText().toString().trim();
                String endTime = finish.getText().toString().trim();
                String eventDescription = deskripsi.getText().toString().trim();

                // Mendapatkan tanggal dari DatePicker
                int day = tanggal.getDayOfMonth();
                int month = tanggal.getMonth() + 1; // Januari dimulai dari 0
                int year = tanggal.getYear();

                // Format tanggal ke dalam string
                String eventDate = day + "/" + month + "/" + year;

                // Memanggil metode untuk menyimpan data event ke Firebase Database
                saveEventData(eventName, startTime, endTime, eventDescription, eventDate);
            }
        });
    }

    // Metode untuk menyimpan data event ke Firebase Database
    private void saveEventData (String eventName, String start, String finish, String description, String eventDate){
        DatabaseReference eventRef = mDatabase.child("Events");
        String eventId = eventRef.push().getKey();

        Event event = new Event(eventName, start, finish, description, eventDate);

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
    private void processDirections(String startLocation, String destinationLocation) {
        com.example.pccsmartapp.TambahEvent.DirectionsTask directionsTask = new com.example.pccsmartapp.TambahEvent.DirectionsTask();
        directionsTask.execute(startLocation, destinationLocation);
    }


    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
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