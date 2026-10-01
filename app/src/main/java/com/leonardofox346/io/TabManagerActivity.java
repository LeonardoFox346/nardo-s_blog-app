package com.leonardofox346.io;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * 标签页集合（MD3）：卡片网格，支持切换 / 休眠 / 关闭。
 *
 * <p>休眠与唤醒不再自己抄一份 WebView 代码，统一调 {@link TabUtils}
 * （原实现在这里用的是 {@code Lab.Nardo/2.2} 的 UA，且漏了下载拦截对话框）。</p>
 */
@SuppressLint("NotifyDataSetChanged") // 标签数量少且状态整体变化，全量刷新比 DiffUtil 更直观
public class TabManagerActivity extends AppCompatActivity
        implements TabCardAdapter.OnTabActionListener {

    private TabCardAdapter adapter;
    private List<Tab> tabs;
    private int currentTabIndex;

    private final TabUtils.OnPageLoadedListener pageLoadedListener = tab -> {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_tab_manager);
        UiInsets.applyTo(findViewById(R.id.tabManagerRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerTabs);

        tabs = MainActivity.getSharedTabs();
        currentTabIndex = MainActivity.getSharedCurrentTabIndex();

        // ⛔ 进程被系统回收后重建本页时，MainActivity 的 static 列表已经没了
        // （Java 静态字段不跨进程存活），此时 tabs 为 null —— 若不拦，
        // RecyclerView 会在 getItemCount() 里 NPE 闪退。直接收摊，让栈里
        // 下方的 MainActivity 重新创建即可（它自己会 addNewTab(HOME_URL)）。
        if (tabs == null || tabs.isEmpty()) {
            finish();
            return;
        }

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

    @Override
    public void onTabClose(int position) {
        if (tabs == null || tabs.size() <= 1) {
            Toast.makeText(this, R.string.toast_keep_one_tab, Toast.LENGTH_SHORT).show();
            return;
        }
        if (position >= 0 && position < tabs.size()) {
            TabUtils.destroyWebView(tabs.get(position));
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

    @Override
    public void onTabSleep(int position) {
        if (tabs == null || position < 0 || position >= tabs.size()) {
            return;
        }
        Tab tab = tabs.get(position);
        if (tab.isHibernated()) {
            TabUtils.wakeUp(this, tab, pageLoadedListener);
            Toast.makeText(this, R.string.toast_tab_woken, Toast.LENGTH_SHORT).show();
        } else {
            TabUtils.hibernate(tab);
            Toast.makeText(this, R.string.toast_tab_hibernated, Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }
}
