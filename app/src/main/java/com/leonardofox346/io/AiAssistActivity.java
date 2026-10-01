package com.leonardofox346.io;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * AI 辅助构建（MD3，三级页）。
 *
 * <p>路径：设置 → 关于本应用 → 开发历程 → AI 辅助构建。
 * 介绍参与本工程开发的那个 AI 助手能做什么 —— 这是「功能说明」，
 * 只描述协作分工，不代表应用本身由 AI 独立完成（APP 由站长
 * LeonardoFox 从 2025 年起搭建并持续维护）。</p>
 *
 * <p>MD3 主题是 NoActionBar，{@code getSupportActionBar()} 恒为 null，
 * 标题与返回箭头改由布局里的 {@link MaterialToolbar} 承担。</p>
 */
public class AiAssistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_ai_assist);
        UiInsets.applyTo(findViewById(R.id.aiAssistRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
