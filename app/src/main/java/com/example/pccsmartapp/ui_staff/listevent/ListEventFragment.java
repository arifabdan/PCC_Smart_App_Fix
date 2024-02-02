package com.example.pccsmartapp.ui_staff.listevent;


import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pccsmartapp.AnggotaAdapter;
import com.example.pccsmartapp.Event;
import com.example.pccsmartapp.EventAdapter;
import com.example.pccsmartapp.R;
import com.example.pccsmartapp.Registrasi;
import com.example.pccsmartapp.TambahEvent;
import com.example.pccsmartapp.User;
import com.example.pccsmartapp.databinding.FragmentListEvent1Binding;
import com.example.pccsmartapp.databinding.FragmentListEventBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.List;

public class ListEventFragment extends Fragment{

    private FragmentListEvent1Binding binding;
    private Button tambahEvent;
    private EventAdapter eventAdapter;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListEvent1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tambahEvent = root.findViewById(R.id.tambahevent);
        tambahEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TambahEvent.class);
                startActivity(intent);
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Setup FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(database.getReference("Event").orderByChild("eventDate"), Event.class)
                        .build();

        // Initialize MemberAdapter
        eventAdapter = new EventAdapter(options);
        recyclerView.setAdapter(eventAdapter);


        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onStart() {
        super.onStart();
        eventAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventAdapter.stopListening();
    }

}