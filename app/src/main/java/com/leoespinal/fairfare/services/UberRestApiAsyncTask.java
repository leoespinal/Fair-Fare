package com.leoespinal.fairfare.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.leoespinal.fairfare.BuildConfig;
import com.leoespinal.fairfare.RideEstimatesActivity;
import com.leoespinal.fairfare.models.RideCoordinates;
import com.leoespinal.fairfare.models.RideServiceOption;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.rides.client.services.RidesService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class UberRestApiAsyncTask extends AsyncTask<Void, Void, List<RideServiceOption>> {
    private Context context;
    private String uberProductsEndpoint = "https://api.uber.com/v1.2/products";
    private String uberPriceEstimatesEndpoint = "https://api.uber.com/v1.2/estimates/price";
    private static final String UBER_SERVER_TOKEN = BuildConfig.UBER_SERVER_TOKEN;
    private String accessToken;
    private RideCoordinates rideCoordinates;
    private List<RideServiceOption> rideServiceOptions;


    public UberRestApiAsyncTask() {}

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    protected List<RideServiceOption> doInBackground(Void... voids) {
        List<RideServiceOption> rideServiceOptions = new ArrayList<>();
        try {
            rideServiceOptions = getUberRideEstimates();
        } catch (Exception e) {
            Log.e("UberRestApiAsyncTask", "Failed to fetch Uber ride service options.");
        }
        return rideServiceOptions;
    }

    @Override
    protected void onPostExecute(List<RideServiceOption> rideServiceOptions) {
        super.onPostExecute(rideServiceOptions);
        RideOptionsService rideOptionsService = RideOptionsService.getUniqueInstance();
        rideOptionsService.setRideServiceOptionList(rideServiceOptions);
        Log.d("UberRestApiAsyncTask", "Executed getUberRideEstimates().");

        //Create intent to start RideEstimatesActivity
        Intent rideEstimatesViewIntent = new Intent(getContext(), RideEstimatesActivity.class);
        rideEstimatesViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(rideEstimatesViewIntent);
    }

    private void getUberProducts() throws Exception {
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


    private List<RideServiceOption> getUberRideEstimates() throws Exception {
        rideServiceOptions = new ArrayList<>();
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
                Log.i("UberRestApiAsyncTask", "Name: " + name);
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    rideServiceOptions.add(parsePrice(jsonReader));
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
        return rideServiceOptions;
    }

    private RideServiceOption parsePrice(JsonReader reader) throws Exception {
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

        RideServiceOption ridesService = new RideServiceOption();
        ridesService.setServiceBaseName(productName);
        ridesService.setEstimateRange(estimate);
        int durationInMins = duration/60;
        ridesService.setEta(durationInMins);

        Log.i("UberRestApiAsyncTask", "Uber product: " + productName + " estimate: " + estimate + " duration in minutes: " + durationInMins);
        return ridesService;
    }

    public RideCoordinates getRideCoordinates() {
        return rideCoordinates;
    }

    public void setRideCoordinates(RideCoordinates rideCoordinates) {
        this.rideCoordinates = rideCoordinates;
    }

    public List<RideServiceOption> getRideServiceOptions() {
        return rideServiceOptions;
    }

    public void setRideServiceOptions(List<RideServiceOption> rideServiceOptions) {
        this.rideServiceOptions = rideServiceOptions;
    }
}
