package com.leoespinal.fairfare.services;

import com.leoespinal.fairfare.models.RideServiceOption;

import java.util.List;

public class RideOptionsService {
    private static RideOptionsService uniqueInstance = new RideOptionsService();
    private List<RideServiceOption> rideServiceOptionList;

    private RideOptionsService() {}

    public static RideOptionsService getUniqueInstance() {
        return uniqueInstance;
    }

    public List<RideServiceOption> getRideServiceOptionList() {
        return rideServiceOptionList;
    }

    public void setRideServiceOptionList(List<RideServiceOption> rideServiceOptionList) {
        this.rideServiceOptionList = rideServiceOptionList;
    }
}
