package com.example.pccsmartapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText emailtxt, passwordtxt;
    private Button kembalibtn, loginbtn;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        emailtxt = findViewById(R.id.emailEditText);
        passwordtxt = findViewById(R.id.passwordEditText);
        kembalibtn = findViewById(R.id.kembali);
        loginbtn = findViewById(R.id.login);

        kembalibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, LoginOrRegis.class);
                startActivity(i);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailtxt.getText().toString().trim();
                String password = passwordtxt.getText().toString().trim();

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      if(task.isSuccessful()){
                          FirebaseUser user = firebaseAuth.getCurrentUser();

                          DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                          userRef.addValueEventListener(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot snapshot) {
                                  String role = snapshot.child("role").getValue(String.class);

                                  if (role.equals("Staff")){
                                      Intent staffIntent = new Intent(Login.this, HomeActivityStaff.class);
                                      startActivity(staffIntent);
                                  } else if (role.equals("Anggota")){
                                      Intent anggotaIntent = new Intent(Login.this, HomeActivityAnggota.class);
                                      startActivity(anggotaIntent);
                                  }
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError error) {

                              }
                          });
                      } else {
                          Toast.makeText(Login.this, "Login Gagal", Toast.LENGTH_SHORT).show();
                      }
                    }
                });
            }
        });
        checkSession();
    }

    private void saveSession(boolean isLoggedIn, String role) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.putString("role", role);
        editor.apply();
    }

    private void checkSession() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String role = prefs.getString("role", "");
            if (role.equals("Staff")) {
                Intent staffIntent = new Intent(Login.this, HomeActivityStaff.class);
                startActivity(staffIntent);
                finish();
            } else if (role.equals("Anggota")) {
                Intent anggotaIntent = new Intent(Login.this, HomeActivityAnggota.class);
                startActivity(anggotaIntent);
                finish();
            }
        }
    }

}