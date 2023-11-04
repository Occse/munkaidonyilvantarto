package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainUserActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainUserActivity.class.getName();
    private FirebaseUser user;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "auth success");
        } else {
            Log.d(LOG_TAG, "auth failed");
            finish();
        }
        mFirestore = FirebaseFirestore.getInstance();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mainuseractivity_list_menu, menu);
        // Force showing icons for menu items
        if (menu instanceof MenuBuilder)
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
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

    private void showProfile() {
        Intent showProfileIntent = new Intent(this, ProfileActivity.class);
        startActivity(showProfileIntent);
    }
}