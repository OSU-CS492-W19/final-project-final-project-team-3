package com.example.android.usdaplantindex.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PlantDao {
    @Insert
    void insert(PlantInfo plant);

    @Delete
    void delete(PlantInfo plant);

    @Query("SELECT * FROM plants")
    LiveData<List<PlantInfo>> getAllPlants();

    @Query("SELECT * FROM plants WHERE id = :idnum LIMIT 1")
    LiveData<PlantInfo> getPlantById(Integer idnum);
}