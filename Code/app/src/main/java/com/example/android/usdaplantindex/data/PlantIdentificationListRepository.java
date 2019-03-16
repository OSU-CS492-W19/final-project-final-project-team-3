package com.example.android.usdaplantindex.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.List;

public class PlantIdentificationListRepository implements LoadPlantsTask.AsyncCallback {

    private static final String TAG = PlantIdentificationListRepository.class.getSimpleName();

    private MutableLiveData<List<PlantItem>> mPlantItems;
    private MutableLiveData<Status> mLoadingStatus;

    private String mCurrentState;
    private String mCurrentGrowthHabit;
    private String mCurrentCategory;
    private String mCurrentDuration;

    public PlantIdentificationListRepository() {
        mPlantItems = new MutableLiveData<>();
        mPlantItems.setValue(null);

        mLoadingStatus = new MutableLiveData<>();
        mLoadingStatus.setValue(Status.SUCCESS);

        mCurrentState = null;
        mCurrentGrowthHabit = null;
        mCurrentCategory = null;
        mCurrentDuration = null;
    }

    /*
     * This method triggers loading of new plant data for a given state, growth habit, category, and/or
     * duration.  New data is not fetched if valid cached data exists matching the specified state,
     * growth habit, category, and/or duration.
     */
    public void loadPlants(String state, String growthHabit, String category, String duration) {
        if (shouldFetchPlants(state, growthHabit, category, duration)) {
            mCurrentState = state;
            mCurrentGrowthHabit = growthHabit;
            mCurrentCategory = category;
            mCurrentDuration = duration;
            mPlantItems.setValue(null);
            mLoadingStatus.setValue(Status.LOADING);
            String url = USDAPlantUtils.buildPlantSearchURL(1000, 0, state, growthHabit, category, duration);
            Log.d(TAG, "fetching new plant data with this URL: " + url);
            new LoadPlantsTask(url, this).execute();
        } else {
            Log.d(TAG, "using cached plant data");
        }
    }

    /*
     * Returns the LiveData object containing the plant data.  An observer can be hooked to this
     * to react to changes in the plant list.
     */
    public LiveData<List<PlantItem>> getPlants() {
        return mPlantItems;
    }

    /*
     * Returns the LiveData object containing the Repository's loading status.  An observer can be
     * hooked to this, e.g. to display a progress bar or error message when appropriate.
     */
    public LiveData<Status> getLoadingStatus() {
        return mLoadingStatus;
    }

    /*
     * This method determines whether a new network call should be made to fetch plant data.
     * New plant data is fetched if one of the following conditions holds:
     *   * The requested state, growth habit, category, or duration don't match the ones
     *     corresponding to the cached plant items.
     *   * If there are currently no cached plant items.
     *   * If the timestamp on the first cached plant item is before the current time (i.e. the
     *     cached plant items are outdated).
     */
    private boolean shouldFetchPlants(String state, String growthHabit, String category, String duration) {
        if (!TextUtils.equals(state, mCurrentState) || !TextUtils.equals(growthHabit, mCurrentGrowthHabit)
                || !TextUtils.equals(category, mCurrentCategory) || !TextUtils.equals(duration, mCurrentDuration)) {
            return true;
        } else {
            List<PlantItem> plantItems = mPlantItems.getValue();
            if (plantItems == null || plantItems.size() == 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /*
     * This is the callback method provided to the AsyncTask that loads new plant data.  It
     * updates the Repository's plant data and loading status with new values when the loading
     * finishes.
     */
    public void onPlantsLoadFinished(List<PlantItem> plantItems) {
        mPlantItems.setValue(plantItems);
        if (plantItems != null) {
            mLoadingStatus.setValue(Status.SUCCESS);
        } else {
            mLoadingStatus.setValue(Status.ERROR);
        }
    }
}