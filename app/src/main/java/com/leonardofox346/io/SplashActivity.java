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
import androidx.core.view.WindowCompat;

/**
 * 启动页（MD3）：主色底 + Logo/标题/副标题依次入场，2.5s 后按隐私协议是否已同意分流。
 *
 * <p>改动用的是 {@code Theme.LabNardo.Splash}（主色窗口背景 + 深色状态栏图标），
 * 不再依赖已被 MD3 主题移除的 ActionBar；底部版本号改为运行期读取
 * {@link BuildConfig#VERSION_NAME}，不会再随发版漂移。</p>
 */
public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_PRIVACY_ACCEPTED = "privacy_accepted";
    private static final long SPLASH_DELAY_MS = 2500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_splash);
        UiInsets.applyTo(findViewById(R.id.splashRoot));

        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvFooter = findViewById(R.id.tvFooter);

        tvFooter.setText(getString(R.string.splash_footer, BuildConfig.VERSION_NAME));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.3f, 1.0f);
        ObjectAnimator fadeLogo = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f);

        ObjectAnimator transY = ObjectAnimator.ofFloat(tvTitle, "translationY", 60f, 0f);
        ObjectAnimator fadeTitle = ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f);

        ObjectAnimator transYSub = ObjectAnimator.ofFloat(tvSubtitle, "translationY", 30f, 0f);
        ObjectAnimator fadeSub = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 1f);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(scaleX, scaleY, fadeLogo);
        logoSet.setDuration(800);

        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(transY, fadeTitle);
        titleSet.setDuration(600);

        AnimatorSet subSet = new AnimatorSet();
        subSet.playTogether(transYSub, fadeSub);
        subSet.setDuration(500);

        AnimatorSet fullSet = new AnimatorSet();
        fullSet.playSequentially(logoSet, titleSet, subSet);
        fullSet.start();

        new Handler(Looper.getMainLooper()).postDelayed(this::goNext, SPLASH_DELAY_MS);
    }

    private void goNext() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean accepted = prefs.getBoolean(KEY_PRIVACY_ACCEPTED, false);
        Class<?> next = accepted ? MainActivity.class : PrivacyConsentActivity.class;
        startActivity(new Intent(this, next));
        finish();
    }
}
