package com.example.android.usdaplantindex;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.example.android.usdaplantindex.data.PlantIdentificationListRepository;
import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.data.Status;
import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.ArrayList;
import java.util.List;

public class PlantSearchByNameViewModel extends ViewModel {
    private LiveData<List<PlantItem>> mPlantItems;
    private LiveData<Status> mLoadingStatus;

    private PlantIdentificationListRepository mRepository;

    public PlantSearchByNameViewModel() {
        mRepository = new PlantIdentificationListRepository();
        mPlantItems = mRepository.getPlants();
        mLoadingStatus = mRepository.getLoadingStatus();
    }

    public void loadPlants(String state, String growthHabit, String category, String duration) {
        mRepository.loadPlants(state, growthHabit, category, duration);
    }

    public LiveData<List<PlantItem>> getPlants() {
        return mPlantItems;
    }

    public LiveData<Status> getLoadingStatus() {
        return mLoadingStatus;
    }
}
