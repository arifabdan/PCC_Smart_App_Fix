package com.example.pccsmartapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class TambahEvent extends AppCompatActivity {
    private EditText namatxt, start, finish, deskripsi;
    private DatePicker tanggal;
    private Button tambahEvent;
    private DatabaseReference mDatabase;

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
}