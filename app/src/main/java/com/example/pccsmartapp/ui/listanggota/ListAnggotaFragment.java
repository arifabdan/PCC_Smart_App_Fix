package com.example.pccsmartapp.ui.listanggota;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pccsmartapp.databinding.FragmentListAnggotaBinding;

public class ListAnggotaFragment extends Fragment {

    private FragmentListAnggotaBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListAnggotaViewModel listAnggotaViewModel =
                new ViewModelProvider(this).get(ListAnggotaViewModel.class);

        binding = FragmentListAnggotaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textListAnggota;
        listAnggotaViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}