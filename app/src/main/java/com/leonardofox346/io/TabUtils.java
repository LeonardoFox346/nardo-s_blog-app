package com.leonardofox346.io;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 标签页 / WebView 的公共逻辑。
 *
 * <p>原实现把「创建 WebView、休眠、唤醒」在 MainActivity 与 TabManagerActivity 里各抄了一份，
 * 而且两处 UA 后缀不一致（{@code Lab.Nardo/2.5} 与 {@code Lab.Nardo/2.2}）；
 * 顺带一提，TabManagerActivity 那份还漏了下载拦截对话框。这里统一成一份。</p>
 */
final class TabUtils {

    /** 页面加载完成回调（MainActivity 借此刷新前进/后退按钮可用态与标题）。 */
    interface OnPageLoadedListener {
        void onPageLoaded(Tab tab);
    }

    /**
     * 加载状态回调 —— 只给真正要显示进度的地方（主界面的进度条）。
     *
     * <p>站点首页子资源多，冷启动时 WebView 会有数秒纯白；没有这个信号，用户分不清
     * 「正在加载」和「挂了」。</p>
     */
    interface OnLoadingListener {
        void onLoadingStarted(Tab tab);

        void onLoadingFinished(Tab tab);
    }

    private TabUtils() {
    }

    /**
     * 新建一个已配置好的 WebView，并把加载完成事件回写到 {@code tab} 上。
     * 注意：这里<b>不</b>调用 loadUrl，由调用方决定加载哪个地址。
     */
    static WebView createWebView(Context context, Tab tab, OnPageLoadedListener listener) {
        return createWebView(context, tab, listener, null);
    }

    /** 带加载进度回调的重载版本（主界面用）。 */
    static WebView createWebView(Context context, Tab tab, OnPageLoadedListener listener,
                                 OnLoadingListener loadingListener) {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString()
                + " Lab.Nardo/" + BuildConfig.VERSION_NAME);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (loadingListener != null) {
                    loadingListener.onLoadingStarted(tab);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (loadingListener != null) {
                    loadingListener.onLoadingFinished(tab);
                }
                tab.setUrl(url);
                // ⛔ 别用 view.getTitle() 直接覆盖：它可能返回 null，会把
                // onReceivedTitle 已经拿到的好标题抹掉，标题栏退回裸 URL。
                String title = view.getTitle();
                if (title != null && !title.trim().isEmpty()) {
                    tab.setTitle(title);
                }
                if (listener != null) {
                    listener.onPageLoaded(tab);
                }
            }

            /** 每次历史变化都刷新前进/后退可用态，不等 onPageFinished。 */
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                tab.setUrl(url);
                if (listener != null) {
                    listener.onPageLoaded(tab);
                }
            }

            /**
             * 主框架加载失败（DNS 失败、连不上、超时）也要把进度条收掉 ——
             * 否则它永远转下去，反而更像卡死。
             */
            @Override
            public void onReceivedError(WebView view, android.webkit.WebResourceRequest request,
                                        android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request != null && request.isForMainFrame() && loadingListener != null) {
                    loadingListener.onLoadingFinished(tab);
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            /**
             * <p>站点的 {@code <title>} 通常远早于 onPageFinished 就到位；而像 nardofox.cn 这种
             * 子资源多、外部 CDN 可能卡住的页面，onPageFinished 会迟迟不来甚至不来 ——
             * 只靠 onPageFinished 会让标题栏与标签卡片一直显示裸 URL。</p>
             */
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (title != null && !title.trim().isEmpty()) {
                    tab.setTitle(title);
                    if (listener != null) {
                        listener.onPageLoaded(tab);
                    }
                }
            }
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) ->
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.download_blocked_title)
                        .setIcon(R.drawable.app_icon)
                        .setMessage(R.string.download_blocked_message)
                        .setPositiveButton(R.string.action_confirm, null)
                        .show());
        return webView;
    }

    /** 休眠：记下当前地址与标题，卸载 WebView，释放内存。 */
    static void hibernate(Tab tab) {
        WebView webView = tab.getWebView();
        if (webView != null) {
            tab.setUrl(webView.getUrl() != null ? webView.getUrl() : tab.getUrl());
            if (webView.getTitle() != null) {
                tab.setTitle(webView.getTitle());
            }
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.clearHistory();
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }
            webView.destroy();
            tab.setWebView(null);
        }
        tab.setHibernated(true);
    }

    /** 唤醒：重建 WebView 并回到休眠前的地址。 */
    static void wakeUp(Context context, Tab tab, OnPageLoadedListener listener) {
        wakeUp(context, tab, listener, null);
    }

    /** 带加载进度回调的唤醒版本。 */
    static void wakeUp(Context context, Tab tab, OnPageLoadedListener listener,
                       OnLoadingListener loadingListener) {
        if (tab.isHibernated()) {
            WebView webView = createWebView(context, tab, listener, loadingListener);
            tab.setWebView(webView);
            tab.setHibernated(false);
            webView.loadUrl(tab.getUrl());
        }
        tab.setLastAccessTime(System.currentTimeMillis());
    }

    /** 彻底销毁一个标签页的 WebView（关闭标签、Activity 退出时用）。 */
    static void destroyWebView(Tab tab) {
        WebView webView = tab.getWebView();
        if (webView != null) {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.clearHistory();
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }
            webView.destroy();
            tab.setWebView(null);
        }
    }
}
