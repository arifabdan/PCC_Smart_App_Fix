package com.example.pccsmartapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.pccsmartapp.ui_anggota.listevent.ListEventFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class TrackingJalur extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private EditText editTextStart;
    private EditText editTextDestination;
    private Button buttonSearch;
    private boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_jalur);




        editTextStart = findViewById(R.id.titikawal);
        editTextDestination = findViewById(R.id.tujuan);
        buttonSearch = findViewById(R.id.lihatrute);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startLocation = editTextStart.getText().toString();
                String destinationLocation = editTextDestination.getText().toString();

                processDirections(startLocation, destinationLocation);
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapsevent);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
       isMapReady = true;
    }
    private void processDirections(String startLocation, String destinationLocation) {
        DirectionsTask directionsTask = new DirectionsTask();
        directionsTask.execute(startLocation, destinationLocation);
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

            // Creating an array of com.google.maps.model.LatLng from List<LatLng>
            com.google.maps.model.LatLng[] latLngArray = new com.google.maps.model.LatLng[coordinates.size()];
            for (int i = 0; i < coordinates.size(); i++) {
                LatLng latLng = coordinates.get(i);
                latLngArray[i] = new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude);
            }

            // Creating a request for the Maps Elevation API
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyARwUqqEC-Fn9rHk9RWm5pr1SBSAPL1yeM")
                    .build();

            try {
                // Getting the elevation values from the response
                ElevationResult[] elevationResults = ElevationApi.getByPoints(geoApiContext, latLngArray).await();
                if (elevationResults != null) {
                    for (int i = 0; i < elevationResults.length; i++) {
                        double elevation = elevationResults[i].elevation;

                        // Setting polyline color based on elevation
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
            Log.d("ElevationApi", "Elevation: " + elevation);
            if (elevation < 600) {
                return Color.RED;
            } else if (elevation < 700) {
                return Color.YELLOW;
            } else {
                return Color.GREEN;
            }
        }
    }
}