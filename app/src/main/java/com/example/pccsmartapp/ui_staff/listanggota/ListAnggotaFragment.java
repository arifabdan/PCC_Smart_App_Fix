package com.example.pccsmartapp.ui_staff.listanggota;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pccsmartapp.R;
import com.example.pccsmartapp.Registrasi;
import com.example.pccsmartapp.databinding.FragmentListAnggota2Binding;
import com.example.pccsmartapp.databinding.FragmentListAnggotaBinding;

public class ListAnggotaFragment extends Fragment {

    private FragmentListAnggota2Binding binding;
    private Button tambahanggota;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListAnggotaViewModel listAnggotaViewModel =
                new ViewModelProvider(this).get(ListAnggotaViewModel.class);

        binding = FragmentListAnggota2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tambahanggota = root.findViewById(R.id.tambahanggota);
        tambahanggota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Registrasi.class);
                startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}