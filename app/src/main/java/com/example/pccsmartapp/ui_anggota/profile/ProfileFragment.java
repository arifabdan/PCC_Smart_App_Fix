package com.example.pccsmartapp.ui_anggota.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pccsmartapp.Login;
import com.example.pccsmartapp.LoginOrRegis;
import com.example.pccsmartapp.R;
import com.example.pccsmartapp.databinding.FragmentNotificationsBinding;
import com.example.pccsmartapp.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Button logoutbutton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
        // Hapus informasi sesi login dari SharedPreferences
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        editor.remove("isLoggedIn");
        editor.remove("role");
        editor.apply();

        // Log out dari Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // Buka kembali halaman login
        Intent intent = new Intent(requireActivity(), Login.class);
        startActivity(intent);
        requireActivity().finish();
    }
}