package com.leonardofox346.io;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_MAX_ACTIVE_TABS = "max_active_tabs";
    private static final int DEFAULT_MAX_ACTIVE_TABS = 3;
    private static final String ABOUT_SITE_URL = "https://leonardofox346.github.io/aboutsite/";
    private static final String ABOUT_APP_URL = "https://leonardofox346.github.io/about/";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("设置");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Button btnClearCache = findViewById(R.id.btnClearCache);
        Button btnActiveTabs = findViewById(R.id.btnActiveTabs);
        Button btnPermissions = findViewById(R.id.btnPermissions);
        Button btnPrivacy = findViewById(R.id.btnPrivacy);
        Button btnAboutSite = findViewById(R.id.btnAboutSite);
        Button btnAboutApp = findViewById(R.id.btnAboutApp);
        Button btnAboutKimi = findViewById(R.id.btnAboutKimi);

        btnClearCache.setOnClickListener(v -> clearCache());
        btnActiveTabs.setOnClickListener(v -> showActiveTabsDialog());
        btnPermissions.setOnClickListener(v -> showPermissions());
        btnPrivacy.setOnClickListener(v -> startActivity(new Intent(this, PrivacyPolicyActivity.class)));
        btnAboutSite.setOnClickListener(v -> openUrl(ABOUT_SITE_URL));
        btnAboutApp.setOnClickListener(v -> openUrl(ABOUT_APP_URL));
        btnAboutKimi.setOnClickListener(v -> startActivity(new Intent(this, AboutKimiActivity.class)));
    }

    private void clearCache() {
        WebView webView = new WebView(this);
        webView.clearCache(true);
        webView.destroy();

        deleteDatabase("webview.db");
        deleteDatabase("webviewCache.db");

        Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
    }

    private void showPermissions() {
        new AlertDialog.Builder(this)
            .setTitle("权限声明")
            .setMessage("本应用为纳多狐狐实验室（leonardofox346.github.io）官方浏览器客户端，申请以下系统权限：\n\n" +
                "• INTERNET（必需）\n   用于访问纳多狐狐实验室网站及其他网页内容，是浏览功能的基础权限。\n\n" +
                "• ACCESS_NETWORK_STATE（必需）\n   用于检测当前网络连接状态，判断网页能否正常加载。\n\n" +
                "• WRITE_EXTERNAL_STORAGE（可选）\n   仅在您主动下载文件时用于将文件保存到设备存储中。\n\n" +
                "以上权限均为实现浏览和下载功能所必需，本应用不会利用这些权限收集或上传您的任何个人信息。")
            .setPositiveButton("确定", null)
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("活跃标签页数量");

        TextView tvValue = new TextView(this);
        tvValue.setText(String.valueOf(currentValue));
        tvValue.setTextSize(24);
        tvValue.setPadding(0, 16, 0, 16);
        tvValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(9);
        seekBar.setProgress(currentValue - 1);
        seekBar.setPadding(32, 16, 32, 16);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvValue.setText(String.valueOf(progress + 1));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(tvValue);
        layout.addView(seekBar);
        builder.setView(layout);

        builder.setPositiveButton("确定", (dialog, which) -> {
            int newValue = seekBar.getProgress() + 1;
            prefs.edit().putInt(KEY_MAX_ACTIVE_TABS, newValue).apply();
            Toast.makeText(this, "活跃标签页数量已设置为 " + newValue, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
