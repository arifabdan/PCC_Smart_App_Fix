package com.example.pccsmartapp.ui_anggota.listanggota;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pccsmartapp.AnggotaAdapter;
import com.example.pccsmartapp.R;
import com.example.pccsmartapp.User;
import com.example.pccsmartapp.databinding.FragmentListAnggotaBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ListAnggotaFragment extends Fragment {

    private FragmentListAnggotaBinding binding;
    private AnggotaAdapter anggotaAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListAnggotaViewModel listAnggotaViewModel =
                new ViewModelProvider(this).get(ListAnggotaViewModel.class);

        binding = FragmentListAnggotaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Setup FirebaseRecyclerOptions
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(database.getReference("users"), User.class)
                        .build();

        // Initialize MemberAdapter
        anggotaAdapter = new AnggotaAdapter(options);
        recyclerView.setAdapter(anggotaAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
            anggotaAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
            anggotaAdapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}