package com.leonardofox346.io;

import android.webkit.WebView;

public class Tab {
    private int id;
    private String url;
    private String title;
    private WebView webView;
    private boolean hibernated;
    private long lastAccessTime;

    public Tab(int id, String url) {
        this.id = id;
        this.url = url;
        this.title = url;
        this.hibernated = false;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public boolean isHibernated() {
        return hibernated;
    }

    public void setHibernated(boolean hibernated) {
        this.hibernated = hibernated;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
