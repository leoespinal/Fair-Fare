package com.leoespinal.fairfare.services;

import android.util.Log;

import com.leoespinal.fairfare.BuildConfig;
import com.leoespinal.fairfare.models.RideCoordinates;
import com.lyft.networking.ApiConfig;
import com.lyft.networking.LyftApiFactory;
import com.lyft.networking.apiObjects.CostEstimateResponse;
import com.lyft.networking.apiObjects.LatLng;
import com.lyft.networking.apiObjects.PricingDetails;
import com.lyft.networking.apiObjects.RideType;
import com.lyft.networking.apiObjects.RideTypesResponse;
import com.lyft.networking.apis.LyftPublicApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LyftRequestService {
    private static LyftRequestService uniqueInstance = new LyftRequestService();
    private final String LYFT_CLIENT_ID = BuildConfig.LYFT_CLIENT_ID;
    private final String LYFT_CLIENT_TOKEN = BuildConfig.LYFT_CLIENT_TOKEN;
    private LyftPublicApi lyftPublicApi;

    //Ride coordinates data
    private RideCoordinates rideCoordinates;

    private LyftRequestService() {
        //Build Lyft SDK
        ApiConfig lyftConfig = new ApiConfig.Builder()
                .setClientId(LYFT_CLIENT_ID)
                .setClientToken(LYFT_CLIENT_TOKEN)
                .build();

        //Init LyftPublicApi with sdk config
        lyftPublicApi = new LyftApiFactory(lyftConfig).getLyftPublicApi();
    }

    public static LyftRequestService getUniqueInstance() {
        return uniqueInstance;
    }

    //TODO: Change this method to return list of RideServiceOption objects
    public List<String> getRideTypes() {
        final List<String> rideOptions = new ArrayList<>();

        Call<RideTypesResponse> rideTypesResponseCall = null;

        //Get current location coordinates from ride coordinates object
        if(rideCoordinates != null && rideCoordinates.getStartingCoordinates() != null) {
            final Double currentLat = rideCoordinates.getStartingCoordinates().latitude;
            final Double currentLong = rideCoordinates.getStartingCoordinates().longitude;
            rideTypesResponseCall = lyftPublicApi.getRidetypes(currentLat, currentLong, null);

            rideTypesResponseCall.enqueue(new Callback<RideTypesResponse>() {
                @Override
                public void onResponse(Call<RideTypesResponse> call, Response<RideTypesResponse> response) {
                    RideTypesResponse rideTypesResponse = response.body();
                    List<RideType> rideTypes = rideTypesResponse.ride_types;

                    for(RideType rideType: rideTypes) {
                        //TODO: Get data from ride type to help populate RideServiceOption objects
                        rideOptions.add(rideType.display_name);
                    }
                }

                @Override
                public void onFailure(Call<RideTypesResponse> call, Throwable t) {
                    Log.e("LyftRequestService", "Failed to get Lyft ride types. Error message: " + t.getMessage());
                }
            });
        } else {
            Log.e("LyftRequestService", "Current location was not found.");
        }

        return rideOptions;
    }

    public RideCoordinates getRideCoordinates() {
        return rideCoordinates;
    }

    public void setRideCoordinates(RideCoordinates rideCoordinates) {
        this.rideCoordinates = rideCoordinates;
    }
}
