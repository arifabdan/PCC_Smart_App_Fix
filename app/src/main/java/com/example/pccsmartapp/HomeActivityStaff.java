package com.example.pccsmartapp;




import static com.example.pccsmartapp.databinding.ActivityHome1Binding.inflate;


import android.os.Bundle;


import com.example.pccsmartapp.databinding.ActivityHome1Binding;
import com.example.pccsmartapp.ui_anggota.home.HomeFragmentAnggota;
import com.example.pccsmartapp.ui_staff.home.HomeFragmentStaff;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;



public class HomeActivityStaff extends AppCompatActivity {

    private ActivityHome1Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        HomeFragmentStaff fragmentStaff = new HomeFragmentStaff();
        HomeFragmentAnggota fragmentAnggota = new HomeFragmentAnggota();


        binding = inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        BottomNavigationView navView = findViewById(R.id.nav_view_1);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_list_anggota, R.id.navigation_list_event)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView1, navController);
    }

}