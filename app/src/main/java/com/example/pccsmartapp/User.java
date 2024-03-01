package com.example.pccsmartapp;

public class User {
    String email, username, tahungbg, role, password;

    public User(){

    }
    public User(String email, String username, String tahungbg, String role, String password){
        this.email = email;
        this.username = username;
        this.tahungbg = tahungbg;
        this.role = role;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTahunGabung() {
        return tahungbg;
    }

    public void setTahunGabung(String tahungbg) {
        this.tahungbg = tahungbg;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
