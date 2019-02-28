package com.example.android.usdaplantindex.utils;

import android.content.Intent;
import android.net.Uri;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

public class USDAUtils {

    public static final String EXTRA_PLANT_ITEM = "com.example.android.usdaplantindex.utils.PlantItem";


    private final static String PLANT_SEARCH_BASE_URL = "https://plantsdb.xyz/search";
    private final static String PLANT_SEARCH_LIMIT_PARAM = "limit";
    private final static Integer PLANT_SEARCH_LIMIT = 25;
    private final static String PLANT_SEARCH_OFFSET_PARAM = "offset";
    private final static Integer PLANT_SEARCH_OFFSET = 0;

    private final static String OWM_ICON_URL_FORMAT_STR = "https://openweathermap.org/img/w/%s.png";

    /*
     * This class is used as a final representation of a single plant item.
     * It condenses the classes below that are used for parsing the OWN JSON response with Gson.
     */
    public static class PlantItem implements Serializable {
        public Integer id;
        public String Scientific_Name_x;
        public String Common_Name;
        public String Symbol;
        public String Group;
        public String Family;
        public String Duration;
        public String Growth_Habit;
        public String Native_Status;
        public String Category;
        public String xOrder;
        public String SubClass;
        public String Class;
        public String Kingdom;
        public String Species;
        public String Subspecies;
        public String State_and_Province;
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

    public static String buildIconURL(String icon) {
        return String.format(OWM_ICON_URL_FORMAT_STR, icon);
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
