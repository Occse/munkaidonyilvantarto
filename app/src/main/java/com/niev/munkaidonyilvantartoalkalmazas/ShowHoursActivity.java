package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ShowHoursActivity extends BaseActivity {
    private static final String TAG = ShowHoursActivity.class.getName();
    private ArrayList<HourData> montPickerOptions;
    private ArrayList<HourData> hours;
    private HourAdapter hAdapter;
    private DocumentReference mItems;
    private String workerId;
    private Spinner monthPicker;
    private ArrayAdapter<String> spinnerAdapter;
    private TextView monthHoursText;
    private TextView monthHoursMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_hours);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(ShowHoursActivity.this, "Nem vagy belépve!", Toast.LENGTH_LONG).show();
            finish();
        }
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        String companyName = getIntent().getStringExtra("companyName");
        workerId = getIntent().getStringExtra("workerId");
        hours = new ArrayList<>();
        montPickerOptions = new ArrayList<>();
        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewHours);
        monthPicker = findViewById(R.id.monthPicker);
        boolean isMiamiTheme = checkTheme();
        if (isMiamiTheme) {
            monthPicker.setBackgroundResource(R.drawable.roundedcorners);
            monthPicker.setPopupBackgroundResource(R.drawable.roundedcorners);
        } else {
            monthPicker.setBackgroundResource(R.drawable.blue_outline_white_background);
            monthPicker.setPopupBackgroundResource(R.drawable.blue_outline_white_background);
        }
        monthHoursText = findViewById(R.id.monthHoursText);
        monthHoursMain = findViewById(R.id.monthHours);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        hAdapter = new HourAdapter(this, hours);
        mRecyclerView.setAdapter(hAdapter);
        mItems = mFirestore.collection("CompaniesWorkHours").document(companyName);
        loadMonthPicker();
        monthPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String monthPickerText = monthPicker.getSelectedItem().toString();
                if (monthPickerText.equals("Válassz hónapot!") || monthPickerText.equals("Nincs munkaidő megadva!")) {
                    hours.clear();
                    monthHoursMain.setText("");
                    monthHoursText.setText("");
                    hAdapter.notifyDataSetChanged();
                } else if (monthPickerText.equals("Minden")) {
                    loadHourData("");
                } else {
                    loadHourData(monthPickerText);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMonthPicker() {
        mItems.get().addOnCompleteListener(companiesTask -> {
            if (companiesTask.isSuccessful()) {
                DocumentSnapshot document = companiesTask.getResult();
                if (document.exists()) {
                    Map<String, Object> map = document.getData();
                    for (String key : map.keySet()) {
                        HashMap<String, Object> currentData = (HashMap<String, Object>) map.get(key);
                        if (currentData.toString().contains(workerId)) {
                            HourData hourData = new HourData(currentData);
                            montPickerOptions.add(hourData);
                        }
                    }
                    ArrayList<String> spinnerOptions = new ArrayList<>();
                    ArrayList<String> toSort = new ArrayList<>();
                    for (int i = 0; i < montPickerOptions.toArray().length; i++) {
                        String currentMonthAndYear = montPickerOptions.get(i).getWorkDay("year") + "/" + montPickerOptions.get(i).getWorkDay("month");
                        if (!toSort.contains(currentMonthAndYear)) {
                            toSort.add(currentMonthAndYear);
                        }
                    }
                    Collections.sort(toSort);
                    Collections.reverse(toSort);
                    if (toSort.size() > 0) {
                        spinnerOptions.add("Válassz hónapot!");
                        spinnerOptions.add("Minden");
                    } else {
                        monthPicker.setEnabled(false);
                        monthPicker.setClickable(false);
                        monthPicker.setAdapter(spinnerAdapter);
                        spinnerOptions.add("Nincs munkaidő megadva!");
                    }
                    spinnerOptions.addAll(toSort);
                    String[] spinnerOptionsArray = new String[spinnerOptions.size()];
                    for (int i = 0; i < spinnerOptions.size(); i++) {
                        spinnerOptionsArray[i] = spinnerOptions.get(i);
                    }
                    spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptionsArray);
                    monthPicker.setAdapter(spinnerAdapter);
                    hAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", companiesTask.getException());
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void loadHourData(String yearAndMonth) {
        hours.clear();


        mItems.get().addOnCompleteListener(companiesTask -> {
            double hour = 0;
            if (companiesTask.isSuccessful()) {
                DocumentSnapshot document = companiesTask.getResult();
                if (document.exists()) {
                    Map<String, Object> map = document.getData();
                    for (String key : map.keySet()) {
                        HashMap<String, Object> currentData = (HashMap<String, Object>) map.get(key);
                        if (currentData.toString().contains(workerId) && currentData.toString().contains(yearAndMonth)) {
                            HourData hourData = new HourData(currentData);
                            hours.add(hourData);
                            hours.sort(new DateComparator());
                        }
                    }
                    for (int i = 0; i < hours.size(); i++) {
                        hour += hours.get(i).getWorkedHours() instanceof Long ? ((Long) hours.get(i).getWorkedHours()).doubleValue() : (double) hours.get(i).getWorkedHours();
                    }
                    monthHoursMain.setText(R.string.monthHours);
                    BigDecimal hourFormat = new BigDecimal(hour);
                    BigDecimal formattedHour = hourFormat.setScale(1, RoundingMode.HALF_UP);
                    monthHoursText.setText(formattedHour + " óra");
                    hAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", companiesTask.getException());
            }
        });
    }
}

class DateComparator implements Comparator<HourData> {
    @Override
    public int compare(HourData dateFirst, HourData dateSecond) {
        return dateSecond.getWorkDay("full").compareTo(dateFirst.getWorkDay("full"));
    }
}