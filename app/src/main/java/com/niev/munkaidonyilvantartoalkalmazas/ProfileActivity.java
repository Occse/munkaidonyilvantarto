package com.niev.munkaidonyilvantartoalkalmazas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProfileActivity.class.getName();
    private final Calendar myCalendar = Calendar.getInstance();
    EditText userNameEditText;
    EditText userEmailEditText;
    EditText userIdNumber;
    EditText userTAJNumber;
    EditText userAdoKartya;
    EditText userLakcim;
    Spinner userDegree;
    EditText birthDateEditText;
    RadioGroup accountTypeGroup;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore mFirestore;
    private CollectionReference mCollection;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "auth success");
            email = user.getEmail();
        } else {
            Log.d(LOG_TAG, "auth failed");
            finish();
        }
        userNameEditText = findViewById(R.id.userNameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        userIdNumber = findViewById(R.id.userIdNumber);
        userTAJNumber = findViewById(R.id.userTAJNumber);
        userAdoKartya = findViewById(R.id.userAdoKartya);
        userLakcim = findViewById(R.id.userLakcim);
        userDegree = findViewById(R.id.userDegree);
        //create a list of items for the spinner.
        String[] degrees = new String[]{"Nincs képesítés", "6 osztály", "8. osztály", "Középiskola/Gimnázium", "Érettségi", "Diploma"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, degrees);
        //set the spinners adapter to the previously created one.
        userDegree.setAdapter(adapter);

        birthDateEditText = findViewById(R.id.userBirthDate);
        accountTypeGroup = findViewById(R.id.accountTypeGroup);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("UserPreferences").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(LOG_TAG, "DocumentSnapshot data: " + document.getData());
                        userNameEditText.setText(String.valueOf(document.get("userName")));
                        userEmailEditText.setText(String.valueOf(document.get("email")));
                        for (int i = 0; i < accountTypeGroup.getChildCount(); i++) {
                            if (((RadioButton) accountTypeGroup.getChildAt(i)).getText().toString().equals(String.valueOf(document.get("accountType")))) {
                                if (String.valueOf(document.get("accountType")).equals("Dolgozó")) {
                                    accountTypeGroup.check(R.id.worker);
                                } else {
                                    accountTypeGroup.check(R.id.employer);
                                }
                            }
                        }
                        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, month);
                                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                                updateLabel();
                            }
                        };
                        birthDateEditText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new DatePickerDialog(ProfileActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                            }
                        });
                        //
                        //
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void updateLabel() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        birthDateEditText.setText(dateFormat.format(myCalendar.getTime()));
    }

    public void cancel(View view) {
        finish();
    }

    public void save(View view) {
        finish();
    }
}