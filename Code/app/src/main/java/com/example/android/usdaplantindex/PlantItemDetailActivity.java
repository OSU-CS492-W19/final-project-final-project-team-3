package com.example.android.usdaplantindex;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.usdaplantindex.utils.USAUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.example.android.usdaplantindex.data.PlantInfo;

public class PlantItemDetailActivity extends AppCompatActivity {

    public PlantItemDetailActivity parentDetail = this;
    public String sciString = "";
    public String comString = "";

    private TextView mPlantSciTV;
    private TextView mPlantComTV;
    private ImageView mPlantPicIV;
    private ImageView mPlantFavoriteIV;
    private TextView mHGeneralTV;
    private TextView mPlantGeneralTV;
    private TextView mHMorphTV;
    private TextView mPlantMorphTV;
    private TextView mHGrowthTV;
    private TextView mPlantGrowthTV;
    private TextView mHRepTV;
    private TextView mPlantRepTV;
    private TextView mHSuitTV;
    private TextView mPlantSuitTV;

    private USAUtils.PlantItem mPlantItem;

    private PlantInfoViewModel mPlantInfoViewModel;
    private PlantInfo mPlantInfo;
    private boolean mIsFav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_item_detail);

        mPlantSciTV = findViewById(R.id.tv_plant_scientific);
        mPlantComTV = findViewById(R.id.tv_plant_common);
        mPlantPicIV = findViewById(R.id.iv_plant_pic_det);
        mPlantFavoriteIV = findViewById(R.id.iv_plant_favorite);
        mPlantInfoViewModel = ViewModelProviders.of(this).get(PlantInfoViewModel.class);
        mHGeneralTV = findViewById(R.id.tv_h_general);
        mPlantGeneralTV = findViewById(R.id.tv_plant_general);
        mHMorphTV = findViewById(R.id.tv_h_morphology);
        mPlantMorphTV = findViewById(R.id.tv_plant_morphology);
        mHGrowthTV = findViewById(R.id.tv_h_growth);
        mPlantGrowthTV = findViewById(R.id.tv_plant_growth);
        mHRepTV = findViewById(R.id.tv_h_reproduction);
        mPlantRepTV = findViewById(R.id.tv_plant_reproduction);
        mHSuitTV = findViewById(R.id.tv_h_suitability);
        mPlantSuitTV = findViewById(R.id.tv_plant_suitability);

        mPlantInfo = null;
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(USAUtils.EXTRA_PLANT_ITEM)) {
            mPlantItem = (USAUtils.PlantItem)intent.getSerializableExtra(
                    USAUtils.EXTRA_PLANT_ITEM
            );
            fillInLayout(mPlantItem);

            sciString = mPlantItem.Scientific_Name_x;
            comString = mPlantItem.Common_Name;

            // Finds an image URL based on the plants name.
            findImageURL mImageFinder = new findImageURL();
            mImageFinder.execute();

            mPlantInfo = new PlantInfo();

            mPlantInfo = createPlantInfo(mPlantItem);

            // checks for favorite
            mPlantInfoViewModel.getPlantById(mPlantInfo.id).observe(this, new Observer<PlantInfo>() {
                @Override
                public void onChanged(@Nullable PlantInfo plant) {
                    if (plant != null) {
                        mIsFav = true;
                        mPlantFavoriteIV.setImageResource(R.drawable.ic_favorites_black_24dp);
                    } else {
                        mIsFav = false;
                        mPlantFavoriteIV.setImageResource(R.drawable.ic_favorites_border_black_24dp);
                    }
                }
            });
        }

        // set click listener on the favorites button
        mPlantFavoriteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlantInfo != null) {
                    if (!mIsFav) {
                        mPlantInfoViewModel.insertPlant(mPlantInfo);
                    } else {
                        mPlantInfoViewModel.deletePlant(mPlantInfo);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.plant_item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                sharePlant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // This will output the string of the header only if the
    // fields are not empty.
    public String getHeaderString(String headerName, String fields) {
        String header = "";
        if(fields != "") {
            header = headerName;
        }
        return header;
    }

    // Takes in the name and value of a string and returns a string
    // of that shows the fields name and value, if the value is not empty.
    public String getFieldString(String fieldName, String fieldValue) {
        String returnString = "";
        if(fieldValue != "" && fieldValue != null) {
            returnString = fieldName + ": " + fieldValue + "\n";
        }
        return returnString;
    }

    // Returns a string with all of the general information
    // for the given plant.
    public String getGeneralString() {
        String detailString = "";

        detailString += getFieldString("Alternative Scientific Name", mPlantItem.Scientific_Name_y);
        detailString += getFieldString("Symbol", mPlantItem.Symbol);
        detailString += getFieldString("Alternative Symbol", mPlantItem.Accepted_Symbol_y);
        detailString += getFieldString("Alternative Synonym Symbol", mPlantItem.Synonym_Symbol_y);
        detailString += getFieldString("Duration", mPlantItem.Duration);
        detailString += getFieldString("Growth Habit", mPlantItem.Growth_Habit);
        detailString += getFieldString("Native Status", mPlantItem.Native_Status);
        detailString += getFieldString("Group", mPlantItem.Category);
        detailString += getFieldString("Parents", mPlantItem.Parents);
        detailString += getFieldString("Forma", mPlantItem.Forma);
        detailString += getFieldString("Forma Prefix", mPlantItem.Forma_Prefix);
        detailString += getFieldString("Variety", mPlantItem.Variety);
        detailString += getFieldString("Variety Prefix", mPlantItem.Variety_Prefix);
        detailString += getFieldString("Hybrid Variety Indicator", mPlantItem.Hybrid_Variety_Indicator);
        detailString += getFieldString("Subvariety", mPlantItem.Subvariety);
        detailString += getFieldString("Subvariety Prefix", mPlantItem.Subvariety_Prefix);
        detailString += getFieldString("Species", mPlantItem.Species);
        detailString += getFieldString("Hybrid Species Indicator", mPlantItem.Hybrid_Species_Indicator);
        detailString += getFieldString("Subspecies", mPlantItem.Subspecies);
        detailString += getFieldString("Subspecies Prefix", mPlantItem.Subspecies_Prefix);
        detailString += getFieldString("Hybrid Subspecies Indicator", mPlantItem.Hybrid_Subspecies_Indicator);
        detailString += getFieldString("Genus", mPlantItem.Genus);
        detailString += getFieldString("Hybrid Genus Indicator", mPlantItem.Hybrid_Genus_Indicator);
        detailString += getFieldString("Family", mPlantItem.Family);
        detailString += getFieldString("Family Symbol", mPlantItem.Family_Symbol);
        detailString += getFieldString("Family Common Name", mPlantItem.Family_Common_Name);
        detailString += getFieldString("Order", mPlantItem.xOrder);
        detailString += getFieldString("Class", mPlantItem.Class);
        detailString += getFieldString("SubClass", mPlantItem.SubClass);
        detailString += getFieldString("Division", mPlantItem.Division);
        detailString += getFieldString("SubDivision", mPlantItem.SubDivision);
        detailString += getFieldString("SuperDivision", mPlantItem.SuperDivision);
        detailString += getFieldString("Kingdom", mPlantItem.Kingdom);
        detailString += getFieldString("SubKingdom", mPlantItem.SubKingdom);
        detailString += getFieldString("State and Province", mPlantItem.State_and_Province);
        detailString += getFieldString("Federal T/E Status", mPlantItem.Federal_T_E_Status);
        detailString += getFieldString("Federal Noxious Status", mPlantItem.Federal_Noxious_Status);
        detailString += getFieldString("State T/E Status", mPlantItem.State_T_E_Status);
        detailString += getFieldString("State T/E Common Name", mPlantItem.State_T_E_Common_Name);
        detailString += getFieldString("State Noxious Status", mPlantItem.State_Noxious_Status);
        detailString += getFieldString("State Noxious Common Name", mPlantItem.State_Noxious_Common_Name);
        detailString += getFieldString("Genera Binomial Author", mPlantItem.Genera_Binomial_Author);
        detailString += getFieldString("Trinomial Author", mPlantItem.Trinomial_Author);
        detailString += getFieldString("Quadranomial Author", mPlantItem.Quadranomial_Author);
        detailString += getFieldString("Questionable Taxon Indicator", mPlantItem.Questionable_Taxon_Indicator);
        detailString += getFieldString("Invasive", mPlantItem.Invasive);

        return detailString;
    }

    // Returns a string with all of the morph information
    // for the given plant.
    public String getMorphString() {
        String detailString = "";

        detailString += getFieldString("Active Growth Period", mPlantItem.Active_Growth_Period);
        detailString += getFieldString("After Harvest Regrowth Rate", mPlantItem.After_Harvest_Regrowth_Rate);
        detailString += getFieldString("Bloat", mPlantItem.Bloat);
        detailString += getFieldString("C:N Ratio", mPlantItem.C_N_Ratio);
        detailString += getFieldString("Coppice Potential", mPlantItem.Coppice_Potential);
        detailString += getFieldString("Fall Conspicuous", mPlantItem.Fall_Conspicuous);
        detailString += getFieldString("Fire Resistance", mPlantItem.Fire_Resistance);
        detailString += getFieldString("Flower Color", mPlantItem.Flower_Color);
        detailString += getFieldString("Flower Conspicous", mPlantItem.Flower_Conspicuous);
        detailString += getFieldString("Foliage Color", mPlantItem.Foliage_Color);
        detailString += getFieldString("Foliage Porosity Summer", mPlantItem.Foliage_Porosity_Summer);
        detailString += getFieldString("Foliage Porosity Winter", mPlantItem.Foliage_Porosity_Winter);
        detailString += getFieldString("Foliage Texture", mPlantItem.Foliage_Texture);
        detailString += getFieldString("Fruit/Seed Color", mPlantItem.Fruit_Color);
        detailString += getFieldString("Fruit/Seed Conspicuous", mPlantItem.Fruit_Conspicuous);
        detailString += getFieldString("Growth Form", mPlantItem.Growth_Form);
        detailString += getFieldString("Growth Rate", mPlantItem.Growth_Rate);
        detailString += getFieldString("Height at 20 Years, Maximum (feet)", mPlantItem.Height_at_Base_Age_Maximum_feet);
        detailString += getFieldString("Height, Mature (feet)", mPlantItem.Height_Mature_feet);
        detailString += getFieldString("Known Allelopath", mPlantItem.Known_Allelopath);
        detailString += getFieldString("Leaf Retention", mPlantItem.Leaf_Retention);
        detailString += getFieldString("Lifespan", mPlantItem.Lifespan);
        detailString += getFieldString("Low Growing Grass", mPlantItem.Low_Growing_Grass);
        detailString += getFieldString("Nitrogen Fixation", mPlantItem.Nitrogen_Fixation);
        detailString += getFieldString("Resprout Ability", mPlantItem.Resprout_Ability);
        detailString += getFieldString("Shape and Orientation", mPlantItem.Shape_and_Orientation);
        detailString += getFieldString("Toxicity", mPlantItem.Toxicity);

        return detailString;
    }

    // Returns a string with all of the growth information
    // for the given plant.
    public String getGrowthString() {
        String detailString = "";

        detailString += getFieldString("Adapted to Coarse Textured Soils", mPlantItem.Adapted_to_Coarse_Textured_Soils);
        detailString += getFieldString("Adapted to Fine Textured Soils", mPlantItem.Adapted_to_Fine_Textured_Soils);
        detailString += getFieldString("Adapted to Medium Textured Soils", mPlantItem.Adapted_to_Medium_Textured_Soils);
        detailString += getFieldString("Anaerobic Tolerance", mPlantItem.Anaerobic_Tolerance);
        detailString += getFieldString("CaCO3 Tolerance", mPlantItem.CaCO_3_Tolerance);
        detailString += getFieldString("Cold Stratification Required", mPlantItem.Cold_Stratification_Required);
        detailString += getFieldString("Drought Tolerance", mPlantItem.Drought_Tolerance);
        detailString += getFieldString("Fertility Requirement", mPlantItem.Fertility_Requirement);
        detailString += getFieldString("Fire Tolerance", mPlantItem.Fire_Tolerance);
        detailString += getFieldString("Frost Free Days, Minimum", mPlantItem.Frost_Free_Days_Minimum);
        detailString += getFieldString("Hedge_Tolerance", mPlantItem.Hedge_Tolerance);
        detailString += getFieldString("Moisture Use", mPlantItem.Moisture_Use);
        detailString += getFieldString("pH, Minimum", mPlantItem.pH_Minimum);
        detailString += getFieldString("pH, Maximum", mPlantItem.pH_Maximum);
        detailString += getFieldString("Planting Density per Acre, Minimum", mPlantItem.Planting_Density_per_Acre_Minimum);
        detailString += getFieldString("Planting Density per Acre, Maximum", mPlantItem.Planting_Density_per_Acre_Maximum);
        detailString += getFieldString("Precipitation, Minimum", mPlantItem.Precipitation_Minimum);
        detailString += getFieldString("Precipitation, Maximum", mPlantItem.Precipitation_Maximum);
        detailString += getFieldString("Root Depth, Minimum (inches)", mPlantItem.Root_Depth_Minimum_inches);
        detailString += getFieldString("Salinity Tolerance", mPlantItem.Salinity_Tolerance);
        detailString += getFieldString("Shade Tolerance", mPlantItem.Shade_Tolerance);
        detailString += getFieldString("Temperature, Minimum (°F)", mPlantItem.Temperature_Minimum_F);

        return detailString;
    }

    // Returns a string with all of the rep information
    // for the given plant.
    public String getRepString() {
        String detailString = "";

        detailString += getFieldString("Bloom Period", mPlantItem.Bloom_Period);
        detailString += getFieldString("Commercial Availability", mPlantItem.Commercial_Availability);
        detailString += getFieldString("Fruit/Seed Abundance", mPlantItem.Fruit_Seed_Abundance);
        detailString += getFieldString("Fruit/Seed Period Begin", mPlantItem.Fruit_Seed_Period_Begin);
        detailString += getFieldString("Fruit/Seed Period End", mPlantItem.Fruit_Seed_Period_End);
        detailString += getFieldString("Fruit/Seed Persistence", mPlantItem.Fruit_Seed_Persistence);
        detailString += getFieldString("Propagated by Bare Root", mPlantItem.Propogated_by_Bare_Root);
        detailString += getFieldString("Propagated by Bulb", mPlantItem.Propogated_by_Bulbs);
        detailString += getFieldString("Propagated by Container", mPlantItem.Propogated_by_Container);
        detailString += getFieldString("Propagated by Corm", mPlantItem.Propogated_by_Corms);
        detailString += getFieldString("Propagated by Cuttings", mPlantItem.Propogated_by_Cuttings);
        detailString += getFieldString("Propagated by Seed", mPlantItem.Propogated_by_Seed);
        detailString += getFieldString("Propagated by Sod", mPlantItem.Propogated_by_Sod);
        detailString += getFieldString("Propagated by Sprigs", mPlantItem.Propogated_by_Sprigs);
        detailString += getFieldString("Propagated by Tubers", mPlantItem.Propogated_by_Tubers);
        detailString += getFieldString("Seed per Pound", mPlantItem.Seeds_per_Pound);
        detailString += getFieldString("Seed Spread Rate", mPlantItem.Seed_Spread_Rate);
        detailString += getFieldString("Seedling Vigor", mPlantItem.Seedling_Vigor);
        detailString += getFieldString("Small Grain", mPlantItem.Small_Grain);
        detailString += getFieldString("Vegetative Spread Rate", mPlantItem.Vegetative_Spread_Rate);

        return detailString;
    }

    // Returns a string with all of the suit information
    // for the given plant.
    public String getSuitString() {
        String detailString = "";

        detailString += getFieldString("Berry/Nut/Seed Product", mPlantItem.Berry_Nut_Seed_Product);
        detailString += getFieldString("Christmas Tree Product", mPlantItem.Christmas_Tree_Product);
        detailString += getFieldString("Fodder Product", mPlantItem.Fodder_Product);
        detailString += getFieldString("Fuelwood Product", mPlantItem.Fuelwood_Product);
        detailString += getFieldString("Lumber Product", mPlantItem.Lumber_Product);
        detailString += getFieldString("Naval Store Product", mPlantItem.Naval_Store_Product);
        detailString += getFieldString("Nursery Stock Product", mPlantItem.Nursery_Stock_Product);
        detailString += getFieldString("Palatable Browse Animal", mPlantItem.Palatable_Browse_Animal);
        detailString += getFieldString("Palatable Graze Animal", mPlantItem.Palatable_Graze_Animal);
        detailString += getFieldString("Palatable Human", mPlantItem.Palatable_Human);
        detailString += getFieldString("Post Product", mPlantItem.Post_Product);
        detailString += getFieldString("Protein Potential", mPlantItem.Protein_Potential);
        detailString += getFieldString("Pulpwood Product", mPlantItem.Pulpwood_Product);
        detailString += getFieldString("Veneer Product", mPlantItem.Veneer_Product);

        return detailString;
    }


    // Allows the user to share this plant with other users.
    public void sharePlant() {
        if (mPlantItem != null) {

            String detailString = getGeneralString();
            String shareText = R.string.plant_item_share_text + "\n" +
                    sciString + "\n" + comString + "\n" +  detailString;
            ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setText(shareText)
                    .setChooserTitle(R.string.share_chooser_title)
                    .startChooser();
        }
    }

    // Displays information about the selected plant.
    private void fillInLayout(USAUtils.PlantItem plantItem) {
        String sciString = plantItem.Scientific_Name_x;
        String comString = plantItem.Common_Name;
        String generalString = getGeneralString();
        String generalHString = getHeaderString(getString(R.string.plant_text_general), generalString);
        String morphString = getMorphString();
        String morphHString = getHeaderString(getString(R.string.plant_text_morphology), morphString);
        String growthString = getGrowthString();
        String growthHString = getHeaderString(getString(R.string.plant_text_growth), growthString);
        String repString = getRepString();
        String repHString = getHeaderString(getString(R.string.plant_text_reproduction), repString);
        String suitString = getSuitString();
        String suitHString = getHeaderString(getString(R.string.plant_text_suitability), suitString);

        mPlantSciTV.setText(sciString);
        mPlantComTV.setText(comString);

        mHGeneralTV.setText(generalHString);
        mPlantGeneralTV.setText(generalString);
        mHMorphTV.setText(morphHString);
        mPlantMorphTV.setText(morphString);
        mHGrowthTV.setText(growthHString);
        mPlantGrowthTV.setText(growthString);
        mHRepTV.setText(repHString);
        mPlantRepTV.setText(repString);
        mHSuitTV.setText(suitHString);
        mPlantSuitTV.setText(suitString);
    }

    private PlantInfo createPlantInfo(USAUtils.PlantItem pItem){

        PlantInfo pInfo = new PlantInfo();
        // change PlantItem to PlantInfo
        pInfo.id = pItem.id;
        pInfo.Scientific_Name_x = pItem.Scientific_Name_x;
        pInfo.Scientific_Name_y = pItem.Scientific_Name_y;
        pInfo.Common_Name = pItem.Common_Name;
        pInfo.Symbol = pItem.Symbol;
        pInfo.Duration = pItem.Duration;
        pInfo.Growth_Habit = pItem.Growth_Habit;
        pInfo.Native_Status = pItem.Native_Status;
        pInfo.Category = pItem.Category;
        pInfo.Parents = pItem.Parents;
        pInfo.Forma = pItem.Forma;
        pInfo.Forma_Prefix = pItem.Forma_Prefix;
        pInfo.Variety = pItem.Variety;
        pInfo.Variety_Prefix = pItem.Variety_Prefix;
        pInfo.Hybrid_Variety_Indicator = pItem.Hybrid_Variety_Indicator;
        pInfo.Subvariety = pItem.Subvariety;
        pInfo.Subvariety_Prefix = pItem.Subvariety_Prefix;
        pInfo.Species = pItem.Species;
        pInfo.Hybrid_Species_Indicator = pItem.Hybrid_Species_Indicator;
        pInfo.Subspecies = pItem.Subspecies;
        pInfo.Subspecies_Prefix = pItem.Subspecies_Prefix;
        pInfo.Hybrid_Subspecies_Indicator = pItem.Hybrid_Subspecies_Indicator;
        pInfo.Genus = pItem.Genus;
        pInfo.Hybrid_Genus_Indicator = pItem.Hybrid_Genus_Indicator;
        pInfo.Family = pItem.Family;
        pInfo.Family_Symbol = pItem.Family_Symbol;
        pInfo.Family_Common_Name = pItem.Family_Common_Name;
        pInfo.xOrder = pItem.xOrder;
        pInfo.Class = pItem.Class;
        pInfo.SubClass = pItem.SubClass;
        pInfo.Division = pItem.Division;
        pInfo.SubDivision = pItem.SubDivision;
        pInfo.SuperDivision = pItem.SuperDivision;
        pInfo.Kingdom = pItem.Kingdom;
        pInfo.SubKingdom = pItem.SubKingdom;
        pInfo.State_and_Province = pItem.State_and_Province;
        pInfo.Federal_T_E_Status = pItem.Federal_T_E_Status;
        pInfo.Federal_Noxious_Status = pItem.Federal_Noxious_Status;
        pInfo.State_T_E_Status = pItem.State_T_E_Status;
        pInfo.State_T_E_Common_Name = pItem.State_T_E_Common_Name;
        pInfo.State_Noxious_Status = pItem.State_Noxious_Status;
        pInfo.State_Noxious_Common_Name = pItem.State_Noxious_Common_Name;
        pInfo.Genera_Binomial_Author = pItem.Genera_Binomial_Author;
        pInfo.Trinomial_Author = pItem.Trinomial_Author;
        pInfo.Quadranomial_Author = pItem.Quadranomial_Author;
        pInfo.Questionable_Taxon_Indicator = pItem.Questionable_Taxon_Indicator;
        pInfo.Invasive = pItem.Invasive;
        pInfo.Accepted_Symbol_y = pItem.Accepted_Symbol_y;
        pInfo.Synonym_Symbol_y = pItem.Synonym_Symbol_y;
        pInfo.Active_Growth_Period = pItem.Active_Growth_Period;
        pInfo.After_Harvest_Regrowth_Rate = pItem.After_Harvest_Regrowth_Rate;
        pInfo.Bloat = pItem.Bloat;
        pInfo.C_N_Ratio = pItem.C_N_Ratio;
        pInfo.Coppice_Potential = pItem.Coppice_Potential;
        pInfo.Fall_Conspicuous = pItem.Fall_Conspicuous;
        pInfo.Fire_Resistance = pItem.Fire_Resistance;
        pInfo.Flower_Color = pItem.Flower_Color;
        pInfo.Flower_Conspicuous = pItem.Flower_Conspicuous;
        pInfo.Foliage_Color = pItem.Foliage_Color;
        pInfo.Foliage_Porosity_Summer = pItem.Foliage_Porosity_Summer;
        pInfo.Foliage_Porosity_Winter = pItem.Foliage_Porosity_Winter;
        pInfo.Foliage_Texture = pItem.Foliage_Texture;
        pInfo.Fruit_Color = pItem.Fruit_Color;
        pInfo.Fruit_Conspicuous = pItem.Fruit_Conspicuous;
        pInfo.Growth_Form = pItem.Growth_Form;
        pInfo.Growth_Rate = pItem.Growth_Rate;
        pInfo.Height_at_Base_Age_Maximum_feet = pItem.Height_at_Base_Age_Maximum_feet;
        pInfo.Height_Mature_feet = pItem.Height_Mature_feet;
        pInfo.Known_Allelopath = pItem.Known_Allelopath;
        pInfo.Leaf_Retention = pItem.Leaf_Retention;
        pInfo.Lifespan = pItem.Lifespan;
        pInfo.Low_Growing_Grass = pItem.Low_Growing_Grass;
        pInfo.Nitrogen_Fixation = pItem.Nitrogen_Fixation;
        pInfo.Resprout_Ability = pItem.Resprout_Ability;
        pInfo.Shape_and_Orientation = pItem.Shape_and_Orientation;
        pInfo.Toxicity = pItem.Toxicity;
        pInfo.Adapted_to_Coarse_Textured_Soils = pItem.Adapted_to_Coarse_Textured_Soils;
        pInfo.Adapted_to_Medium_Textured_Soils = pItem.Adapted_to_Medium_Textured_Soils;
        pInfo.Adapted_to_Fine_Textured_Soils = pItem.Adapted_to_Fine_Textured_Soils;
        pInfo.Anaerobic_Tolerance = pItem.Anaerobic_Tolerance;
        pInfo.CaCO_3_Tolerance = pItem.CaCO_3_Tolerance;
        pInfo.Cold_Stratification_Required = pItem.Cold_Stratification_Required;
        pInfo.Drought_Tolerance = pItem.Drought_Tolerance;
        pInfo.Fertility_Requirement = pItem.Fertility_Requirement;
        pInfo.Fire_Tolerance = pItem.Fire_Tolerance;
        pInfo.Frost_Free_Days_Minimum = pItem.Frost_Free_Days_Minimum;
        pInfo.Hedge_Tolerance = pItem.Hedge_Tolerance;
        pInfo.Moisture_Use = pItem.Moisture_Use;
        pInfo.pH_Minimum = pItem.pH_Minimum;
        pInfo.pH_Maximum = pItem.pH_Maximum;
        pInfo.Planting_Density_per_Acre_Minimum = pItem.Planting_Density_per_Acre_Minimum;
        pInfo.Planting_Density_per_Acre_Maximum = pItem.Planting_Density_per_Acre_Maximum;
        pInfo.Precipitation_Minimum = pItem.Precipitation_Minimum;
        pInfo.Precipitation_Maximum = pItem.Precipitation_Maximum;
        pInfo.Root_Depth_Minimum_inches = pItem.Root_Depth_Minimum_inches;
        pInfo.Salinity_Tolerance = pItem.Salinity_Tolerance;
        pInfo.Shade_Tolerance = pItem.Shade_Tolerance;
        pInfo.Temperature_Minimum_F = pItem.Temperature_Minimum_F;
        pInfo.Bloom_Period = pItem.Bloom_Period;
        pInfo.Commercial_Availability = pItem.Commercial_Availability;
        pInfo.Fruit_Seed_Abundance = pItem.Fruit_Seed_Abundance;
        pInfo.Fruit_Seed_Period_Begin = pItem.Fruit_Seed_Period_Begin;
        pInfo.Fruit_Seed_Period_End = pItem.Fruit_Seed_Period_End;
        pInfo.Fruit_Seed_Persistence = pItem.Fruit_Seed_Persistence;
        pInfo.Propogated_by_Bare_Root = pItem.Propogated_by_Bare_Root;
        pInfo.Propogated_by_Bulbs = pItem.Propogated_by_Bulbs;
        pInfo.Propogated_by_Container = pItem.Propogated_by_Container;
        pInfo.Propogated_by_Corms = pItem.Propogated_by_Corms;
        pInfo.Propogated_by_Cuttings = pItem.Propogated_by_Cuttings;
        pInfo.Propogated_by_Seed = pItem.Propogated_by_Seed;
        pInfo.Propogated_by_Sod = pItem.Propogated_by_Sod;
        pInfo.Propogated_by_Sprigs = pItem.Propogated_by_Sprigs;
        pInfo.Propogated_by_Tubers = pItem.Propogated_by_Tubers;
        pInfo.Seeds_per_Pound = pItem.Seeds_per_Pound;
        pInfo.Seed_Spread_Rate = pItem.Seed_Spread_Rate;
        pInfo.Seedling_Vigor = pItem.Seedling_Vigor;
        pInfo.Small_Grain = pItem.Small_Grain;
        pInfo.Vegetative_Spread_Rate = pItem.Vegetative_Spread_Rate;
        pInfo.Berry_Nut_Seed_Product = pItem.Berry_Nut_Seed_Product;
        pInfo.Christmas_Tree_Product = pItem.Christmas_Tree_Product;
        pInfo.Fodder_Product = pItem.Fodder_Product;
        pInfo.Fuelwood_Product = pItem.Fuelwood_Product;
        pInfo.Lumber_Product = pItem.Lumber_Product;
        pInfo.Naval_Store_Product = pItem.Naval_Store_Product;
        pInfo.Nursery_Stock_Product = pItem.Nursery_Stock_Product;
        pInfo.Palatable_Browse_Animal = pItem.Palatable_Browse_Animal;
        pInfo.Palatable_Graze_Animal = pItem.Palatable_Graze_Animal;
        pInfo.Palatable_Human = pItem.Palatable_Human;
        pInfo.Post_Product = pItem.Post_Product;
        pInfo.Protein_Potential = pItem.Protein_Potential;
        pInfo.Pulpwood_Product = pItem.Pulpwood_Product;
        pInfo.Veneer_Product = pItem.Veneer_Product;

        return pInfo;
    }

    // Sets the plants image.
    private void setImage(String plantImage){
        Glide.with(mPlantPicIV).load(plantImage).into(mPlantPicIV);
    }

    // Class that handles getting image urls.
    class findImageURL extends AsyncTask<Void, Void, Void> {

        private String mImageURL = "";

        // Gets the first image from an image search.
        protected Void FindImage(String plantName) {
            String ua = System.getProperty("http.agent");
            String finRes = "";
            try {
                String googleUrl = "https://www.google.com/search?tbm=isch&q=" + plantName.replace(",", "").replace(" ","%20").replace("'","%27");
                Document doc1 = Jsoup.connect(googleUrl).userAgent(ua).timeout(10 * 1000).get();
                Element media = doc1.select("[data-src]").first();
                String finUrl = media.attr("abs:data-src");

                finRes= finUrl.replace("&quot", "");
            } catch (Exception e) {
                System.out.println(e);
            }
            mImageURL = finRes;

            return null;
        }

        @Override
        protected Void doInBackground(Void ... records) {
            FindImage(comString + " " + sciString);
            return null;
        }

        protected void onPostExecute(Void result) {
            parentDetail.setImage(mImageURL);
        }
    }
}
