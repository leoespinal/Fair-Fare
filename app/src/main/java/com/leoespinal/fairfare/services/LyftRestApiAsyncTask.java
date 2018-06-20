package com.leoespinal.fairfare.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.google.gson.JsonObject;
import com.leoespinal.fairfare.BuildConfig;
import com.leoespinal.fairfare.RideEstimatesActivity;
import com.leoespinal.fairfare.models.RideCoordinates;
import com.leoespinal.fairfare.models.RideServiceOption;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class LyftRestApiAsyncTask extends AsyncTask<Void, Void, List<RideServiceOption>> {
    private Context context;
    private String lyftRideEstimatesEndpoint = "https://api.lyft.com/v1/cost";
    private String lyftRequestAccessTokenEndpoint = "https://api.lyft.com/oauth/token";
    private RideCoordinates rideCoordinates;
    private List<RideServiceOption> rideServiceOptions;
    private final String LYFT_CLIENT_ID = BuildConfig.LYFT_CLIENT_ID;
    private final String LYFT_CLIENT_SECRET = BuildConfig.LYFT_CLIENT_SECRET;
    private SharedPreferences sharedPreferences;

    public LyftRestApiAsyncTask() {}

    @Override
    protected List<RideServiceOption> doInBackground(Void... voids) {
        List<RideServiceOption> rideServiceOptions = new ArrayList<>();
        try {
            requestAuthToken();
            rideServiceOptions = getLyftRideEstimates();
        } catch (Exception e) {
            Log.e("LyftRestApiAsyncTask", "Failed to fetch Lyft ride service options.");
        }
        return rideServiceOptions;
    }

    @Override
    protected void onPostExecute(List<RideServiceOption> rideServiceOptions) {
        super.onPostExecute(rideServiceOptions);
        RideOptionsService rideOptionsService = RideOptionsService.getUniqueInstance();

        for(RideServiceOption rideServiceOption: rideServiceOptions) {
            rideOptionsService.add(rideServiceOption);
        }

        //Create intent to start RideEstimatesActivity
        Intent rideEstimatesViewIntent = new Intent(getContext(), RideEstimatesActivity.class);
        rideEstimatesViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(rideEstimatesViewIntent);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public RideCoordinates getRideCoordinates() {
        return rideCoordinates;
    }

    public void setRideCoordinates(RideCoordinates rideCoordinates) {
        this.rideCoordinates = rideCoordinates;
    }

    public void requestAuthToken() throws Exception {
        String userAndPassword = LYFT_CLIENT_ID + ":" + LYFT_CLIENT_SECRET;
        String lyftAccessTokenUrl = lyftRequestAccessTokenEndpoint;

        URL lyftAccessTokenEndpoint = new URL(lyftAccessTokenUrl);

        HttpsURLConnection connection = (HttpsURLConnection) lyftAccessTokenEndpoint.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + userAndPassword);
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //Create json for connection input data
        String json = "{\"grant_type\": \"client_credentials\", \"scope\": \"public\"}";

        //Write the data to the output stream of the Https connection
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(json.getBytes("UTF-8"));
        outputStream.close();


        if(connection.getResponseCode() == 200) {
            //Read the json and store the access token in shared preferences

            InputStream responseBody = connection.getInputStream();
            InputStreamReader responseBodyReader = new InputStreamReader(responseBody);

            JsonReader jsonReader = new JsonReader(responseBodyReader);

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if(name.equals("access_token")) {
                    String accessToken = jsonReader.nextString();
                    //Stores access token in shared preferences
                    sharedPreferences = getContext().getSharedPreferences("lyft_user_access_token", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("useraccesstoken", accessToken);
                    editor.commit();
                }
            }
            jsonReader.endObject();
            jsonReader.close();

        } else {
            Log.e("LyftRestApiAsyncTask", "Error occurred requesting Lyft access token. Response code: " + Integer.toString(connection.getResponseCode()) + " Response message: " + connection.getResponseMessage() + " Fields: " + connection.getHeaderFields());
        }
    }

    private List<RideServiceOption> getLyftRideEstimates() throws Exception {
        rideServiceOptions = new ArrayList<>();
        //Create URL to get uber price estimates for destination
        String lyftPriceEstimatesUrl = lyftRideEstimatesEndpoint.concat("?start_lat=" + (float) rideCoordinates.getStartingCoordinates().latitude + "&start_lng=" + (float) rideCoordinates.getStartingCoordinates().longitude + "&end_lat=" + (float) rideCoordinates.getDestinationCoordinates().latitude + "&end_lng=" + (float) rideCoordinates.getDestinationCoordinates().longitude);

        URL lyftEndpoint = new URL(lyftPriceEstimatesUrl);

        //Get access code from shared preferences
        sharedPreferences = context.getSharedPreferences("lyft_user_access_token", Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("useraccesstoken", "");

        //Create connection
        HttpsURLConnection connection = (HttpsURLConnection) lyftEndpoint.openConnection();
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
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
                Log.i("LyftRestApiAsyncTask", "Name: " + name);
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    rideServiceOptions.add(parseRideEstimate(jsonReader));
                }
                jsonReader.endArray();
            }
            jsonReader.endObject();

            //Close the json reader
            jsonReader.close();

            //Close connection
            connection.disconnect();

        } else {
            Log.e("LyftRestApiAsyncTask", "Error occurred getting Lyft price estimates data. Response code: " + Integer.toString(connection.getResponseCode()) + " Response message: " + connection.getResponseMessage() + " Fields: " + connection.getHeaderFields());
        }
        return rideServiceOptions;
    }

    private RideServiceOption parseRideEstimate(JsonReader jsonReader) throws Exception {
        String productName = "";
        Integer estimateLow = 0;
        Integer estimateHigh = 0;
        Integer duration = 0;

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if(name.equals("display_name")) {
                productName = jsonReader.nextString();
            } else if (name.equals("estimated_cost_cents_max")) {
                //Get estimate in dollars
                Double estimateHi = Math.floor(jsonReader.nextInt()/100);
                estimateHigh = estimateHi.intValue();
            } else if (name.equals("estimated_cost_cents_min")) {
                //Get estimate in dollars
                Double estimateLo = Math.floor(jsonReader.nextInt()/100);
                estimateLow = estimateLo.intValue();
            } else if (name.equals("estimated_duration_seconds")) {
                duration = jsonReader.nextInt();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();

        RideServiceOption ridesService = new RideServiceOption();
        String estimateRange = "$" + estimateLow + "-" + estimateHigh;
        ridesService.setServiceBaseName(productName);
        ridesService.setEstimateRange(estimateRange);
        int durationInMins = duration/60;
        ridesService.setEta(durationInMins);

        Log.i("LyftRestApiAsyncTask", "Lyft product: " + productName + " estimate: " + estimateRange + " duration in minutes: " + durationInMins);

        return ridesService;
    }
}
