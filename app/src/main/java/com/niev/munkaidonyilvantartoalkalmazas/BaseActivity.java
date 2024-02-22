package com.niev.munkaidonyilvantartoalkalmazas;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public abstract class BaseActivity extends AppCompatActivity {
    static final String PREF_KEY = "com.niev.munkaidonyilvantartoalkalmazas";
    static SharedPreferences preferences;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    String email;
    boolean night;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        night = preferences.getBoolean("isMiamiTheme", false);
        setTheme(night ? R.style.Theme_MunkaidoNyilvantartoAlkalmazas_Miami : R.style.Theme_MunkaidoNyilvantartoAlkalmazas_Default);
    }

    protected void setDialogTheme(Dialog dialog) {
        boolean isMiamiTheme = preferences.getBoolean("isMiamiTheme", false);
        if (isMiamiTheme) {
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.roundedcorners);
        } else {
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.roundedcornerswhite);
        }
    }

    protected static boolean checkTheme() {
        return preferences.getBoolean("isMiamiTheme", false);
    }
}
