package com.leonardofox346.io;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL = "https://leonardofox346.github.io";
    private static final String NEW_TAB_URL = "https://www.bing.com";
    private static final String MUSIC_URL = "https://leonardofox346.github.io/music/";
    private static final int REQUEST_TAB_MANAGER = 1001;
    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_MAX_ACTIVE_TABS = "max_active_tabs";
    private static final int DEFAULT_MAX_ACTIVE_TABS = 3;

    private static List<Tab> sharedTabs;
    private static int sharedCurrentTabIndex = -1;

    private FrameLayout webViewContainer;
    private List<Tab> tabs = new ArrayList<>();
    private int currentTabIndex = -1;
    private int nextTabId = 1;

    private ImageButton btnBack;
    private ImageButton btnForward;
    private ImageButton btnNewTab;
    private ImageButton btnRefresh;
    private ImageButton btnMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        webViewContainer = findViewById(R.id.webViewContainer);
        btnBack = findViewById(R.id.btnBack);
        btnForward = findViewById(R.id.btnForward);
        btnNewTab = findViewById(R.id.btnNewTab);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnMore = findViewById(R.id.btnMore);

        setupButtons();
        addNewTab(HOME_URL);

        String openUrl = getIntent().getStringExtra("open_url");
        if (openUrl != null) {
            openUrlInCurrentTab(openUrl);
        }
    }

    private int getMaxActiveTabs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_MAX_ACTIVE_TABS, DEFAULT_MAX_ACTIVE_TABS);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> goBack());
        btnForward.setOnClickListener(v -> goForward());
        btnNewTab.setOnClickListener(v -> addNewTab(NEW_TAB_URL));
        btnRefresh.setOnClickListener(v -> refreshCurrentTab());
        btnMore.setOnClickListener(v -> showMoreMenu());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void addNewTab(String url) {
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " Lab.Nardo/2.5");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
                    Tab currentTab = tabs.get(currentTabIndex);
                    currentTab.setUrl(url);
                    currentTab.setTitle(view.getTitle());
                    updateNavigationButtons();
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.setDownloadListener((downloadUrl, userAgent, contentDisposition, mimeType, contentLength) -> {
            new AlertDialog.Builder(this)
                .setTitle("下载已禁止")
                .setIcon(R.drawable.app_icon)
                .setMessage("为了您的设备安全，本应用已禁止下载功能。如需保存网页内容，建议截图或使用系统浏览器。")
                .setPositiveButton("知道了", null)
                .show();
        });
        webView.loadUrl(url);

        Tab tab = new Tab(nextTabId++, url);
        tab.setWebView(webView);
        tabs.add(tab);

        switchToTab(tabs.size() - 1);
        enforceActiveTabLimit();
    }

    private void switchToTab(int index) {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView currentWebView = tabs.get(currentTabIndex).getWebView();
            if (currentWebView != null && currentWebView.getParent() != null) {
                ((ViewGroup) currentWebView.getParent()).removeView(currentWebView);
            }
        }

        currentTabIndex = index;
        if (index >= 0 && index < tabs.size()) {
            Tab tab = tabs.get(index);
            tab.setLastAccessTime(System.currentTimeMillis());
            if (tab.isHibernated()) {
                wakeUpTab(tab);
            }
            WebView webView = tab.getWebView();
            if (webView != null) {
                webViewContainer.addView(webView);
            }
        }

        updateNavigationButtons();
    }

    private void enforceActiveTabLimit() {
        int maxActive = getMaxActiveTabs();
        int activeCount = 0;
        for (Tab t : tabs) {
            if (!t.isHibernated()) activeCount++;
        }
        while (activeCount > maxActive) {
            Tab oldest = findOldestActiveTabExcludingCurrent();
            if (oldest == null) break;
            hibernateTab(oldest);
            activeCount--;
        }
    }

    private Tab findOldestActiveTabExcludingCurrent() {
        Tab oldest = null;
        long oldestTime = Long.MAX_VALUE;
        for (Tab t : tabs) {
            if (t.isHibernated()) continue;
            if (tabs.indexOf(t) == currentTabIndex) continue;
            if (t.getLastAccessTime() < oldestTime) {
                oldestTime = t.getLastAccessTime();
                oldest = t;
            }
        }
        return oldest;
    }

    private void hibernateTab(Tab tab) {
        if (tab.getWebView() != null) {
            tab.setUrl(tab.getWebView().getUrl());
            tab.setTitle(tab.getWebView().getTitle());
            tab.getWebView().stopLoading();
            tab.getWebView().loadUrl("about:blank");
            tab.getWebView().clearHistory();
            if (tab.getWebView().getParent() != null) {
                ((ViewGroup) tab.getWebView().getParent()).removeView(tab.getWebView());
            }
            tab.setWebView(null);
        }
        tab.setHibernated(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void wakeUpTab(Tab tab) {
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " Lab.Nardo/2.5");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                tab.setUrl(url);
                tab.setTitle(view.getTitle());
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.setDownloadListener((downloadUrl, userAgent, contentDisposition, mimeType, contentLength) -> {
            new AlertDialog.Builder(this)
                .setTitle("下载已禁止")
                .setIcon(R.drawable.app_icon)
                .setMessage("为了您的设备安全，本应用已禁止下载功能。如需保存网页内容，建议截图或使用系统浏览器。")
                .setPositiveButton("知道了", null)
                .show();
        });
        webView.loadUrl(tab.getUrl());
        tab.setWebView(webView);
        tab.setHibernated(false);
        tab.setLastAccessTime(System.currentTimeMillis());
    }

    private void goBack() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView webView = tabs.get(currentTabIndex).getWebView();
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                Toast.makeText(this, "已经是第一页了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goForward() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView webView = tabs.get(currentTabIndex).getWebView();
            if (webView != null && webView.canGoForward()) {
                webView.goForward();
            } else {
                Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshCurrentTab() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView webView = tabs.get(currentTabIndex).getWebView();
            if (webView != null) {
                webView.reload();
                Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMoreMenu() {
        PopupMenu popup = new PopupMenu(this, btnMore);
        popup.getMenuInflater().inflate(R.menu.menu_more, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                goHome();
            } else if (id == R.id.menu_music) {
                openUrlInCurrentTab(MUSIC_URL);
            } else if (id == R.id.menu_tabs) {
                openTabManager();
            } else if (id == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            return true;
        });

        popup.show();
    }

    private void goHome() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView webView = tabs.get(currentTabIndex).getWebView();
            if (webView != null) {
                webView.loadUrl(HOME_URL);
            }
        }
    }

    private void openUrlInCurrentTab(String url) {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView webView = tabs.get(currentTabIndex).getWebView();
            if (webView != null) {
                webView.loadUrl(url);
            }
        }
    }

    private void openTabManager() {
        sharedTabs = tabs;
        sharedCurrentTabIndex = currentTabIndex;
        Intent intent = new Intent(this, TabManagerActivity.class);
        startActivityForResult(intent, REQUEST_TAB_MANAGER);
    }

    public static List<Tab> getSharedTabs() {
        return sharedTabs;
    }

    public static int getSharedCurrentTabIndex() {
        return sharedCurrentTabIndex;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAB_MANAGER && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("selected_tab")) {
                int selected = data.getIntExtra("selected_tab", currentTabIndex);
                switchToTab(selected);
            }
            if ("close".equals(data.getStringExtra("action"))) {
                int closed = data.getIntExtra("closed_position", -1);
                if (closed >= 0 && closed < tabs.size()) {
                    if (closed == currentTabIndex) {
                        currentTabIndex = Math.min(currentTabIndex, tabs.size() - 1);
                    } else if (closed < currentTabIndex) {
                        currentTabIndex--;
                    }
                }
                if (tabs.size() > 0) {
                    switchToTab(currentTabIndex);
                }
            }
        }
    }

    private void updateNavigationButtons() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView webView = tabs.get(currentTabIndex).getWebView();
            if (webView != null) {
                btnBack.setEnabled(webView.canGoBack());
                btnForward.setEnabled(webView.canGoForward());
            } else {
                btnBack.setEnabled(false);
                btnForward.setEnabled(false);
            }
        } else {
            btnBack.setEnabled(false);
            btnForward.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            WebView webView = tabs.get(currentTabIndex).getWebView();
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Tab tab : tabs) {
            WebView webView = tab.getWebView();
            if (webView != null) {
                webView.stopLoading();
                webView.loadUrl("about:blank");
                webView.clearHistory();
                if (webView.getParent() != null) {
                    ((ViewGroup) webView.getParent()).removeView(webView);
                }
                webView.destroy();
            }
        }
        tabs.clear();
        sharedTabs = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = intent.getStringExtra("open_url");
        if (url != null) {
            openUrlInCurrentTab(url);
        }
    }
}
