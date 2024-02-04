package com.example.pccsmartapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

public class EventAdapter extends FirebaseRecyclerAdapter<Event, EventAdapter.EventViewHolder> {
private Button Lihat,Edit,Delete;
    public EventAdapter(@NonNull FirebaseRecyclerOptions<Event> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull EventViewHolder holder, int position, @NonNull Event model) {
        // Bind data to ViewHolder
        holder.bind(model);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);

    }

    // ViewHolder untuk menangani tampilan setiap item di RecyclerView
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView namaTextView,startTxt,finishTxt,TanggalTxt;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            namaTextView = itemView.findViewById(R.id.namaTextView);
            startTxt = itemView.findViewById(R.id.StartTextView);
            finishTxt = itemView.findViewById(R.id.FinishTextView);
            TanggalTxt = itemView.findViewById(R.id.TanggalTextView);
        }

        public void bind(Event event) {
            namaTextView.setText(event.getEventName());
            startTxt.setText(event.getStart());
            finishTxt.setText(event.getFinish());
            TanggalTxt.setText(event.getEventDate());
        }

    }
}
