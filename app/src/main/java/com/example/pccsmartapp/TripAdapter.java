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

public class TripAdapter extends FirebaseRecyclerAdapter<Trip, TripAdapter.TripViewHolder> {
    private DatabaseReference databaseRef;
    private FirebaseDatabase firebaseDatabase;
    private List<Trip> tripList;
    private Context context;


    public TripAdapter(@NonNull FirebaseRecyclerOptions<Trip> options, Context context) {
        super(options);
        this.context = context;
        this.tripList = tripList;
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Event");
    }

    public TripAdapter(FirebaseRecyclerOptions<Trip> options) {
        super(options);
    }


    @Override
    protected void onBindViewHolder(@NonNull TripViewHolder holder, int position, @NonNull Trip model) {
        DatabaseReference ref = getRef(position);
        // Dapatkan ID dari referensi Firebase
        String eventId = ref.getKey();
        // Kemudian Anda dapat menggunakan ID event ini sesuai kebutuhan Anda
        holder.bind(model, eventId);
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder
        Context context = parent.getContext(); // Dapatkan context dari parent
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view, context); // Panggil konstruktor dengan dua parameter
    }

    // ViewHolder untuk menangani tampilan setiap item di RecyclerView
    public class TripViewHolder extends RecyclerView.ViewHolder {
        private TextView TripTextView, startTxt,finishTxt;
        private Button pantautripButton;
        private Button daftarDiriButton;
        private Button tambahTripButton;
        private LocationManager locationManager;
        private LocationListener locationListener;
        private Context context;



        public TripViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            TripTextView = itemView.findViewById(R.id.namaTextView);
            startTxt = itemView.findViewById(R.id.StartTextView);
            finishTxt = itemView.findViewById(R.id.FinishTextView);
            pantautripButton =itemView.findViewById(R.id.pantau_trip_button);
            daftarDiriButton = itemView.findViewById(R.id.daftar_diri_button);
            tambahTripButton = itemView.findViewById(R.id.tambah_trip_button);
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

        public void bind(Trip trip, String eventId, String tripId) {
            TripTextView.setText("Trip : " + trip.getTrip());
            startTxt.setText("Start Trip : " + trip.getStart());
            finishTxt.setText("Finish Trip : " + trip.getFinish());

            pantautripButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Buka halaman baru yang berisi peta dengan polylines
                    Intent intent = new Intent(context, PantauEvent.class);
                    intent.putExtra("tripId", tripId);
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

                    Trip trip = getItem(position);
                    if (trip == null) {
                        Toast.makeText(context, "Gagal mendaftar! Data acara tidak valid.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String tripId = Trip.getId();
                    if (tripId == null || tripId.isEmpty()) {
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
