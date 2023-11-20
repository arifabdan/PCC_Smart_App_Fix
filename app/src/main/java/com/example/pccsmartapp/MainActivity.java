package com.example.pccsmartapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSessionAndNavigate();
            }
        }, 5000);
    }
    private void checkSessionAndNavigate() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Pengguna sudah login, arahkan ke halaman beranda berdasarkan rolenya
            String role = prefs.getString("role", "");
            redirectToHome(role);
        } else {
            // Pengguna belum login, arahkan ke halaman login
            Intent intent = new Intent(MainActivity.this, LoginOrRegis.class);
            startActivity(intent);
            finish();
        }
    }

    private void redirectToHome(String role) {
        Intent intent;
        if (role.equals("Staff")) {
            intent = new Intent(MainActivity.this, HomeActivityStaff.class);
        } else if (role.equals("Anggota")) {
            intent = new Intent(MainActivity.this, HomeActivityAnggota.class);
        } else {
            // Role tidak dikenali, arahkan ke halaman login
            intent = new Intent(MainActivity.this, LoginOrRegis.class);
        }

        startActivity(intent);
        finish();
    }
}