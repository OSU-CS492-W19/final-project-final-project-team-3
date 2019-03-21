package com.example.android.usdaplantindex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

// This activity is the entry to "Search Plants" and "Identify by Map" activities.
public class StartupActivity extends AppCompatActivity {

    private LinearLayout mSearchByNameBtn;
    private LinearLayout mSearchBySpeciesBtn;
    private LinearLayout mSearchByLocationBtn;
    private LinearLayout mFavoritesBtn;
    private Button mAboutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        mSearchByNameBtn = findViewById(R.id.btn_search_by_name);
        mSearchBySpeciesBtn = findViewById(R.id.btn_search_by_common);
        mSearchByLocationBtn = findViewById(R.id.btn_search_by_location);
        mFavoritesBtn = findViewById(R.id.btn_favorites);
        mAboutBtn = findViewById(R.id.btn_about);

        mSearchByNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, PlantSearchByNameActivity.class);
                startActivity(intent);
            }
        });

        mSearchBySpeciesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, PlantSearchByCommonActivity.class);
                startActivity(intent);
            }
        });

        mSearchByLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, PlantIdentificationActivity.class);
                startActivity(intent);
            }
        });

        mFavoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, PlantFavoriteActivity.class);
                startActivity(intent);
            }
        });

        mAboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }
}
