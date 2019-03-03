package com.example.android.usdaplantindex;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.android.usdaplantindex.utils.USAUtils;

public class PlantIdentificationActivity extends AppCompatActivity {

    private static final String TAG = PlantIdentificationActivity.class.getSimpleName();

    private Spinner mSpinnerStates;
    private Spinner mSpinnerGrowthHabits;
    private Spinner mSpinnerCategories;
    private Spinner mSpinnerDurations;

    public USAUtils.PlantIdentify plantIdentify = new USAUtils.PlantIdentify();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_identification);

        // Remove shadow under action bar.
        getSupportActionBar().setElevation(0);

        mSpinnerStates = findViewById(R.id.spinner_state);
        mSpinnerGrowthHabits = findViewById(R.id.spinner_growth_habits);
        mSpinnerCategories = findViewById(R.id.spinner_category);
        mSpinnerDurations = findViewById(R.id.spinner_duration);

        ArrayAdapter<CharSequence> mStatesAdapter = ArrayAdapter.createFromResource(this,
                R.array.states_labels, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> mGrowthHabitsAdapter = ArrayAdapter.createFromResource(this,
                R.array.growth_habits, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> mCategoriesAdapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> mDurationsAdapter = ArrayAdapter.createFromResource(this,
                R.array.durations, android.R.layout.simple_spinner_item);

        mSpinnerStates.setAdapter(mStatesAdapter);
        mSpinnerGrowthHabits.setAdapter(mGrowthHabitsAdapter);
        mSpinnerCategories.setAdapter(mCategoriesAdapter);
        mSpinnerDurations.setAdapter(mDurationsAdapter);

        mSpinnerStates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(!selectedItem.equals("All"))
                {
                    plantIdentify.plantState = getResources().getStringArray(R.array.states_values)[position];
                }
                else {
                    plantIdentify.plantState = "";
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent) { /* Do Nothing */ }
        });

        mSpinnerGrowthHabits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(!selectedItem.equals("All"))
                {
                    plantIdentify.plantGrowthHabit = selectedItem;
                }
                else {
                    plantIdentify.plantGrowthHabit = "";
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent) { /* Do Nothing */ }
        });

        mSpinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(!selectedItem.equals("All"))
                {
                    plantIdentify.plantCategory = selectedItem;
                }
                else {
                    plantIdentify.plantCategory = "";
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent) { /* Do Nothing */ }
        });

        mSpinnerDurations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(!selectedItem.equals("All"))
                {
                    plantIdentify.plantDuration = selectedItem;
                }
                else {
                    plantIdentify.plantDuration = "";
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent) { /* Do Nothing */ }
        });

        Button identifyButton = findViewById(R.id.btn_identify);
        identifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlantIdentificationActivity.this, IdentifyListActivity.class);
                intent.putExtra(USAUtils.EXTRA_PLANT_IDENTIFY, plantIdentify);
                startActivity(intent);
            }
        });
    }
}
