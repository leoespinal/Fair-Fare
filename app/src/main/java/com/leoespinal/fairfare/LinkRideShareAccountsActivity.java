package com.leoespinal.fairfare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lyft.networking.ApiConfig;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.util.Arrays;

public class LinkRideShareAccountsActivity extends AppCompatActivity {

    //Uber developer credentials
    private static final String UBER_CLIENT_ID = BuildConfig.UBER_CLIENT_ID;
    private static final String UBER_REDIRECT_URI = BuildConfig.UBER_REDIRECT_URI;

    //Lyft developer credentials
    private static final String LYFT_CLIENT_ID = BuildConfig.LYFT_CLIENT_ID;
    private static final String LYFT_CLIENT_TOKEN = BuildConfig.LYFT_CLIENT_TOKEN;

    //Lyft SDK
    private ApiConfig lyftConfig;
    private static final String LYFT_PACKAGE_NAME = BuildConfig.LYFT_PACKAGE_NAME;

    //View objects
    private Button connectUberAccountButton;
    private Button connectLyftAccountButton;
    private Button finishButton;

    //Uber SDK
    private SessionConfiguration uberSessionConfiguration;
    private LoginManager uberLoginManager;
    private AccessTokenManager accessTokenManager;
    private static final int UBER_LOGIN_CUSTOM_REQUEST_CODE = 1120;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_ride_share_accounts);

        initUberSdk();
        initLyftSdk();

        //Set on click listener for custom button
        connectUberAccountButton = (Button) findViewById(R.id.linkUberButton);
        connectUberAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uberLoginManager.login(LinkRideShareAccountsActivity.this);
            }
        });

        connectLyftAccountButton = (Button) findViewById(R.id.linkLyftButton);
        connectLyftAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deepLinkIntoLyftApp();
            }
        });

        finishButton = (Button) findViewById(R.id.finishButtonId);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                Intent mapIntent = new Intent(context, MapsActivity.class);
                startActivity(mapIntent);
            }
        });


    }

    public void initLyftSdk() {
        lyftConfig = new ApiConfig.Builder()
                .setClientId(LYFT_CLIENT_ID)
                .setClientToken(LYFT_CLIENT_TOKEN)
                .build();
    }

    public void initUberSdk() {
        //Build the Uber SDK
        uberSessionConfiguration = new SessionConfiguration.Builder()
                .setClientId(UBER_CLIENT_ID)
                .setRedirectUri(UBER_REDIRECT_URI)
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                .build();

        //Create access token storage object
        accessTokenManager = new AccessTokenManager(this);

        //Configure uber login manager
        uberLoginManager = new LoginManager(accessTokenManager, new UberLoginCallback(), uberSessionConfiguration, UBER_LOGIN_CUSTOM_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uberLoginManager.onActivityResult(this, requestCode, resultCode, data);
    }

    private class UberLoginCallback implements LoginCallback {
        @Override
        public void onLoginCancel() {

        }

        @Override
        public void onLoginError(@NonNull AuthenticationError error) {
            Toast.makeText(LinkRideShareAccountsActivity.this, "Authentication error. Error message: " + error.name(), Toast.LENGTH_LONG).show();
            Log.e("UberLoginCallback", error.name());
        }

        @Override
        public void onLoginSuccess(@NonNull AccessToken accessToken) {
            //Stores access token in shared preferences
            accessTokenManager.setAccessToken(accessToken);
        }

        @Override
        public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {

        }
    }

    public void deepLinkIntoLyftApp() {
        if(isLyftInstalled(this, LYFT_PACKAGE_NAME)) {
            //Open the app
            openLink(this, "lyft://");
            Log.d("deepLinkInfoLyftApp", "Lyft is installed on device.");
        } else {
            //Have the user sign up
            String signUpUrl = "https://www.lyft.com/signup/SDKSIGNUP?clientId=" + LYFT_CLIENT_ID + "&sdkName=android_direct";
            openLink(this, signUpUrl);
            Log.d("deepLinkInfoLyftApp", "Lyft is not installed on device");
        }
    }

    public boolean isLyftInstalled(Context context, String lyftPackageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(lyftPackageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("isLyftInstalled", "Lyft package name can not be found. Error message: " + e.getMessage());
        }
        return false;
    }

    public void openLink(Activity activity, String link) {
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
        playStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        playStoreIntent.setData(Uri.parse(link));
        activity.startActivity(playStoreIntent);
    }
}
