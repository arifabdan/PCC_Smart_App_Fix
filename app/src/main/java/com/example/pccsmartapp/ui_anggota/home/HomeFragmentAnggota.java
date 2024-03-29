package com.example.pccsmartapp.ui_anggota.home;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pccsmartapp.LihatAnggota1;
import com.example.pccsmartapp.R;


import com.example.pccsmartapp.TrackingJalur;
import com.example.pccsmartapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragmentAnggota extends Fragment {

    private FragmentHomeBinding binding;
    private Button lihatanggota,tracking;
    private TextView remindertxt,greettxt, namatxt, starttxt, finishtxt;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userReference;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));


        greettxt = root.findViewById(R.id.greetingstxt);
        remindertxt = root.findViewById(R.id.remindertxt);
        namatxt = root.findViewById(R.id.namaeventtxt);
        starttxt = root.findViewById(R.id.starttxt);
        finishtxt = root.findViewById(R.id.finishtxt);



        firebaseAuth = FirebaseAuth.getInstance();

        // Cek apakah pengguna sudah login
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Pengguna sudah login, dapatkan UID pengguna
            String uid = currentUser.getUid();

            // Inisialisasi DatabaseReference ke data pengguna di Firebase Database
            userReference = FirebaseDatabase.getInstance().getReference("users").child(uid);

            // Ambil data pengguna dari Firebase Database
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Dapatkan nama pengguna dari dataSnapshot
                        String username = dataSnapshot.child("username").getValue(String.class);

                        // Tampilkan pesan selamat datang dengan nama pengguna
                        if (username != null && !username.isEmpty()) {
                            greettxt.setText("Selamat datang, " + username + "!");
                        } else {
                            greettxt.setText("Selamat datang!");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle kesalahan jika diperlukan
                }
            });
        }


        lihatanggota = root.findViewById(R.id.button1);
        lihatanggota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LihatAnggota1.class);
                startActivity(intent);
            }
        });

        tracking = root.findViewById(R.id.button2);
        tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TrackingJalur.class);
                startActivity(intent);
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Event");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long currentDateInMillis = System.currentTimeMillis();
                    long closestDateDifference = Long.MAX_VALUE;
                    String closestDate = "";
                    String NamaEvent = "";
                    String Start = "";
                    String Finish = "";



                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String tanggal = snapshot.child("eventDate").getValue(String.class);
                        String Nama = snapshot.child("eventName").getValue(String.class);
                        String start = snapshot.child("start").getValue(String.class);
                        String finish = snapshot.child("finish").getValue(String.class);

                        long dateInMillis = convertDateToMillis(tanggal);

                        // Pastikan tanggal tidak kurang dari tanggal saat ini
                        if (dateInMillis >= currentDateInMillis) {
                            long dateDifference = dateInMillis - currentDateInMillis;

                            // Jika tanggal lebih dekat, atau tanggal sama tetapi lebih awal
                            if (dateDifference < closestDateDifference || (dateDifference == closestDateDifference && dateInMillis < convertDateToMillis(closestDate))) {
                                closestDateDifference = dateDifference;
                                closestDate = tanggal;
                                NamaEvent = Nama;
                                Start = start;
                                Finish = finish;
                            }
                        }
                    }

                    if (!closestDate.isEmpty()) {
                        remindertxt.setText("Reminder Tanggal Event: " + closestDate);
                        namatxt.setText("Nama Event : " + NamaEvent);
                        starttxt.setText("Start : " + Start);
                        finishtxt.setText("Finish: " + Finish);

                    } else {
                        remindertxt.setText("Tidak ada acara mendatang");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle kesalahan jika diperlukan
            }
        });

        return root;
    }
    private long convertDateToMillis(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date parsedDate = sdf.parse(date);
            return parsedDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
