package com.example.android.usdaplantindex.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {PlantInfo.class}, version = 1, exportSchema = false)
public abstract class PlantDatabase extends RoomDatabase {
    private static volatile PlantDatabase INSTANCE;

    static PlantDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PlantDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PlantDatabase.class, "usda_plant_db").build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract PlantDao plantDao();
}
