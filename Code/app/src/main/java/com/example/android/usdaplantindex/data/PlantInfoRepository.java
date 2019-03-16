package com.example.android.usdaplantindex.data;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class PlantInfoRepository {
    private PlantDao mPlantDao;

    public PlantInfoRepository(Application application) {
        PlantDatabase db = PlantDatabase.getDatabase(application);
        mPlantDao = db.plantDao();
    }

    public void insertPlant(PlantInfo plant) {
        new InsertAsyncTask(mPlantDao).execute(plant);
    }

    public void deletePlant(PlantInfo plant) {
        new DeleteAsyncTask(mPlantDao).execute(plant);
    }

    public LiveData<List<PlantInfo>> getAllPlants() {
        return mPlantDao.getAllPlants();
    }

    public LiveData<PlantInfo> getPlantById(Integer id) {
        return mPlantDao.getPlantById(id);
    }

    private static class InsertAsyncTask extends AsyncTask<PlantInfo, Void, Void> {
        private PlantDao mAsyncTaskDao;
        InsertAsyncTask(PlantDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(PlantInfo... plants) {
            mAsyncTaskDao.insert(plants[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<PlantInfo, Void, Void> {
        private PlantDao mAsyncTaskDao;
        DeleteAsyncTask(PlantDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(PlantInfo... plants) {
            mAsyncTaskDao.delete(plants[0]);
            return null;
        }
    }
}
