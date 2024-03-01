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
private Button Lihat,Edit,Delete;
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
        private Button pantauEventButton;
        private Button daftarDiriButton;
        private LocationManager locationManager;
        private LocationListener locationListener;
        private Context context;



        public EventViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            namaTextView = itemView.findViewById(R.id.namaTextView);
            faseTxt = itemView.findViewById(R.id.fasetxt);
            startTxt = itemView.findViewById(R.id.StartTextView);
            finishTxt = itemView.findViewById(R.id.FinishTextView);
            TanggalTxt = itemView.findViewById(R.id.TanggalTextView);
            pantauEventButton = itemView.findViewById(R.id.pantau_event_button);
            daftarDiriButton = itemView.findViewById(R.id.daftar_diri_button);
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
            faseTxt.setText("Fase : " + event.getFase());
            startTxt.setText("Lokasi Start : " + event.getStart());
            finishTxt.setText("Lokasi Finish : " + event.getFinish());
            TanggalTxt.setText("Tanggal Event : " + event.getEventDate());
            pantauEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Buka halaman baru yang berisi peta dengan polylines
                    Intent intent = new Intent(context, PantauEvent.class);
                    intent.putExtra("eventId", eventId);
                    context.startActivity(intent);

                }
            });

            daftarDiriButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Memeriksa apakah pengguna sudah login
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(context, "Gagal mendaftar! Harap login terlebih dahulu.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Dapatkan data pengguna yang sesungguhnya dari Firebase Authentication
                    String userId = currentUser.getUid();

                    // Mendapatkan posisi adapter dan data acara
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        Toast.makeText(context, "Gagal mendaftar! Tidak ada item yang dipilih.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Event event = getItem(position);
                    if (event == null) {
                        Toast.makeText(context, "Gagal mendaftar! Data acara tidak valid.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String eventId = event.getId();
                    if (eventId == null || eventId.isEmpty()) {
                        Toast.makeText(context, "Gagal mendaftar! ID acara tidak valid.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Dapatkan lokasi terkini
                    @SuppressLint("MissingPermission") Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation == null) {
                        Toast.makeText(context, "Gagal mendapatkan lokasi. Pastikan GPS Anda aktif.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Memperoleh data lokasi
                    double latitude = lastLocation.getLatitude();
                    double longitude = lastLocation.getLongitude();

                    // Dapatkan referensi ke data pengguna di Firebase Database
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Data pengguna tersedia, Anda bisa mengambil username di sini
                                String username = dataSnapshot.child("username").getValue(String.class);

                                // Mendapatkan referensi ke event di Firebase Database
                                DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("Event").child(eventId);
                                // Mendapatkan referensi ke node anggota event di bawah event yang bersangkutan
                                DatabaseReference memberRef = eventRef.child("peserta").child(userId);

                                // Simpan data pengguna ke dalam node anggota event
                                memberRef.child("username").setValue(username);
                                memberRef.child("latitude").setValue(latitude);
                                memberRef.child("longitude").setValue(longitude);

                                Toast.makeText(context, "Berhasil mendaftar!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Data pengguna tidak tersedia di Firebase Database
                                Toast.makeText(context, "Gagal mendaftar! Data pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Penanganan kesalahan
                        }
                    });
                }
            });

        }

    }
}
