package com.niev.munkaidonyilvantartoalkalmazas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private static final int RC_SIGN_IN = 5437;
    private static final int SECRET_KEY = 99;
    EditText userEmailET;
    EditText passwordET;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userEmailET = findViewById(R.id.editTextUserName);
        passwordET = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    public void login(View view) {
        String userEmail = userEmailET.getText().toString();
        String password = passwordET.getText().toString();

        mAuth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "login successful");
                    showMainUser();
                } else {
                    Log.d(LOG_TAG, "login failed");
                    Toast.makeText(MainActivity.this, "Couldn't log in: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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
                Log.d(LOG_TAG, "Google sign in success!" + account.getId());
                String email = account.getEmail();
                mFirestore = FirebaseFirestore.getInstance();
                DocumentReference docRef = mFirestore.collection("UserPreferences").document(email);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()) {
                                HashMap<String, String> userData = new HashMap<>();
                                userData.put("userName", account.getFamilyName() + " " + account.getGivenName());
                                userData.put("password", "");
                                userData.put("email", email);
                                userData.put("accountType", "Dolgozó");
                                mFirestore.collection("UserPreferences").document(email).set(userData);
                            }
                        } else {
                            Log.d(LOG_TAG, "get failed with ", task.getException());
                        }
                    }
                });
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException apiException) {
                Log.w(LOG_TAG, "Google sign in failed!", apiException);
            }

        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LOG_TAG, "signInWithCredential:success");
                            showMainUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LOG_TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    public void register(View view) {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        registerIntent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(registerIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        super.onResume();
    }

}