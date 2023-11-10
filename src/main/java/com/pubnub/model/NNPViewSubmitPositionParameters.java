package com.pubnub.model;

import lombok.Data;

@Data
public class NNPViewSubmitPositionParameters {
    String firstName;
    String lastName;
    /*userType;*/
    float latitude;
    float longitude;
    float heightAboveTerrain;
    boolean isLocationUserPinned;
    Location2d location2d;
    int timestamp;
    public NNPViewSubmitPositionParameters(String firstName, String lastname, float confidence, float[] position, float hat, boolean isLocationUserPinned, int timestamp){
        this.firstName = firstName;
        this.lastName = lastname;
        this.latitude = position[0];
        this.longitude = position[1];
        this.heightAboveTerrain = hat;
        this.location2d = new Location2d(confidence,this.latitude, this.longitude);
        this.isLocationUserPinned = isLocationUserPinned;
        this.timestamp = timestamp;
    }
}

class Location2d{
    float uncertainty;
    float longitude;
    float latitude;

    Location2d(float uncertainty, float longitude, float latitude){
        this.uncertainty = uncertainty;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}