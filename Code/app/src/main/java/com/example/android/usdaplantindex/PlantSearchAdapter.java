package com.example.android.usdaplantindex;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.usdaplantindex.data.PlantItem;
import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.util.ArrayList;
import java.util.List;

public class PlantSearchAdapter extends RecyclerView.Adapter<PlantSearchAdapter.PlantItemViewHolder> {
    private List<PlantItem> mPlantItems;
    private OnPlantItemClickListener mPlantItemClickListener;

    public interface OnPlantItemClickListener {
        void onPlantItemClick(PlantItem plantItem);
    }

    public PlantSearchAdapter(OnPlantItemClickListener clickListener) {
        mPlantItemClickListener = clickListener;
    }

    public void updatePlantItems(List<PlantItem> plantItems) {
        mPlantItems = plantItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mPlantItems != null) {
            return mPlantItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public PlantItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.plant_list_item, parent, false);
        return new PlantItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlantItemViewHolder holder, int position) {
        holder.bind(mPlantItems.get(position));
    }

    class PlantItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mPlantSciTV;
        private TextView mPlantComTV;

        public PlantItemViewHolder(View itemView) {
            super(itemView);
            mPlantSciTV = itemView.findViewById(R.id.tv_plant_scientific);
            mPlantComTV = itemView.findViewById(R.id.tv_plant_common);
            itemView.setOnClickListener(this);
        }

        // Binds plant item to a view holder.
        public void bind(PlantItem plantItem) {
            String sciString = plantItem.Scientific_Name_x;
            String comString = plantItem.Common_Name;
            mPlantSciTV.setText(sciString);
            mPlantComTV.setText(comString);
        }

        @Override
        public void onClick(View v) {
            PlantItem plantItem = mPlantItems.get(getAdapterPosition());
            mPlantItemClickListener.onPlantItemClick(plantItem);
        }
    }
}
