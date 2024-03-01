package com.example.pccsmartapp;

public class Event {

    private String id;
    private String eventName;
    private String fase;
    private String start;
    private String finish;
    private String description;
    private String eventDate;

    public Event() {

    }

    public Event(String eventName,String fase, String start, String finish, String description, String eventDate) {
        this.eventName = eventName;
        this.fase = fase;
        this.start = start;
        this.finish = finish;
        this.description = description;
        this.eventDate = eventDate;
    }

    public Event(String id, String eventName,String fase, String start, String finish, String description, String eventDate) {
        this.id = id; // Menyimpan ID event
        this.eventName = eventName;
        this.fase = fase;
        this.start = start;
        this.finish = finish;
        this.description = description;
        this.eventDate = eventDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter dan setter (sesuaikan kebutuhan)
    public String getFase(){
        return fase;
    }
    public void setFase(String fase){
        this.fase = fase;
    }
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }
}

