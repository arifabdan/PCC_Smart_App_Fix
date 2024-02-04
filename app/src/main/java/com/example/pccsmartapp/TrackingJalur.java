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
import com.google.android.gms.common.api.ApiException;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrackingJalur extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private EditText editTextStart;
    private EditText editTextDestination;
    private Button buttonSearch;
    private AtomicBoolean isMapReady = new AtomicBoolean(false);
    private ElevationResult[] elevationResults;
    private List<Polyline> polylineList = new ArrayList<>();
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
        isMapReady.set(true);
        mMap = googleMap;
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

            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBa2hl7Po3V-ALGMmIdbV_mUdDIS82sHUw")
                    .build();

            DirectionsApiRequest directionsApiRequest = DirectionsApi.getDirections(geoApiContext,
                    startLocation, destinationLocation);
            directionsApiRequest.mode(TravelMode.WALKING);

            try {
                DirectionsResult directionsResult = directionsApiRequest.await();

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
            if (!isMapReady.get()) {
                Log.e("DirectionsTask", "Map is not ready yet.");
                return;
            }

            clearPolylines();

            List<LatLng> coordinates = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            try {
                // Creating a request for the Maps Elevation API
                GeoApiContext geoApiContext = new GeoApiContext.Builder()
                        .apiKey("AIzaSyDkKvzMxiKerYMBZuUr4sFulKIb_ieIV7c")
                        .build();

                // Getting the elevation values from the response
                com.google.maps.model.LatLng[] latLngArray = getLatLngArray(directionsResult);
                ElevationResult[] elevationResults = ElevationApi.getByPoints(geoApiContext, latLngArray).await();

                for (int i = 0; i < directionsResult.routes.length; i++) {
                    com.google.maps.model.LatLng[] path = directionsResult.routes[i].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);

                    // Iterasi melalui setiap titik pada rute
                    for (int j = 0; j < path.length; j++) {
                        LatLng coordinate = new LatLng(path[j].lat, path[j].lng);
                        coordinates.add(coordinate);

                        // Periksa apakah elevationResults tidak null dan indeks valid
                        if (elevationResults != null && j < elevationResults.length) {
                            double elevation = elevationResults[j].elevation;
                            int color = getColorByElevationAndSlope(elevation, getKemiringanJalan(j, path, elevationResults));
                            colors.add(color);
                        } else {
                            // Jika data elevasi tidak tersedia, gunakan warna default
                            colors.add(Color.GRAY);
                        }
                    }
                }

                // Drawing polylines with corresponding colors
                int start = 0;
                int end = 0;
                int zIndex = 1;

                while (end < coordinates.size()) {
                    int currentColor = colors.get(end);
                    while (end < coordinates.size() && colors.get(end) == currentColor) {
                        end++;
                    }

                    List<LatLng> segment = coordinates.subList(start, end);

                    PolylineOptions polylineOptions = new PolylineOptions().addAll(segment);
                    polylineOptions.color(currentColor);
                    polylineOptions.zIndex(zIndex++);
                    polylineOptions.width(8);

                    Polyline segmentPolyline = mMap.addPolyline(polylineOptions);
                    polylineList.add(segmentPolyline);

                    start = end;
                }

                // Set camera bounds based on coordinates
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

            // Iterate through the routes
            for (int i = 0; i < directionsResult.routes.length; i++) {
                com.google.maps.model.LatLng[] path = directionsResult.routes[i].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);

                // Iterate through each point on the route
                for (int j = 0; j < path.length; j++) {
                    Log.d("PathPoint", "Lat: " + path[j].lat + ", Lng: " + path[j].lng);
                    latLngList.add(path[j]);
                }
            }

            return latLngList.toArray(new com.google.maps.model.LatLng[0]);
        }

        private double getKemiringanJalan(int currentIndex, com.google.maps.model.LatLng[] path, ElevationResult[] elevationResults) {
            if (currentIndex > 0 && currentIndex < path.length - 1) {
                double jarakKemiringan = hitungJarak(path[currentIndex - 1], path[currentIndex + 1]);
                double kemiringanElevasi = elevationResults[currentIndex + 1].elevation - elevationResults[currentIndex - 1].elevation;
                return kemiringanElevasi / jarakKemiringan;
            } else {
                return 0.0;
            }
        }

        private double hitungJarak(com.google.maps.model.LatLng titik1, com.google.maps.model.LatLng titik2) {
            double radiusBumi = 6371000;
            double dLat = Math.toRadians(titik2.lat - titik1.lat);
            double dLng = Math.toRadians(titik2.lng - titik1.lng);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(titik1.lat)) * Math.cos(Math.toRadians(titik2.lat)) *
                            Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return radiusBumi * c;
        }

        private int getColorByElevationAndSlope(double elevation, double slope) {
            // Adjust color based on elevation and slope
            Log.d("ElevationApi", "Elevation: " + elevation + ", Slope: " + slope);
            double slopeIncrement = 0.01;

            if (slope < slopeIncrement) {
                return Color.rgb(0, 255, 0);
            } else if (slope < slopeIncrement * 2) {
                return Color.rgb(255, 255, 0);
            } else if (slope < slopeIncrement * 5) {
                return Color.rgb(255, 165, 0);
            } else {
                return Color.rgb(255, 0, 0);
            }
        }
    }
}