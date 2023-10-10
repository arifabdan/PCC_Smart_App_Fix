package com.example.pccsmartapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.xml.transform.Result;

public class ElevationResponse {
    @SerializedName("result")
    private List<Result> results;

    public List<Result> getResults(){
        return results;
    }
    public class Result{
        @SerializedName("elevation")
        private double elevation;

        public double getElevation(){
            return elevation;
        }
    }
}
