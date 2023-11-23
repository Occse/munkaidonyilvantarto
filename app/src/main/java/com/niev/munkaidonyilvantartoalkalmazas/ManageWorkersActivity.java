package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageWorkersActivity extends AppCompatActivity {
    private static final String TAG = ManageWorkersActivity.class.getName();
    private RecyclerView mRecyclerView;
    private ArrayList<WorkerData> workers;
    private FirebaseFirestore mFirestore;
    private WorkerAdapter mAdapter;
    private DocumentReference mItems;
    private FirebaseUser user;
    private String email;
    private String companyName;
    private int members;
    private int fullCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_workers);
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
        workers = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recyclerViewWorkers);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mAdapter = new WorkerAdapter(this, workers);
        mRecyclerView.setAdapter(mAdapter);
        mItems = mFirestore.collection("Companies").document(companyName);
        loadWorkerData();
    }

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
                                Log.d(TAG, "number of fields: " + map.size());
                                fullCount = map.size();
                                members = map.size() - 1;
                                Log.d(TAG, "Count: " + members);
                                Log.d(TAG, String.valueOf(document1.get("Worker0Data")));
                                for (int i = 0; i < fullCount; i++) {
                                    Log.d(TAG, String.valueOf(i));
                                    if (document1.get("Worker" + i + "Data") == null) {
                                        continue;
                                    }
                                    Log.d(TAG, String.valueOf(document1.get("Worker" + i + "Data")));
                                    HashMap<String, String> currentData = (HashMap<String, String>) document1.get("Worker" + i + "Data");
                                    Log.d(TAG, "currentData: " + document1.get("Worker" + i + "Data"));
                                    Log.d(TAG, "typeof: " + currentData.getClass());
                                    WorkerData workerData = new WorkerData(currentData);
                                    Log.d(TAG, "workerData: " + workerData.getUserName());
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
            Log.d(TAG, "AddWorker clicked!");
            addWorker();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void addWorker() {
        Intent addWorkerIntent = new Intent(this, AddWorkerActivity.class);
        startActivity(addWorkerIntent);
    }
}