package com.example.android.usdaplantindex.utils;

import android.net.Uri;

import com.example.android.usdaplantindex.data.PlantItem;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

public class USDAPlantUtils {

    public static final String EXTRA_PLANT_ITEM = "com.example.android.usdaplantindex.utils.PlantItem";
    public static final String EXTRA_PLANT_IDENTIFY = "com.example.android.usdaplantindex.utils.PlantIdentify";

    private final static String PLANT_SEARCH_BASE_URL = "https://plantsdb.xyz/search";
    private final static String PLANT_SEARCH_LIMIT_PARAM = "limit";
    private final static Integer PLANT_SEARCH_LIMIT = 25;
    private final static String PLANT_SEARCH_OFFSET_PARAM = "offset";
    private final static Integer PLANT_SEARCH_OFFSET = 0;
    private final static String PLANT_SEARCH_URL_AMPERSAND = "&";
    private final static String PLANT_SEARCH_URL_EQUAL = "=";
    private final static String PLANT_SEARCH_FIELD_STATE_AND_PROVINCE = "State_and_Province";
    private final static String PLANT_SEARCH_FIELD_GROWTH_HABIT = "Growth_Habit";
    private final static String PLANT_SEARCH_FIELD_CATEGORY = "Category";
    private final static String PLANT_SEARCH_FIELD_DURATION = "Duration";

    /*
     * This class is used in passing values via Intent from the PlantIdentificationActivity
     * to the PlantIdentificationListActivity for URL building.
     */
    public static class PlantIdentify implements Serializable {
        public String plantState;
        public String plantGrowthHabit;
        public String plantCategory;
        public String plantDuration;
    }

    /*
     * The below several classes are used only for JSON parsing with Gson.
     */
    static class PlantResults implements Serializable {
        public ArrayList<PlantItem> data;
    }

    public static String buildPlantSearchURL() {
        return Uri.parse(PLANT_SEARCH_BASE_URL)
                .buildUpon()
                .appendQueryParameter(PLANT_SEARCH_LIMIT_PARAM, String.valueOf(PLANT_SEARCH_LIMIT))
                .appendQueryParameter(PLANT_SEARCH_OFFSET_PARAM, String.valueOf(PLANT_SEARCH_OFFSET))
                .build()
                .toString();
    }

    public static String buildPlantSearchURL(Integer limit, Integer offset) {
        return Uri.parse(PLANT_SEARCH_BASE_URL)
                .buildUpon()
                .appendQueryParameter(PLANT_SEARCH_LIMIT_PARAM, String.valueOf(limit))
                .appendQueryParameter(PLANT_SEARCH_OFFSET_PARAM, String.valueOf(offset))
                .build()
                .toString();
    }

    public static String buildPlantSearchURL(String query_param, String query_value) {
        return Uri.parse(PLANT_SEARCH_BASE_URL)
                .buildUpon()
                .appendQueryParameter(PLANT_SEARCH_LIMIT_PARAM, String.valueOf(PLANT_SEARCH_LIMIT))
                .appendQueryParameter(PLANT_SEARCH_OFFSET_PARAM, String.valueOf(PLANT_SEARCH_OFFSET))
                .appendQueryParameter(query_param, query_value)
                .build()
                .toString();
    }

    public static String buildPlantSearchURL(Integer limit, Integer offset, String query_param, String query_value) {
        return Uri.parse(PLANT_SEARCH_BASE_URL)
                .buildUpon()
                .appendQueryParameter(PLANT_SEARCH_LIMIT_PARAM, String.valueOf(limit))
                .appendQueryParameter(PLANT_SEARCH_OFFSET_PARAM, String.valueOf(offset))
                .appendQueryParameter(query_param, query_value)
                .build()
                .toString();
    }

    /*
     * URL builder for PlantIdentificationActivity -> PlantIdentificationListActivity
     */
    public static String buildPlantSearchURL(Integer limit, Integer offset, String state, String growthHabit, String category, String duration) {
        String url = Uri.parse(PLANT_SEARCH_BASE_URL)
                .buildUpon()
                .appendQueryParameter(PLANT_SEARCH_LIMIT_PARAM, String.valueOf(limit))
                .appendQueryParameter(PLANT_SEARCH_OFFSET_PARAM, String.valueOf(offset))
                .build()
                .toString();

        /*
         * Add parameters on to the URL dynamically depending upon what is available
         */
        if (!state.equals("")) {
            url += PLANT_SEARCH_URL_AMPERSAND.concat(PLANT_SEARCH_FIELD_STATE_AND_PROVINCE).concat(PLANT_SEARCH_URL_EQUAL).concat(state);
        }

        if (!growthHabit.equals("")) {
            url += PLANT_SEARCH_URL_AMPERSAND.concat(PLANT_SEARCH_FIELD_GROWTH_HABIT).concat(PLANT_SEARCH_URL_EQUAL).concat(growthHabit);
        }

        if (!category.equals("")) {
            url += PLANT_SEARCH_URL_AMPERSAND.concat(PLANT_SEARCH_FIELD_CATEGORY).concat(PLANT_SEARCH_URL_EQUAL).concat(category);
        }

        if (!duration.equals("")) {
            url += PLANT_SEARCH_URL_AMPERSAND.concat(PLANT_SEARCH_FIELD_DURATION).concat(PLANT_SEARCH_URL_EQUAL).concat(duration);
        }

        return url;
    }

    public static ArrayList<PlantItem> parsePlantJSON(String plantJSON) {
        Gson gson = new Gson();
        PlantResults results = gson.fromJson(plantJSON, PlantResults.class);
        if (results != null && results.data != null) {
            return results.data;
        } else {
            return null;
        }
    }
}
