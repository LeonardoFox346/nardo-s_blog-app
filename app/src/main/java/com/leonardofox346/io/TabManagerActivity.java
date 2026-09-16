package com.leonardofox346.io;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TabManagerActivity extends AppCompatActivity implements TabCardAdapter.OnTabActionListener {

    private RecyclerView recyclerView;
    private TabCardAdapter adapter;
    private List<Tab> tabs;
    private int currentTabIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_manager);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("标签页集合");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerTabs);

        tabs = MainActivity.getSharedTabs();
        currentTabIndex = MainActivity.getSharedCurrentTabIndex();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new TabCardAdapter(tabs, currentTabIndex, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onTabClick(int position) {
        Intent result = new Intent();
        result.putExtra("selected_tab", position);
        setResult(RESULT_OK, result);
        finish();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onTabClose(int position) {
        if (tabs.size() <= 1) {
            Toast.makeText(this, "至少保留一个标签页", Toast.LENGTH_SHORT).show();
            return;
        }
        if (position >= 0 && position < tabs.size()) {
            Tab tab = tabs.get(position);
            if (tab.getWebView() != null) {
                tab.getWebView().stopLoading();
                tab.getWebView().loadUrl("about:blank");
                tab.getWebView().clearHistory();
                tab.getWebView().destroy();
            }
            tabs.remove(position);
            if (currentTabIndex >= tabs.size()) {
                currentTabIndex = tabs.size() - 1;
            }
            if (position == currentTabIndex) {
                currentTabIndex = 0;
            }
            adapter.notifyDataSetChanged();

            Intent result = new Intent();
            result.putExtra("action", "close");
            result.putExtra("closed_position", position);
            setResult(RESULT_OK, result);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onTabSleep(int position) {
        if (position >= 0 && position < tabs.size()) {
            Tab tab = tabs.get(position);
            if (tab.isHibernated()) {
                wakeUpTab(tab);
                Toast.makeText(this, "已唤醒标签页", Toast.LENGTH_SHORT).show();
            } else {
                hibernateTab(tab);
                Toast.makeText(this, "标签页已休眠", Toast.LENGTH_SHORT).show();
            }
            adapter.notifyDataSetChanged();
        }
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
        android.webkit.WebView webView = new android.webkit.WebView(this);
        android.webkit.WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString(settings.getUserAgentString() + " Lab.Nardo/2.2");

        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                tab.setUrl(url);
                tab.setTitle(view.getTitle());
            }
        });
        webView.setWebChromeClient(new android.webkit.WebChromeClient());
        webView.loadUrl(tab.getUrl());
        tab.setWebView(webView);
        tab.setHibernated(false);
        tab.setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
