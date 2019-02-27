package com.example.android.usdaplantindex;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.android.usdaplantindex.R;
import com.example.android.usdaplantindex.utils.USDAUtils;

// This activity is the entry to "Search Plants" and "Identify by Map" activities.
public class StartupActivity extends AppCompatActivity {

    private Button mSearchByNameBtn;
    private Button mSearchByLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        mSearchByNameBtn = findViewById(R.id.btn_search_by_name);
        mSearchByLocationBtn = findViewById(R.id.btn_search_by_location);

        mSearchByNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, PlantSearchActivity.class);
                startActivity(intent);
            }
        });

        mSearchByLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
