package com.example.android.usdaplantindex;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.data.Status;
import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.List;

public class IdentifyListActivity extends AppCompatActivity implements PlantSearchAdapter.OnPlantItemClickListener {

    private static final String TAG = IdentifyListActivity.class.getSimpleName();

    private RecyclerView mPlantItemsRV;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mLoadingErrorMessageTV;
    private PlantSearchAdapter mPlantAdapter;
    private USDAPlantUtils.PlantIdentify mPlantIdentify;
    private IdentifyPlantViewModel mIdentifyPlantViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_identify_list);

        // Remove shadow under action bar.
        getSupportActionBar().setElevation(0);

        mLoadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        mLoadingErrorMessageTV = findViewById(R.id.tv_loading_error_message);
        mPlantItemsRV = findViewById(R.id.rv_plant_items);

        mPlantAdapter = new PlantSearchAdapter(this);
        mPlantItemsRV.setAdapter(mPlantAdapter);
        mPlantItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mPlantItemsRV.setHasFixedSize(true);

        /*
         * This version of the app code uses the new ViewModel architecture to manage data for
         * the activity.  See the classes in the data package for more about how the ViewModel
         * is set up.  Here, we simply grab the plant data ViewModel.
         */
        mIdentifyPlantViewModel = ViewModelProviders.of(this).get(IdentifyPlantViewModel.class);

        /*
         * Attach an Observer to the plant data.  Whenever the plant data changes, this
         * Observer will send the new data into our RecyclerView's adapter.
         */
        mIdentifyPlantViewModel.getPlants().observe(this, new Observer<List<PlantItem>>() {
            @Override
            public void onChanged(@Nullable List<PlantItem> plantItems) {
                mPlantAdapter.updatePlantItems(plantItems);
            }
        });

        /*
         * Attach an Observer to the network loading status.  Whenever the loading status changes,
         * this Observer will ensure that the correct layout components are visible.  Specifically,
         * it will make the loading indicator visible only when the plant is being loaded.
         * Otherwise, it will display the RecyclerView if plant data was successfully fetched,
         * or it will display the error message if there was an error fetching data.
         */
        mIdentifyPlantViewModel.getLoadingStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                if (status == Status.LOADING) {
                    mLoadingIndicatorPB.setVisibility(View.VISIBLE);
                } else if (status == Status.SUCCESS) {
                    mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
                    mLoadingErrorMessageTV.setVisibility(View.INVISIBLE);
                    mPlantItemsRV.setVisibility(View.VISIBLE);
                } else {
                    mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
                    mPlantItemsRV.setVisibility(View.INVISIBLE);
                    mLoadingErrorMessageTV.setVisibility(View.VISIBLE);
                }
            }
        });

        loadPlants();
    }

    @Override
    public void onPlantItemClick(PlantItem plantItem) {
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USDAPlantUtils.EXTRA_PLANT_ITEM, plantItem);
        startActivity(intent);
    }

    public void loadPlants() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(USDAPlantUtils.EXTRA_PLANT_IDENTIFY)) {
            mPlantIdentify = (USDAPlantUtils.PlantIdentify)intent.getSerializableExtra(
                    USDAPlantUtils.EXTRA_PLANT_IDENTIFY
            );
        }

        String plantState = mPlantIdentify.plantState;
        String plantGrowthHabit = mPlantIdentify.plantGrowthHabit;
        String plantCategory = mPlantIdentify.plantCategory;
        String plantDuration = mPlantIdentify.plantDuration;

        /*
         * We must ensure that our PlantIdentify values aren't null
         * to avoid null pointer exceptions in the URL builder
         */
        if (plantState == null) {
            plantState = "";
        }
        if (plantGrowthHabit == null) {
            plantGrowthHabit = "";
        }
        if (plantCategory == null) {
            plantCategory = "";
        }
        if (plantDuration == null) {
            plantDuration = "";
        }

        mIdentifyPlantViewModel.loadPlants(plantState, plantGrowthHabit, plantCategory, plantDuration);
    }
}