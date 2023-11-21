package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageWorkersActivity extends AppCompatActivity {
    private static final String LOG_TAG = ManageWorkersActivity.class.getName();
    private Menu menuList;
    private RecyclerView mRecyclerView;
    private ArrayList<WorkerData> workers;
    private ManageWorkersActivity manageWorkersActivity;
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
            Log.d(LOG_TAG, "auth success");
            email = user.getEmail();
        } else {
            Log.d(LOG_TAG, "auth failed");
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

        mItems.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (true) {
                            //counting workers
                            Query query = mFirestore.collection("Companies").document(companyName).getParent();
                            AggregateQuery countQuery = query.count();
                            countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        // Count fetched successfully
                                        AggregateQuerySnapshot snapshot = task.getResult();
                                        fullCount = (int) snapshot.getCount();
                                        members = (int) snapshot.getCount() / 2;
                                        Log.d(LOG_TAG, "Count: " + members);
                                        Log.d(LOG_TAG, String.valueOf(document.get("Worker0Data")));
                                        for (int i = 0; i < fullCount; i++) {
                                            Log.d(LOG_TAG, String.valueOf(i));
                                            if (document.get("Worker" + i + "Data") == null) {
                                                continue;
                                            }
                                            Log.d(LOG_TAG, String.valueOf(document.get("Worker" + i + "Data")));
                                            HashMap<String, String> currentData = (HashMap<String, String>) document.get("Worker" + i + "Data");
                                            Log.d(LOG_TAG, "currentData: " + document.get("Worker" + i + "Data"));
                                            Log.d(LOG_TAG, "typeof: " + currentData.getClass());
                                            WorkerData workerData = new WorkerData(currentData);
                                            Log.d(LOG_TAG, "workerData: " + workerData.getUserName());
                                            workers.add(workerData);
                                            // Notify the adapter of the change
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        Log.d(LOG_TAG, "Count failed: ", task.getException());
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }

                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menuList = menu;
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
            Log.d(LOG_TAG, "AddWorker clicked!");
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