package com.niev.munkaidonyilvantartoalkalmazas;

import static android.widget.Toast.makeText;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();

    private static final int SECRET_KEY = 99;
    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText passwordAgainEditText;
    RadioGroup accountTypeGroup;
    SwitchMaterial agreement;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        int secretKey = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secretKey != 99) {
            finish();
        }
        userNameEditText = findViewById(R.id.userNameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordAgainEditText = findViewById(R.id.passwordAgainEditText);
        accountTypeGroup = findViewById(R.id.accountTypeGroup);
        agreement = findViewById(R.id.agreement);
        accountTypeGroup.check(R.id.worker);


        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        String email = preferences.getString("email", "");
        String password = preferences.getString("password", "");

        userEmailEditText.setText(email);
        passwordEditText.setText(password);
        passwordAgainEditText.setText(password);

        mAuth = FirebaseAuth.getInstance();

    }

    public void registration(View view) {
        String userName = userNameEditText.getText().toString();
        String email = userEmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordAgainEditText.getText().toString();
        boolean agreementConfirmed = agreement.isChecked();
        int accountTypeId = accountTypeGroup.getCheckedRadioButtonId();
        View radioButton = accountTypeGroup.findViewById(accountTypeId);
        int id = accountTypeGroup.indexOfChild(radioButton);
        String accountType = ((RadioButton) accountTypeGroup.getChildAt(id)).getText().toString();


        if (!password.equals(passwordConfirm)) {
            makeText(this, "The two passwords didn't match.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "The two passwords didn't match.");
            return;
        }
        if (!agreementConfirmed) {
            makeText(this, "Nem fogadtad el az adatkezelési nyilatkozatot!", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Agreement wasn't confirmed.");
            return;
        }
        Log.i(TAG, "Registered as: " + userName + ", e-mail: " + email + ", accountType: " + accountType);
        mFirestore = FirebaseFirestore.getInstance();
        HashMap<String, String> userData = new HashMap<>();
        userData.put("userName", userName);
        userData.put("password", password);
        userData.put("email", email);
        userData.put("accountType", accountType);
        if (accountType.equals("Dolgozó")) {
            userData.put("companyName", "Munkanélküli");
        }
        mFirestore.collection("UserPreferences").document(email).set(userData);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, userPreferencesTask -> {
            if (userPreferencesTask.isSuccessful()) {
                Log.d(TAG, "successful reg");
                finish();
                showMainUser();
            } else {
                Log.d(TAG, "reg failed");
                makeText(RegisterActivity.this, "Couldn't register user: " + Objects.requireNonNull(userPreferencesTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showMainUser() {
        Intent showMainUserIntent = new Intent(this, MainUserActivity.class);
        showMainUserIntent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(showMainUserIntent);
    }

    public void cancel(View view) {
        finish();
    }
}