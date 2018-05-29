package com.leoespinal.fairfare.models;

import com.google.android.gms.maps.model.LatLng;

public class RideCoordinates {
    private LatLng startingCoordinates;
    private LatLng destinationCoordinates;

    public RideCoordinates() {}

    public RideCoordinates(LatLng startingCoordinates, LatLng destinationCoordinates) {
        this.startingCoordinates = startingCoordinates;
        this.destinationCoordinates = destinationCoordinates;
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
