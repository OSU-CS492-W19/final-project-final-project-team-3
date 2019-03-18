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
import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.List;

public class PlantFavoriteActivity extends AppCompatActivity implements PlantInfoAdapter.OnPlantInfoClickListener {

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
        PlantItem temp = new PlantItem();
        // create new PlantItem
        temp.id = plant.id;
        temp.Scientific_Name_x = plant.Scientific_Name_x;
        temp.Common_Name = plant.Common_Name;
        temp.Symbol = plant.Symbol;
        temp.Duration = plant.Duration;
        temp.Growth_Habit = plant.Growth_Habit;
        temp.Native_Status = plant.Native_Status;
        temp.Category = plant.Category;
        temp.Parents = plant.Parents;
        temp.Forma = plant.Forma;
        temp.Forma_Prefix = plant.Forma_Prefix;
        temp.Variety = plant.Variety;
        temp.Variety_Prefix = plant.Variety_Prefix;
        temp.Hybrid_Variety_Indicator = plant.Hybrid_Variety_Indicator;
        temp.Subvariety = plant.Subvariety;
        temp.Subvariety_Prefix = plant.Subvariety_Prefix;
        temp.Species = plant.Species;
        temp.Hybrid_Species_Indicator = plant.Hybrid_Species_Indicator;
        temp.Subspecies = plant.Subspecies;
        temp.Subspecies_Prefix = plant.Subspecies_Prefix;
        temp.Hybrid_Subspecies_Indicator = plant.Hybrid_Subspecies_Indicator;
        temp.Genus = plant.Genus;
        temp.Hybrid_Genus_Indicator = plant.Hybrid_Genus_Indicator;
        temp.Family = plant.Family;
        temp.Family_Symbol = plant.Family_Symbol;
        temp.Family_Common_Name = plant.Family_Common_Name;
        temp.xOrder = plant.xOrder;
        temp.Class = plant.Class;
        temp.SubClass = plant.SubClass;
        temp.Division = plant.Division;
        temp.SubDivision = plant.SubDivision;
        temp.SuperDivision = plant.SuperDivision;
        temp.Kingdom = plant.Kingdom;
        temp.SubKingdom = plant.SubKingdom;
        temp.State_and_Province = plant.State_and_Province;
        temp.Federal_T_E_Status = plant.Federal_T_E_Status;
        temp.Federal_Noxious_Status = plant.Federal_Noxious_Status;
        temp.State_T_E_Status = plant.State_T_E_Status;
        temp.State_T_E_Common_Name = plant.State_T_E_Common_Name;
        temp.State_Noxious_Status = plant.State_Noxious_Status;
        temp.State_Noxious_Common_Name = plant.State_Noxious_Common_Name;
        temp.Genera_Binomial_Author = plant.Genera_Binomial_Author;
        temp.Trinomial_Author = plant.Trinomial_Author;
        temp.Quadranomial_Author = plant.Quadranomial_Author;
        temp.Questionable_Taxon_Indicator = plant.Questionable_Taxon_Indicator;
        temp.Invasive = plant.Invasive;
        temp.Active_Growth_Period = plant.Active_Growth_Period;
        temp.After_Harvest_Regrowth_Rate = plant.After_Harvest_Regrowth_Rate;
        temp.Bloat = plant.Bloat;
        temp.C_N_Ratio = plant.C_N_Ratio;
        temp.Coppice_Potential = plant.Coppice_Potential;
        temp.Fall_Conspicuous = plant.Fall_Conspicuous;
        temp.Fire_Resistance = plant.Fire_Resistance;
        temp.Flower_Color = plant.Flower_Color;
        temp.Flower_Conspicuous = plant.Flower_Conspicuous;
        temp.Foliage_Color = plant.Foliage_Color;
        temp.Foliage_Porosity_Summer = plant.Foliage_Porosity_Summer;
        temp.Foliage_Porosity_Winter = plant.Foliage_Porosity_Winter;
        temp.Foliage_Texture = plant.Foliage_Texture;
        temp.Fruit_Color = plant.Fruit_Color;
        temp.Fruit_Conspicuous = plant.Fruit_Conspicuous;
        temp.Growth_Form = plant.Growth_Form;
        temp.Growth_Rate = plant.Growth_Rate;
        temp.Height_at_Base_Age_Maximum_feet = plant.Height_at_Base_Age_Maximum_feet;
        temp.Height_Mature_feet = plant.Height_Mature_feet;
        temp.Known_Allelopath = plant.Known_Allelopath;
        temp.Leaf_Retention = plant.Leaf_Retention;
        temp.Lifespan = plant.Lifespan;
        temp.Low_Growing_Grass = plant.Low_Growing_Grass;
        temp.Nitrogen_Fixation = plant.Nitrogen_Fixation;
        temp.Resprout_Ability = plant.Resprout_Ability;
        temp.Shape_and_Orientation = plant.Shape_and_Orientation;
        temp.Toxicity = plant.Toxicity;
        temp.Adapted_to_Coarse_Textured_Soils = plant.Adapted_to_Coarse_Textured_Soils;
        temp.Adapted_to_Medium_Textured_Soils = plant.Adapted_to_Medium_Textured_Soils;
        temp.Adapted_to_Fine_Textured_Soils = plant.Adapted_to_Fine_Textured_Soils;
        temp.Anaerobic_Tolerance = plant.Anaerobic_Tolerance;
        temp.CaCO_3_Tolerance = plant.CaCO_3_Tolerance;
        temp.Cold_Stratification_Required = plant.Cold_Stratification_Required;
        temp.Drought_Tolerance = plant.Drought_Tolerance;
        temp.Fertility_Requirement = plant.Fertility_Requirement;
        temp.Fire_Tolerance = plant.Fire_Tolerance;
        temp.Frost_Free_Days_Minimum = plant.Frost_Free_Days_Minimum;
        temp.Hedge_Tolerance = plant.Hedge_Tolerance;
        temp.Moisture_Use = plant.Moisture_Use;
        temp.pH_Minimum = plant.pH_Minimum;
        temp.pH_Maximum = plant.pH_Maximum;
        temp.Planting_Density_per_Acre_Minimum = plant.Planting_Density_per_Acre_Minimum;
        temp.Planting_Density_per_Acre_Maximum = plant.Planting_Density_per_Acre_Maximum;
        temp.Precipitation_Minimum = plant.Precipitation_Minimum;
        temp.Precipitation_Maximum = plant.Precipitation_Maximum;
        temp.Root_Depth_Minimum_inches = plant.Root_Depth_Minimum_inches;
        temp.Salinity_Tolerance = plant.Salinity_Tolerance;
        temp.Shade_Tolerance = plant.Shade_Tolerance;
        temp.Temperature_Minimum_F = plant.Temperature_Minimum_F;
        temp.Bloom_Period = plant.Bloom_Period;
        temp.Commercial_Availability = plant.Commercial_Availability;
        temp.Fruit_Seed_Abundance = plant.Fruit_Seed_Abundance;
        temp.Fruit_Seed_Period_Begin = plant.Fruit_Seed_Period_Begin;
        temp.Fruit_Seed_Period_End = plant.Fruit_Seed_Period_End;
        temp.Fruit_Seed_Persistence = plant.Fruit_Seed_Persistence;
        temp.Propogated_by_Bare_Root = plant.Propogated_by_Bare_Root;
        temp.Propogated_by_Bulbs = plant.Propogated_by_Bulbs;
        temp.Propogated_by_Container = plant.Propogated_by_Container;
        temp.Propogated_by_Corms = plant.Propogated_by_Corms;
        temp.Propogated_by_Cuttings = plant.Propogated_by_Cuttings;
        temp.Propogated_by_Seed = plant.Propogated_by_Seed;
        temp.Propogated_by_Sod = plant.Propogated_by_Sod;
        temp.Propogated_by_Sprigs = plant.Propogated_by_Sprigs;
        temp.Propogated_by_Tubers = plant.Propogated_by_Tubers;
        temp.Seeds_per_Pound = plant.Seeds_per_Pound;
        temp.Seed_Spread_Rate = plant.Seed_Spread_Rate;
        temp.Seedling_Vigor = plant.Seedling_Vigor;
        temp.Small_Grain = plant.Small_Grain;
        temp.Vegetative_Spread_Rate = plant.Vegetative_Spread_Rate;
        temp.Berry_Nut_Seed_Product = plant.Berry_Nut_Seed_Product;
        temp.Christmas_Tree_Product = plant.Christmas_Tree_Product;
        temp.Fodder_Product = plant.Fodder_Product;
        temp.Fuelwood_Product = plant.Fuelwood_Product;
        temp.Lumber_Product = plant.Lumber_Product;
        temp.Naval_Store_Product = plant.Naval_Store_Product;
        temp.Nursery_Stock_Product = plant.Nursery_Stock_Product;
        temp.Palatable_Browse_Animal = plant.Palatable_Browse_Animal;
        temp.Palatable_Graze_Animal = plant.Palatable_Graze_Animal;
        temp.Palatable_Human = plant.Palatable_Human;
        temp.Post_Product = plant.Post_Product;
        temp.Protein_Potential = plant.Protein_Potential;
        temp.Pulpwood_Product = plant.Pulpwood_Product;
        temp.Veneer_Product = plant.Veneer_Product;

        // start the detail activity
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USDAPlantUtils.EXTRA_PLANT_ITEM, temp);
        startActivity(intent);
    }
}