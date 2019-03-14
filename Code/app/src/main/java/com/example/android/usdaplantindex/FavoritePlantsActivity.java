package com.example.android.usdaplantindex;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.usdaplantindex.data.PlantInfo;
import com.example.android.usdaplantindex.utils.USDAUtils;

import java.util.List;
import java.util.ArrayList;

public class FavoritePlantsActivity extends AppCompatActivity implements PlantInfoAdapter.OnPlantInfoClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_plants);

        RecyclerView favoritePlantsRV = findViewById(R.id.rv_favorite_plants);
        favoritePlantsRV.setLayoutManager(new LinearLayoutManager(this));
        favoritePlantsRV.setHasFixedSize(true);

        final PlantInfoAdapter adapter = new PlantInfoAdapter(this);
        favoritePlantsRV.setAdapter(adapter);

        PlantInfoViewModel viewModel = ViewModelProviders.of(this).get(PlantInfoViewModel.class);
        viewModel.getAllPlants().observe(this, new Observer<List<PlantInfo>>() {
            @Override
            public void onChanged(@Nullable List<PlantInfo> plants) {
                // update the adapter
                adapter.updatePlantItems(plants);
            }
        });
    }

    @Override
    public void onPlantInfoClick(PlantInfo plant) {
        USDAUtils.PlantItem temp = new USDAUtils.PlantItem();
        // create new PlantItem
        temp.id = plant.id;
        temp.Scientific_Name_x = plant.Scientific_Name_x;
        temp.Common_Name = plant.Common_Name;
        temp.Symbol = plant.Symbol;
        temp.Group = plant.Group;
        temp.Family = plant.Family;
        temp.Duration = plant.Duration;
        temp.Growth_Habit = plant.Growth_Habit;
        temp.Native_Status = plant.Native_Status;
        temp.Category = plant.Category;
        temp.xOrder = plant.xOrder;
        temp.SubClass = plant.SubClass;
        temp.Class = plant.Class;
        temp.Kingdom = plant.Kingdom;
        temp.Species = plant.Species;
        temp.Subspecies = plant.Subspecies;
        temp.State_and_Province = plant.State_and_Province;

        // start the detail activity
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USDAUtils.EXTRA_PLANT_ITEM, temp);
        startActivity(intent);
    }
}