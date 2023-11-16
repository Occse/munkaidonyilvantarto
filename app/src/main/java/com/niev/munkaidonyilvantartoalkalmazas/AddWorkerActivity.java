package com.niev.munkaidonyilvantartoalkalmazas;

import static android.widget.Toast.makeText;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Objects;

public class AddWorkerActivity extends AppCompatActivity {
    private static final String LOG_TAG = AddWorkerActivity.class.getName();
    EditText workerID;
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
            Log.d(LOG_TAG, "auth success");
            email = user.getEmail();
        } else {
            Log.d(LOG_TAG, "auth failed");
            finish();
        }
        workerID = findViewById(R.id.workerID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("UserPreferences").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(LOG_TAG, "DocumentSnapshot data: " + document.getData());
                        companyName = String.valueOf(document.get("companyName"));
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    public void addWorker(View view) {
        String workerIDText = String.valueOf(workerID.getText());
        HashMap<String, String> workerData = new HashMap<>();
        mFirestore = FirebaseFirestore.getInstance();
        // getting user from cardID
        mFirestore.collection("userCardIDs").document(workerIDText).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(LOG_TAG, "DocumentSnapshot data: " + document.getData());
                        workerEmail = String.valueOf(document.get("email"));
                        DocumentReference docRef = mFirestore.collection("UserPreferences").document(String.valueOf(document.get("email")));
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot workerDoc = task.getResult();
                                    if (workerDoc.exists()) {
                                        Log.d(LOG_TAG, "DocumentSnapshot data: " + workerDoc.getData());
                                        Log.d(LOG_TAG, "putting in data");
                                        Log.d(LOG_TAG, "putting in username: " + workerDoc.get("userName"));
                                        Log.d(LOG_TAG, "putting in email: " + workerDoc.get("email"));
                                        workerData.put("userName", String.valueOf(workerDoc.get("userName")));
                                        workerData.put("userId", String.valueOf(workerDoc.get("userId")));
                                        workerData.put("userTAJ", String.valueOf(workerDoc.get("userTAJ")));
                                        workerData.put("userAdo", String.valueOf(workerDoc.get("userAdo")));
                                        workerData.put("userLakcim", String.valueOf(workerDoc.get("userLakcim")));
                                        workerData.put("userDegree", String.valueOf(workerDoc.get("userDegree")));
                                        workerData.put("userBirthDate", String.valueOf(workerDoc.get("userBirthDate")));
                                        company = String.valueOf(workerDoc.get("companyName"));
                                        Log.d(LOG_TAG, "email: " + workerEmail);

                                        //getting user from email

                                        Log.d(LOG_TAG, "worker: " + workerData);
                                        //counting workers
                                        Query query = mFirestore.collection("Companies").document(companyName).getParent();
                                        AggregateQuery countQuery = query.count();
                                        countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    // Count fetched successfully
                                                    AggregateQuerySnapshot snapshot = task.getResult();
                                                    Log.d(LOG_TAG, "Count: " + snapshot.getCount());
                                                    members = (int) snapshot.getCount() / 2;
                                                } else {
                                                    Log.d(LOG_TAG, "Count failed: ", task.getException());
                                                }
                                            }
                                        });

                                        //adding workers
                                        HashMap<String, String> companyData = new HashMap<>();
                                        companyData.put("Worker" + members, workerData.get("userName"));
                                        companyData.put("Worker" + members + "Data", String.valueOf(workerData));
                                        mFirestore.collection("Companies").document(companyName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Log.d(LOG_TAG, "DocumentSnapshot data: " + document.getData());
                                                        if (!document.toString().contains("userId=" + workerData.get("userId")) && !company.equals(companyName) && company.equals("Munkanélküli") ) {
                                                            mFirestore.collection("Companies").document(companyName).set(companyData, SetOptions.merge());
                                                            HashMap<String, String> userData = new HashMap<>();
                                                            userData.put("companyName", companyName);
                                                            mFirestore.collection("UserPreferences").document(workerEmail).set(userData, SetOptions.merge());
                                                            finish();
                                                        }
                                                        else{
                                                            makeText(AddWorkerActivity.this, "Worker is already in a company", Toast.LENGTH_LONG).show();
                                                        }
                                                    } else {
                                                        Log.d(LOG_TAG, "No such document");
                                                    }
                                                } else {
                                                    Log.d(LOG_TAG, "get failed with ", task.getException());
                                                }
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
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}