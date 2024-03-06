package com.example.pccsmartapp;

import static android.content.ContentValues.TAG;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

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

import com.example.pccsmartapp.ui_anggota.listevent.ListEventFragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

public class TambahEvent extends AppCompatActivity {
    private EditText namatxt, deskripsi;
    private DatePicker tanggal;
    private Button tambahEvent;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_event);


        mDatabase = FirebaseDatabase.getInstance().getReference();


        namatxt = findViewById(R.id.namaevent);
        deskripsi = findViewById(R.id.deskripsi);
        tanggal = findViewById(R.id.tanggalPicker);
        tambahEvent = findViewById(R.id.simpanevent);


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

                saveEventData(eventName, eventDescription, eventDate);

            }
        });

    }


    // Metode untuk menyimpan data event ke Firebase Database
// Metode untuk menyimpan data event ke Firebase Database
    private void saveEventData(String eventName, String description, String eventDate) {
        DatabaseReference eventRef = mDatabase.child("Event");

        // Mengambil jumlah event saat ini untuk membuat ID berurutan
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long eventCount = dataSnapshot.getChildrenCount();

                // Membuat ID berurutan dengan menambahkan jumlah event saat ini
                String eventId = String.valueOf(eventCount + 1);

                Event event = new Event(eventId, eventName, description, eventDate);

                // Simpan data event beserta ID-nya ke Firebase Database
                eventRef.child(eventId).setValue(event)
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Error handling jika pembacaan data gagal
                Toast.makeText(TambahEvent.this, "Gagal membaca data", Toast.LENGTH_SHORT).show();
            }
        });
    }


}