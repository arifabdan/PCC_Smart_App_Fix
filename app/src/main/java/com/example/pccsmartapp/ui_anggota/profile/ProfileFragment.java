package com.example.pccsmartapp.ui_anggota.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.pccsmartapp.Login;
import com.example.pccsmartapp.R;
import com.example.pccsmartapp.Preferences;
import com.example.pccsmartapp.User;
import com.example.pccsmartapp.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Button logoutbutton;
    private TextView nama, email, tahungabung;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        nama = root.findViewById(R.id.tvNama);
        email = root.findViewById(R.id.tvEmail);
        tahungabung = root.findViewById(R.id.tvTahunGabung);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);

                    if (user != null) {
                        nama.setText("Nama: " + user.getUsername());
                        email.setText("Email: " + user.getEmail());
                        tahungabung.setText("Tahun Gabung: " + user.getTahunGabung());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            // Pengguna belum login, atur teks atau tindakan sesuai kebutuhan Anda
            nama.setText("Anda belum login");
            email.setText("");
            tahungabung.setText("");
        }

        logoutbutton = root.findViewById(R.id.logoutbtn);

        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        return root;
    }
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        Preferences preferences = new Preferences(requireContext());
        preferences.setLogin(false);

        Intent intent = new Intent(requireActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}