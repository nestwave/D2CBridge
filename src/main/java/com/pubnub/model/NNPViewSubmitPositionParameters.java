package com.pubnub.model;

import lombok.Data;

import static com.nestwave.device.util.GpsTime.getUtcAssistanceTime;

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
    long timestamp;
    public NNPViewSubmitPositionParameters(String firstName, String lastname, float confidence, float[] position, float hat, boolean isLocationUserPinned, int timestamp){
        this.firstName = firstName;
        this.lastName = lastname;
        this.longitude = position[0];
        this.latitude = position[1];
        this.heightAboveTerrain = hat;
        this.location2d = new Location2d(confidence,this.longitude, this.latitude);
        this.isLocationUserPinned = isLocationUserPinned;
        // As timestamp is GPS Time and we need Unix Time
        this.timestamp = getUtcAssistanceTime(timestamp).toEpochSecond();
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