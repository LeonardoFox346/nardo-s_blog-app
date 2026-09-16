package com.leonardofox346.io;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyConsentActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_PRIVACY_ACCEPTED = "privacy_accepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_consent);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        CheckBox cbAgree = findViewById(R.id.cbAgree);
        Button btnReadPrivacy = findViewById(R.id.btnReadPrivacy);
        Button btnEnter = findViewById(R.id.btnEnter);
        TextView tvPrivacyHint = findViewById(R.id.tvPrivacyHint);

        btnReadPrivacy.setOnClickListener(v -> {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
        });

        btnEnter.setOnClickListener(v -> {
            if (!cbAgree.isChecked()) {
                Toast.makeText(this, "请先勾选同意隐私协议", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, true).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
