package com.example.pccsmartapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

public class LihatDetail extends AppCompatActivity {
    private EditText namatxt, starttxt, finishtxt, deskripsi, tanggaltxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_detail);
        namatxt.setEnabled(false);
        starttxt.setEnabled(false);
        finishtxt.setEnabled(false);
        deskripsi.setEnabled(false);
        tanggaltxt.setEnabled(false);

        Intent intent = getIntent();

        String eventName = intent.getStringExtra("eventName");
        String start = intent.getStringExtra("start");
        String finish = intent.getStringExtra("finish");
        String description = intent.getStringExtra("description");
        String eventDate = intent.getStringExtra("eventDate");

        namatxt = findViewById(R.id.namaevent);
        starttxt = findViewById(R.id.Start);
        finishtxt = findViewById(R.id.tujuan);
        deskripsi = findViewById(R.id.deskripsi);
        tanggaltxt = findViewById(R.id.tanggalPicker);


        namatxt.setText("Nama Event: " + eventName);
        starttxt.setText("Start: " + start);
        finishtxt.setText("Finish: " + finish);
        deskripsi.setText("Deskripsi: " + description);
        tanggaltxt.setText("Tanggal: " + eventDate);

    }
}