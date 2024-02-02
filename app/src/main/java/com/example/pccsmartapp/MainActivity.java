package com.example.pccsmartapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 5000; // Durasi splash screen dalam milidetik
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSessionAndNavigate();
            }
        }, SPLASH_DURATION);
    }

    private void checkSessionAndNavigate() {
        Preferences preferences = new Preferences(this);

        if (preferences.isLoggedIn()) {
            String userRole = preferences.getUserRole();

            // Start the appropriate activity based on the user's role
            if (userRole.equals("Staff")) {
                startActivity(new Intent(MainActivity.this, HomeActivityStaff.class));
            } else if (userRole.equals("Anggota")) {
                startActivity(new Intent(MainActivity.this, HomeActivityAnggota.class));
            }

            finish(); // Finish the current activity to prevent going back to login
        } else {
            // Jika tidak ada sesi, navigasikan ke aktivitas login atau aktivitas lainnya
            startActivity(new Intent(MainActivity.this, Login.class));
            finish(); // Finish the current activity after navigating to login
        }
    }
}
