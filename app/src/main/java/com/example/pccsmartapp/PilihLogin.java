package com.example.pccsmartapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PilihLogin extends AppCompatActivity {
Button toLoginPage;
EditText tokentxt ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_regis);
        getSupportActionBar().hide();
        toLoginPage = findViewById(R.id.toLoginPage);

        toLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PilihLogin.this, Login.class);
                startActivity(i);
            }
        });

    }
}