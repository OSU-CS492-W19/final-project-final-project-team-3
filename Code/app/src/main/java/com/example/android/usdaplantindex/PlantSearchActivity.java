package com.example.android.usdaplantindex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.usdaplantindex.utils.USDAUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

// This activity provides the functionality for searching plants by species
// - We activity is created, we first load all species (this doesn't take a while)
// - When search box text changes, we obtain all species that partially match the text,
//   and have the asynchronous task partially fill the adapter with detailed search results.

public class PlantSearchActivity extends AppCompatActivity
        implements PlantSearchAdapter.OnPlantItemClickListener {

    private static final String TAG = PlantSearchActivity.class.getSimpleName();

    private static final String PLANT_SEARCH_LITE_ARRAY_KEY = "plantSearchLite";
    private static final String PLANT_SEARCH_LITE_URL_KEY = "plantSearchLiteURL";
    private static final Integer PLANT_SEARCH_LITE_LOADER_ID = 1;

    private static final String PLANT_SEARCH_HEAVY_ARRAY_KEY = "plantSearchHeavy";
    private static final String PLANT_SEARCH_HEAVY_URL_KEY = "plantSearchHeavyURL";
    private static final Integer PLANT_SEARCH_HEAVY_LOADER_ID = 2;

    private static final Integer LITE_LOAD_LIMIT = 5000; // Load all at once
    private static final Integer HEAVY_LOAD_LIMIT = 5; // Load partially
    private static final Integer HEAVY_DIPLAY_LIMIT = 25;

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private TextView mLoadingErrorTV;
    private ProgressBar mLoadingPB;

    private String mFilter;

    private PlantSearchAdapter mPlantSearchAdapter;
    private HashSet<String> mAllPlantSpecies;
    private HashMap<Integer, USDAUtils.PlantItem> mPlants;
    private HashMap<String, Integer> mPlantSpeciesLoadOffsets;

    private LoaderManager.LoaderCallbacks<String> mSearchLiteLoader;
    private LoaderManager.LoaderCallbacks<String> mSearchHeavyLoader;

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

        mFilter = mSearchBoxET.getText().toString();

        mAllPlantSpecies = new HashSet<>();
        mPlants = new HashMap<>();

        mSearchBoxET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mFilter = s.toString();
                filterSearchResults();
            }
        });

        mSearchLiteLoader = new LoaderManager.LoaderCallbacks<String>() {
            @NonNull
            @Override
            public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
                String url = null;
                if (bundle != null) {
                    url = bundle.getString(PLANT_SEARCH_LITE_URL_KEY);
                }
                return new PlantSearchLoader(PlantSearchActivity.this, url);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<String> loader, String s) {
                Log.d(TAG, "Lite loader finished loading.");
                if (s != null) {
                    ArrayList<USDAUtils.PlantItem> items = USDAUtils.parsePlantJSON(s);
                    for (USDAUtils.PlantItem item : items) {
                        mAllPlantSpecies.add(item.Species);
                    }
                    mLoadingErrorTV.setVisibility(View.INVISIBLE);
                    mSearchResultsRV.setVisibility(View.VISIBLE);
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
        };

        mSearchHeavyLoader = new LoaderManager.LoaderCallbacks<String>() {
            @NonNull
            @Override
            public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
                String url = null;
                if (bundle != null) {
                    url = bundle.getString(PLANT_SEARCH_HEAVY_URL_KEY);
                }
                return new PlantSearchLoader(PlantSearchActivity.this, url);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<String> loader, String s) {
                Log.d(TAG, "Heavy loader finished loading.");
                if (s != null) {
                    ArrayList<USDAUtils.PlantItem> items = USDAUtils.parsePlantJSON(s);
                    for (USDAUtils.PlantItem item : items) {
                        mPlants.put(item.id, item);
                        Integer offset = mPlantSpeciesLoadOffsets.get(item.Species);
                        if (offset == null || offset < item.id) {
                            mPlantSpeciesLoadOffsets.put(item.Species, item.id);
                        }
                        mPlants.put(item.id, item);
                    }
                    if (items.isEmpty()) {
                        mPlantSpeciesLoadOffsets.put()
                    }
                    filterSearchResults();
                    mLoadingErrorTV.setVisibility(View.INVISIBLE);
                    mSearchResultsRV.setVisibility(View.VISIBLE);
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
        };

        getSupportLoaderManager().initLoader(PLANT_SEARCH_LITE_LOADER_ID, null, mSearchLiteLoader);
        getSupportLoaderManager().initLoader(PLANT_SEARCH_HEAVY_LOADER_ID, null, mSearchHeavyLoader);

        if (savedInstanceState != null && savedInstanceState.containsKey(PLANT_SEARCH_LITE_ARRAY_KEY)) {
            mPlantsLite = (ArrayList<USDAUtils.PlantItem>) savedInstanceState.getSerializable(PLANT_SEARCH_LITE_ARRAY_KEY);
        }
        else {
            queryPlantsLite();
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(PLANT_SEARCH_HEAVY_ARRAY_KEY)) {
            mPlantsHeavy = (ArrayList<USDAUtils.PlantItem>) savedInstanceState.getSerializable(PLANT_SEARCH_HEAVY_ARRAY_KEY);
        }
    }

    private void filterSearchResults() {
        Log.d(TAG, "Filtering: " + mFilter);
        ArrayList<USDAUtils.PlantItem> filteredPlants = new ArrayList<>();
        if (!mFilter.isEmpty()) {
            String s2 = mFilter.toLowerCase();
            for (USDAUtils.PlantItem item : mPlants) {
                if (item.Species.toLowerCase().contains(s2)) {
                    filteredPlants.add(item);
                    if (filteredPlants.size() >= FILTER_MATCH_LIMIT)
                        break;
                }
            }
        }
        mPlantSearchAdapter.updatePlantItems(filteredPlants);
    }

    private void queryPlantsLite() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String url = USDAUtils.buildPlantSearchURL(LITE_LIMIT, 0, "fields", "Species");

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_LITE_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(PLANT_SEARCH_LITE_LOADER_ID, args, mSearchLiteLoader);
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
        if (mPlantsLite != null) {
            outState.putSerializable(PLANT_SEARCH_LITE_ARRAY_KEY, mPlantsLite);
        }
        if (mPlantsHeavy != null) {
            outState.putSerializable(PLANT_SEARCH_HEAVY_ARRAY_KEY, mPlantsHeavy);
        }
    }
}
