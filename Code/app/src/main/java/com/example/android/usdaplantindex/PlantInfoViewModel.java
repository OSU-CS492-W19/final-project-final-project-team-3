package com.example.android.usdaplantindex;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.android.usdaplantindex.data.PlantInfo;
import com.example.android.usdaplantindex.data.PlantRepository;

import java.util.List;

public class PlantInfoViewModel extends AndroidViewModel {
    private PlantRepository mPlantRepository;

    public PlantInfoViewModel(Application application) {
        super(application);
        mPlantRepository = new PlantRepository(application);
    }

    public void insertPlant(PlantInfo plant) {
        mPlantRepository.insertPlant(plant);
    }

    public void deletePlant(PlantInfo plant) {
        mPlantRepository.deletePlant(plant);
    }

    public LiveData<List<PlantInfo>> getAllPlants() {
        return mPlantRepository.getAllPlants();
    }

    public LiveData<PlantInfo> getPlantById(Integer id) {
        return mPlantRepository.getPlantById(id);
    }
}
