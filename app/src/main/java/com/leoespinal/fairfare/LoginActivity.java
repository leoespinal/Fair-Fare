package com.leoespinal.fairfare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;

    //Firebase instance
    private FirebaseAuth mAuth;

    //UI Element References
    private Button signInButton;
    private Button signUpButton;
    private Button googleLoginButton;
    private EditText emailTextView;
    private EditText passwordTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //Init View object references
        emailTextView = (EditText) findViewById(R.id.emailTextViewId);
        passwordTextView = (EditText) findViewById(R.id.passwordTextViewId);
        signInButton = (Button) findViewById(R.id.signInButtonId);
        signUpButton = (Button) findViewById(R.id.signUpButtonId);
        googleLoginButton = (Button) findViewById(R.id.googleSignInButton);


        //Setup on click listeners for all buttons
        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Check to see if the user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    protected void updateUI(FirebaseUser user) {
        if(user != null) {
//            //There is a signed in user
//            Toast.makeText(LoginActivity.this, "Welcome back, " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
//
//            //TODO: Display MapActivity
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);

        } else {
            Log.d("updateUI", "The user is not logged in.");
        }
    }

    protected void signInExisitingUser(final String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //Sign in successful, update UI with signed in user's info
                            Log.d("signInExisitingUser", "Signed in exisiting user with email: " + email);
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Welcome back, " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            //Sign in failed, display error message to user
                            Log.w("signInExisitingUser", "Failed to sign user in with: " + email + " Exception: " + task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed. Error message: " + task.getException(), Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void signInViaGoogle() {
        //Choose auth providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        //Create sign in intent
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            //Get the IDP response
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if(resultCode == RESULT_OK) {
                //Sign in was successful
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(LoginActivity.this, "Welcome, " + user.getDisplayName() + "!", Toast.LENGTH_LONG).show();

                //TODO: Change this logic below based on weather the user has already linked their uber and lyft accounts yet or not
                //Open link ride share accounts activity
                Intent intent = new Intent(this, LinkRideShareAccountsActivity.class);
                startActivity(intent);

            } else {
                //Sign in failed
                Toast.makeText(LoginActivity.this, response.getError().getMessage() + " Error code: " + response.getError().getErrorCode(), Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();

        if(viewID == signInButton.getId()) {
            signInExisitingUser(emailTextView.getText().toString(), passwordTextView.getText().toString());
        } else if(viewID == signUpButton.getId()) {
            //Create a new intent to call the sign up form activity
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        } else if(viewID == googleLoginButton.getId()) {
            signInViaGoogle();
        }
    }
}
