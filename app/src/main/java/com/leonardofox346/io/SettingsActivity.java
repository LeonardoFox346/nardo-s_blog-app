package com.leonardofox346.io;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 设置页（MD3）。
 *
 * <p>入口按钮由普通 {@code Button} 换成 {@code MaterialButton}（{@code Widget.LabNardo.SettingsAction}），
 * 对话框由 {@code android.app.AlertDialog} 换成 {@link MaterialAlertDialogBuilder}，
 * 标题栏由 ActionBar 换成布局里的 {@link MaterialToolbar}，全部文案外置到 strings.xml。</p>
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_MAX_ACTIVE_TABS = "max_active_tabs";
    private static final int DEFAULT_MAX_ACTIVE_TABS = 3;
    private static final int MAX_ACTIVE_TABS = 9;
    private static final String ABOUT_SITE_URL = "https://nardofox.cn/aboutsite/";
    private static final String ABOUT_APP_URL = "https://nardofox.cn/about/";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);
        UiInsets.applyTo(findViewById(R.id.settingsRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        TextView tvBrandInfo = findViewById(R.id.tvBrandInfo);
        tvBrandInfo.setText(getString(R.string.settings_brand_info, BuildConfig.VERSION_NAME));

        MaterialButton btnClearCache = findViewById(R.id.btnClearCache);
        MaterialButton btnActiveTabs = findViewById(R.id.btnActiveTabs);
        MaterialButton btnPermissions = findViewById(R.id.btnPermissions);
        MaterialButton btnPrivacy = findViewById(R.id.btnPrivacy);
        MaterialButton btnAboutSite = findViewById(R.id.btnAboutSite);
        MaterialButton btnAboutApp = findViewById(R.id.btnAboutApp);
        MaterialButton btnAboutAi = findViewById(R.id.btnAboutAi);

        btnClearCache.setOnClickListener(v -> clearCache());
        btnActiveTabs.setOnClickListener(v -> showActiveTabsDialog());
        btnPermissions.setOnClickListener(v -> showPermissions());
        btnPrivacy.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));
        btnAboutSite.setOnClickListener(v -> openUrl(ABOUT_SITE_URL));
        btnAboutApp.setOnClickListener(v -> openUrl(ABOUT_APP_URL));
        btnAboutAi.setOnClickListener(v ->
                startActivity(new Intent(this, AboutAiActivity.class)));
    }

    private void clearCache() {
        WebView webView = new WebView(this);
        webView.clearCache(true);
        webView.destroy();

        deleteDatabase("webview.db");
        deleteDatabase("webviewCache.db");

        Toast.makeText(this, R.string.toast_cache_cleared, Toast.LENGTH_SHORT).show();
    }

    private void showPermissions() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.permissions_title)
                .setMessage(R.string.permissions_message)
                .setPositiveButton(R.string.action_confirm, null)
                .show();
    }

    private void openUrl(String url) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("open_url", url);
        startActivity(intent);
    }

    private void showActiveTabsDialog() {
        int currentValue = prefs.getInt(KEY_MAX_ACTIVE_TABS, DEFAULT_MAX_ACTIVE_TABS);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_active_tabs, null);
        TextView tvValue = dialogView.findViewById(R.id.tvTabCount);
        TextView tvHint = dialogView.findViewById(R.id.tvTabCountHint);
        SeekBar seekBar = dialogView.findViewById(R.id.seekTabCount);

        tvValue.setText(String.valueOf(currentValue));
        tvHint.setText(R.string.dialog_tabs_hint);
        seekBar.setMax(MAX_ACTIVE_TABS - 1);
        seekBar.setProgress(currentValue - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                tvValue.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_tabs_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                    int newValue = seekBar.getProgress() + 1;
                    prefs.edit().putInt(KEY_MAX_ACTIVE_TABS, newValue).apply();
                    Toast.makeText(this, getString(R.string.toast_tabs_updated, newValue),
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
