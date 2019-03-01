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
import com.example.android.usdaplantindex.utils.USDAUtils;

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

    private USDAUtils.PlantItem mPlantItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_item_detail);

        mPlantSciTV = findViewById(R.id.tv_plant_scientific);
        mPlantComTV = findViewById(R.id.tv_plant_common);
        mPlantDetails = findViewById(R.id.tv_plant_details);
        mPlantPicIV = findViewById(R.id.iv_plant_pic_det);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(USDAUtils.EXTRA_PLANT_ITEM)) {
            mPlantItem = (USDAUtils.PlantItem)intent.getSerializableExtra(
                    USDAUtils.EXTRA_PLANT_ITEM
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

    public String getDetailedString() {
        String detailString = "";

        if(mPlantItem.Symbol != "" && mPlantItem.Symbol != null) {
            detailString += "Symbol: " + mPlantItem.Symbol + "\n";
        }
        if(mPlantItem.Group != "" && mPlantItem.Group != null) {
            detailString += "Group: " + mPlantItem.Group + "\n";
        }
        if(mPlantItem.Family != "" && mPlantItem.Family != null) {
            detailString += "Family: " + mPlantItem.Family + "\n";
        }
        if(mPlantItem.Duration != "" && mPlantItem.Duration != null) {
            detailString += "Duration: " + mPlantItem.Duration + "\n";
        }
        if(mPlantItem.Growth_Habit != "" && mPlantItem.Growth_Habit != null) {
            detailString += "Growth Habit: " + mPlantItem.Growth_Habit + "\n";
        }
        if(mPlantItem.Native_Status != "" && mPlantItem.Native_Status != null) {
            detailString += "Native status: " + mPlantItem.Native_Status + "\n";
        }
        if(mPlantItem.Category != "" && mPlantItem.Category != null) {
            detailString += "Category: " + mPlantItem.Category + "\n";
        }
        if(mPlantItem.xOrder != "" && mPlantItem.xOrder != null) {
            detailString += "Order: " + mPlantItem.xOrder + "\n";
        }
        if(mPlantItem.Class != "" && mPlantItem.Class != null) {
            detailString += "Class: " + mPlantItem.Class + "\n";
        }
        if(mPlantItem.SubClass != "" && mPlantItem.SubClass != null) {
            detailString += "Subclass: " + mPlantItem.SubClass + "\n";
        }
        if(mPlantItem.Kingdom != "" && mPlantItem.Kingdom != null) {
            detailString += "Kingdom: " + mPlantItem.Kingdom + "\n";
        }
        if(mPlantItem.Species != "" && mPlantItem.Species != null) {
            detailString += "Species: " + mPlantItem.Species + "\n";
        }
        if(mPlantItem.Subspecies != "" && mPlantItem.Subspecies != null) {
            detailString += "Subspecies: " + mPlantItem.Subspecies + "\n";
        }
        if(mPlantItem.State_and_Province != "" && mPlantItem.State_and_Province != null) {
            detailString += "State and Province: " + mPlantItem.State_and_Province + "\n";
        }

        return detailString;
    }

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

    private void fillInLayout(USDAUtils.PlantItem plantItem) {
        String sciString = plantItem.Scientific_Name_x;
        String comString = plantItem.Common_Name;
        String detailString = getDetailedString();

        mPlantSciTV.setText(sciString);
        mPlantComTV.setText(comString);
        mPlantDetails.setText(detailString);
    }

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
