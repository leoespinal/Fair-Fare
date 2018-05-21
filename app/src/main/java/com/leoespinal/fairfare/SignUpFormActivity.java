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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpFormActivity extends AppCompatActivity {

    //Firebase instance
    private FirebaseAuth mAuth;

    //UI Element References
    EditText displayNameEditTextView;
    EditText emailEditTextView;
    EditText passwordEditTextView;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);

        mAuth = FirebaseAuth.getInstance();

        displayNameEditTextView = (EditText) findViewById(R.id.displayNameEditTextFieldId);
        emailEditTextView = (EditText) findViewById(R.id.emailEditTextFieldId);
        passwordEditTextView = (EditText) findViewById(R.id.passwordEditTextFieldId);
        nextButton = (Button) findViewById(R.id.nextButtonId);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName = displayNameEditTextView.getText().toString();
                String emailAddress = emailEditTextView.getText().toString();
                String password = passwordEditTextView.getText().toString();
                createUserAccount(emailAddress, password, displayName);
            }
        });

    }


    protected void updateUI(FirebaseUser user) {
        if(user != null) {
            //Show them the LinkRideShareAccountsActivity
            Intent intent = new Intent(this, LinkRideShareAccountsActivity.class);
            startActivity(intent);
        } else {
            Log.d("updateUI", "Failed to create user.");
        }
    }

    protected void createUserAccount(final String email, String password, final String displayName) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //Sign in was a success, create intent to go to the LinkRideShareAccountsActivity
                            Log.d("createUserAccount", "Created user with email: " + email);
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUserProfile(user, displayName);
                            updateUI(user);
                        } else {
                            //Sign in failed display error message
                            Log.w("createUserAccount", "Failed to create user with email: " + email);
                            Toast.makeText(SignUpFormActivity.this, "Authentication failed. Error message: " + task.getException(), Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }


    public void updateUserProfile(FirebaseUser user, String displayName) {
        //Update the user's profile with their display name entered in the form
        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
        user.updateProfile(userProfileChangeRequest);
    }
}
