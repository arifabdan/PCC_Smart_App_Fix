package com.example.pccsmartapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registrasi extends AppCompatActivity {

    private EditText emailtxt, usernametxt, tahungbgtxt, passwordtxt;
    private Spinner rolespin;
    private Button kembalibtn, regisbtn;
    private String[] role = {"Anggota", "Staff"};

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        emailtxt = findViewById(R.id.emailEditText);
        usernametxt = findViewById(R.id.usernameEditText);
        tahungbgtxt = findViewById(R.id.tahungbgEditText);
        rolespin = findViewById(R.id.roleSpinner);
        passwordtxt = findViewById(R.id.passwordEditText);
        kembalibtn = findViewById(R.id.kembali);
        regisbtn = findViewById(R.id.registrasi);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, role);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rolespin.setAdapter(adapter);

        kembalibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Registrasi.this, LoginOrRegis.class);
                startActivity(i);
            }
        });

        regisbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailtxt.getText().toString().trim();
                String username = usernametxt.getText().toString().trim();
                String tahungbg = tahungbgtxt.getText().toString().trim();
                String role = rolespin.getSelectedItem().toString();
                String password = passwordtxt.getText().toString().trim();

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Registrasi.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null){
                                        String userID = user.getUid();
                                        saveData(userID, email, username, tahungbg, role, password);
                                    }
                                } else {
                                    Toast.makeText(Registrasi.this, "Registrasi Gagal", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            private void saveData (String userID, String email, String username, String tahungbg, String role, String password){
                User user = new User(email, username, tahungbg, role, password);

                databaseReference.child("users").child(userID).setValue(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Registrasi.this, "Registrasi Berhasil", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(Registrasi.this, Login.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(Registrasi.this, "Registrasi Gagal", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}