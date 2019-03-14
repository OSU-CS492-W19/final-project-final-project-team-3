package com.example.android.usdaplantindex;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.usdaplantindex.data.PlantInfo;
import com.example.android.usdaplantindex.utils.USDAUtils;

import java.util.List;

public class PlantInfoAdapter extends RecyclerView.Adapter<PlantInfoAdapter.PlantInfoViewHolder> {

    private List<PlantInfo> mPlantInfos;
    private OnPlantInfoClickListener mPlantInfoClickListener;

    public interface OnPlantInfoClickListener {
        void onPlantInfoClick(PlantInfo plantInfo);
    }

    public PlantInfoAdapter(OnPlantInfoClickListener clickListener) {
        mPlantInfoClickListener = clickListener;
    }

    public void updatePlantItems(List<PlantInfo> plantInfos) {
        mPlantInfos = plantInfos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mPlantInfos != null) {
            return mPlantInfos.size();
        } else {
            return 0;
        }
    }

    @Override
    public PlantInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.plant_list_item, parent, false);
        return new PlantInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlantInfoViewHolder holder, int position) {
        holder.bind(mPlantInfos.get(position));
    }

    class PlantInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mPlantSciTV;
        private TextView mPlantComTV;
        private ImageView mPlantPicIV;

        public PlantInfoViewHolder(View view) {
            super(view);
            mPlantSciTV = view.findViewById(R.id.tv_plant_scientific);
            mPlantComTV = view.findViewById(R.id.tv_plant_common);
            mPlantPicIV = view.findViewById(R.id.iv_plant_pic);
            view.setOnClickListener(this);
        }

        // Binds plant item to a view holder.
        public void bind(PlantInfo plantInfo) {
            String sciString = plantInfo.Scientific_Name_x;
            String comString = plantInfo.Common_Name;
            //String iconURL = USDAUtils.buildIconURL(plantItem.icon);
            mPlantSciTV.setText(sciString);
            mPlantComTV.setText(comString);
            //Glide.with(mPlantPicIV.getContext()).load(iconURL).into(mPlantPicIV);
        }

        @Override
        public void onClick(View v) {
            PlantInfo plantInfo = mPlantInfos.get(getAdapterPosition());
            mPlantInfoClickListener.onPlantInfoClick(plantInfo);
        }
    }
}
