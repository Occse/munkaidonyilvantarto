package com.niev.munkaidonyilvantartoalkalmazas;

import static android.widget.Toast.makeText;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AddWorkerActivity extends AppCompatActivity {
    private static final String TAG = AddWorkerActivity.class.getName();
    EditText workerID;
    EditText workerMunkakor;
    private FirebaseUser user;
    private FirebaseFirestore mFirestore;
    private String email;
    private String workerEmail;
    private String company;
    private int members;
    private String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_worker);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "auth success");
            email = user.getEmail();
        } else {
            Log.d(TAG, "auth failed");
            finish();
        }
        workerID = findViewById(R.id.workerID);
        workerMunkakor = findViewById(R.id.userMunkakor);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("UserPreferences").document(email);
        docRef.get().addOnCompleteListener(userPreferencesTask -> {
            if (userPreferencesTask.isSuccessful()) {
                DocumentSnapshot document = userPreferencesTask.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    companyName = String.valueOf(document.get("companyName"));
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", userPreferencesTask.getException());
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    public void addWorker(View view) {
        String workerIDText = String.valueOf(workerID.getText());
        String workerMunkakorText = String.valueOf(workerMunkakor.getText());
        HashMap<String, String> workerData = new HashMap<>();
        mFirestore = FirebaseFirestore.getInstance();
        // getting user from cardID
        mFirestore.collection("userCardIDs").document(workerIDText).get().addOnCompleteListener(userCardIdTask -> {
            if (userCardIdTask.isSuccessful()) {
                DocumentSnapshot document = userCardIdTask.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    workerEmail = String.valueOf(document.get("email"));
                    DocumentReference docRef = mFirestore.collection("UserPreferences").document(String.valueOf(document.get("email")));
                    docRef.get().addOnCompleteListener(userPreferencesTask -> {
                        if (userPreferencesTask.isSuccessful()) {
                            DocumentSnapshot workerDoc = userPreferencesTask.getResult();
                            if (workerDoc.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + workerDoc.getData());
                                Log.d(TAG, "putting in data");
                                Log.d(TAG, "putting in username: " + workerDoc.get("userName"));
                                Log.d(TAG, "putting in email: " + workerDoc.get("email"));
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
                                Log.d(TAG, "email: " + workerEmail);

                                //getting user from email

                                Log.d(TAG, "worker: " + workerData);
                                //counting workers
                                DocumentReference docRef1 = mFirestore.collection("Companies").document(companyName);
                                docRef1.get().addOnCompleteListener(companyMembersTask -> {
                                    if (companyMembersTask.isSuccessful()) {
                                        DocumentSnapshot document1 = companyMembersTask.getResult();
                                        if (document1.exists()) {
                                            Map<String, Object> map = document1.getData();
                                            Log.d(TAG, "number of fields: " + map.size());
                                            members = map.size() - 1;
                                            Log.d(TAG, "Count: " + members);
                                            //adding workers
                                            HashMap<String, Object> companyWorkerData = new HashMap<>();
                                            companyWorkerData.put("Worker" + members + "Data", workerData);
                                            Log.d(TAG, "workermemberdata: " + companyWorkerData);
                                            mFirestore.collection("Companies").document(companyName).get().addOnCompleteListener(companyAddTask -> {
                                                if (companyAddTask.isSuccessful()) {
                                                    DocumentSnapshot document11 = companyAddTask.getResult();
                                                    if (document11.exists()) {
                                                        Log.d(TAG, "DocumentSnapshot data: " + document11.getData());
                                                        if (!document11.toString().contains("userId=" + workerData.get("userId")) && !company.equals(companyName) && company.equals("Munkanélküli")) {
                                                            mFirestore.collection("Companies").document(companyName).set(companyWorkerData, SetOptions.merge());
                                                            HashMap<String, String> userData = new HashMap<>();
                                                            userData.put("companyName", companyName);
                                                            mFirestore.collection("UserPreferences").document(workerEmail).set(userData, SetOptions.merge());
                                                            finish();
                                                        } else {
                                                            makeText(AddWorkerActivity.this, "Worker is already in a company", Toast.LENGTH_LONG).show();
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
    }
}