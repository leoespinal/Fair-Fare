package com.leoespinal.fairfare.services;

import com.leoespinal.fairfare.models.RideServiceOption;

import java.util.ArrayList;
import java.util.List;

public class RideOptionsService {
    private static RideOptionsService uniqueInstance = new RideOptionsService();
    private List<RideServiceOption> rideServiceOptionList;

    private RideOptionsService() {
        rideServiceOptionList = new ArrayList<>();
    }

    public static RideOptionsService getUniqueInstance() {
        return uniqueInstance;
    }

    public List<RideServiceOption> getRideServiceOptionList() {
        return rideServiceOptionList;
    }

    public void add(RideServiceOption rideServiceOption) {
        this.rideServiceOptionList.add(rideServiceOption);
    }
}
