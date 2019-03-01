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
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.usdaplantindex.utils.USAUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingDeque;

// This activity provides the functionality for searching plants by scientific name.
public class PlantSearchByNameActivity extends AppCompatActivity
        implements PlantSearchAdapter.OnPlantItemClickListener {

    private static final String TAG = PlantSearchByNameActivity.class.getSimpleName();

    private static final String PLANT_SEARCH_LITE_ARRAY_KEY = "plantSearchByNameLite";
    private static final String PLANT_SEARCH_LITE_URL_KEY = "plantSearchByNameLiteURL";
    private static final Integer PLANT_SEARCH_LITE_LOADER_ID = 3;

    private static final String PLANT_SEARCH_HEAVY_ARRAY_KEY = "plantSearchByNameHeavy";
    private static final String PLANT_SEARCH_HEAVY_URL_KEY = "plantSearchByNameHeavyURL";
    private static final Integer PLANT_SEARCH_HEAVY_LOADER_ID = 4;

    /*
     - When the activity is created, we first load all the Scientific_Name_x fields and
       nothing else. This doesn't take a while; it would take longer to load additional fields,
       so we restrict to only pre-loading one field.
     - The load limit is there just in case there is actually more items than that, that could
       affect the pre-loading performance.
     - When search box text changes, we iterate the pre-loaded array and for Scientific_Name_x that
       partially match the search query, have an asynchronous task gradually load their plant item
       details and fill the results adapter.
     */
    private static final Integer LITE_LOAD_LIMIT = 5000;

    /*
     - The is one interesting property about plant scientific names and it is that no two plants
       share a common scientific name.
     - Having preloaded all the scientific names, we store them into an array.
     - Now, whenever a user enters something into the search box, we first get all the fully
       or partially matching scientific names.
     - Having obtained the matching scientific names, we then load details of the matched
       scientific names, one by one.
     - All already-loaded details are stored in the array to decrease the loading time.
     */

    // Number of plant items to load per single request
    private static final Integer RESULTS_LOAD_LIMIT = 5;

    // Number of plant items to display in search results
    private static final Integer RESULTS_DIPLAY_LIMIT = 25;

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private TextView mLoadingErrorTV;
    private ProgressBar mLoadingPB;

    // Contains all plant scientific names and their ids
    private Hashtable<String, Integer> mAllPlantNames;

    // Whenever we enter something to search, this is emptied and
    // filled in with all the ids of all the scientific names that need to be loaded.
    private HashSet<Integer> mPlantIdsToLoad;

    // Contains plant details (initially empty)
    private Hashtable<Integer, USAUtils.PlantItem> mPlants;

    // Contains search box text split into individual words
    private ArrayList<String> mFilters;

    private PlantSearchAdapter mPlantSearchAdapter;
    private LoaderManager.LoaderCallbacks<String> mSearchLiteLoader;
    private LoaderManager.LoaderCallbacks<String> mSearchHeavyLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_search_by_name);

        mSearchBoxET = findViewById(R.id.plant_search_by_name_et);
        mSearchResultsRV = findViewById(R.id.plant_search_by_name_results);
        mLoadingErrorTV = findViewById(R.id.plant_search_by_name_loading_error_tv);
        mLoadingPB = findViewById(R.id.plant_search_by_name_loading_pb);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mPlantSearchAdapter = new PlantSearchAdapter(this);
        mSearchResultsRV.setAdapter(mPlantSearchAdapter);

        mAllPlantNames = new Hashtable<>();
        mPlantIdsToLoad = new HashSet<>();
        mPlants = new Hashtable<>();
        mFilters = new ArrayList<>();

        mSearchBoxET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchChanged(s.toString());
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
                return new PlantSearchLoader(PlantSearchByNameActivity.this, url);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<String> loader, String s) {
                Log.d(TAG, "Lite loader finished loading.");
                if (s != null) {
                    ArrayList<USAUtils.PlantItem> items = USAUtils.parsePlantJSON(s);
                    if (items != null) {
                        updateAllPlantNames(items);
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
                return new PlantSearchLoader(PlantSearchByNameActivity.this, url);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<String> loader, String s) {
                Log.d(TAG, "Heavy loader finished loading.");
                if (s != null) {
                    ArrayList<USAUtils.PlantItem> items = USAUtils.parsePlantJSON(s);
                    if (items != null) {
                        Log.d(TAG, "items not null");
                        // Store details
                        storePlantDetails(items);
                        // Update search results with new/additional information
                        updateSearchResults();
                    }
                }

                // Resume loading until plant details of all, matched plant scientific names
                // are loaded
                loadPlantDetails();
            }

            @Override
            public void onLoaderReset(@NonNull Loader<String> loader) {
                // Nothing to do here...
            }
        };

        getSupportLoaderManager().initLoader(PLANT_SEARCH_LITE_LOADER_ID, null, mSearchLiteLoader);
        getSupportLoaderManager().initLoader(PLANT_SEARCH_HEAVY_LOADER_ID, null, mSearchHeavyLoader);

        if (savedInstanceState != null && savedInstanceState.containsKey(PLANT_SEARCH_LITE_ARRAY_KEY)) {
            mAllPlantNames = (Hashtable<String, Integer>) savedInstanceState.getSerializable(PLANT_SEARCH_LITE_ARRAY_KEY);
        }
        else {
            loadPlantNames();
        }

        // TODO Also load search results
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update search results
        searchChanged(mSearchBoxET.getText().toString());
    }

    private void searchChanged(String s) {
        // Update filters
        mFilters.clear();
        String[] filters = s.toLowerCase().split("\\s+|,"); // split by space or comma
        for (String filter : filters) {
            if (!filter.isEmpty()) {
                mFilters.add(filter);
            }
        }

        // Look through all, pre-loaded plant scientific names for matches
        mPlantIdsToLoad.clear();
        if (!mFilters.isEmpty()) {
            for (String item : mAllPlantNames.keySet()) {
                String name = item.toLowerCase();
                Integer id = mAllPlantNames.get(item);
                for (String filter : mFilters) {
                    if (name.contains(filter)) {
                        if (!mPlants.containsKey(id)) // if not already loaded
                            mPlantIdsToLoad.add(id);
                        break;
                    }
                }
            }
        }

        // Load the details of the plants not already loaded
        loadPlantDetails();

        // Update search results with what already is currently loaded
        // The loader will call this function as additional results are loaded
        updateSearchResults();
    }

    private void updateSearchResults() {
        Log.d(TAG, "Filtering: " + mFilters.toString());
        ArrayList<USAUtils.PlantItem> filteredPlants = new ArrayList<>();
        for (USAUtils.PlantItem item : mPlants.values()) {
            String name = item.Scientific_Name_x.toLowerCase();
            for (String filter : mFilters) {
                if (name.contains(filter)) {
                    filteredPlants.add(item);
                    break;
                }
            }
            if (filteredPlants.size() >= RESULTS_DIPLAY_LIMIT)
                break;
        }
        mPlantSearchAdapter.updatePlantItems(filteredPlants);
    }

    private void updateAllPlantNames(ArrayList<USAUtils.PlantItem> items) {
        mAllPlantNames.clear(); // TODO this may need to be removed if we load partially
        for (USAUtils.PlantItem item : items) {
            mAllPlantNames.put(item.Scientific_Name_x, item.id);
        }
    }

    private void storePlantDetails(ArrayList<USAUtils.PlantItem> items) {
        for (USAUtils.PlantItem item : items) {
            // Store plant details
            mPlants.put(item.id, item);
            // Remove id from the to load set
            mPlantIdsToLoad.remove(item.id);
        }
    }

    private void loadPlantNames() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String url = USAUtils.buildPlantSearchURL(LITE_LOAD_LIMIT, 0, "fields", "id,Scientific_Name_x");

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_LITE_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(PLANT_SEARCH_LITE_LOADER_ID, args, mSearchLiteLoader);
    }

    private void loadPlantDetails() {
        if (mPlantIdsToLoad.isEmpty()) return;
        Integer id = mPlantIdsToLoad.iterator().next();
        String url = USAUtils.buildPlantSearchURL(RESULTS_LOAD_LIMIT, 0, "id", String.valueOf(id));

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_HEAVY_URL_KEY, url);
        getSupportLoaderManager().restartLoader(PLANT_SEARCH_HEAVY_LOADER_ID, args, mSearchHeavyLoader);
    }

    @Override
    public void onPlantItemClick(USAUtils.PlantItem repo) {
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USAUtils.EXTRA_PLANT_ITEM, repo);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAllPlantNames != null) {
            outState.putSerializable(PLANT_SEARCH_LITE_ARRAY_KEY, mAllPlantNames);
        }

        // TODO Also store search results
    }
}
