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
    private TextView mPlantDetails;
    private ImageView mPlantPicIV;

    private USAUtils.PlantItem mPlantItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_item_detail);

        mPlantSciTV = findViewById(R.id.tv_plant_scientific);
        mPlantComTV = findViewById(R.id.tv_plant_common);
        mPlantDetails = findViewById(R.id.tv_plant_details);
        mPlantPicIV = findViewById(R.id.iv_plant_pic_det);

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

    // Takes in the name and value of a string and returns a string
    // of that shows the fields name and value, if the value is not empty.
    public String getFieldString(String fieldName, String fieldValue) {
        String returnString = "";
        if(fieldValue != "" && fieldValue != null) {
            returnString = fieldName + ": " + fieldValue + "\n";
        }
        return returnString;
    }

    // Returns a string with all of the detailed information
    // for the given plant.
    public String getDetailedString() {
        String detailString = "";

        getFieldString("Symbol", mPlantItem.Symbol);
        getFieldString("Group", mPlantItem.Group);
        getFieldString("Family", mPlantItem.Family);
        getFieldString("Duration", mPlantItem.Duration);
        getFieldString("Growth Habit", mPlantItem.Growth_Habit);
        getFieldString("Native status", mPlantItem.Native_Status);
        getFieldString("Category", mPlantItem.Category);
        getFieldString("Order", mPlantItem.xOrder);
        getFieldString("Class", mPlantItem.Class);
        getFieldString("SubClass", mPlantItem.SubClass);
        getFieldString("Kingdom", mPlantItem.Kingdom);
        getFieldString("Species", mPlantItem.Species);
        getFieldString("Subspecies", mPlantItem.Subspecies);
        getFieldString("State and Province", mPlantItem.State_and_Province);
        getFieldString("Class", mPlantItem.Class);
        getFieldString("Class", mPlantItem.Class);

        return detailString;
    }

    // Allows the user to share this plant with other users.
    public void sharePlant() {
        if (mPlantItem != null) {

            String detailString = getDetailedString();
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
        String detailString = getDetailedString();

        mPlantSciTV.setText(sciString);
        mPlantComTV.setText(comString);
        mPlantDetails.setText(detailString);
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
