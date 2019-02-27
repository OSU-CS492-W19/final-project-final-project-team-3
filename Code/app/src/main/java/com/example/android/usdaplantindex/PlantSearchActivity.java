package com.example.android.usdaplantindex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.usdaplantindex.utils.NetworkUtils;
import com.example.android.usdaplantindex.utils.USDAUtils;

import java.util.ArrayList;

// This activity provides the functionality for searching plants by common name or species
public class PlantSearchActivity extends AppCompatActivity
        implements PlantSearchAdapter.OnPlantItemClickListener,
        LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = PlantSearchActivity.class.getSimpleName();
    private static final String PLANT_SEARCH_ARRAY_KEY = "plantSearchPlants";
    private static final String PLANT_SEARCH_URL_KEY = "plantSearchURL";

    private static final int GITHUB_SEARCH_LOADER_ID = 0;

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private TextView mLoadingErrorTV;
    private ProgressBar mLoadingPB;

    private PlantSearchAdapter mPlantSearchAdapter;
    private ArrayList<USDAUtils.PlantItem> mPlants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_search);

        mSearchBoxET = findViewById(R.id.plant_search_et);
        mSearchResultsRV = findViewById(R.id.plant_search_results);
        mLoadingErrorTV = findViewById(R.id.plant_search_loading_error_tv);
        mLoadingPB = findViewById(R.id.plant_search_loading_pb);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mPlantSearchAdapter = new PlantSearchAdapter(this);
        mSearchResultsRV.setAdapter(mPlantSearchAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(PLANT_SEARCH_ARRAY_KEY)) {
            mPlants = (ArrayList<USDAUtils.PlantItem>) savedInstanceState.getSerializable(PLANT_SEARCH_ARRAY_KEY);
            mPlantSearchAdapter.updatePlantItems(mPlants);
        }

        getSupportLoaderManager().initLoader(GITHUB_SEARCH_LOADER_ID, null, this);

        Button searchButton = findViewById(R.id.plant_search_btn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBoxET.getText().toString();
                if (!TextUtils.isEmpty(searchQuery)) {
                    doPlantSearch(searchQuery);
                }
            }
        });
    }

    private void doPlantSearch(String query) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String url = USDAUtils.buildPlantSearchURL("Common_Name", query);

        Log.d(TAG, "Querying search URL: " + url);

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(GITHUB_SEARCH_LOADER_ID, args, this);
    }

    @Override
    public void onPlantItemClick(USDAUtils.PlantItem repo) {
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USDAUtils.EXTRA_PLANT_ITEM, repo);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPlants != null) {
            outState.putSerializable(PLANT_SEARCH_ARRAY_KEY, mPlants);
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
        String url = null;
        if (bundle != null) {
            url = bundle.getString(PLANT_SEARCH_URL_KEY);
        }
        return new PlantSearchLoader(this, url);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {
        Log.d(TAG, "Loader finished loading");
        if (s != null) {
            mLoadingErrorTV.setVisibility(View.INVISIBLE);
            mSearchResultsRV.setVisibility(View.VISIBLE);
            mPlants = USDAUtils.parsePlantJSON(s);
            mPlantSearchAdapter.updatePlantItems(mPlants);
        } else {
            mLoadingErrorTV.setVisibility(View.VISIBLE);
            mSearchResultsRV.setVisibility(View.INVISIBLE);
        }
        mLoadingPB.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        // Nothing to do here...
    }
}
