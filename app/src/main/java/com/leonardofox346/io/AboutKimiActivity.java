package com.leonardofox346.io;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AboutKimiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_kimi);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("关于 KIMI Code");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
