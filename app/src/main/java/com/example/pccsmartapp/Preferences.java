package com.example.pccsmartapp;
import android.content.Context;
import android.content.SharedPreferences;


public class Preferences {
    private static final String PREF_NAME = "YourAppPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ROLE = "userRole";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public Preferences(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setUserRole(String role) {
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "");
    }
}