package com.example.android.usdaplantindex;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.usdaplantindex.utils.NetworkUtils;
import com.example.android.usdaplantindex.utils.USAUtils;

import java.io.IOException;
import java.util.ArrayList;

public class IdentifyListActivity extends AppCompatActivity implements PlantSearchAdapter.OnPlantItemClickListener {

    private static final String TAG = IdentifyListActivity.class.getSimpleName();

    private RecyclerView mPlantItemsRV;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private PlantSearchAdapter mPlantAdapter;
    private USAUtils.PlantIdentify mPlantIdentify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_identify_list);

        // Remove shadow under action bar.
        getSupportActionBar().setElevation(0);

        mLoadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = findViewById(R.id.tv_loading_error_message);
        mPlantItemsRV = findViewById(R.id.rv_plant_items);

        mPlantAdapter = new PlantSearchAdapter(this);
        mPlantItemsRV.setAdapter(mPlantAdapter);
        mPlantItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mPlantItemsRV.setHasFixedSize(true);

        loadPlant();
    }

    @Override
    public void onPlantItemClick(USAUtils.PlantItem plantItem) {
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USAUtils.EXTRA_PLANT_ITEM, plantItem);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadPlant() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(USAUtils.EXTRA_PLANT_IDENTIFY)) {
            mPlantIdentify = (USAUtils.PlantIdentify)intent.getSerializableExtra(
                    USAUtils.EXTRA_PLANT_IDENTIFY
            );
        }

        if (mPlantIdentify.plantState == null) {
            mPlantIdentify.plantState = "";
        }

        if (mPlantIdentify.plantGrowthHabit == null) {
            mPlantIdentify.plantGrowthHabit = "";
        }

        if (mPlantIdentify.plantCategory == null) {
            mPlantIdentify.plantCategory = "";
        }

        if (mPlantIdentify.plantDuration == null) {
            mPlantIdentify.plantDuration = "";
        }

        String url = USAUtils.buildPlantSearchURL(1000,0,
                mPlantIdentify.plantState, mPlantIdentify.plantGrowthHabit,
                mPlantIdentify.plantCategory, mPlantIdentify.plantDuration);
        new PlantTask().execute(url);
    }

    class PlantTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicatorPB.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String USDAplantsURL = params[0];
            String plantJSON = null;
            try {
                plantJSON = NetworkUtils.doHTTPGet(USDAplantsURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return plantJSON;
        }

        @Override
        protected void onPostExecute(String plantJSON) {
            mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
            if (plantJSON != null) {
                mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
                mPlantItemsRV.setVisibility(View.VISIBLE);
                ArrayList<USAUtils.PlantItem> plantItems = USAUtils.parsePlantJSON(plantJSON);
                mPlantAdapter.updatePlantItems(plantItems);
            } else {
                mPlantItemsRV.setVisibility(View.INVISIBLE);
                mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
            }
        }
    }
}