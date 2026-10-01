package com.leonardofox346.io;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * 标签页卡片适配器（MD3）。
 *
 * <p>原实现的两个问题在本次一并修掉：</p>
 * <ul>
 *   <li>状态文字写死 {@code 0xFF9E9E9E} / {@code 0xFF4CAF50}，深色模式下不可读
 *       → 改用 {@code @color/status_hibernated} / {@code @color/status_active}（values 与 values-night 各一份）。</li>
 *   <li>用 {@code WebView.getDrawingCache()} 抓缩略图 —— 该 API 自 API 28 起废弃，
 *       且硬件加速下通常返回 null，卡片预览一直是空白（还可能触发
 *       "View too large to fit into drawing cache"）。
 *       → 改为按状态显示图标：休眠用 {@code ic_sleep}，活跃用 {@code ic_link}，
 *       当前标签页额外用主题主色着色 + 加粗描边表达选中。</li>
 * </ul>
 */
@SuppressLint("NotifyDataSetChanged") // 标签数量少，切换/休眠后整体重绑更直观
public class TabCardAdapter extends RecyclerView.Adapter<TabCardAdapter.ViewHolder> {

    private final List<Tab> tabs;
    private int currentTabIndex;
    private final OnTabActionListener listener;

    public interface OnTabActionListener {
        void onTabClick(int position);

        void onTabClose(int position);

        void onTabSleep(int position);
    }

    public TabCardAdapter(List<Tab> tabs, int currentTabIndex, OnTabActionListener listener) {
        this.tabs = tabs;
        this.currentTabIndex = currentTabIndex;
        this.listener = listener;
    }

    public void updateCurrentTab(int index) {
        this.currentTabIndex = index;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tab_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tab tab = tabs.get(position);
        boolean isActive = position == currentTabIndex;

        holder.tvTitle.setText(tab.getTitle() != null ? tab.getTitle() : tab.getUrl());
        holder.tvUrl.setText(tab.getUrl());

        // 用工程自己的 MD3 色彩角色（values 与 values-night 各一份），深色模式自动跟随
        int colorPrimary = ContextCompat.getColor(holder.itemView.getContext(), R.color.md_primary);
        int colorOutline = ContextCompat.getColor(holder.itemView.getContext(), R.color.md_outline_variant);
        int colorOnSurfaceVariant = ContextCompat.getColor(holder.itemView.getContext(),
                R.color.md_on_surface_variant);

        holder.cardTab.setStrokeColor(isActive ? colorPrimary : colorOutline);
        holder.cardTab.setStrokeWidth(isActive ? dp(holder, 2) : dp(holder, 1));
        holder.itemView.setAlpha(isActive ? 1.0f : 0.72f);

        if (tab.isHibernated()) {
            holder.tvStatus.setText(R.string.tab_status_hibernated);
            holder.tvStatus.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.status_hibernated));
            holder.ivPreview.setImageResource(R.drawable.ic_sleep);
            holder.ivPreview.setColorFilter(colorOnSurfaceVariant);
            holder.btnSleep.setImageResource(R.drawable.ic_nav_refresh);
        } else {
            holder.tvStatus.setText(isActive ? R.string.tab_status_current : R.string.tab_status_active);
            holder.tvStatus.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.status_active));
            holder.ivPreview.setImageResource(R.drawable.ic_link);
            holder.ivPreview.setColorFilter(isActive ? colorPrimary : colorOutline);
            holder.btnSleep.setImageResource(R.drawable.ic_sleep);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabClick(holder.getBindingAdapterPosition());
            }
        });
        holder.btnClose.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabClose(holder.getBindingAdapterPosition());
            }
        });
        holder.btnSleep.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabSleep(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return tabs == null ? 0 : tabs.size();
    }

    private static int dp(ViewHolder holder, int value) {
        return Math.round(value * holder.itemView.getResources().getDisplayMetrics().density);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardTab;
        final ImageView ivPreview;
        final TextView tvTitle;
        final TextView tvUrl;
        final TextView tvStatus;
        final ImageButton btnClose;
        final ImageButton btnSleep;

        ViewHolder(View itemView) {
            super(itemView);
            cardTab = itemView.findViewById(R.id.cardTab);
            ivPreview = itemView.findViewById(R.id.ivPreview);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvUrl = itemView.findViewById(R.id.tvUrl);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnClose = itemView.findViewById(R.id.btnClose);
            btnSleep = itemView.findViewById(R.id.btnSleep);
        }
    }
}
