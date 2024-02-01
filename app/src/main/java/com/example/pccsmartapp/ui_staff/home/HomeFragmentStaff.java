package com.example.pccsmartapp.ui_staff.home;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pccsmartapp.LihatAnggota2;


import com.example.pccsmartapp.R;
import com.example.pccsmartapp.databinding.FragmentHome1Binding;

public class HomeFragmentStaff extends Fragment {

    private FragmentHome1Binding binding;
    private Button lihatanggota;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHome1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Intent serviceI = new Intent(getActivity(), MyService.class);
        getActivity().startService(serviceI);

        lihatanggota = root.findViewById(R.id.button1);
        lihatanggota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LihatAnggota2.class);
                startActivity(intent);
            }
        });

        return root;
    }

}