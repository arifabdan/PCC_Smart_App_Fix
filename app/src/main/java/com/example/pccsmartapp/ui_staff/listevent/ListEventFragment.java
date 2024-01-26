package com.example.pccsmartapp.ui_staff.listevent;


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
import com.example.pccsmartapp.databinding.FragmentListEvent1Binding;
import com.example.pccsmartapp.databinding.FragmentListEventBinding;
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

public class ListEventFragment extends Fragment{

    private FragmentListEvent1Binding binding;
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private EditText editTextStart;
    private EditText editTextDestination;
    private Button buttonSearch;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListEventViewModel listEventViewModel =
                new ViewModelProvider(this).get(ListEventViewModel.class);

        binding = FragmentListEvent1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}