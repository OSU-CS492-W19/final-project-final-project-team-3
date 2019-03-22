package com.example.android.usdaplantindex.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class PlantSearchByScientificRepository {

    private static final String TAG = PlantSearchByScientificRepository.class.getSimpleName();

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
    private static final Integer RESULTS_DISPLAY_LIMIT = 25;

    // Contains all plant scientific names and their ids
    private Hashtable<Integer, String> mAllPlantNames;
    private Integer mLiteLoadOffset;

    // Contains plant details (initially empty)
    private Hashtable<Integer, PlantItem> mPlants;

    // Whenever we enter something to search, this is emptied and
    // filled in with all the ids of all the scientific names that need to be loaded.
    private HashSet<Integer> mPlantIdsToLoad;

    // Contains search box text split into individual words
    private List<String> mFilters;
    private List<Integer> mFilteredPlantIds;

    private LoadPlantsTask.AsyncCallback mAsyncTaskLite;
    private LoadPlantsTask.AsyncCallback mAsyncTaskHeavy;

    private MutableLiveData<List<PlantItem>> mFilteredPlants;

    private MutableLiveData<Status> mLiteLoadingStatus;
    private MutableLiveData<Status> mHeavyLoadingStatus;

    private MutableLiveData<Integer> mLitePlantCount;

    public PlantSearchByScientificRepository() {
        mAllPlantNames = new Hashtable<>();
        mPlantIdsToLoad = new HashSet<>();
        mPlants = new Hashtable<>();

        mFilteredPlantIds = new LinkedList<>();
        mLiteLoadOffset = new Integer(0);

        mFilters = new LinkedList<>();

        mFilteredPlants = new MutableLiveData<>();
        mFilteredPlants.setValue(new LinkedList<PlantItem>());

        mLiteLoadingStatus = new MutableLiveData<>();
        mLiteLoadingStatus.setValue(Status.SUCCESS);

        mHeavyLoadingStatus = new MutableLiveData<>();
        mHeavyLoadingStatus.setValue(Status.SUCCESS);

        mLitePlantCount = new MutableLiveData<>();
        mLitePlantCount.setValue(new Integer(0));

        mAsyncTaskLite = new LoadPlantsTask.AsyncCallback() {
            @Override
            public void onPlantsLoadFinished(List<PlantItem> items) {
                if (items != null) {
                    updateAllPlantNames(items);
                    if (!items.isEmpty()) {
                        mLiteLoadOffset += LITE_LOAD_LIMIT;
                        mLiteLoadingStatus.setValue(Status.SUCCESS);
                        return;
                    }
                }
                mLiteLoadingStatus.setValue(Status.ERROR);
            }
        };

        mAsyncTaskHeavy = new LoadPlantsTask.AsyncCallback() {
            @Override
            public void onPlantsLoadFinished(List<PlantItem> items) {
                // Store details
                if (items != null) {
                    storePlantDetails(items);
                    mHeavyLoadingStatus.setValue(Status.SUCCESS);
                }
                else {
                    mHeavyLoadingStatus.setValue(Status.ERROR);
                }

                // Update search results with new/additional information
                updateSearchResults();

                // Resume loading until plant details of all, matched plant scientific names
                // are loaded
                if (mFilteredPlantIds.size() < RESULTS_DISPLAY_LIMIT) {
                    loadPlantDetails();
                }
            }
        };
    }

    public LiveData<Integer> getLitePlantCount() {
        return mLitePlantCount;
    }

    public LiveData<List<PlantItem>> getFilteredPlants() {
        return mFilteredPlants;
    }

    public LiveData<Status> getLiteLoadingStatus() {
        return mLiteLoadingStatus;
    }

    public LiveData<Status> getHeavyLoadingStatus() {
        return mHeavyLoadingStatus;
    }

    public void searchChanged(String s) {
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

        LinkedList<PlantItem> filteredPlants = new LinkedList<>();

        ArrayList<Integer> prevIds = new ArrayList<Integer>(mFilteredPlantIds);
        mFilteredPlantIds.clear();
        if (!mFilters.isEmpty()) {
            // Remove current filters that do not match the results
            for (Integer id : prevIds) {
                PlantItem item = mPlants.get(id);
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
            for (PlantItem item : mPlants.values()) {
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
                    if (filteredPlants.size() >= RESULTS_DISPLAY_LIMIT) // verify limit
                        break;
                }
            }
        }

        mFilteredPlants.setValue(filteredPlants);
    }

    private void updateAllPlantNames(List<PlantItem> items) {
        for (PlantItem item : items) {
            mAllPlantNames.put(item.id, item.Scientific_Name_x.toLowerCase());
        }
        mLitePlantCount.setValue(mAllPlantNames.size());
    }

    private void storePlantDetails(List<PlantItem> items) {
        for (PlantItem item : items) {
            // Store plant details
            mPlants.put(item.id, item);
            // Remove id from the to load set
            mPlantIdsToLoad.remove(item.id);
        }
    }

    public void loadPlantNames() {
        Log.d(TAG, "Lite load " + String.valueOf(mLiteLoadOffset));

        String url = USDAPlantUtils.buildPlantSearchURL(
                LITE_LOAD_LIMIT, mLiteLoadOffset, "fields", "id,Scientific_Name_x");

        mLiteLoadingStatus.setValue(Status.LOADING);

        new LoadPlantsTask(url, mAsyncTaskLite).execute();
    }

    public void loadPlantDetails() {
        if (mPlantIdsToLoad.isEmpty()) return;

        Integer id = mPlantIdsToLoad.iterator().next();
        String url = USDAPlantUtils.buildPlantSearchURL(
                RESULTS_LOAD_LIMIT, 0, "id", String.valueOf(id));

        mHeavyLoadingStatus.setValue(Status.LOADING);

        new LoadPlantsTask(url, mAsyncTaskHeavy).execute();
    }
}
