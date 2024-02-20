package com.niev.munkaidonyilvantartoalkalmazas;

import static android.widget.Toast.makeText;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ManageWorkersActivity extends AppCompatActivity {
    private static final String TAG = ManageWorkersActivity.class.getName();
    private ArrayList<WorkerData> workers;
    private FirebaseFirestore mFirestore;
    private WorkerAdapter mAdapter;
    private DocumentReference mItems;
    private String companyName;
    private int members;
    private int fullCount;
    private String workerEmail;
    private String company;
    private EditText workerID;
    private EditText workerMunkakor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_workers);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(ManageWorkersActivity.this, "You are not logged in!", Toast.LENGTH_LONG).show();
            finish();
        }
        mFirestore = FirebaseFirestore.getInstance();
        companyName = getIntent().getStringExtra("companyName");
        workers = new ArrayList<>();
        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewWorkers);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new WorkerAdapter(this, workers);
        mRecyclerView.setAdapter(mAdapter);
        mItems = mFirestore.collection("Companies").document(companyName);
        loadWorkerData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadWorkerData() {
        workers.clear();

        mItems.get().addOnCompleteListener(companiesTask -> {
            if (companiesTask.isSuccessful()) {
                DocumentSnapshot document = companiesTask.getResult();
                if (document.exists()) {
                    //counting workers
                    DocumentReference docRef = mFirestore.collection("Companies").document(companyName);
                    docRef.get().addOnCompleteListener(countWorkersTask -> {
                        if (countWorkersTask.isSuccessful()) {
                            DocumentSnapshot document1 = countWorkersTask.getResult();
                            if (document1.exists()) {
                                Map<String, Object> map = document1.getData();
                                fullCount = map.size();
                                members = map.size() - 1;
                                for (int i = 0; i < fullCount; i++) {
                                    if (document1.get("Worker" + i + "Data") == null) {
                                        continue;
                                    }
                                    HashMap<String, String> currentData = (HashMap<String, String>) document1.get("Worker" + i + "Data");
                                    WorkerData workerData = new WorkerData(currentData);
                                    workers.add(workerData);
                                    // Notify the adapter of the change
                                    mAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.d(TAG, "Count failed: ", countWorkersTask.getException());
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", companiesTask.getException());
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        supportInvalidateOptionsMenu();
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.manageworkers_list_menu, menu);
        // Force showing icons for menu items
        if (menu instanceof MenuBuilder)
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addWorker) {
            addWorkerDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void addWorkerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_worker);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.roundedcorners);
        dialog.setCanceledOnTouchOutside(false);
        workerID = dialog.findViewById(R.id.workerID);
        workerMunkakor = dialog.findViewById(R.id.userMunkakor);

        Button cancelButton = dialog.findViewById(R.id.cancelAddWorker);
        cancelButton.setOnClickListener(view -> dialog.dismiss());

        Button addButton = dialog.findViewById(R.id.addWorkerButton);
        addButton.setOnClickListener(view -> addWorker());

        dialog.show();
    }

    public void addWorker() {
        String workerIDText = String.valueOf(workerID.getText());
        String workerMunkakorText = String.valueOf(workerMunkakor.getText());
        if (!workerIDText.equals("") && !workerMunkakorText.equals("")) {
            HashMap<String, String> workerData = new HashMap<>();
            mFirestore = FirebaseFirestore.getInstance();
            // getting user from cardID
            mFirestore.collection("userCardIDs").document(workerIDText).get().addOnCompleteListener(userCardIdTask -> {
                if (userCardIdTask.isSuccessful()) {
                    DocumentSnapshot document = userCardIdTask.getResult();
                    if (document.exists()) {
                        workerEmail = String.valueOf(document.get("email"));
                        DocumentReference docRef = mFirestore.collection("UserPreferences").document(String.valueOf(document.get("email")));
                        docRef.get().addOnCompleteListener(userPreferencesTask -> {
                            if (userPreferencesTask.isSuccessful()) {
                                DocumentSnapshot workerDoc = userPreferencesTask.getResult();
                                if (workerDoc.exists()) {
                                    workerData.put("userName", String.valueOf(workerDoc.get("userName")));
                                    workerData.put("userId", String.valueOf(workerDoc.get("userId")));
                                    workerData.put("userTAJ", String.valueOf(workerDoc.get("userTAJ")));
                                    workerData.put("userAdo", String.valueOf(workerDoc.get("userAdo")));
                                    workerData.put("email", String.valueOf(workerDoc.get("email")));
                                    workerData.put("userLakcim", String.valueOf(workerDoc.get("userLakcim")));
                                    workerData.put("userDegree", String.valueOf(workerDoc.get("userDegree")));
                                    workerData.put("userBirthDate", String.valueOf(workerDoc.get("userBirthDate")));
                                    workerData.put("userMunkakor", workerMunkakorText);
                                    company = String.valueOf(workerDoc.get("companyName"));
                                    //counting workers
                                    DocumentReference docRef1 = mFirestore.collection("Companies").document(companyName);
                                    docRef1.get().addOnCompleteListener(companyMembersTask -> {
                                        if (companyMembersTask.isSuccessful()) {
                                            DocumentSnapshot document1 = companyMembersTask.getResult();
                                            if (document1.exists()) {
                                                Map<String, Object> map = document1.getData();
                                                members = map.size() - 1;
                                                //adding worker
                                                HashMap<String, Object> companyWorkerData = new HashMap<>();
                                                workerData.put("id", String.valueOf(members));
                                                companyWorkerData.put("Worker" + members + "Data", workerData);
                                                mFirestore.collection("Companies").document(companyName).get().addOnCompleteListener(companyAddTask -> {
                                                    if (companyAddTask.isSuccessful()) {
                                                        DocumentSnapshot document11 = companyAddTask.getResult();
                                                        if (document11.exists()) {
                                                            if (!document11.toString().contains("userId=" + workerData.get("userId")) && !company.equals(companyName) && company.equals("Munkanélküli")) {
                                                                mFirestore.collection("Companies").document(companyName).set(companyWorkerData, SetOptions.merge());
                                                                HashMap<String, String> userData = new HashMap<>();
                                                                userData.put("companyName", companyName);
                                                                mFirestore.collection("UserPreferences").document(workerEmail).set(userData, SetOptions.merge());
                                                                Intent intent = getIntent();
                                                                finish();
                                                                startActivity(intent);
                                                                overridePendingTransition(0, 0);
                                                            } else {
                                                                makeText(ManageWorkersActivity.this, "Worker is already in a company", Toast.LENGTH_LONG).show();
                                                            }
                                                        } else {
                                                            Log.d(TAG, "No such document");
                                                        }
                                                    } else {
                                                        Log.d(TAG, "get failed with ", companyAddTask.getException());
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", userPreferencesTask.getException());
                            }
                        });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", userCardIdTask.getException());
                }
            });
        } else {
            String empty = "";
            boolean emptyId = false;
            if (workerIDText.equals("")) {
                empty += "személyigazolványszám";
                emptyId = true;
            }
            if (workerMunkakorText.equals("")) {
                empty += emptyId ? ", " : "";
                empty += "munkakör";
            }

            Toast.makeText(this, "Nincs megadva " + empty + "!", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    public void kickWorkerDialog(WorkerData currentWorker) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_alert);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.roundedcorners);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = dialog.findViewById(R.id.alertTextView);
        alertTitle.setText(alertTitle.getText() + " \n" + currentWorker.getUserName() + "?");

        Button cancelButton = dialog.findViewById(R.id.cancelkickWorker);
        cancelButton.setOnClickListener(view -> dialog.dismiss());

        Button okButton = dialog.findViewById(R.id.kickWorkerButton);
        okButton.setOnClickListener(view -> {
            kickWorker(currentWorker);
            dialog.dismiss();
        });

        dialog.show();
    }

    public void kickWorker(WorkerData currentWorker) {
        DocumentReference docRef = mFirestore.collection("Companies").document(companyName);
        docRef.get().addOnCompleteListener(userPreferencesTask -> {
            if (userPreferencesTask.isSuccessful()) {
                DocumentSnapshot document = userPreferencesTask.getResult();
                if (document != null && document.exists()) {
                    Map<String, Object> companyWorkers = document.getData();
                    Map<String, String> workers = (Map<String, String>) companyWorkers.get("Worker" + currentWorker.getId() + "Data");
                    HashMap<String, String> workerData = new HashMap<>();
                    workerData.put("companyName", "Munkanélküli");
                    mFirestore.collection("UserPreferences").document(workers.get("email")).set(workerData, SetOptions.merge());
                    HashMap<String, Object> deletable = new HashMap<>();
                    deletable.put("Worker" + currentWorker.getId() + "Data", FieldValue.delete());
                    docRef.update(deletable).addOnCompleteListener(task -> {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    });
                }
            }
        });
    }

    public void checkWorkerHours(WorkerData currentWorker) {
        Intent showHoursIntent = new Intent(this, ShowHoursActivity.class);
        showHoursIntent.putExtra("companyName", getIntent().getStringExtra("companyName"));
        showHoursIntent.putExtra("workerId", currentWorker.getUserId());
        startActivity(showHoursIntent);
    }
}