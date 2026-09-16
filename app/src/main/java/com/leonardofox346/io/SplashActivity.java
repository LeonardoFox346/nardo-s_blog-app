package com.leonardofox346.io;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_PRIVACY_ACCEPTED = "privacy_accepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);

        // Logo scale up + fade in
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.3f, 1.0f);
        ObjectAnimator fadeLogo = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f);

        // Title slide up + fade in
        ObjectAnimator transY = ObjectAnimator.ofFloat(tvTitle, "translationY", 60f, 0f);
        ObjectAnimator fadeTitle = ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f);

        // Subtitle fade in
        ObjectAnimator fadeSub = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 1f);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(scaleX, scaleY, fadeLogo);
        logoSet.setDuration(800);

        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(transY, fadeTitle);
        titleSet.setDuration(600);

        AnimatorSet subSet = new AnimatorSet();
        subSet.play(fadeSub);
        subSet.setDuration(500);

        AnimatorSet fullSet = new AnimatorSet();
        fullSet.playSequentially(logoSet, titleSet, subSet);
        fullSet.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean accepted = prefs.getBoolean(KEY_PRIVACY_ACCEPTED, false);
            if (accepted) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, PrivacyConsentActivity.class));
            }
            finish();
        }, 2500);
    }
}
