package com.example.android.usdaplantindex;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.data.Status;
import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.List;

// This activity provides the functionality for searching plants by scientific name.
public class PlantSearchByScientificActivity extends AppCompatActivity
        implements PlantSearchAdapter.OnPlantItemClickListener {

    private static final String TAG = PlantSearchByScientificActivity.class.getSimpleName();

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private TextView mLoadingErrorTV;
    private TextView mLoadingTextTV;
    private ProgressBar mLoadingPB;

    private PlantSearchAdapter mPlantSearchAdapter;

    private PlantSearchByScientificViewModel mPlantSearchByNameViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_search_by_name);

        mSearchBoxET = findViewById(R.id.plant_search_by_name_et);
        mSearchResultsRV = findViewById(R.id.plant_search_by_name_results);
        mLoadingErrorTV = findViewById(R.id.plant_search_by_name_loading_error_tv);
        mLoadingTextTV = findViewById(R.id.plant_search_by_name_loading_tv);
        mLoadingPB = findViewById(R.id.plant_search_by_name_loading_pb);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mPlantSearchAdapter = new PlantSearchAdapter(this);
        mSearchResultsRV.setAdapter(mPlantSearchAdapter);

        mPlantSearchByNameViewModel = ViewModelProviders.of(this)
                .get(PlantSearchByScientificViewModel.class);

        mPlantSearchByNameViewModel.getFilteredPlants().observe(this,
                new Observer<List<PlantItem>>() {
                    @Override
                    public void onChanged(@Nullable List<PlantItem> filteredPlants) {
                        mPlantSearchAdapter.updatePlantItems(filteredPlants);
                    }
                });

        mPlantSearchByNameViewModel.getLiteLoadingStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                updateLoadingText();
                if (status == Status.LOADING) {
                    mLoadingPB.setVisibility(View.VISIBLE);
                } else if (status == Status.SUCCESS) {
                    mLoadingPB.setVisibility(View.INVISIBLE);
                    //mSearchResultsRV.setVisibility(View.VISIBLE);
                    //mLoadingErrorTV.setVisibility(View.INVISIBLE);
                } else {
                    mLoadingPB.setVisibility(View.INVISIBLE);
                    //mSearchResultsRV.setVisibility(View.INVISIBLE);
                    //mLoadingErrorTV.setVisibility(View.VISIBLE);
                }
            }
        });

        mSearchBoxET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mPlantSearchByNameViewModel.searchChanged(s.toString());
            }
        });

        mPlantSearchByNameViewModel.loadPlantNames();
        updateLoadingText();
    }

    private void updateLoadingText() {
        mLoadingTextTV.setText(String.valueOf(mPlantSearchByNameViewModel.getLitePlantCount().getValue()));
    }

    @Override
    public void onPlantItemClick(PlantItem repo) {
        Intent intent = new Intent(this, PlantItemDetailActivity.class);
        intent.putExtra(USDAPlantUtils.EXTRA_PLANT_ITEM, repo);
        startActivity(intent);
    }
}
