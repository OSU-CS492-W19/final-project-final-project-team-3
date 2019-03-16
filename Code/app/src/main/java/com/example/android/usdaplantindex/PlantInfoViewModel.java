package com.example.android.usdaplantindex;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.android.usdaplantindex.data.PlantInfo;
import com.example.android.usdaplantindex.data.PlantInfoRepository;

import java.util.List;

public class PlantInfoViewModel extends AndroidViewModel {
    private PlantInfoRepository mPlantInfoRepository;

    public PlantInfoViewModel(Application application) {
        super(application);
        mPlantInfoRepository = new PlantInfoRepository(application);
    }

    public void insertPlant(PlantInfo plant) {
        mPlantInfoRepository.insertPlant(plant);
    }

    public void deletePlant(PlantInfo plant) {
        mPlantInfoRepository.deletePlant(plant);
    }

    public LiveData<List<PlantInfo>> getAllPlants() {
        return mPlantInfoRepository.getAllPlants();
    }

    public LiveData<PlantInfo> getPlantById(Integer id) {
        return mPlantInfoRepository.getPlantById(id);
    }
}
