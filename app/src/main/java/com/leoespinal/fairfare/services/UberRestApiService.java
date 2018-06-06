package com.leoespinal.fairfare.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.leoespinal.fairfare.BuildConfig;
import com.leoespinal.fairfare.models.RideCoordinates;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class UberRestApiService {
    private static UberRestApiService uniqueInstance = new UberRestApiService();
    private Context context;
    private String uberProductsEndpoint = "https://api.uber.com/v1.2/products";
    private String uberPriceEstimatesEndpoint = "https://api.uber.com/v1.2/estimates/price";
    private static final String UBER_SERVER_TOKEN = BuildConfig.UBER_SERVER_TOKEN;
    private String accessToken;
    private RideCoordinates rideCoordinates;

    private UberRestApiService() {
//        SharedPreferences sharedPreferences = getContext().getSharedPreferences("uber_user_access_token", Context.MODE_PRIVATE);
//        accessToken = sharedPreferences.getString("useraccesstoken", "");
    }

    public static UberRestApiService getUniqueInstance() {
        return uniqueInstance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //Start background thread for request
    public void startBackgroundThread() throws Exception {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //Networking logic
                try {
                    getUberRideEstimates();
                    //getUberProducts();
                } catch (Exception e) {
                    Log.e("EXCEPTION", e.getMessage());
                }


            }
        });
    }

    public void getUberProducts() throws Exception {
        //Create URL to get all uber products
        String uberProductsUrl = uberProductsEndpoint.concat("?latitude=" + (float) rideCoordinates.getStartingCoordinates().latitude + "&longitude=" + (float) rideCoordinates.getStartingCoordinates().longitude);
        URL uberProductsEndpoint = new URL(uberProductsUrl);

        HttpsURLConnection connection = (HttpsURLConnection) uberProductsEndpoint.openConnection();
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Accept-Language", "en_US");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);

        if(connection.getResponseCode() == 200) {
            Log.i("UberHttpService", "Successfully obtained products JSON.");

            //Get input stream
            InputStream responseBody = connection.getInputStream();
            InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

            //Read in JSON
            JsonReader jsonReader = new JsonReader(responseBodyReader);

        } else {
            Log.e("UberHttpService", "Error occurred getting uber products data. Response code: " + Integer.toString(connection.getResponseCode()) + " Response message: " + connection.getResponseMessage() + " Fields: " + connection.getHeaderFields());
        }
    }


    public void getUberRideEstimates() throws Exception {
        //Create URL to get uber price estimates for destination
        String uberPriceEstimatesUrl = uberPriceEstimatesEndpoint.concat("?start_latitude=" + (float) rideCoordinates.getStartingCoordinates().latitude + "&start_longitude=" + (float) rideCoordinates.getStartingCoordinates().longitude + "&end_latitude=" + (float) rideCoordinates.getDestinationCoordinates().latitude + "&end_longitude=" + (float) rideCoordinates.getDestinationCoordinates().longitude);

        URL uberEndpoint = new URL(uberPriceEstimatesUrl);

        //Create connection
        HttpsURLConnection connection = (HttpsURLConnection) uberEndpoint.openConnection();
        connection.setRequestProperty("Authorization", "Token " + UBER_SERVER_TOKEN);
        connection.setRequestProperty("Accept-Language", "en_US");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);

        if(connection.getResponseCode() == 200) {
            Log.i("UberHttpService", "Successfully obtained price estimates JSON.");

            //Get input stream
            InputStream responseBody = connection.getInputStream();
            InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

            //Read in JSON
            JsonReader jsonReader = new JsonReader(responseBodyReader);

            //Parse JSON
            jsonReader.beginObject(); //start processing
            while (jsonReader.hasNext()) { //loop through all keys
                String name = jsonReader.nextName();
                Log.i("UberRestApiService", "Name: " + name);
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    parsePrice(jsonReader);
                }
                jsonReader.endArray();
            }
            jsonReader.endObject();

            //Close the json reader
            jsonReader.close();

            //Close connection
            connection.disconnect();

        } else {
            Log.e("UberHttpService", "Error occurred getting uber price estimates data. Response code: " + Integer.toString(connection.getResponseCode()) + " Response message: " + connection.getResponseMessage() + " Fields: " + connection.getHeaderFields());
        }
    }

    public void parsePrice(JsonReader reader) throws Exception {
        String productName = null;
        String estimate = null;
        Integer duration = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if(name.equals("localized_display_name")) {
                productName = reader.nextString();
            } else if (name.equals("estimate")) {
                estimate = reader.nextString();
            } else if (name.equals("duration")) {
                duration = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        Log.i("UberRestApiService", "Uber product: " + productName + " estimate: " + estimate + " duration: " + duration);

    }

    public RideCoordinates getRideCoordinates() {
        return rideCoordinates;
    }

    public void setRideCoordinates(RideCoordinates rideCoordinates) {
        this.rideCoordinates = rideCoordinates;
    }
}
