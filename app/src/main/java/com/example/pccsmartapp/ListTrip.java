package com.example.pccsmartapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListTrip extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TripAdapter tripAdapter;
    private DatabaseReference eventRef;
    private String selectedEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_trip);
        // Mendapatkan event ID yang dipilih (misalnya dari intent)
        Intent intent = getIntent();
        if (intent != null) {
            selectedEventId = intent.getStringExtra("selectedEventId");
        }

        recyclerView = findViewById(R.id.list_trip);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Referensi ke node trip dalam event yang dipilih
        eventRef = FirebaseDatabase.getInstance().getReference().child("Event").child(selectedEventId).child("Trip");

        // Membuat opsi untuk adapter FirebaseRecyclerAdapter
        FirebaseRecyclerOptions<Trip> options =
                new FirebaseRecyclerOptions.Builder<Trip>()
                        .setQuery(eventRef, Trip.class)
                        .build();

        // Membuat adapter untuk RecyclerView
        tripAdapter = new TripAdapter(options);

        // Mengatur adapter ke RecyclerView
        recyclerView.setAdapter(tripAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Memulai adapter FirebaseRecyclerAdapter
        tripAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Menghentikan adapter FirebaseRecyclerAdapter
        tripAdapter.stopListening();
    }
}