package com.niev.munkaidonyilvantartoalkalmazas;

import static android.widget.Toast.makeText;

import android.app.DatePickerDialog;
import android.graphics.Color;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = ProfileActivity.class.getName();
    private final Calendar myCalendar = Calendar.getInstance();
    FirebaseUser user;
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
    TextView userEmailVerified;
    LinearLayout functionButtons;
    TextView workerCompanyName;
    boolean dataWarning = false;
    private String lastOptionText;
    private String lastID;
    private String error;
    private int errorCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();
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
        userEmailVerified = findViewById(R.id.userEmailVerified);
        boolean isMiamiTheme = checkTheme();
        if (isMiamiTheme) {
            userDegree.setBackgroundResource(R.drawable.roundedcorners);
            userDegree.setPopupBackgroundResource(R.drawable.roundedcorners);
        } else {
            userDegree.setBackgroundResource(R.drawable.blue_outline_white_background);
            userDegree.setPopupBackgroundResource(R.drawable.blue_outline_white_background);
        }
        if (Objects.requireNonNull(user).isEmailVerified()) {
            userEmailVerified.setText(R.string.verified);
            userEmailVerified.setTextColor(Color.GREEN);
        } else {
            userEmailVerified.setText(R.string.not_verified);
            userEmailVerified.setTextColor(Color.RED);
        }
        companyName = findViewById(R.id.companyName);
        companyNamePlaceholder = findViewById(R.id.companyNamePlaceholder);
        functionButtons = findViewById(R.id.functionButtons);
        workerCompanyName = findViewById(R.id.companyNameText);
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
                                workerCompanyName.setText(document.get("companyName") != "" ? String.valueOf(document.get("companyName")) : "Munkanélküli");
                                accountTypeGroup.check(R.id.worker);
                                companyName.setVisibility(View.INVISIBLE);
                                workerCompanyName.setVisibility(View.VISIBLE);
                            } else {
                                accountTypeGroup.check(R.id.employer);
                                companyName.setText(String.valueOf(document.get("companyName")));
                                companyName.setVisibility(View.VISIBLE);
                                workerCompanyName.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    accountTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        if (checkedId == R.id.employer) {
                            companyName.setVisibility(View.VISIBLE);
                            workerCompanyName.setVisibility(View.INVISIBLE);
                        } else {
                            if (workerCompanyName.getText().equals("")) {
                                workerCompanyName.setText(R.string.unemployed);
                            }
                            companyName.setVisibility(View.INVISIBLE);
                            workerCompanyName.setVisibility(View.VISIBLE);
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
        boolean correctIdFormat = true;
        boolean correctTAJFormat = true;
        boolean correctAdoFormat = true;
        String userNameText = userNameEditText.getText().toString();
        String userIdText = userId.getText().toString();
        String userTAJText = userTAJNumber.getText().toString();
        String userAdoText = userAdoKartya.getText().toString();
        String userLakcimText = userLakcim.getText().toString();
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
            correctIdFormat = digits == 6 && chars == 2;
        }
        if (userTAJText.length() > 0 && correctIdFormat) {
            int digits = 0;
            for (int i = 0; i < userTAJText.length(); i++) {
                if (userTAJText.charAt(i) >= 48 && userTAJText.charAt(i) <= 57) {
                    digits++;
                }
            }
            correctTAJFormat = digits == 9;
        }
        if (userAdoText.length() > 0 && correctIdFormat && correctTAJFormat) {
            int digits = 0;
            for (int i = 0; i < userAdoText.length(); i++) {
                if (userAdoText.charAt(i) >= 48 && userAdoText.charAt(i) <= 57) {
                    digits++;
                }
            }
            correctAdoFormat = digits == 10;
        }
        if (userLakcimText.length() > 0 && correctIdFormat && correctTAJFormat && correctAdoFormat) {
            int chars = 0;
            int digits = 0;
            for (int i = 0; i < userLakcimText.length(); i++) {
                if (userLakcimText.charAt(i) >= 48 && userLakcimText.charAt(i) <= 57) {
                    digits++;
                } else {
                    chars++;
                }
            }
            correctIdFormat = digits == 6 && chars == 2;
        }
        if (!correctIdFormat) {
            makeText(this, "Rossz személyigazolvány/lakcímkártya szám formátum! (12345ab)", Toast.LENGTH_LONG).show();
            return;
        } else if (!correctTAJFormat) {
            makeText(this, "Rossz TAJ szám formátum! (123456789)", Toast.LENGTH_LONG).show();
            return;
        } else if (!correctAdoFormat) {
            makeText(this, "Rossz adószám formátum! (1234567890)", Toast.LENGTH_LONG).show();
            return;
        }
        mFirestore = FirebaseFirestore.getInstance();
        HashMap<String, String> userIds = new HashMap<>();
        userIds.put("userName", userNameText);
        userIds.put("email", email);
        userIds.put("userId", userIdText);
        userIds.put("userTAJ", userTAJText);
        userIds.put("userAdo", userAdoText);
        userIds.put("userLakcim", userLakcimText);
        mFirestore.collection("UserPreferences").get().addOnCompleteListener(userCards -> {
            if (userCards.isSuccessful()) {
                QuerySnapshot documentSnapshot = userCards.getResult();
                error = "";
                errorCounter = 0;
                dataWarning = false;
                for (QueryDocumentSnapshot documentSnapshots : documentSnapshot) {
                    String documentData = documentSnapshots.getData().toString();
                    String userIdTextData = String.valueOf(documentSnapshots.getData().get("userId"));
                    String userTAJTextData = String.valueOf(documentSnapshots.getData().get("userTAJ"));
                    String userAdoTextData = String.valueOf(documentSnapshots.getData().get("userAdo"));
                    String userLakcimTextData = String.valueOf(documentSnapshots.getData().get("userLakcim"));
                    if (userIdTextData.equals(userIdText) && !documentData.contains(email)) {
                        dataWarning = true;
                        error = "Személyigazolvány";
                        errorCounter++;
                    }
                    if (userTAJTextData.equals(userTAJText) && !documentData.contains(email)) {
                        dataWarning = true;
                        if (errorCounter > 0) {
                            error += ", TAJ kártya";
                        } else {
                            error = "TAJ kártya";
                        }
                        errorCounter++;
                    }
                    if (userAdoTextData.equals(userAdoText) && !documentData.contains(email)) {
                        dataWarning = true;
                        if (errorCounter > 0) {
                            error += ", Adó kártya";
                        } else {
                            error = "Adó kártya";
                        }
                        errorCounter++;
                    }
                    if (userLakcimTextData.equals(userLakcimText) && !documentData.contains(email)) {
                        dataWarning = true;
                        if (errorCounter > 0) {
                            error += ", Lakcímkártya";
                        } else {
                            error = "Lakcímkártya";
                        }
                        errorCounter++;
                    }
                }
                if (!dataWarning) {
                    DocumentReference docRef = mFirestore.collection("userCardIDs").document(userIdText);
                    docRef.get().addOnCompleteListener(userCardsIdTask -> {
                        if (userCardsIdTask.isSuccessful()) {
                            DocumentSnapshot document = userCardsIdTask.getResult();
                            if (document.get("email") == null || email.equals(document.get("email"))) {
                                if (document.exists() && userIdText.equals(lastID)) {
                                    mFirestore.collection("userCardIDs").document(userIdText).set(userIds, SetOptions.merge());
                                } else if (!document.exists()) {
                                    mFirestore.collection("userCardIDs").document(lastID).delete().addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!")).addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                                    mFirestore.collection("userCardIDs").document(userIdText).set(userIds);
                                }
                            }
                        } else {
                            Log.d(TAG, "get failed with ", userCardsIdTask.getException());
                        }
                    });
                    saveData();
                } else {
                    Toast.makeText(ProfileActivity.this, "Van már személy rendelve ehhez a " + error + " számhoz!", Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.d(TAG, "Error getting documents: ", userCards.getException());
            }
        });
    }

    public void saveData() {
        String userNameText = userNameEditText.getText().toString();
        String userIdText = userId.getText().toString();
        String userTAJText = userTAJNumber.getText().toString();
        String userAdoText = userAdoKartya.getText().toString();
        String userLakcimText = userLakcim.getText().toString();
        String userDegreeText = userDegree.getSelectedItem().toString();
        String birthDateText = birthDateEditText.getText().toString();
        String companyNameTextVal;
        int accountTypeId = accountTypeGroup.getCheckedRadioButtonId();
        View radioButton = accountTypeGroup.findViewById(accountTypeId);
        int id = accountTypeGroup.indexOfChild(radioButton);
        String accountTypeText = ((RadioButton) accountTypeGroup.getChildAt(id)).getText().toString();
        if (accountTypeText.equals("Munkáltató")) {
            companyNameTextVal = companyName.getText().toString();
        } else {
            companyNameTextVal = workerCompanyName.getText().toString() == null ? "Munkanélküli" : workerCompanyName.getText().toString();
        }
        HashMap<String, String> userData = new HashMap<>();
        userData.put("userName", userNameText);
        userData.put("userId", userIdText);
        userData.put("userTAJ", userTAJText);
        userData.put("userAdo", userAdoText);
        userData.put("userLakcim", userLakcimText);
        userData.put("userDegree", userDegreeText);
        userData.put("userBirthDate", birthDateText);
        userData.put("accountType", accountTypeText);
        userData.put("companyName", companyNameTextVal);
        if (!accountTypeText.equals("Dolgozó")) {
            HashMap<String, Object> companyData = new HashMap<>();
            companyData.put("EmployerData", userData);
            mFirestore.collection("Companies").document(companyNameTextVal).get().addOnCompleteListener(companyCheck -> {
                if (companyCheck.isSuccessful()) {
                    if (String.valueOf(companyCheck.getResult().get("EmployerData")).contains(email)) {
                        mFirestore.collection("UserPreferences").document(email).set(userData, SetOptions.merge());
                        mFirestore.collection("Companies").document(companyNameTextVal).set(companyData, SetOptions.merge());
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Van már ilyen nevű cég!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mFirestore.collection("UserPreferences").document(email).set(userData, SetOptions.merge());
                    mFirestore.collection("Companies").document(companyNameTextVal).set(companyData, SetOptions.merge());
                    finish();
                }
            });
        } else {
            mFirestore.collection("UserPreferences").document(email).set(userData, SetOptions.merge());
            finish();
        }
    }
}