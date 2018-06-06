package com.leoespinal.fairfare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.leoespinal.fairfare.models.RideCoordinates;
import com.leoespinal.fairfare.services.LyftRequestService;
import com.leoespinal.fairfare.services.UberRequestService;
import com.leoespinal.fairfare.services.UberRestApiService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PlaceSelectionListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FirebaseAuth mAuth;

    //Coordinate data
    private RideCoordinates rideCoordinates;

    //UI Elements
    private Button getRideEstimatesButton;


    public void displayCustomUserToast() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            //There is a signed in user
            Toast.makeText(MapsActivity.this, "Where to, " + user.getDisplayName() + "?", Toast.LENGTH_LONG).show();
        } else {
            Log.d("updateUI", "The user is not logged in.");
            Toast.makeText(this, "Authentication error.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment placeAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        placeAutocompleteFragment.setOnPlaceSelectedListener(this);

        //init ride coordinate object
        rideCoordinates = new RideCoordinates();

        //TODO: Disable this button if a destination address has not been selected
        getRideEstimatesButton = (Button) findViewById(R.id.getRideEstimatesButtonId);
        getRideEstimatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity.this, "Getting ride share estimates...", Toast.LENGTH_SHORT).show();
                //TODO: Issue ride estimates request to both Uber and Lyft Http services

                UberRestApiService uberRestApiService = UberRestApiService.getUniqueInstance();
                uberRestApiService.setContext(getApplicationContext());
                uberRestApiService.setRideCoordinates(rideCoordinates);

                try {
                    uberRestApiService.startBackgroundThread();
                } catch (Exception e) {
                    Log.e("MapsActivity", "Failed to connect to Uber REST Api.");
                }

//                UberRequestService uberRequestService = UberRequestService.getUniqueInstance();
//                uberRequestService.setContext(getApplicationContext());
//                uberRequestService.configureAccessTokenAndLoginManager();
//                uberRequestService.setRideCoordinates(rideCoordinates);
//
//                try {
//                    uberRequestService.getLocalUberServiceOptions();
//                } catch (Exception e) {
//                    Log.e("MapsActivity", "Failed to get local ride services from Uber. Error message: " + e.getMessage());
//                }


                LyftRequestService lyftRequestService = LyftRequestService.getUniqueInstance();
                lyftRequestService.setRideCoordinates(rideCoordinates);

                //TODO: Create an intent to launch RideEstimatesActivity
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //we have permission
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //Start listening to user's location
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }

        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Get the user's location
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); // get device location
        displayCustomUserToast();

        //Listen for location changes
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Toast.makeText(MapsActivity.this, location.toString(), Toast.LENGTH_SHORT).show();
                //Use the user's current location and store it as long, lat for map view
                mMap.clear(); //clear existing markers to add the latest one
                LatLng usersCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(usersCurrentLocation).title("Current location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usersCurrentLocation, 15));

                //Create Geocoder to get address from coordinates
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);
                try {
                    List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    if(listAddresses != null && listAddresses.size() > 0) {
                        Log.i("PlaceInfo", listAddresses.get(0).toString());
                        Address address = listAddresses.get(0);
                        //Toast.makeText(MapsActivity.this, address.getFeatureName() + " " + address.getThoroughfare() + "\n" + address.getAdminArea() + " " + address.getPostalCode() + ", " + address.getCountryCode(), Toast.LENGTH_LONG);
                    }

                } catch (IOException e) {
                    Log.e("onLocationChanged", e.getMessage());
                }

                //Remove feature updates, just get current
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        //If device is running SDK < 23, permission not required
        if(Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else  {
            //Request user's permission
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else {
                //We have permission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                //update the location on the map as soon as the map loads
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng usersCurrentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                //Current location as starting coordinates for ride
                rideCoordinates.setStartingCoordinates(usersCurrentLocation);

                mMap.clear(); // remove any old markers
                mMap.addMarker(new MarkerOptions().position(usersCurrentLocation).title("Current location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usersCurrentLocation, 15));
                //Remove feature updates, just get current
                locationManager.removeUpdates(locationListener);
            }
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i("PlaceSelectionListener", "Selected place: " + place.getAddress());

        //Get coordinates from place address
        LatLng userSelectedDestination = place.getLatLng();

        Log.i("PlaceSelectionListener", "Selected place coordinates: " + userSelectedDestination);

        //Set destination coordinates
        rideCoordinates.setDestinationCoordinates(userSelectedDestination);

        //Add marker for this location and pan camera out
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(rideCoordinates.getStartingCoordinates()).title("Current location"));
        mMap.addMarker(new MarkerOptions().position(userSelectedDestination).title("Destination location"));

        //Create a polyline to show route on map
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(rideCoordinates.getStartingCoordinates());
        polylineOptions.add(rideCoordinates.getDestinationCoordinates());
        mMap.addPolyline(polylineOptions);

        //Set camera bounds to show current location and destination map markers
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(rideCoordinates.getStartingCoordinates());
        boundsBuilder.include(rideCoordinates.getDestinationCoordinates());
        LatLngBounds bounds = boundsBuilder.build();

        //Move camera on map
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Log.i("onPlaceSelected", "Finished setting map camera bounds.");
//                CameraUpdate zoomOut = CameraUpdateFactory.zoomOut();
//                mMap.animateCamera(zoomOut);
            }

            @Override
            public void onCancel() {
                Log.d("onPlaceSelected", "Canceled animate camera.");
            }
        });
    }

    @Override
    public void onError(Status status) {
        Log.e("PlaceSelectionListener", "Failed to search for place. Error message: " + status.getStatus());
    }
}
