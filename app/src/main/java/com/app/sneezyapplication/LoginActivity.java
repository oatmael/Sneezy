package com.app.sneezyapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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
import com.mongodb.stitch.core.auth.providers.google.GoogleCredential;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

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

        _googleApiClient = new GoogleApiClient.Builder(LoginActivity.this)
                .enableAutoManage(LoginActivity.this, connectionResult ->
                        Log.e("Stitch Auth", "Error connecting to google: " + connectionResult.getErrorMessage()))
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.google_login_button).setOnClickListener(v -> {
            if (!_googleApiClient.isConnected()) {
                _googleApiClient.connect();
            }
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

            final GoogleCredential googleCredential =
                    new GoogleCredential(account.getServerAuthCode());

            MainActivity.client.getAuth().loginWithCredential(googleCredential).addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            Log.e("Stitch Auth", "Error logging in with Google", task.getException());
                        }
                    });

        } catch (ApiException e) {
            Log.w("GOOGLE AUTH FAILURE", "signInResult:failed code=" + e.getStatusCode());
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    private void enableAnonymousAuth() {
        findViewById(R.id.anon_login_button).setOnClickListener(ignored ->
                MainActivity.client.getAuth().loginWithCredential(new AnonymousCredential())
                        .addOnSuccessListener(user -> {
                            Toast.makeText(LoginActivity.this,
                                    "Logged in Anonymously. ID: " + user.getId(),
                                    Toast.LENGTH_LONG).show();
                            setResult(Activity.RESULT_OK);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.d("Stitch Auth", "error logging in", e);
                            Toast.makeText(LoginActivity.this, "Failed to log in Anonymously. " +
                                            "Did you enable Anonymous Auth in your Stitch backend and copy " +
                                            "your Stitch App ID to strings.xml?",
                                    Toast.LENGTH_LONG).show();
                        }));
    }
}
