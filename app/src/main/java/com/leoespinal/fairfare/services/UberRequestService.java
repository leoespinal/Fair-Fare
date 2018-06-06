package com.leoespinal.fairfare.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.leoespinal.fairfare.BuildConfig;
import com.leoespinal.fairfare.LinkRideShareAccountsActivity;
import com.leoespinal.fairfare.models.RideCoordinates;
import com.leoespinal.fairfare.models.RideServiceOption;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.Session;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.model.Product;
import com.uber.sdk.rides.client.model.RideEstimate;
import com.uber.sdk.rides.client.model.RideRequestParameters;
import com.uber.sdk.rides.client.services.RidesService;
import com.uber.sdk.rides.client.model.ProductsResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Response;

public class UberRequestService {
    private static UberRequestService uniqueInstance = new UberRequestService();
    private final String UBER_CLIENT_ID = BuildConfig.UBER_CLIENT_ID;
    private final String UBER_REDIRECT_URI = BuildConfig.UBER_REDIRECT_URI;
    private final String UBER_SERVER_TOKEN = BuildConfig.UBER_SERVER_TOKEN;
    private SessionConfiguration uberSessionConfiguration;
    private LoginManager uberLoginManager;
    private AccessTokenManager accessTokenManager;
    private Context context;
    private RidesService uberRidesService;
    private static final int UBER_LOGIN_CUSTOM_REQUEST_CODE = 1120;
    private static final String UBER = "Uber ";

    //Ride coordinates data
    private RideCoordinates rideCoordinates;

    private UberRequestService() {
        //Build the Uber SDK
        uberSessionConfiguration = new SessionConfiguration.Builder()
                .setClientId(UBER_CLIENT_ID)
                .setRedirectUri(UBER_REDIRECT_URI)
                .setServerToken(UBER_SERVER_TOKEN)
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS, Scope.REQUEST))
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();
    }

    public static UberRequestService getUniqueInstance() {
        return uniqueInstance;
    }

    public RideCoordinates getRideCoordinates() {
        return rideCoordinates;
    }

    public void setRideCoordinates(RideCoordinates rideCoordinates) {
        this.rideCoordinates = rideCoordinates;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void configureAccessTokenAndLoginManager() {
        //Create access token storage object
        accessTokenManager = new AccessTokenManager(getContext());

        //Configure uber login manager
        uberLoginManager = new LoginManager(accessTokenManager, new UberLoginCallback(), uberSessionConfiguration, UBER_LOGIN_CUSTOM_REQUEST_CODE);
    }

    public LoginManager getLoginManager() {
        return uberLoginManager;
    }


    //TODO: Fix this method
//    public List<RideServiceOption> getLocalUberServiceOptions() throws Exception {
//        List<RideServiceOption> rideServiceOptions = new ArrayList<>();
//
//        try {
//            uberRidesService = UberRidesApi.with(uberLoginManager.getSession()).build().createService();
//        } catch (Exception e) {
//            Log.e("UberRequestService", "Failed to get session for Uber api. Error message: " + e.getMessage());
//        }
//
//        float startingLat = (float) rideCoordinates.getStartingCoordinates().latitude;
//        float startingLong = (float) rideCoordinates.getStartingCoordinates().longitude;
//        float destinationLat = (float) rideCoordinates.getDestinationCoordinates().latitude;
//        float destinationLong = (float) rideCoordinates.getDestinationCoordinates().longitude;
//
//        Response<ProductsResponse> response = uberRidesService.getProducts(startingLat, startingLong).execute();
//
//        //Response<ProductsResponse> productsResponse = uberRidesService.getProducts(startingLat, startingLong).execute();
//
//        List<Product> products = null;
//        if(response.isSuccessful()) {
//            products = response.body().getProducts();
//        } else {
//            Log.e("UberRequestService", "Failed to get Uber products.");
//        }
//
//        //RideServiceOption(String serviceBaseName, String rideProductName, Integer lowRateEstimate, Integer highRateEstimate, Float surgeMultiplier, String fareId, Integer eta)
//
//        for(Product product: products) {
//            RideServiceOption rideServiceOption = new RideServiceOption();
//            //Set service name and product name
//            rideServiceOption.setServiceBaseName(UBER);
//            rideServiceOption.setRideProductName(product.getDisplayName());
//
//            //Build ride request parameters for ride estimate
//            RideRequestParameters requestParameters = new RideRequestParameters.Builder()
//                    .setProductId(product.getProductId())
//                    .setPickupCoordinates(startingLat, startingLong)
//                    .setDropoffCoordinates(destinationLat, destinationLong)
//                    .build();
//
//            RideEstimate rideEstimate = null;
//            try {
//                rideEstimate = uberRidesService.estimateRide(requestParameters).execute().body();
//
//                //Get price range for the estimate
//                RideEstimate.Price price = rideEstimate.getPrice();
//                rideServiceOption.setLowRateEstimate(price.getLowEstimate());
//                rideServiceOption.setHighRateEstimate(price.getHighEstimate());
//
//                //Get driver eta
//                rideServiceOption.setEta(rideEstimate.getPickupEstimate());
//
//                //Add ride option to list
//                rideServiceOptions.add(rideServiceOption);
//            } catch (Exception e) {
//                Log.e("UberRequestService", "Failed to get Uber ride estimates.");
//            }
//
//        }
//
//        return rideServiceOptions;
//    }

    private class UberLoginCallback implements LoginCallback {
        @Override
        public void onLoginCancel() {

        }

        @Override
        public void onLoginError(@NonNull AuthenticationError error) {
            Log.e("UberLoginCallback", error.name());
        }

        @Override
        public void onLoginSuccess(@NonNull AccessToken accessToken) {
            //Stores access token with Uber access token manager
            accessTokenManager.setAccessToken(accessToken);

            //Stores access token in shared preferences
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("uber_user_access_token", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("useraccesstoken", accessToken.getToken());
            editor.commit();
        }

        @Override
        public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {

        }
    }
}
