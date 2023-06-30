package com.example.pccsmartapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomeActivity extends AppCompatActivity {
    private SensorManager sensorManager;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private View homeView, listanggotaView, listeventView, profileView;
    private HomeController homeController;
    private ListAnggotaController listAnggotaController;
    private ListEventController listEventController;
    private ProfileController profileController;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.navbar);
        frameLayout = findViewById(R.id.frame_container);
        homeView = getLayoutInflater().inflate(R.layout.layout_home, frameLayout, false);
        listanggotaView = getLayoutInflater().inflate(R.layout.layout_list_anggota, frameLayout, false);
        listeventView = getLayoutInflater().inflate(R.layout.layout_list_event, frameLayout, false);
        profileView = getLayoutInflater().inflate(R.layout.layout_profile, frameLayout, false);

        homeController = new HomeController(HomeActivity.this);
        listAnggotaController = new ListAnggotaController();
        listEventController = new ListEventController();
        profileController = new ProfileController();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        showView(homeView);
                        homeController.setHomeView(homeView, HomeActivity.this);
                        homeController.homeFuction();
                        return true;
                    case R.id.nav_listanggota:
                        showView(listanggotaView);
                        listAnggotaController.listAnggotaFunction();
                        return true;
                    case R.id.nav_listevent:
                        showView(listeventView);
                        listEventController.listEventFunction();
                        return true;
                    case R.id.nav_profile:
                        showView(profileView);
                        profileController.profileFunction();
                        return true;
                }
                return false;
            }
        });

        showView (homeView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (sensorManager != null){
            sensorManager.unregisterListener(listener);
        }
    }
    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float xValue = Math.abs(event.values[0]);
            float yValue = Math.abs(event.values[1]);
            float zValue = Math.abs(event.values[2]);
            if (xValue > 15 || yValue > 15 || zValue > 15){
                Toast.makeText(HomeActivity.this, "shake fuction activated" , Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void showView(View viewPage){
        frameLayout.removeAllViews();
        frameLayout.addView(viewPage);
    }
}