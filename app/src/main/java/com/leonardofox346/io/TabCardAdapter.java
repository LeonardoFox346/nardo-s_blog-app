package com.leonardofox346.io;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TabCardAdapter extends RecyclerView.Adapter<TabCardAdapter.ViewHolder> {

    private List<Tab> tabs;
    private int currentTabIndex;
    private OnTabActionListener listener;

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
        holder.tvTitle.setText(tab.getTitle() != null ? tab.getTitle() : tab.getUrl());
        holder.tvUrl.setText(tab.getUrl());

        boolean isActive = position == currentTabIndex;
        holder.itemView.setAlpha(isActive ? 1.0f : 0.7f);

        if (tab.isHibernated()) {
            holder.tvStatus.setText("休眠中");
            holder.tvStatus.setTextColor(0xFF9E9E9E);
            holder.ivPreview.setImageResource(R.drawable.ic_sleep);
            holder.btnSleep.setImageResource(R.drawable.ic_nav_refresh);
        } else {
            holder.tvStatus.setText(isActive ? "当前活跃" : "活跃");
            holder.tvStatus.setTextColor(0xFF4CAF50);
            if (tab.getWebView() != null) {
                tab.getWebView().setDrawingCacheEnabled(true);
                tab.getWebView().buildDrawingCache();
                holder.ivPreview.setImageBitmap(tab.getWebView().getDrawingCache());
            }
            holder.btnSleep.setImageResource(R.drawable.ic_sleep);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTabClick(position);
        });

        holder.btnClose.setOnClickListener(v -> {
            if (listener != null) listener.onTabClose(position);
        });

        holder.btnSleep.setOnClickListener(v -> {
            if (listener != null) listener.onTabSleep(position);
        });
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPreview;
        TextView tvTitle;
        TextView tvUrl;
        TextView tvStatus;
        ImageButton btnClose;
        ImageButton btnSleep;

        ViewHolder(View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivPreview);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvUrl = itemView.findViewById(R.id.tvUrl);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnClose = itemView.findViewById(R.id.btnClose);
            btnSleep = itemView.findViewById(R.id.btnSleep);
        }
    }
}
