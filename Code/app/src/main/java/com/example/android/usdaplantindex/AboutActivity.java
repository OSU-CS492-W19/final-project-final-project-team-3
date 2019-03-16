package com.example.android.usdaplantindex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    private TextView mHeadCitationTV;
    private TextView mCitationTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mHeadCitationTV = findViewById(R.id.tv_citation_header);
        mCitationTV = findViewById(R.id.tv_citation);
    }
}
