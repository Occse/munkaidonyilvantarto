package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


public class MainUserActivity extends AppCompatActivity {
    private static final String TAG = MainUserActivity.class.getName();
    private FirebaseUser user;
    private FirebaseFirestore mFirestore;
    private String email;
    private boolean isEmployer;
    private Menu menuList;
    private TextView welcomeText;
    private TextView companyNameText;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "auth success");
            email = user.getEmail();
        } else {
            Log.d(TAG, "auth failed");
            finish();
        }
        welcomeText = findViewById(R.id.welcomeText);
        companyNameText = findViewById(R.id.companyNameText);
        mFirestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = mFirestore.collection("UserPreferences").document(email);
        docRef.get().addOnCompleteListener(userPreferencesTask -> {
            if (userPreferencesTask.isSuccessful()) {
                DocumentSnapshot document = userPreferencesTask.getResult();
                if (document.exists()) {
                    isEmployer = String.valueOf(document.get("accountType")).equals("Munkáltató");
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", userPreferencesTask.getException());
            }
        });
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                companyNameText.setText("cég: " + Objects.requireNonNull(documentSnapshot.getData()).get("companyName"));
                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                isEmployer = String.valueOf(documentSnapshot.getData().get("accountType")).equals("Munkáltató");
                String welcomeString = documentSnapshot.getData().get("userName") == null ? "Felhasználó" : String.valueOf(documentSnapshot.getData().get("userName"));
                welcomeText.setText("Üdvözöljük " + welcomeString + "!");
            } else {
                Log.d(TAG, "Current data: null");
            }
        });

        if (isEmployer) {

        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menuList = menu;
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mainuseractivity_list_menu, menu);
        // Force showing icons for menu items
        if (menu instanceof MenuBuilder)
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        supportInvalidateOptionsMenu();
        return true;
    }

    @Override
    public void supportInvalidateOptionsMenu() {
        this.menuList.findItem(R.id.manageWorkers).setVisible(isEmployer);
        super.supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.manageWorkers) {
            Log.d(TAG, "ManageWorkers clicked!");
            manageWorkers();
            return true;
        } else if (item.getItemId() == R.id.logout) {
            Log.d(TAG, "Logout clicked!");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.profile) {
            Log.d(TAG, "Profile clicked!");
            showProfile();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void manageWorkers() {
        Intent manageWorkersIntent = new Intent(this, ManageWorkersActivity.class);
        manageWorkersIntent.putExtra("companyName", companyNameText.getText().toString().split(" ")[1]);
        startActivity(manageWorkersIntent);
    }

    private void showProfile() {
        Intent showProfileIntent = new Intent(this, ProfileActivity.class);
        startActivity(showProfileIntent);
    }
}