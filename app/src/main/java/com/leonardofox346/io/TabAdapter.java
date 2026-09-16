package com.leonardofox346.io;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TabAdapter extends RecyclerView.Adapter<TabAdapter.ViewHolder> {

    private List<Tab> tabs;
    private int currentTabIndex;
    private OnTabClickListener listener;

    public interface OnTabClickListener {
        void onTabClick(int position);
    }

    public TabAdapter(List<Tab> tabs, int currentTabIndex, OnTabClickListener listener) {
        this.tabs = tabs;
        this.currentTabIndex = currentTabIndex;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tab tab = tabs.get(position);
        holder.textView.setText(tab.getTitle() != null ? tab.getTitle() : tab.getUrl());
        holder.textView.setBackgroundColor(position == currentTabIndex ? 0xFFE0E0E0 : 0xFFFFFFFF);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
