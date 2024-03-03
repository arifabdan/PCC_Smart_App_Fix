package com.example.pccsmartapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class EventAdapter extends FirebaseRecyclerAdapter<Event, EventAdapter.EventViewHolder> {
private DatabaseReference databaseRef;
private FirebaseDatabase firebaseDatabase;
    private List<Event> eventList;
    private Context context;


    public EventAdapter(@NonNull FirebaseRecyclerOptions<Event> options, Context context) {
        super(options);
        this.context = context;
        this.eventList = eventList;
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Event");
    }

    public EventAdapter(FirebaseRecyclerOptions<Event> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull EventViewHolder holder, int position, @NonNull Event model) {
        DatabaseReference ref = getRef(position);
        // Dapatkan ID dari referensi Firebase
        String eventId = ref.getKey();
        // Kemudian Anda dapat menggunakan ID event ini sesuai kebutuhan Anda
        holder.bind(model, eventId);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder
        Context context = parent.getContext(); // Dapatkan context dari parent
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view, context); // Panggil konstruktor dengan dua parameter
    }

    // ViewHolder untuk menangani tampilan setiap item di RecyclerView
    public class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView namaTextView,faseTxt,startTxt,finishTxt,TanggalTxt;
        private Button LihatEventButton, TambahTripButton;
        private LocationManager locationManager;
        private LocationListener locationListener;
        private Context context;



        public EventViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            namaTextView = itemView.findViewById(R.id.namaTextView);
            startTxt = itemView.findViewById(R.id.StartTextView);
            finishTxt = itemView.findViewById(R.id.FinishTextView);
            TanggalTxt = itemView.findViewById(R.id.TanggalTextView);
            LihatEventButton = itemView.findViewById(R.id.lihat_event_button);
            TambahTripButton = itemView.findViewById(R.id.tambah_trip_button);
            locationManager = (LocationManager) itemView.getContext().getSystemService(Context.LOCATION_SERVICE);
            // Inisialisasi LocationListener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            } else {
                // Jika izin tidak diberikan, Anda dapat meminta izin kepada pengguna di sini
                // Atau memberikan pesan kepada pengguna tentang pentingnya izin lokasi
            }
        }

        public void bind(Event event, String eventId) {
            namaTextView.setText("Nama Event : " + event.getEventName());
            TanggalTxt.setText("Tanggal Event : " + event.getEventDate());
            LihatEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Buka halaman baru yang menampilkan daftar trip dari event yang dipilih
                    Intent intent = new Intent(context, ListTrip.class);
                    intent.putExtra("selectedEventId", eventId); // Mengirim eventId ke PantauEve
                    context.startActivity(intent);
                }
            });

            TambahTripButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, Buat_Trip.class);
                    intent.putExtra("selectedEventId", eventId);
                    context.startActivity(intent);
                }
            });

        }

    }
}
