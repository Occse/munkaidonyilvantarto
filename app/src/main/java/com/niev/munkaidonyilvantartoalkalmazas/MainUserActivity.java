package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


public class MainUserActivity extends AppCompatActivity {
    private static final String TAG = MainUserActivity.class.getName();
    private final Calendar myCalendar = Calendar.getInstance();
    private FirebaseUser user;
    private FirebaseFirestore mFirestore;
    private String email;
    private boolean isEmployer;
    private boolean isUnemployed;
    private String companyName;
    private Menu menuList;
    private TextView welcomeText;
    private TextView companyNameText;
    private EditText workHourStart;
    private EditText workHourEnd;
    private EditText lunchHourStart;
    private EditText lunchHourEnd;

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
        if(!isEmployer) {
            docRef.get().addOnCompleteListener(userPreferencesTask -> {
                if (userPreferencesTask.isSuccessful()) {
                    DocumentSnapshot document = userPreferencesTask.getResult();
                    if (document.exists()) {
                        isEmployer = String.valueOf(document.get("accountType")).equals("Munkáltató");
                        isUnemployed = String.valueOf(document.get("companyName")).equals("Munkanélküli");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", userPreferencesTask.getException());
                }
            });
        }
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                companyNameText.setText("cég: " + Objects.requireNonNull(documentSnapshot.getData()).get("companyName"));
                Log.d(TAG, "Current data: " + documentSnapshot.getData());
                isEmployer = String.valueOf(documentSnapshot.getData().get("accountType")).equals("Munkáltató");
                isUnemployed = String.valueOf(documentSnapshot.get("companyName")).equals("Munkanélküli");
                companyName = String.valueOf(documentSnapshot.get("companyName"));
                String welcomeString = documentSnapshot.getData().get("userName") == null ? "Felhasználó" : String.valueOf(documentSnapshot.getData().get("userName"));
                welcomeText.setText("Üdvözöljük " + welcomeString + "!");
            } else {
                Log.d(TAG, "Current data: null");
            }
        });

        if (!isEmployer) {

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
        if (!isUnemployed) {
            this.menuList.findItem(R.id.manageWorkers).setVisible(isEmployer);
            this.menuList.findItem(R.id.addWorkHours).setVisible(!isEmployer);
        } else {
            this.menuList.findItem(R.id.manageWorkers).setVisible(false);
            this.menuList.findItem(R.id.addWorkHours).setVisible(false);
        }
        super.supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.manageWorkers) {
            Log.d(TAG, "ManageWorkers clicked!");
            manageWorkers();
            return true;
        } else if (item.getItemId() == R.id.addWorkHours) {
            Log.d(TAG, "addWorkHours clicked!");
            showAddHours();
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

    private void showAddHours() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_hours);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.roundedcorners);
        dialog.setCanceledOnTouchOutside(false);
        workHourStart = dialog.findViewById(R.id.hourWorkStart);
        workHourEnd = dialog.findViewById(R.id.hourWorkEnd);
        lunchHourStart = dialog.findViewById(R.id.hourLunchStart);
        lunchHourEnd = dialog.findViewById(R.id.hourLunchEnd);

        Button cancelButton = dialog.findViewById(R.id.cancelAddHour);
        cancelButton.setOnClickListener(view -> dialog.dismiss());

        Button addButton = dialog.findViewById(R.id.addHour);
        addButton.setOnClickListener(view -> addHours(dialog));

        workHourStart.setOnClickListener(view -> showTimePicker(workHourStart));
        workHourEnd.setOnClickListener(view -> showTimePicker(workHourEnd));
        lunchHourStart.setOnClickListener(view -> showTimePicker(lunchHourStart));
        lunchHourEnd.setOnClickListener(view -> showTimePicker(lunchHourEnd));

        dialog.show();
    }

    private void showTimePicker(EditText editText) {
        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(MainUserActivity.this, (timePicker, selectedHour, selectedMinute) -> editText.setText(selectedHour + ":" + convertMinute(selectedMinute)), hour, minute, true);
        mTimePicker.show();
    }

    private String convertMinute(int minute) {
        return minute < 10 ? "0" + minute : Integer.toString(minute);
    }

    private void addHours(Dialog dialog) {
        String workStart = String.valueOf(workHourStart.getText());
        String workEnd = String.valueOf(workHourEnd.getText());
        String lunchStart = String.valueOf(lunchHourStart.getText());
        String lunchEnd = String.valueOf(lunchHourEnd.getText());

        DocumentReference docRef = mFirestore.collection("UserPreferences").document(email);
        docRef.get().addOnCompleteListener(userPreferencesTask -> {
            if (userPreferencesTask.isSuccessful()) {
                DocumentSnapshot document = userPreferencesTask.getResult();
                if (document.exists()) {
                    String userId = String.valueOf(document.get("userId"));
                    mFirestore = FirebaseFirestore.getInstance();
                    HashMap<String, String> workerHourData = new HashMap<>();
                    Date todayDate = Calendar.getInstance().getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                    String formattedDate = dateFormat.format(todayDate);
                    workerHourData.put("worker", userId);
                    workerHourData.put("workDay", formattedDate);
                    workerHourData.put("workStart", workStart);
                    workerHourData.put("workEnd", workEnd);
                    workerHourData.put("lunchStart", lunchStart);
                    workerHourData.put("lunchEnd", lunchEnd);
                    double workHour;
                    String[] workStartArray = workStart.split(":");
                    String[] workEndArray = workEnd.split(":");
                    String[] lunchStartArray = lunchStart.split(":");
                    String[] lunchEndArray = lunchEnd.split(":");
                    workHour = calcWorkHour(workStartArray, workEndArray) - calcWorkHour(lunchStartArray, lunchEndArray);
                    workerHourData.put("workedHours", String.valueOf(workHour));
                    HashMap<String, Object> companyWorkerHourData = new HashMap<>();
                    String userDay = userId + "|" + formattedDate;
                    companyWorkerHourData.put(userDay, workerHourData);
                    mFirestore.collection("CompaniesWorkHours").document(companyName).set(companyWorkerHourData, SetOptions.merge());
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", userPreferencesTask.getException());
            }
        });
        dialog.dismiss();
    }

    private double calcWorkHour(String[] shorterTime, String[] longerTime) {
        int shorterTimeInMinutes = Integer.valueOf(shorterTime[0]) * 60 + Integer.valueOf(shorterTime[1]);
        int longerTimeInMinutes = Integer.valueOf(longerTime[0]) * 60 + Integer.valueOf(longerTime[1]);

        return (longerTimeInMinutes - shorterTimeInMinutes) / 60;
    }
}