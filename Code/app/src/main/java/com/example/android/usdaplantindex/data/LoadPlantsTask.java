package com.example.android.usdaplantindex.data;

import android.os.AsyncTask;

import com.example.android.usdaplantindex.utils.NetworkUtils;
import com.example.android.usdaplantindex.utils.USDAPlantUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class LoadPlantsTask extends AsyncTask<Void, Void, String> {

    public interface AsyncCallback {
        void onPlantsLoadFinished(List<PlantItem> plantItems);
    }

    private String mURL;
    private AsyncCallback mCallback;

    LoadPlantsTask(String url, AsyncCallback callback) {
        mURL = url;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String forecastJSON = null;
        try {
            forecastJSON = NetworkUtils.doHTTPGet(mURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return forecastJSON;
    }

    @Override
    protected void onPostExecute(String s) {
        ArrayList<PlantItem> plantItems = null;
        if (s != null) {
            plantItems = USDAPlantUtils.parsePlantJSON(s);
        }
        mCallback.onPlantsLoadFinished(plantItems);
    }
}
