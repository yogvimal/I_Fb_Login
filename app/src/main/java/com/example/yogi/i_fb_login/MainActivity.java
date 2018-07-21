package com.example.yogi.i_fb_login;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private CallbackManager mCallbackManager;
    public static final String TAG = "Fb_Login";
    private FirebaseAuth mAuth;

    private CardView fb_login_card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        fb_login_card = findViewById(R.id.fb_login_card);

        fb_login_card.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fb_login_card)
        {
            fb_login_card.setEnabled(false);

            LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email","public_profile","user_location"));

            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);

                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel");
                    // ...
                    fb_login_card.setEnabled(true);
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "facebook:onError", error);
                    // ...
                    fb_login_card.setEnabled(true);
                }
            });
        }
    }
    @Override
    public void onStart() {
        super.onStart();

        //Check if the user has already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            updateUI(AccessToken.getCurrentAccessToken());

        }

    }
    private void updateUI(AccessToken token) {
        Toast.makeText(this,"Congrats, You have logged in",Toast.LENGTH_LONG).show();



        Intent mProfileIntent = new Intent(this,Profile_Activity.class);
        mProfileIntent.putExtra("access_token",token);
        startActivity(mProfileIntent);
        finish();
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            fb_login_card.setEnabled(true);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");



                            //FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(token);
                        } else {

                            fb_login_card.setEnabled(true);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(token);
                        }

                        // ...
                    }
                });
    }

    }
