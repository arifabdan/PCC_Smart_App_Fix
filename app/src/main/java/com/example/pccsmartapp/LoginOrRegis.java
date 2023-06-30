package com.example.pccsmartapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoginOrRegis extends AppCompatActivity {
Button toLoginPage, toRegisPage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_regis);
        getSupportActionBar().hide();
        toLoginPage = findViewById(R.id.toLoginPage);
        toRegisPage = findViewById(R.id.toRegisPage);

        toLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginOrRegis.this, Login.class);
                startActivity(i);
            }
        });

        toRegisPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginOrRegis.this, Registrasi.class);
                startActivity(i);
            }
        });

    }
}