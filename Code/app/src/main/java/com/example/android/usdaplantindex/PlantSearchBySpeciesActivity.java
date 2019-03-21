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

import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingDeque;

// This activity provides the functionality for searching plants by species.
public class PlantSearchBySpeciesActivity extends AppCompatActivity
        implements PlantSearchAdapter.OnPlantItemClickListener {

    private static final String TAG = PlantSearchBySpeciesActivity.class.getSimpleName();

    private static final String PLANT_SEARCH_LITE_ARRAY_KEY = "plantSearchBySpeciesLite";
    private static final String PLANT_SEARCH_LITE_URL_KEY = "plantSearchBySpeciesLiteURL";
    private static final String PLANT_SEARCH_LITE_LOAD_OFFSET_KEY = "plantSearchBySpeciesLiteOffset";
    private static final Integer PLANT_SEARCH_LITE_LOADER_ID = 1;

    private static final String PLANT_SEARCH_HEAVY_ARRAY_KEY = "plantSearchBySpeciesHeavy";
    private static final String PLANT_SEARCH_HEAVY_URL_KEY = "plantSearchBySpeciesHeavyURL";
    private static final Integer PLANT_SEARCH_HEAVY_LOADER_ID = 2;

    /*
     - When the activity is created, we first load all the Species fields
     - Because there are more than 20000 plant items, requesting all of them fails and we have to
       load them partially, with time and incrementing load offset.
     - The load limit indicates the number of plant items to load per call.
     - When search box text changes, we iterate the pre-loaded array and for Species that
       partially match the search query, have an asynchronous task gradually load their plant item
       details and fill the results adapter.
     */
    private static final Integer LITE_LOAD_LIMIT = 3000;

    /*
     - Having all the species fields, we first store the species fields into a unique array, to
       make searching more efficient and also store their count; there are usually multiple
       plant items that correspond to the same species.
     - Now, whenever a user enters something into the search box, we first get all the fully
       or partially matching species.
     - Having obtained the species that match the search query, we then request the database
       for the detailed information of the species.
     - To prevent requesting a significant number of matched results, we set a limit to how much
       we can request and display in the search results.
     - Furthermore, requesting the database for items and all their details at once takes a long
       time and timeout errors can be generated. It is, therefore, crucial to load plant details
       partially and fill the adapter with time.
     - We know that when we query items, the database searches for items in order, from the
       smallest ID + offset to the greatest ID. Keeping that in mind, we can load the matched
       species partially, incrementing a load offset for the species each time we request an
       additional chunk of information.
     - All already-loaded details are stored in the array to decrease the loading time.
       This array is not saved. When activity is recreated (due to screen rotation or
       activation of different activity), the details array is empty.
     */

    // Number of plant items to load per single request
    private static final Integer RESULTS_LOAD_LIMIT = 5;

    // Number of plant items to display in search results
    private static final Integer RESULTS_DISPLAY_LIMIT = 25;

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private TextView mLoadingErrorTV;
    private TextView mLoadingTextTV;
    private ProgressBar mLoadingPB;

    // Contains all plant species and their count (filled on create)
    private Hashtable<String, Integer> mAllPlantSpecies;
    private Integer mLiteLoadOffset;

    // Contains plant detail load offsets (this is initially empty)
    private Hashtable<String, Integer> mPlantSpeciesLoadOffsets;
    private Hashtable<String, Integer> mLoadedPlantSpeciesCounts;

    // Whenever we enter something to search, this is emptied and
    // filled in with all the species that need to be loaded.
    private LinkedBlockingDeque<String> mPlantSpeciesToLoad;

    // Contains plant details (initially empty)
    private Hashtable<Integer, PlantItem> mPlants;

    // Contains search box text split into individual words
    private ArrayList<String> mFilters;
    private ArrayList<Integer> mFilteredPlantIds;

    private PlantSearchAdapter mPlantSearchAdapter;
    private LoaderManager.LoaderCallbacks<String> mSearchLiteLoader;
    private LoaderManager.LoaderCallbacks<String> mSearchHeavyLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_search_by_species);

        mSearchBoxET = findViewById(R.id.plant_search_by_species_et);
        mSearchResultsRV = findViewById(R.id.plant_search_by_species_results);
        mLoadingErrorTV = findViewById(R.id.plant_search_by_species_loading_error_tv);
        mLoadingTextTV = findViewById(R.id.plant_search_by_species_loading_tv);
        mLoadingPB = findViewById(R.id.plant_search_by_species_loading_pb);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mPlantSearchAdapter = new PlantSearchAdapter(this);
        mSearchResultsRV.setAdapter(mPlantSearchAdapter);

        mAllPlantSpecies = new Hashtable<>();
        mPlantSpeciesLoadOffsets = new Hashtable<>();
        mLoadedPlantSpeciesCounts = new Hashtable<>();
        mPlantSpeciesToLoad = new LinkedBlockingDeque<>();
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
                return new PlantSearchLoader(PlantSearchBySpeciesActivity.this, url);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<String> loader, String s) {
                Log.d(TAG, "Lite loader finished loading.");
                mLoadingPB.setVisibility(View.INVISIBLE);

                if (s != null) {
                    ArrayList<PlantItem> items = USDAPlantUtils.parsePlantJSON(s);
                    if (items != null) {
                        updateAllPlantSpecies(items);
                        if (!items.isEmpty()) {
                            mLiteLoadOffset += LITE_LOAD_LIMIT;
                            loadPlantSpecies();
                            updateLoadingText();
                            return;
                        }
                    }
                    //mLoadingErrorTV.setVisibility(View.INVISIBLE);
                    //mSearchResultsRV.setVisibility(View.VISIBLE);
                } else {
                    //mLoadingErrorTV.setVisibility(View.VISIBLE);
                    //mSearchResultsRV.setVisibility(View.INVISIBLE);
                }
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
                return new PlantSearchLoader(PlantSearchBySpeciesActivity.this, url);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<String> loader, String s) {
                Log.d(TAG, "Heavy loader finished loading.");

                // Store details
                if (s != null) {
                    ArrayList<PlantItem> items = USDAPlantUtils.parsePlantJSON(s);
                    if (items != null) {
                        storePlantDetails(items);
                    }
                }

                // Update search results with new/additional information
                updateSearchResults();

                // Resume loading until plant details of all, matched plant Species
                // are loaded
                if (mFilteredPlantIds.size() < RESULTS_DISPLAY_LIMIT) {
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
            mAllPlantSpecies = (Hashtable<String, Integer>) savedInstanceState.getSerializable(PLANT_SEARCH_LITE_ARRAY_KEY);
            mLiteLoadOffset = (Integer)savedInstanceState.getSerializable(PLANT_SEARCH_LITE_LOAD_OFFSET_KEY);
        }

        loadPlantSpecies();
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
        mLoadingTextTV.setText(String.valueOf(mAllPlantSpecies.size()));
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

        // Look through all, pre-loaded plant species for matches
        mPlantSpeciesToLoad.clear();
        if (!mFilters.isEmpty()) {
            for (String item : mAllPlantSpecies.keySet()) {
                String species = item.toLowerCase();
                for (String filter : mFilters) {
                    if (species.contains(filter)) {
                        Integer max_count = mAllPlantSpecies.get(item);
                        if (max_count == null) break; // this will almost never happen
                        Integer count = mLoadedPlantSpeciesCounts.get(item);
                        if (count == null || count < max_count) {
                            mPlantSpeciesToLoad.add(item);
                        }
                        break;
                    }
                }
            }
        }

        // Load plant species that are not already loaded
        loadPlantDetails();

        // Update search results with what already is loaded
        // The loader will call this function as additional results are loaded
        updateSearchResults();
    }

    private void updateSearchResults() {
        Log.d(TAG, "Filtering: " + mFilters.toString());

        ArrayList<PlantItem> filteredPlants = new ArrayList<>();

        ArrayList<Integer> prevIds = new ArrayList<Integer>(mFilteredPlantIds);
        mFilteredPlantIds.clear();
        if (!mFilters.isEmpty()) {
            // Remove current filters that do not match the results
            for (Integer id : prevIds) {
                PlantItem item = mPlants.get(id);
                if (item == null) continue;
                String name = item.Species.toLowerCase();
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
            for (PlantItem item : mPlants.values()) {
                if (mFilteredPlantIds.contains(item.id)) continue;
                String name = item.Species.toLowerCase();
                int i;
                for (i = 0; i < mFilters.size(); ++i) {
                    String filter = mFilters.get(i);
                    if (!name.contains(filter)) break;
                }
                if (i == mFilters.size()) { // if plant contains all filters
                    filteredPlants.add(item);
                    mFilteredPlantIds.add(item.id);
                    if (filteredPlants.size() >= RESULTS_DISPLAY_LIMIT) // verify limit
                        break;
                }
            }
        }

        mPlantSearchAdapter.updatePlantItems(filteredPlants);
    }

    private void updateAllPlantSpecies(ArrayList<PlantItem> items) {
        for (PlantItem item : items) {
            Integer count = mAllPlantSpecies.get(item.Species);
            if (count != null) {
                mAllPlantSpecies.put(item.Species, count + 1);
            }
            else {
                mAllPlantSpecies.put(item.Species, 1);
            }
        }
    }

    private void storePlantDetails(ArrayList<PlantItem> items) {
        for (PlantItem item : items) {
            // Store plant details
            mPlants.put(item.id, item);
            // Update search offset for the species
            Integer offset = mPlantSpeciesLoadOffsets.get(item.Species);
            Log.d(TAG, "Offset ID " + String.valueOf(item.id));
            if (offset == null || offset < item.id) {
                mPlantSpeciesLoadOffsets.put(item.Species, item.id);
            }
            // Update loaded plant count for the species
            Integer count = mLoadedPlantSpeciesCounts.get(item.Species);
            count = (count == null ? 1 : count + 1);
            Log.d(TAG, "Count " + String.valueOf(count));
            mLoadedPlantSpeciesCounts.put(item.Species, count);
            // Check if all plants of the species are loaded
            Integer max_count = mAllPlantSpecies.get(item.Species);
            Log.d(TAG, "Max Count " + String.valueOf(max_count));
            if (max_count == null) continue; // this will almost never happen
            // If they are, we no longer need to search for the species in our consequent searches
            if (count >= max_count) {
                mPlantSpeciesToLoad.remove(item.Species);
            }
        }
    }

    private void loadPlantSpecies() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String url = USDAPlantUtils.buildPlantSearchURL(LITE_LOAD_LIMIT, mLiteLoadOffset, "fields", "Species");

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_LITE_URL_KEY, url);
        mLoadingPB.setVisibility(View.VISIBLE);
        getSupportLoaderManager().restartLoader(PLANT_SEARCH_LITE_LOADER_ID, args, mSearchLiteLoader);
    }

    private void loadPlantDetails() {
        if (mPlantSpeciesToLoad.isEmpty()) return;

        String species = mPlantSpeciesToLoad.getFirst();
        Integer offset = mPlantSpeciesLoadOffsets.get(species);
        if (offset == null) {
            offset = 0;
        }
        String url = USDAPlantUtils.buildPlantSearchURL(RESULTS_LOAD_LIMIT, offset, "Species", species);

        Bundle args = new Bundle();
        args.putString(PLANT_SEARCH_HEAVY_URL_KEY, url);
        getSupportLoaderManager().restartLoader(PLANT_SEARCH_HEAVY_LOADER_ID, args, mSearchHeavyLoader);
    }

    @Override
    public void onPlantItemClick(PlantItem repo) {
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USDAPlantUtils.EXTRA_PLANT_ITEM, repo);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAllPlantSpecies != null) {
            // Saving large amount of data results with android.os.TransactionTooLargeException
            //~ outState.putSerializable(PLANT_SEARCH_LITE_ARRAY_KEY, mAllPlantSpecies);
            //~ outState.putSerializable(PLANT_SEARCH_LITE_LOAD_OFFSET_KEY, mLiteLoadOffset);
        }

        // TODO Also store search results
    }
}
