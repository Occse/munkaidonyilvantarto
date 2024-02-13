package com.niev.munkaidonyilvantartoalkalmazas;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ShowHoursActivity extends AppCompatActivity {
    private static final String TAG = ShowHoursActivity.class.getName();
    private RecyclerView mRecyclerView;
    private ArrayList<HourData> montPickerOptions;
    private ArrayList<HourData> hours;
    private FirebaseFirestore mFirestore;
    private HourAdapter hAdapter;
    private DocumentReference mItems;
    private FirebaseUser user;
    private String email;
    private String companyName;
    private String workerId;
    private Spinner monthPicker;
    private ArrayAdapter<String> spinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_hours);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "auth success");
            email = user.getEmail();
        } else {
            Log.d(TAG, "auth failed");
            finish();
        }
        mFirestore = FirebaseFirestore.getInstance();
        companyName = getIntent().getStringExtra("companyName");
        workerId = getIntent().getStringExtra("workerId");
        hours = new ArrayList<>();
        montPickerOptions = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recyclerViewHours);
        monthPicker = findViewById(R.id.monthPicker);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        hAdapter = new HourAdapter(this, hours);
        mRecyclerView.setAdapter(hAdapter);
        mItems = mFirestore.collection("CompaniesWorkHours").document(companyName);
        loadMonthPicker();
        monthPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String monthPickerText = monthPicker.getSelectedItem().toString();
                if (monthPickerText.equals("Válassz hónapot!")) {
                    hours.clear();
                    hAdapter.notifyDataSetChanged();
                } else if (monthPickerText.equals("Minden")) {
                    loadHourData();
                } else {
                    loadHourData(monthPickerText);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    private void loadMonthPicker() {
        mItems.get().addOnCompleteListener(companiesTask -> {
            if (companiesTask.isSuccessful()) {
                DocumentSnapshot document = companiesTask.getResult();
                if (document.exists()) {
                    Map<String, Object> map = document.getData();
                    for (String key : map.keySet()) {
                        HashMap<String, String> currentData = (HashMap<String, String>) map.get(key);
                        if (currentData.toString().contains(workerId)) {
                            HourData hourData = new HourData(currentData);
                            montPickerOptions.add(hourData);
                        }
                    }
                    ArrayList<String> spinnerOptions = new ArrayList<>();
                    spinnerOptions.add("Válassz hónapot!");
                    spinnerOptions.add("Minden");
                    ArrayList<String> toSort = new ArrayList<>();
                    for (int i = 0; i < montPickerOptions.toArray().length - 1; i++) {
                        String currentMonthAndYear = montPickerOptions.get(i).getWorkDay("year") + "/" + montPickerOptions.get(i).getWorkDay("month");
                        if (!toSort.contains(currentMonthAndYear)) {
                            toSort.add(currentMonthAndYear);
                        }
                    }
                    Collections.sort(toSort);
                    Collections.reverse(toSort);
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

    private void loadHourData() {
        hours.clear();

        mItems.get().addOnCompleteListener(companiesTask -> {
            if (companiesTask.isSuccessful()) {
                DocumentSnapshot document = companiesTask.getResult();
                if (document.exists()) {
                    Map<String, Object> map = document.getData();
                    for (String key : map.keySet()) {
                        HashMap<String, String> currentData = (HashMap<String, String>) map.get(key);
                        if (currentData.toString().contains(workerId)) {
                            HourData hourData = new HourData(currentData);
                            hours.add(hourData);
                            Collections.sort(hours, new DateComparator());
                        }
                    }
                    hAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", companiesTask.getException());
            }
        });
    }

    private void loadHourData(String yearAndMonth) {
        hours.clear();

        mItems.get().addOnCompleteListener(companiesTask -> {
            if (companiesTask.isSuccessful()) {
                DocumentSnapshot document = companiesTask.getResult();
                if (document.exists()) {
                    Map<String, Object> map = document.getData();
                    for (String key : map.keySet()) {
                        HashMap<String, String> currentData = (HashMap<String, String>) map.get(key);
                        if (currentData.toString().contains(workerId) && currentData.toString().contains(yearAndMonth)) {
                            HourData hourData = new HourData(currentData);
                            hours.add(hourData);
                        }
                    }
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
class DateComparator implements Comparator<HourData>
{
    @Override
    public int compare(HourData dateFirst, HourData dateSecond) {
        return dateSecond.getWorkDay("full").compareTo(dateFirst.getWorkDay("full"));
    }
}