package com.niev.munkaidonyilvantartoalkalmazas;

import static android.widget.Toast.makeText;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProfileActivity.class.getName();
    private final Calendar myCalendar = Calendar.getInstance();
    EditText userNameEditText;
    TextView userEmailEditText;
    EditText userId;
    EditText userTAJNumber;
    EditText userAdoKartya;
    EditText userLakcim;
    Spinner userDegree;
    EditText birthDateEditText;
    RadioGroup accountTypeGroup;
    EditText companyName;
    TextView companyNamePlaceholder;
    LinearLayout functionButtons;
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
        userId = findViewById(R.id.userId);
        userTAJNumber = findViewById(R.id.userTAJNumber);
        userAdoKartya = findViewById(R.id.userAdoKartya);
        userLakcim = findViewById(R.id.userLakcim);
        userDegree = findViewById(R.id.userDegree);
        companyName = findViewById(R.id.companyName);
        companyNamePlaceholder = findViewById(R.id.companyNamePlaceholder);
        functionButtons = findViewById(R.id.functionButtons);
        String[] degrees = new String[]{"Nincs képesítés", "6 osztály", "8. osztály", "Középiskola/Gimnázium", "Érettségi", "Diploma"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, degrees);
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
                        userId.setText(String.valueOf(document.get("userId") == null ? "" : (document.get("userId"))));
                        userTAJNumber.setText(String.valueOf(document.get("userTAJ") == null ? "" : (document.get("userTAJ"))));
                        userAdoKartya.setText(String.valueOf(document.get("userAdo") == null ? "" : (document.get("userAdo"))));
                        userLakcim.setText(String.valueOf(document.get("userLakcim") == null ? "" : (document.get("userLakcim"))));
                        int index = adapter.getPosition(document.get("userDegree") == null ? "Nincs képzés" : (String) document.get("userDegree"));
                        userDegree.setSelection(index);
                        birthDateEditText.setText(String.valueOf(document.get("userBirthDate") == null ? "" : (document.get("userBirthDate"))));
                        for (int i = 0; i < accountTypeGroup.getChildCount(); i++) {
                            if (((RadioButton) accountTypeGroup.getChildAt(i)).getText().toString().equals(String.valueOf(document.get("accountType")))) {
                                if (String.valueOf(document.get("accountType")).equals("Dolgozó")) {
                                    accountTypeGroup.check(R.id.worker);
                                    companyName.setVisibility(View.GONE);
                                    companyNamePlaceholder.setVisibility(View.GONE);
                                } else {
                                    accountTypeGroup.check(R.id.employer);
                                    companyName.setText(String.valueOf(document.get("companyName")));
                                    companyName.setVisibility(View.VISIBLE);
                                    companyNamePlaceholder.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        accountTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if (checkedId == R.id.employer) {
                                    companyName.setVisibility(View.VISIBLE);
                                    companyNamePlaceholder.setVisibility(View.VISIBLE);
                                } else {
                                    companyName.setVisibility(View.GONE);
                                    companyNamePlaceholder.setVisibility(View.GONE);
                                }
                            }
                        });
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
        boolean correctFormat = true;
        String userNameText = userNameEditText.getText().toString();
        String userIdText = userId.getText().toString();
        String userTAJText = userTAJNumber.getText().toString();
        String userAdoText = userAdoKartya.getText().toString();
        String userLakcimText = userLakcim.getText().toString();
        String userDegreeText = userDegree.getSelectedItem().toString();
        String birthDateText = birthDateEditText.getText().toString();
        String companyNameText = companyName.getText().toString();
        int accountTypeId = accountTypeGroup.getCheckedRadioButtonId();
        View radioButton = accountTypeGroup.findViewById(accountTypeId);
        int id = accountTypeGroup.indexOfChild(radioButton);
        String accountTypeText = ((RadioButton) accountTypeGroup.getChildAt(id)).getText().toString();
        if (userIdText.length() > 0) {
            int chars = 0;
            int digits = 0;
            for (int i = 0; i < userIdText.length(); i++) {
                if (userIdText.charAt(i) >= 48 && userIdText.charAt(i) <= 57) {
                    digits++;
                } else {
                    chars++;
                }
            }
            correctFormat = digits == 6 && chars == 2;
        }
        if (userTAJText.length() > 0 && correctFormat) {
            int digits = 0;
            for (int i = 0; i < userTAJText.length(); i++) {
                if (userTAJText.charAt(i) >= 48 && userTAJText.charAt(i) <= 57) {
                    digits++;
                }
            }
            correctFormat = digits == 9;
        }
        if (userAdoText.length() > 0 && correctFormat) {
            int digits = 0;
            for (int i = 0; i < userAdoText.length(); i++) {
                if (userAdoText.charAt(i) >= 48 && userAdoText.charAt(i) <= 57) {
                    digits++;
                }
            }
            correctFormat = digits == 10;
        }
        if (userLakcimText.length() > 0 && correctFormat) {
            int chars = 0;
            int digits = 0;
            for (int i = 0; i < userLakcimText.length(); i++) {
                if (userLakcimText.charAt(i) >= 48 && userLakcimText.charAt(i) <= 57) {
                    digits++;
                } else {
                    chars++;
                }
            }
            correctFormat = digits == 6 && chars == 2;
        }
        if (!correctFormat) {
            makeText(this, "Wrong ID format.", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "ID misformat.");
            return;
        }
        mFirestore = FirebaseFirestore.getInstance();
        HashMap<String, String> userData = new HashMap<>();
        userData.put("userName", userNameText);
        userData.put("userId", userIdText);
        userData.put("userTAJ", userTAJText);
        userData.put("userAdo", userAdoText);
        userData.put("userLakcim", userLakcimText);
        userData.put("userDegree", userDegreeText);
        userData.put("userBirthDate", birthDateText);
        userData.put("accountType", accountTypeText);
        userData.put("companyName", companyNameText);
        mFirestore.collection("UserPreferences").document(email).set(userData, SetOptions.merge());
        finish();
    }
}