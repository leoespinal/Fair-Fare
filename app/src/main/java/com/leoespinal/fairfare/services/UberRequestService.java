package com.leoespinal.fairfare.services;

import com.leoespinal.fairfare.BuildConfig;
import com.leoespinal.fairfare.models.RideCoordinates;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.rides.client.SessionConfiguration;

public class UberRequestService {
    private static UberRequestService uniqueInstance = new UberRequestService();
    private final String UBER_CLIENT_ID = BuildConfig.UBER_CLIENT_ID;
    private final String UBER_REDIRECT_URI = BuildConfig.UBER_REDIRECT_URI;
    private SessionConfiguration uberSessionConfiguration;
    private LoginManager uberLoginManager;
    private AccessTokenManager accessTokenManager;
    private static final int UBER_LOGIN_CUSTOM_REQUEST_CODE = 1120;

    //Ride coordinates data
    private RideCoordinates rideCoordinates;

    private UberRequestService() {}

    public static UberRequestService getUniqueInstance() {
        return uniqueInstance;
    }

    public RideCoordinates getRideCoordinates() {
        return rideCoordinates;
    }

    public void setRideCoordinates(RideCoordinates rideCoordinates) {
        this.rideCoordinates = rideCoordinates;
    }
}
