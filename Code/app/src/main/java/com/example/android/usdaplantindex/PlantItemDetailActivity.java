package com.example.android.usdaplantindex;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.usdaplantindex.utils.USAUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class PlantItemDetailActivity extends AppCompatActivity {

    public PlantItemDetailActivity parentDetail = this;
    public String sciString = "";
    public String comString = "";

    private TextView mPlantSciTV;
    private TextView mPlantComTV;
    private ImageView mPlantPicIV;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_item_detail);

        mPlantSciTV = findViewById(R.id.tv_plant_scientific);
        mPlantComTV = findViewById(R.id.tv_plant_common);
        mPlantPicIV = findViewById(R.id.iv_plant_pic_det);
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
        }
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

        detailString += getFieldString("Symbol", mPlantItem.Symbol);
        detailString += getFieldString("Group", mPlantItem.Category);
        detailString += getFieldString("Family", mPlantItem.Family);
        detailString += getFieldString("Duration", mPlantItem.Duration);
        detailString += getFieldString("Growth Habit", mPlantItem.Growth_Habit);
        detailString += getFieldString("Native status", mPlantItem.Native_Status);
        detailString += getFieldString("State and Province", mPlantItem.State_and_Province);



        detailString += getFieldString("Order", mPlantItem.xOrder);
        detailString += getFieldString("Class", mPlantItem.Class);
        detailString += getFieldString("SubClass", mPlantItem.SubClass);
        detailString += getFieldString("Kingdom", mPlantItem.Kingdom);
        detailString += getFieldString("Species", mPlantItem.Species);
        detailString += getFieldString("Subspecies", mPlantItem.Subspecies);
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

        mPlantSciTV.setText(sciString);
        mPlantComTV.setText(comString);

        mHGeneralTV.setText(generalHString);
        mPlantGeneralTV.setText(generalString);
        //mHMorphTV;
        //mPlantMorphTV;
        //mHGrowthTV;
        //mPlantGrowthTV;
        //mHRepTV;
        //mPlantRepTV;
        //mHSuitTV;
        //mPlantSuitTV;
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
