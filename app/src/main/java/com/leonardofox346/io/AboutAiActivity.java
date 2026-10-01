package com.leonardofox346.io;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * 关于本应用（MD3）。
 *
 * <p>设置页第 7 项进来。页面名刻意不含任何第三方 AI 产品名，只讲这个 APP 本身：
 * 应用头部 + 简介 + 开发历程时间线，末尾一张入口卡通向 {@link AiAssistActivity}
 * （AI 辅助编程，同页展示 DeepSeek 与 Kimi 两段）。</p>
 *
 * <p>MD3 主题是 NoActionBar，{@code getSupportActionBar()} 恒为 null，
 * 标题与返回箭头改由布局里的 {@link MaterialToolbar} 承担。</p>
 */
public class AboutAiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_about_ai);
        UiInsets.applyTo(findViewById(R.id.aboutRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 版本号取自 BuildConfig，避免像旧启动页那样把版本写死在文案里
        ((TextView) findViewById(R.id.tvAppVersion))
                .setText(getString(R.string.about_app_version, BuildConfig.VERSION_NAME));

        findViewById(R.id.cardAiAssist).setOnClickListener(
                v -> startActivity(new Intent(this, AiAssistActivity.class)));
    }
}
