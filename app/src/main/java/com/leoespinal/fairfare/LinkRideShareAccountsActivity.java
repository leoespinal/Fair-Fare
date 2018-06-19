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

import com.leoespinal.fairfare.services.UberRequestService;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_ride_share_accounts);

        initLyftSdk();

        //Set on click listener for custom button
        connectUberAccountButton = (Button) findViewById(R.id.linkUberButton);
        connectUberAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UberRequestService uberRequestService = UberRequestService.getUniqueInstance();
                uberRequestService.setContext(getApplicationContext());
                uberRequestService.configureAccessTokenAndLoginManager();
                uberRequestService.getLoginManager().login(LinkRideShareAccountsActivity.this);
            }
        });

        connectLyftAccountButton = (Button) findViewById(R.id.linkLyftButton);
        connectLyftAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LinkRideShareAccountsActivity.this, "Authenticating Lyft.", Toast.LENGTH_LONG).show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UberRequestService.getUniqueInstance().getLoginManager().onActivityResult(this, requestCode, resultCode, data);
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
