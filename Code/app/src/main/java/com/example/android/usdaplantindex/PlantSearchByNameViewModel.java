package com.example.android.usdaplantindex;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.data.PlantSearchByNameRepository;
import com.example.android.usdaplantindex.data.Status;

import java.util.List;

public class PlantSearchByNameViewModel extends ViewModel {
    private LiveData<List<PlantItem>> mFilteredPlants;
    private LiveData<Integer> mLitePlantCount;
    private LiveData<Status> mLiteLoadingStatus;
    private LiveData<Status> mHeavyLoadingStatus;

    private PlantSearchByNameRepository mRepository;

    public PlantSearchByNameViewModel() {
        mRepository = new PlantSearchByNameRepository();
        mFilteredPlants = mRepository.getFilteredPlants();
        mLitePlantCount = mRepository.getLitePlantCount();
        mLiteLoadingStatus = mRepository.getLiteLoadingStatus();
        mHeavyLoadingStatus = mRepository.getHeavyLoadingStatus();
    }

    public void loadPlantNames() {
        mRepository.loadPlantNames();
    }

    public void searchChanged(String s) {
        mRepository.searchChanged(s);
    }

    public LiveData<List<PlantItem>> getFilteredPlants() {
        return mFilteredPlants;
    }

    public LiveData<Integer> getLitePlantCount() {
        return mLitePlantCount;
    }

    public LiveData<Status> getLiteLoadingStatus() {
        return mLiteLoadingStatus;
    }

    public LiveData<Status> getHeavyLoadingStatus() {
        return mHeavyLoadingStatus;
    }
}
