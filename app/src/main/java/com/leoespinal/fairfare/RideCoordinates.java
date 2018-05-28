package com.leoespinal.fairfare;

import com.google.android.gms.maps.model.LatLng;

public class RideCoordinates {
    private double startingLatitude;
    private double startingLongitude;
    private double destinationLatitude;
    private double destinationLongitude;

    private LatLng startingCoordinates;
    private LatLng destinationCoordinates;


    public RideCoordinates() {}

    public RideCoordinates(double startingLatitude, double startingLongitude, double destinationLatitude, double destinationLongitude) {
        this.startingLatitude = startingLatitude;
        this.startingLongitude = startingLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
    }

    public RideCoordinates(LatLng startingCoordinates, LatLng destinationCoordinates) {
        this.startingCoordinates = startingCoordinates;
        this.destinationCoordinates = destinationCoordinates;
    }

    public double getStartingLatitude() {
        return startingLatitude;
    }

    public void setStartingLatitude(double startingLatitude) {
        this.startingLatitude = startingLatitude;
    }

    public double getStartingLongitude() {
        return startingLongitude;
    }

    public void setStartingLongitude(double startingLongitude) {
        this.startingLongitude = startingLongitude;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public LatLng getStartingCoordinates() {
        return startingCoordinates;
    }

    public void setStartingCoordinates(LatLng startingCoordinates) {
        this.startingCoordinates = startingCoordinates;
    }

    public LatLng getDestinationCoordinates() {
        return destinationCoordinates;
    }

    public void setDestinationCoordinates(LatLng destinationCoordinates) {
        this.destinationCoordinates = destinationCoordinates;
    }
}
