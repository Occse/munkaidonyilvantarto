package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainUserActivity extends BaseActivity {
    private static final String TAG = MainUserActivity.class.getName();
    private final Calendar myCalendar = Calendar.getInstance();
    private boolean isEmployer;
    private boolean isUnemployed;
    private String companyName;
    private String workerId;
    private Menu menuList;
    private TextView welcomeText;
    private TextView companyNameText;
    private EditText workHourStart;
    private EditText workHourEnd;
    private EditText lunchHourStart;
    private EditText lunchHourEnd;
    private Button manageWorkers;
    private Button addWorkHours;
    private Button showWorkHours;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        } else {
            Toast.makeText(MainUserActivity.this, "Nem vagy belépve!", Toast.LENGTH_LONG).show();
            finish();
        }
        welcomeText = findViewById(R.id.welcomeText);
        companyNameText = findViewById(R.id.companyNameText);
        mFirestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = mFirestore.collection("UserPreferences").document(email);
        if (!isEmployer) {
            docRef.get().addOnCompleteListener(userPreferencesTask -> {
                if (userPreferencesTask.isSuccessful()) {
                    DocumentSnapshot document = userPreferencesTask.getResult();
                    if (document.exists()) {
                        workerId = String.valueOf(document.get("userId"));
                        isEmployer = String.valueOf(document.get("accountType")).equals("Munkáltató");
                        isUnemployed = String.valueOf(document.get("companyName")).equals("Munkanélküli");
                        hideButtons();
                    }
                } else {
                    Log.d(TAG, "get failed with ", userPreferencesTask.getException());
                }
            });
        }
        manageWorkers = findViewById(R.id.manageWorkers);
        addWorkHours = findViewById(R.id.addWorkHours);
        showWorkHours = findViewById(R.id.showWorkHours);
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                companyNameText.setText("cég: " + Objects.requireNonNull(documentSnapshot.getData()).get("companyName"));
                isEmployer = String.valueOf(documentSnapshot.getData().get("accountType")).equals("Munkáltató");
                isUnemployed = String.valueOf(documentSnapshot.get("companyName")).equals("Munkanélküli");
                companyName = String.valueOf(documentSnapshot.get("companyName"));
                String welcomeString = documentSnapshot.getData().get("userName") == null ? "Felhasználó" : String.valueOf(documentSnapshot.getData().get("userName"));
                welcomeText.setText("Üdvözöljük " + welcomeString + "!");
            }
        });

    }

    private void hideButtons() {
        if (!isUnemployed) {
            manageWorkers.setVisibility(isEmployer ? View.VISIBLE : View.GONE);
            addWorkHours.setVisibility(!isEmployer ? View.VISIBLE : View.GONE);
            showWorkHours.setVisibility(!isEmployer ? View.VISIBLE : View.GONE);
        } else {
            manageWorkers.setVisibility(View.GONE);
            addWorkHours.setVisibility(View.GONE);
            showWorkHours.setVisibility(View.GONE);
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
            this.menuList.findItem(R.id.showWorkHours).setVisible(!isEmployer);
        } else {
            this.menuList.findItem(R.id.manageWorkers).setVisible(false);
            this.menuList.findItem(R.id.addWorkHours).setVisible(false);
            this.menuList.findItem(R.id.showWorkHours).setVisible(false);
        }
        super.supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.manageWorkers) {
            manageWorkers();
            return true;
        } else if (item.getItemId() == R.id.addWorkHours) {
            showAddHours();
            return true;
        } else if (item.getItemId() == R.id.showWorkHours) {
            showShowHours();
            return true;
        } else if (item.getItemId() == R.id.profile) {
            showProfile();
            return true;
        } else if (item.getItemId() == R.id.themeChanger) {
            changeTheme();
            return true;
        } else if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void changeTheme() {
        boolean isMiamiTheme = preferences.getBoolean("isMiamiTheme", false);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isMiamiTheme", !isMiamiTheme);
        editor.apply();
        recreate();
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
        setDialogTheme(dialog);
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
        lunchHourStart.setOnClickListener(view -> showTimePicker(lunchHourStart));
        lunchHourEnd.setOnClickListener(view -> showTimePicker(lunchHourEnd));
        workHourEnd.setOnClickListener(view -> showTimePicker(workHourEnd));

        dialog.show();
    }

    private void showShowHours() {
        Intent showHoursIntent = new Intent(this, ShowHoursActivity.class);
        showHoursIntent.putExtra("companyName", companyNameText.getText().toString().split(" ")[1]);
        showHoursIntent.putExtra("workerId", workerId);
        startActivity(showHoursIntent);
    }

    @SuppressLint("SetTextI18n")
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
                    HashMap<String, Object> workerHourData = new HashMap<>();
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
                    workerHourData.put("workedHours", workHour);
                    HashMap<String, Object> companyWorkerHourData = new HashMap<>();
                    String userDay = userId + "-" + formattedDate;
                    companyWorkerHourData.put(userDay, workerHourData);
                    mFirestore.collection("CompaniesWorkHours").document(companyName).set(companyWorkerHourData, SetOptions.merge());
                }
            } else {
                Log.d(TAG, "get failed with ", userPreferencesTask.getException());
            }
        });
        dialog.dismiss();
    }

    private double calcWorkHour(String[] shorterTime, String[] longerTime) {
        if (Objects.equals(shorterTime[0], "") || Objects.equals(longerTime[0], "")) {
            return 0;
        }
        double shorterTimeInMinutes = Integer.parseInt(shorterTime[0]) * 60 + Integer.parseInt(shorterTime[1]);
        double longerTimeInMinutes = Integer.parseInt(longerTime[0]) * 60 + Integer.parseInt(longerTime[1]);
        return (longerTimeInMinutes - shorterTimeInMinutes) / 60;
    }

    public void manageWorkers(View view) {
        manageWorkers();
    }

    public void showAddHours(View view) {
        showAddHours();
    }

    public void showWorkHours(View view) {
        showShowHours();
    }

    public void changeTheme(View view) {
        changeTheme();
    }

    public void showProfile(View view) {
        showProfile();
    }

    public void logout(View view) {
        finish();
    }
}