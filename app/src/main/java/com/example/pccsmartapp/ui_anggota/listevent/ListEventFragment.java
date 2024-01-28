package com.example.pccsmartapp.ui_anggota.listevent;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pccsmartapp.R;
import com.example.pccsmartapp.databinding.FragmentListEventBinding;
import com.example.pccsmartapp.ui_staff.listevent.ListEventViewModel;
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
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class ListEventFragment extends Fragment implements OnMapReadyCallback {

    private FragmentListEventBinding binding;
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private EditText editTextStart;
    private EditText editTextDestination;
    private Button buttonSearch;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListEventViewModel listEventViewModel =
                new ViewModelProvider(this).get(ListEventViewModel.class);

        binding = FragmentListEventBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapsevent);
        mapFragment.getMapAsync(this);

        editTextStart = root.findViewById(R.id.titikawal);
        editTextDestination = root.findViewById(R.id.tujuan);
        buttonSearch = root.findViewById(R.id.lihatrute);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startLocation = editTextStart.getText().toString();
                String destinationLocation = editTextDestination.getText().toString();

                processDirections(startLocation, destinationLocation);
            }
        });


        return root;
    }
    private void processDirections(String startLocation, String destinationLocation) {
        com.example.pccsmartapp.ui_anggota.listevent.ListEventFragment.DirectionsTask directionsTask = new com.example.pccsmartapp.ui_anggota.listevent.ListEventFragment.DirectionsTask();
        directionsTask.execute(startLocation, destinationLocation);
    }

    @Override
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}