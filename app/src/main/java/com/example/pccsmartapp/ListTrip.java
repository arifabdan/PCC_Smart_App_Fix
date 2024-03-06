package com.example.pccsmartapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

        tripAdapter = new TripAdapter(options, this, selectedEventId);
        recyclerView.setAdapter(tripAdapter);

        FirebaseDatabase.getInstance().getReference().child("Event").child(selectedEventId).child("Trip").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                tripAdapter.notifyDataSetChanged(); // Memperbarui adapter saat ada penambahan data baru
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                tripAdapter.notifyDataSetChanged(); // Memperbarui adapter saat ada perubahan data
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                tripAdapter.notifyDataSetChanged(); // Memperbarui adapter saat ada penghapusan data
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Tidak melakukan apa pun saat ada perubahan urutan data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error handling
            }
        });

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