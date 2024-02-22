package com.niev.munkaidonyilvantartoalkalmazas;

import static android.widget.Toast.makeText;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = ProfileActivity.class.getName();
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
    TextView companyNameText;
    private String lastOptionText;
    private String lastID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        } else {
            Toast.makeText(ProfileActivity.this, "Nem vagy belépve!", Toast.LENGTH_LONG).show();
            finish();
        }
        userNameEditText = findViewById(R.id.userNameEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        userId = findViewById(R.id.userId);
        userTAJNumber = findViewById(R.id.userTAJNumber);
        userAdoKartya = findViewById(R.id.userAdoKartya);
        userLakcim = findViewById(R.id.userLakcim);
        userDegree = findViewById(R.id.userDegree);
        boolean isMiamiTheme = checkTheme();
        if(isMiamiTheme) {
            userDegree.setBackgroundResource(R.drawable.roundedcorners);
            userDegree.setPopupBackgroundResource(R.drawable.roundedcorners);
        } else {
            userDegree.setBackgroundResource(R.drawable.blue_outline_white_background);
            userDegree.setPopupBackgroundResource(R.drawable.blue_outline_white_background);
        }
        companyName = findViewById(R.id.companyName);
        companyNamePlaceholder = findViewById(R.id.companyNamePlaceholder);
        functionButtons = findViewById(R.id.functionButtons);
        companyNameText = findViewById(R.id.companyNameText);
        String[] degrees = new String[]{"Nincs képesítés", "6 osztály", "8. osztály", "Középiskola/Gimnázium", "Érettségi", "Diploma"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, degrees);
        userDegree.setAdapter(adapter);

        birthDateEditText = findViewById(R.id.userBirthDate);
        accountTypeGroup = findViewById(R.id.accountTypeGroup);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("UserPreferences").document(email);
        docRef.get().addOnCompleteListener(userPreferencesTask -> {
            if (userPreferencesTask.isSuccessful()) {
                DocumentSnapshot document = userPreferencesTask.getResult();
                if (document.exists()) {
                    userNameEditText.setText(String.valueOf(document.get("userName")));
                    userEmailEditText.setText(String.valueOf(document.get("email")));
                    userId.setText(String.valueOf(document.get("userId") == null ? "" : (document.get("userId"))));
                    lastID = String.valueOf(document.get("userId"));
                    userTAJNumber.setText(String.valueOf(document.get("userTAJ") == null ? "" : (document.get("userTAJ"))));
                    userAdoKartya.setText(String.valueOf(document.get("userAdo") == null ? "" : (document.get("userAdo"))));
                    userLakcim.setText(String.valueOf(document.get("userLakcim") == null ? "" : (document.get("userLakcim"))));
                    int index = adapter.getPosition(document.get("userDegree") == null ? "Nincs képzés" : (String) document.get("userDegree"));
                    userDegree.setSelection(index);
                    birthDateEditText.setText(String.valueOf(document.get("userBirthDate") == null ? "" : (document.get("userBirthDate"))));
                    for (int i = 0; i < accountTypeGroup.getChildCount(); i++) {
                        if (((RadioButton) accountTypeGroup.getChildAt(i)).getText().toString().equals(String.valueOf(document.get("accountType")))) {
                            if (String.valueOf(document.get("accountType")).equals("Dolgozó")) {
                                companyNameText.setText(document.get("companyName") != null ? String.valueOf(document.get("companyName")) : "Munkanélküli");
                                accountTypeGroup.check(R.id.worker);
                                companyName.setVisibility(View.INVISIBLE);
                                companyNameText.setVisibility(View.VISIBLE);
                                lastOptionText = String.valueOf(companyName.getText());
                                companyName.setText(lastOptionText);
                            } else {
                                accountTypeGroup.check(R.id.employer);
                                companyName.setText(String.valueOf(document.get("companyName")));
                                companyName.setVisibility(View.VISIBLE);
                                companyNameText.setVisibility(View.INVISIBLE);
                                lastOptionText = String.valueOf(companyNameText.getText());
                                companyNameText.setText(lastOptionText);
                            }
                        }
                    }
                    accountTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        if (checkedId == R.id.employer) {
                            companyName.setVisibility(View.VISIBLE);
                            companyNameText.setVisibility(View.INVISIBLE);
                            lastOptionText = String.valueOf(companyName.getText());
                            companyName.setText(lastOptionText);
                        } else {
                            if (lastOptionText.equals("")) {
                                companyNameText.setText(R.string.unemployed);
                            } else {
                                lastOptionText = String.valueOf(companyNameText.getText());
                                companyNameText.setText(lastOptionText);
                            }
                            companyName.setVisibility(View.INVISIBLE);
                            companyNameText.setVisibility(View.VISIBLE);
                        }
                    });
                    DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, day);
                        updateLabel();
                    };
                    birthDateEditText.setOnClickListener(view -> new DatePickerDialog(ProfileActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show());
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", userPreferencesTask.getException());
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
        String companyNameText;
        int accountTypeId = accountTypeGroup.getCheckedRadioButtonId();
        View radioButton = accountTypeGroup.findViewById(accountTypeId);
        int id = accountTypeGroup.indexOfChild(radioButton);
        String accountTypeText = ((RadioButton) accountTypeGroup.getChildAt(id)).getText().toString();
        if (accountTypeText.equals("Munkáltató")) {
            companyNameText = companyName.getText().toString();
        } else {
            companyNameText = "Munkanélküli";
        }
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
            makeText(this, "Wrong ID format! (12345ab)", Toast.LENGTH_LONG).show();
            return;
        }
        mFirestore = FirebaseFirestore.getInstance();
        HashMap<String, String> userIds = new HashMap<>();
        userIds.put("userName", userNameText);
        userIds.put("userId", userIdText);
        userIds.put("email", email);
        DocumentReference docRef = mFirestore.collection("userCardIDs").document(userIdText);
        docRef.get().addOnCompleteListener(userCarsIdTask -> {
            if (userCarsIdTask.isSuccessful()) {
                DocumentSnapshot document = userCarsIdTask.getResult();
                if (document.exists() && userIdText.equals(lastID)) {
                    mFirestore.collection("userCardIDs").document(userIdText).set(userIds, SetOptions.merge());
                } else if (!document.exists()) {
                    mFirestore.collection("userCardIDs").document(lastID).delete().addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                    mFirestore.collection("userCardIDs").document(userIdText).set(userIds);
                }
            } else {
                Log.d(TAG, "get failed with ", userCarsIdTask.getException());
            }
        });


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
        if (!companyNameText.equals("Munkanélküli")) {
            userData.remove("accountType");
            userData.remove("companyName");
            HashMap<String, Object> companyData = new HashMap<>();
            companyData.put("EmployerData", userData);
            mFirestore.collection("Companies").document(companyNameText).set(companyData, SetOptions.merge());
        }
        finish();
    }
}