package com.leonardofox346.io;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面：单 Activity + 多标签 WebView 宿主。
 *
 * <p>MD3 改造要点：
 * <ul>
 *   <li>标题栏由隐藏的 ActionBar 换成 {@link MaterialToolbar}（MD3 top app bar），
 *       并顺带显示当前网页标题；「更多」菜单同时挂在工具栏溢出菜单与底部栏按钮上。</li>
 *   <li>边到边：{@code WindowCompat.setDecorFitsSystemWindows(false)} + {@link UiInsets}。</li>
 *   <li>WebView 创建/休眠/唤醒统一走 {@link TabUtils}（原先两份重复代码）。</li>
 *   <li>{@code onBackPressed()} 覆写改为 {@link OnBackPressedCallback}（前者已废弃）。</li>
 *   <li>{@code startActivityForResult} 改为 {@link ActivityResultLauncher}（前者已废弃）。</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL = "https://nardofox.cn";
    private static final String NEW_TAB_URL = "https://www.bing.com";
    private static final String MUSIC_URL = "https://nardofox.cn/music/";
    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_MAX_ACTIVE_TABS = "max_active_tabs";
    private static final int DEFAULT_MAX_ACTIVE_TABS = 3;

    private static List<Tab> sharedTabs;
    private static int sharedCurrentTabIndex = -1;

    private MaterialToolbar toolbar;
    private FrameLayout webViewContainer;
    private final List<Tab> tabs = new ArrayList<>();
    private int currentTabIndex = -1;
    private int nextTabId = 1;

    private ImageButton btnBack;
    private ImageButton btnForward;
    private ImageButton btnNewTab;
    private ImageButton btnRefresh;
    private ImageButton btnMore;

    private com.google.android.material.progressindicator.LinearProgressIndicator progressBar;

    private final TabUtils.OnPageLoadedListener pageLoadedListener = tab -> {
        if (tab == getCurrentTab()) {
            updateNavigationButtons();
            updateToolbarTitle();
        }
    };

    /**
     * 加载进度回调。站点首页子资源多，冷启动会有数秒纯白；进度条让用户知道
     * 「在加载」而不是「挂了」（站长 2026-10-01 反馈的正是这个观感问题）。
     * 只有当前标签页的加载才动进度条，后台标签页不干扰。
     */
    private final TabUtils.OnLoadingListener loadingListener = new TabUtils.OnLoadingListener() {
        @Override
        public void onLoadingStarted(Tab tab) {
            if (tab == getCurrentTab()) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingFinished(Tab tab) {
            if (tab == getCurrentTab()) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    };

    private final ActivityResultLauncher<Intent> tabManagerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK) {
                    return;
                }
                Intent data = result.getData();
                if (data == null) {
                    return;
                }
                if (data.hasExtra("selected_tab")) {
                    switchToTab(data.getIntExtra("selected_tab", currentTabIndex));
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
                    if (!tabs.isEmpty()) {
                        switchToTab(currentTabIndex);
                    }
                }
                updateNavigationButtons();
                updateToolbarTitle();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            handleMenuAction(item.getItemId());
            return true;
        });

        webViewContainer = findViewById(R.id.webViewContainer);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnForward = findViewById(R.id.btnForward);
        btnNewTab = findViewById(R.id.btnNewTab);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnMore = findViewById(R.id.btnMore);

        UiInsets.applyTopAndSidesTo(findViewById(R.id.mainRoot));
        UiInsets.applyBottomTo(findViewById(R.id.bottomBar));

        setupButtons();
        registerBackCallback();
        addNewTab(HOME_URL);

        String openUrl = getIntent().getStringExtra("open_url");
        if (openUrl != null) {
            openUrlInCurrentTab(openUrl);
        }
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> goBack());
        btnForward.setOnClickListener(v -> goForward());
        btnNewTab.setOnClickListener(v -> addNewTab(NEW_TAB_URL));
        btnRefresh.setOnClickListener(v -> refreshCurrentTab());
        btnMore.setOnClickListener(v -> showMoreMenu());
    }

    private void registerBackCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Tab tab = getCurrentTab();
                if (tab != null && tab.getWebView() != null && tab.getWebView().canGoBack()) {
                    tab.getWebView().goBack();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                // 交还控制权后必须重新启用，否则本回调一旦关掉就再也不拦返回键，
                // 之后在 WebView 里按返回会直接退出应用（而不是先回上一页）。
                setEnabled(true);
            }
        });
    }

    private int getMaxActiveTabs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_MAX_ACTIVE_TABS, DEFAULT_MAX_ACTIVE_TABS);
    }

    private Tab getCurrentTab() {
        if (currentTabIndex >= 0 && currentTabIndex < tabs.size()) {
            return tabs.get(currentTabIndex);
        }
        return null;
    }

    private void addNewTab(String url) {
        Tab tab = new Tab(nextTabId++, url);
        tab.setWebView(TabUtils.createWebView(this, tab, pageLoadedListener, loadingListener));
        tabs.add(tab);

        switchToTab(tabs.size() - 1);
        // ⛔ TabUtils.createWebView 刻意「不」loadUrl（地址由调用方决定），
        // 这里必须显式加载 —— 漏掉这一行的话，正常冷启动（走 SplashActivity、
        // 不带 open_url extra）会永远停在空白页，只有带 extra 的路径才碰巧正常。
        tab.getWebView().loadUrl(url);
        enforceActiveTabLimit();
    }

    private void switchToTab(int index) {
        Tab previous = getCurrentTab();
        if (previous != null && previous.getWebView() != null
                && previous.getWebView().getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) previous.getWebView().getParent())
                    .removeView(previous.getWebView());
        }

        currentTabIndex = index;
        Tab tab = getCurrentTab();
        if (tab != null) {
            tab.setLastAccessTime(System.currentTimeMillis());
            if (tab.isHibernated()) {
                TabUtils.wakeUp(this, tab, pageLoadedListener, loadingListener);
            }
            if (tab.getWebView() != null && tab.getWebView().getParent() == null) {
                webViewContainer.addView(tab.getWebView());
            }
        }

        // 切标签时进度条归零：另一个标签页可能正在后台加载，进度条状态不该被带过来
        progressBar.setVisibility(View.INVISIBLE);

        updateNavigationButtons();
        updateToolbarTitle();
    }

    private void enforceActiveTabLimit() {
        int maxActive = getMaxActiveTabs();
        int activeCount = 0;
        for (Tab t : tabs) {
            if (!t.isHibernated()) {
                activeCount++;
            }
        }
        while (activeCount > maxActive) {
            Tab oldest = findOldestActiveTabExcludingCurrent();
            if (oldest == null) {
                break;
            }
            TabUtils.hibernate(oldest);
            activeCount--;
        }
    }

    private Tab findOldestActiveTabExcludingCurrent() {
        Tab oldest = null;
        long oldestTime = Long.MAX_VALUE;
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            if (t.isHibernated() || i == currentTabIndex) {
                continue;
            }
            if (t.getLastAccessTime() < oldestTime) {
                oldestTime = t.getLastAccessTime();
                oldest = t;
            }
        }
        return oldest;
    }

    private void goBack() {
        Tab tab = getCurrentTab();
        if (tab == null || tab.getWebView() == null) {
            return;
        }
        if (tab.getWebView().canGoBack()) {
            tab.getWebView().goBack();
        } else {
            Toast.makeText(this, R.string.toast_first_page, Toast.LENGTH_SHORT).show();
        }
    }

    private void goForward() {
        Tab tab = getCurrentTab();
        if (tab == null || tab.getWebView() == null) {
            return;
        }
        if (tab.getWebView().canGoForward()) {
            tab.getWebView().goForward();
        } else {
            Toast.makeText(this, R.string.toast_last_page, Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshCurrentTab() {
        Tab tab = getCurrentTab();
        if (tab == null || tab.getWebView() == null) {
            return;
        }
        tab.getWebView().reload();
        Toast.makeText(this, R.string.toast_refreshed, Toast.LENGTH_SHORT).show();
    }

    private void showMoreMenu() {
        PopupMenu popup = new PopupMenu(this, btnMore);
        popup.getMenuInflater().inflate(R.menu.menu_more, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            handleMenuAction(item.getItemId());
            return true;
        });
        popup.show();
    }

    private void handleMenuAction(int itemId) {
        if (itemId == R.id.menu_home) {
            goHome();
        } else if (itemId == R.id.menu_music) {
            openUrlInCurrentTab(MUSIC_URL);
        } else if (itemId == R.id.menu_tabs) {
            openTabManager();
        } else if (itemId == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
    }

    private void goHome() {
        openUrlInCurrentTab(HOME_URL);
    }

    private void openUrlInCurrentTab(String url) {
        Tab tab = getCurrentTab();
        if (tab != null && tab.getWebView() != null) {
            tab.getWebView().loadUrl(url);
        }
    }

    private void openTabManager() {
        sharedTabs = tabs;
        sharedCurrentTabIndex = currentTabIndex;
        tabManagerLauncher.launch(new Intent(this, TabManagerActivity.class));
    }

    public static List<Tab> getSharedTabs() {
        return sharedTabs;
    }

    public static int getSharedCurrentTabIndex() {
        return sharedCurrentTabIndex;
    }

    private void updateNavigationButtons() {
        Tab tab = getCurrentTab();
        boolean canGoBack = tab != null && tab.getWebView() != null && tab.getWebView().canGoBack();
        boolean canGoForward = tab != null && tab.getWebView() != null
                && tab.getWebView().canGoForward();
        btnBack.setEnabled(canGoBack);
        btnForward.setEnabled(canGoForward);
        btnBack.setAlpha(canGoBack ? 1.0f : 0.38f);
        btnForward.setAlpha(canGoForward ? 1.0f : 0.38f);
    }

    private void updateToolbarTitle() {
        Tab tab = getCurrentTab();
        String title = tab == null ? null : tab.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = getString(R.string.app_name);
        }
        toolbar.setTitle(title);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Tab tab : tabs) {
            TabUtils.destroyWebView(tab);
        }
        tabs.clear();
        sharedTabs = null;
        sharedCurrentTabIndex = -1;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = intent.getStringExtra("open_url");
        if (url != null) {
            openUrlInCurrentTab(url);
        }
    }
}
