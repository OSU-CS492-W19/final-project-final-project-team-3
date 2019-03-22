package com.example.android.usdaplantindex;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.data.PlantSearchByCommonRepository;
import com.example.android.usdaplantindex.data.PlantSearchByScientificRepository;
import com.example.android.usdaplantindex.data.Status;

import java.util.List;

public class PlantSearchByCommonViewModel extends AndroidViewModel {
    private LiveData<List<PlantItem>> mFilteredPlants;
    private LiveData<Integer> mLitePlantCount;
    private LiveData<Status> mLiteLoadingStatus;
    private LiveData<Status> mHeavyLoadingStatus;

    private PlantSearchByCommonRepository mRepository;

    public PlantSearchByCommonViewModel(Application application) {
        super(application);

        mRepository = new PlantSearchByCommonRepository();
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
