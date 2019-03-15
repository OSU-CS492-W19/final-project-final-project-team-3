package com.example.android.usdaplantindex;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.usdaplantindex.data.IdentifyPlantRepository;
import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.data.Status;

import java.util.List;

public class IdentifyPlantViewModel extends ViewModel {

    private LiveData<List<PlantItem>> mPlantItems;
    private LiveData<Status> mLoadingStatus;

    private IdentifyPlantRepository mRepository;

    public IdentifyPlantViewModel() {
        mRepository = new IdentifyPlantRepository();
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