package com.app.sneezyapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import io.realm.mongodb.Credentials;

public class LoginActivity extends AppCompatActivity {

    private GoogleApiClient _googleApiClient;
    private static final int GOOGLE_SIGN_IN = 421;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final String googleWebClientId = getString(R.string.google_web_client_id);
        enableGoogleAuth(googleWebClientId);

        enableAnonymousAuth();
    }

    private void enableGoogleAuth(String googleWebClientId){
        final GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestServerAuthCode(googleWebClientId, true).build();

        /*_googleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .enableAutoManage(LoginActivity.this, connectionResult ->
                        Log.e("Stitch Auth", "Error connecting to google: " + connectionResult.getErrorMessage()))
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/

        findViewById(R.id.google_login_button).setOnClickListener(v -> {
            /*if (!_googleApiClient.isConnected()) {
                _googleApiClient.connect();
            }*/
            GoogleSignInClient mGoogleSignInClient =
                    GoogleSignIn.getClient(LoginActivity.this, gso);

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
            return;
        } else {
            //
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getServerAuthCode();

            Credentials googleCredentials = Credentials.google(idToken);

            MainActivity.app.loginAsync(googleCredentials, it -> {
                if (it.isSuccess()) {
                    Toast.makeText(LoginActivity.this,
                            "Logged in with Google. ID: ",
                                    //+  MainActivity.user.getId(),
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, it.getError().getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (ApiException e) {
            Log.w("GOOGLE AUTH FAILURE", "signInResult:failed code=" + e.getStatusCode());
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    private void enableAnonymousAuth() {
        Credentials anonCredentials = Credentials.anonymous();

        findViewById(R.id.anon_login_button).setOnClickListener(ignored ->
                MainActivity.app.loginAsync(anonCredentials, it -> {
                    if (it.isSuccess()){
                        Toast.makeText(LoginActivity.this,
                                "Logged in Anonymously. ID: ",
                                        //+  MainActivity.user.getId(),
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, it.getError().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }));
    }
}
