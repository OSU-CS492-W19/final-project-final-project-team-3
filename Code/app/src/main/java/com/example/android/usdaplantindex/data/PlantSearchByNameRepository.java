package com.example.android.usdaplantindex.data;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.ArrayList;
import java.util.List;

public class PlantSearchByNameRepository implements LoadPlantsTask.AsyncCallback {

    private static final String TAG = PlantSearchByNameRepository.class.getSimpleName();

    @Override
    public void onPlantsLoadFinished(List<PlantItem> plantItems) {

    }

    private void updateAllPlantNames(ArrayList<PlantItem> items) {
        for (PlantItem item : items) {
            mAllPlantNames.put(item.id, item.Scientific_Name_x.toLowerCase());
        }
    }

    private void storePlantDetails(ArrayList<PlantItem> items) {
        for (PlantItem item : items) {
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
}
