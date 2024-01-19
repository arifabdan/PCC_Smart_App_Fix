package com.example.pccsmartapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class AnggotaAdapter extends FirebaseRecyclerAdapter<User, AnggotaAdapter.AnggotaViewHolder> {

    public AnggotaAdapter(@NonNull FirebaseRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AnggotaViewHolder holder, int position, @NonNull User model) {
        // Bind data to ViewHolder
        holder.bind(model);
    }

    @NonNull
    @Override
    public AnggotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anggota, parent, false);
        return new AnggotaViewHolder(view);
    }

    // ViewHolder untuk menangani tampilan setiap item di RecyclerView
    public static class AnggotaViewHolder extends RecyclerView.ViewHolder {
        private TextView namaTextView;

        public AnggotaViewHolder(@NonNull View itemView) {
            super(itemView);
            namaTextView = itemView.findViewById(R.id.namaTextView);
        }

        public void bind(User user) {
            namaTextView.setText(user.getUsername());
        }
    }
}
