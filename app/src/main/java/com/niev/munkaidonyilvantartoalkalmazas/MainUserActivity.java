package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class MainUserActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainUserActivity.class.getName();
    private FirebaseUser user;
    private FirebaseFirestore mFirestore;
    private String email;
    private boolean isEmployer;
    private Menu menuList;
    private TextView welcomeText;
    private TextView companyNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "auth success");
            email = user.getEmail();
        } else {
            Log.d(LOG_TAG, "auth failed");
            finish();
        }
        welcomeText = findViewById(R.id.welcomeText);
        companyNameText = findViewById(R.id.companyNameText);
        mFirestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = mFirestore.collection("UserPreferences").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        isEmployer = String.valueOf(document.get("accountType")).equals("Munkáltató");
                        onPrepareOptionsMenu(menuList);
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(LOG_TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    companyNameText.setText("cég: " + String.valueOf(snapshot.getData().get("companyName")));
                    Log.d(LOG_TAG, "Current data: " + snapshot.getData());
                    isEmployer = String.valueOf(snapshot.getData().get("accountType")).equals("Munkáltató");
                    String welcomeString = snapshot.getData().get("userName") == null ? "Felhasználó" : String.valueOf(snapshot.getData().get("userName"));
                    welcomeText.setText("Üdvözöljük " + welcomeString + "!");
                } else {
                    Log.d(LOG_TAG, "Current data: null");
                }
            }
        });

        if (isEmployer) {

        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menuList = menu;
        supportInvalidateOptionsMenu();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mainuseractivity_list_menu, menu);
        // Force showing icons for menu items
        if (menu instanceof MenuBuilder)
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.manageWorkers);
        item.setVisible(isEmployer);
        super.onPrepareOptionsMenu(menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.manageWorkers) {
            Log.d(LOG_TAG, "manageWorkers clicked!");
            manageWorkers();
            return true;
        } else if (item.getItemId() == R.id.logout) {
            Log.d(LOG_TAG, "Logout clicked!");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.profile) {
            Log.d(LOG_TAG, "Profile clicked!");
            showProfile();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void manageWorkers() {
        Intent manageWorkersIntent = new Intent(this, ManageWorkersActivity.class);
        startActivity(manageWorkersIntent);
    }

    private void showProfile() {
        Intent showProfileIntent = new Intent(this, ProfileActivity.class);
        startActivity(showProfileIntent);
    }
}