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

import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

// This activity provides the functionality for searching plants by scientific name.
public class PlantSearchByNameActivity extends AppCompatActivity
        implements PlantSearchAdapter.OnPlantItemClickListener {

    private static final String TAG = PlantSearchByNameActivity.class.getSimpleName();

    private static final String PLANT_SEARCH_LITE_ARRAY_KEY = "plantSearchByNameLite";
    private static final String PLANT_SEARCH_LITE_URL_KEY = "plantSearchByNameLiteURL";
    private static final String PLANT_SEARCH_LITE_LOAD_OFFSET_KEY = "plantSearchByNameLiteOffset";
    private static final Integer PLANT_SEARCH_LITE_LOADER_ID = 3;

    private static final String PLANT_SEARCH_HEAVY_ARRAY_KEY = "plantSearchByNameHeavy";
    private static final String PLANT_SEARCH_HEAVY_URL_KEY = "plantSearchByNameHeavyURL";
    private static final Integer PLANT_SEARCH_HEAVY_LOADER_ID = 4;

    /*
     - When the activity is created, we first load all the Scientific_Name_x fields
     - Because there are more than 20000 plants, requesting all of them fails and we have to
       load them partially with time and incrementing load offset.
     - The load limit indicates the number of plant scientific names to load per call.
     - When search box text changes, we iterate the pre-loaded array and for Scientific_Name_x that
       partially match the search query, have an asynchronous task gradually load their plant item
       details and fill the results adapter.
     */
    private static final Integer LITE_LOAD_LIMIT = 3000;

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
    private TextView mLoadingTextTV;
    private ProgressBar mLoadingPB;

    // Contains all plant scientific names and their ids
    private Hashtable<Integer, String> mAllPlantNames;
    private Integer mLiteLoadOffset;

    // Whenever we enter something to search, this is emptied and
    // filled in with all the ids of all the scientific names that need to be loaded.
    private HashSet<Integer> mPlantIdsToLoad;

    // Contains plant details (initially empty)
    private Hashtable<Integer, USDAPlantUtils.PlantItem> mPlants;

    // Contains search box text split into individual words
    private ArrayList<String> mFilters;
    private ArrayList<Integer> mFilteredPlantIds;

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
        mLoadingTextTV = findViewById(R.id.plant_search_by_name_loading_tv);
        mLoadingPB = findViewById(R.id.plant_search_by_name_loading_pb);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mPlantSearchAdapter = new PlantSearchAdapter(this);
        mSearchResultsRV.setAdapter(mPlantSearchAdapter);

        mAllPlantNames = new Hashtable<>();
        mPlantIdsToLoad = new HashSet<>();
        mPlants = new Hashtable<>();
        mFilters = new ArrayList<>();

        mFilteredPlantIds = new ArrayList<>();
        mLiteLoadOffset = 0;

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
                mLoadingPB.setVisibility(View.INVISIBLE);

                if (s != null) {
                    ArrayList<USDAPlantUtils.PlantItem> items = USDAPlantUtils.parsePlantJSON(s);
                    if (items != null) {
                        updateAllPlantNames(items);
                        //mLoadingErrorTV.setVisibility(View.INVISIBLE);
                        //mSearchResultsRV.setVisibility(View.VISIBLE);
                        //return;
                        if (!items.isEmpty()) {
                            mLiteLoadOffset += LITE_LOAD_LIMIT;
                            loadPlantNames();
                            updateLoadingText();
                            return;
                        }
                    }
                }
                //mLoadingErrorTV.setVisibility(View.VISIBLE);
                //mSearchResultsRV.setVisibility(View.INVISIBLE);
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

                // Store details
                if (s != null) {
                    ArrayList<USDAPlantUtils.PlantItem> items = USDAPlantUtils.parsePlantJSON(s);
                    if (items != null) {
                        storePlantDetails(items);
                    }
                }

                // Update search results with new/additional information

                updateSearchResults();

                // Resume loading until plant details of all, matched plant scientific names
                // are loaded
                if (mFilteredPlantIds.size() < RESULTS_DIPLAY_LIMIT) {
                    loadPlantDetails();
                }
            }

            @Override
            public void onLoaderReset(@NonNull Loader<String> loader) {
                // Nothing to do here...
            }
        };

        getSupportLoaderManager().initLoader(PLANT_SEARCH_LITE_LOADER_ID, null, mSearchLiteLoader);
        getSupportLoaderManager().initLoader(PLANT_SEARCH_HEAVY_LOADER_ID, null, mSearchHeavyLoader);

        if (savedInstanceState != null && savedInstanceState.containsKey(PLANT_SEARCH_LITE_ARRAY_KEY)) {
            mAllPlantNames = (Hashtable<Integer, String>) savedInstanceState.getSerializable(PLANT_SEARCH_LITE_ARRAY_KEY);
            mLiteLoadOffset = (Integer)savedInstanceState.getSerializable(PLANT_SEARCH_LITE_LOAD_OFFSET_KEY);
        }
        loadPlantNames();
        updateLoadingText();

        // TODO Also load search results
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update search results
        searchChanged(mSearchBoxET.getText().toString());
    }

    private void updateLoadingText() {
        mLoadingTextTV.setText(String.valueOf(mAllPlantNames.size()));
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
            for (Integer id : mAllPlantNames.keySet()) {
                String name = mAllPlantNames.get(id);
                // Proceed if not already loaded
                if (mPlants.containsKey(id)) continue;
                // Check if plant matches all filters
                int i;
                for (i = 0; i < mFilters.size(); ++i) {
                    String filter = mFilters.get(i);
                    if (!name.contains(filter)) break;
                }
                if (i == mFilters.size()) { // if plant contains all filters
                    mPlantIdsToLoad.add(id);
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

        ArrayList<USDAPlantUtils.PlantItem> filteredPlants = new ArrayList<>();

        ArrayList<Integer> prevIds = new ArrayList<Integer>(mFilteredPlantIds);
        mFilteredPlantIds.clear();
        if (!mFilters.isEmpty()) {
            // Remove current filters that do not match the results
            for (Integer id : prevIds) {
                USDAPlantUtils.PlantItem item = mPlants.get(id);
                if (item == null) continue;
                String name = item.Scientific_Name_x.toLowerCase();
                int i;
                for (i = 0; i < mFilters.size(); ++i) {
                    String filter = mFilters.get(i);
                    if (!name.contains(filter)) break;
                }
                if (i == mFilters.size()) { // if plant contains all filters
                    filteredPlants.add(item);
                    mFilteredPlantIds.add(id);
                }
            }

            // Add new filters
            for (USDAPlantUtils.PlantItem item : mPlants.values()) {
                if (mFilteredPlantIds.contains(item.id)) continue;
                String name = item.Scientific_Name_x.toLowerCase();
                int i;
                for (i = 0; i < mFilters.size(); ++i) {
                    String filter = mFilters.get(i);
                    if (!name.contains(filter)) break;
                }
                if (i == mFilters.size()) { // if plant contains all filters
                    filteredPlants.add(item);
                    mFilteredPlantIds.add(item.id);
                    if (filteredPlants.size() >= RESULTS_DIPLAY_LIMIT) // verify limit
                        break;
                }
            }
        }

        // Show results
        mPlantSearchAdapter.updatePlantItems(filteredPlants);
    }

    private void updateAllPlantNames(ArrayList<USDAPlantUtils.PlantItem> items) {
        for (USDAPlantUtils.PlantItem item : items) {
            mAllPlantNames.put(item.id, item.Scientific_Name_x.toLowerCase());
        }
    }

    private void storePlantDetails(ArrayList<USDAPlantUtils.PlantItem> items) {
        for (USDAPlantUtils.PlantItem item : items) {
            // Store plant details
            mPlants.put(item.id, item);
            // Remove id from the to load set
            mPlantIdsToLoad.remove(item.id);
        }
    }

    private void loadPlantNames() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d(TAG, "Lite load " + String.valueOf(mLiteLoadOffset));

        String url = USDAPlantUtils.buildPlantSearchURL(LITE_LOAD_LIMIT, mLiteLoadOffset, "fields", "id,Scientific_Name_x");

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_LITE_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(PLANT_SEARCH_LITE_LOADER_ID, args, mSearchLiteLoader);
    }

    private void loadPlantDetails() {
        if (mPlantIdsToLoad.isEmpty()) return;
        Integer id = mPlantIdsToLoad.iterator().next();
        String url = USDAPlantUtils.buildPlantSearchURL(RESULTS_LOAD_LIMIT, 0, "id", String.valueOf(id));

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_HEAVY_URL_KEY, url);
        getSupportLoaderManager().restartLoader(PLANT_SEARCH_HEAVY_LOADER_ID, args, mSearchHeavyLoader);
    }

    @Override
    public void onPlantItemClick(USDAPlantUtils.PlantItem repo) {
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USDAPlantUtils.EXTRA_PLANT_ITEM, repo);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAllPlantNames != null) {
            // Saving large amount of data results with android.os.TransactionTooLargeException
            //~ outState.putSerializable(PLANT_SEARCH_LITE_ARRAY_KEY, mAllPlantNames);
            //~ outState.putSerializable(PLANT_SEARCH_LITE_LOAD_OFFSET_KEY, mLiteLoadOffset);
        }

        // TODO Also store search results
    }
}
