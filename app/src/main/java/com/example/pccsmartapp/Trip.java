package com.example.pccsmartapp;

public class Trip {

    private String id;
    private String trip;
    private String start;
    private String finish;

    public Trip() {

    }

    public Trip(String trip, String start, String finish) {
        this.trip = trip;
        this.start = start;
        this.finish = finish;
    }

    public Trip(String id, String trip, String start, String finish) {
        this.id = id; // Menyimpan ID event
        this.trip = trip;
        this.start = start;
        this.finish = finish;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter dan setter (sesuaikan kebutuhan)
    public String getTrip(){
        return trip;
    }
    public void setFase(String trip){
        this.trip = trip;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getFinish() {
        return finish;
    }

    public void setFinish(String finish) {
        this.finish = finish;
    }

}

