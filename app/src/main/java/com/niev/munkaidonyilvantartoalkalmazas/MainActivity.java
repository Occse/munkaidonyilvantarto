package com.niev.munkaidonyilvantartoalkalmazas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int RC_SIGN_IN = 5437;
    private static final int SECRET_KEY = 99;
    EditText userEmailET;
    EditText passwordET;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isMiamiTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userEmailET = findViewById(R.id.editTextUserName);
        passwordET = findViewById(R.id.editTextPassword);

        mAuth = FirebaseAuth.getInstance();

        isMiamiTheme = preferences.getBoolean("isMiamiTheme", false);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    public void login(View view) {
        String userEmail = userEmailET.getText().toString();
        String password = passwordET.getText().toString();
        if (!userEmail.equals("") && !password.equals("")) {
            mAuth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener(this, loginTask -> {
                if (loginTask.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Sikeres bejelentkezés!", Toast.LENGTH_SHORT).show();
                    showMainUser();
                } else {
                    Toast.makeText(MainActivity.this, "Nem sikerült belépni: " + Objects.requireNonNull(loginTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showMainUser() {
        Intent showMainUserIntent = new Intent(this, MainUserActivity.class);
        showMainUserIntent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(showMainUserIntent);
    }

    public void loginWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String email = account.getEmail();
                mFirestore = FirebaseFirestore.getInstance();
                DocumentReference docRef = mFirestore.collection("UserPreferences").document(Objects.requireNonNull(email));
                docRef.get().addOnCompleteListener(userPreferencesTask -> {
                    if (userPreferencesTask.isSuccessful()) {
                        DocumentSnapshot document = userPreferencesTask.getResult();
                        if (!document.exists()) {
                            HashMap<String, String> userData = new HashMap<>();
                            userData.put("userName", account.getFamilyName() + " " + account.getGivenName());
                            userData.put("password", "");
                            userData.put("email", email);
                            userData.put("accountType", "Dolgozó");
                            mFirestore.collection("UserPreferences").document(email).set(userData);
                        }
                    } else {
                        Log.d(TAG, "get failed with ", userPreferencesTask.getException());
                    }
                });
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException apiException) {
                Log.w(TAG, "Google sign in failed!", apiException);
            }

        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, signInWithGoogleTask -> {
                    if (signInWithGoogleTask.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Sikeres Google bejelentkezés!", Toast.LENGTH_SHORT).show();
                        showMainUser();
                    } else {
                        Toast.makeText(MainActivity.this, "Sikertelen Google bejelentkezés!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void register(View view) {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        registerIntent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(registerIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", userEmailET.getText().toString());
        editor.putString("password", passwordET.getText().toString());
        editor.apply();
    }

    @Override
    protected void onResume() {
        if (isMiamiTheme != preferences.getBoolean("isMiamiTheme", false)) {
            isMiamiTheme = preferences.getBoolean("isMiamiTheme", false);
            recreate();
        }
        super.onResume();
    }

}