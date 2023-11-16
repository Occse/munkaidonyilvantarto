package com.niev.munkaidonyilvantartoalkalmazas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

public class ManageWorkersActivity extends AppCompatActivity {

    private static final String LOG_TAG = ManageWorkersActivity.class.getName();
    private Menu menuList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_workers);
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