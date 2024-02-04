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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pccsmartapp.AnggotaAdapter;
import com.example.pccsmartapp.R;
import com.example.pccsmartapp.Registrasi;
import com.example.pccsmartapp.User;
import com.example.pccsmartapp.databinding.FragmentListAnggota2Binding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ListAnggotaFragment extends Fragment {

    private FragmentListAnggota2Binding binding;
    private Button tambahanggota;
    private AnggotaAdapter anggotaAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Setup FirebaseRecyclerOptions
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(database.getReference("users").orderByChild("username"), User.class)
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