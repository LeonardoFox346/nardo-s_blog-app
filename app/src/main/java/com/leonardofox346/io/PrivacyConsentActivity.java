package com.leonardofox346.io;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;

/**
 * 首次启动的「隐私政策与使用条款」确认弹窗。
 *
 * <p>写法对齐博客站的前置弹窗（遮罩 + 居中卡片、三条摘要、两个平级的真选项），
 * 见 {@code D:\WEB\blog\themes\anzhiyu\layout\includes\agreement-modal.pug}。
 * 与旧版整页式同意页的三点差别：</p>
 * <ul>
 *   <li>⛔ 去掉「我已阅读并同意」勾选框 —— 默认勾选/只留一个按钮属变相强制，
 *       博客那边的弹窗也是两个平级按钮；</li>
 *   <li>「不同意」是**真选项**：留在本页无路可走，直接退出应用；</li>
 *   <li>返回键等同「不同意」（不能绕过选择直接进主界面）。</li>
 * </ul>
 */
public class PrivacyConsentActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LabNardoPrefs";
    private static final String KEY_PRIVACY_ACCEPTED = "privacy_accepted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_privacy_consent);
        UiInsets.applyTo(findViewById(R.id.consentRoot));

        MaterialButton btnAgree = findViewById(R.id.btnAgree);
        MaterialButton btnDecline = findViewById(R.id.btnDecline);
        TextView tvReadFull = findViewById(R.id.tvReadFull);

        tvReadFull.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));

        btnAgree.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, true).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        btnDecline.setOnClickListener(v -> declineAndExit());

        // 返回键 = 不同意：本页必须作出选择才能离开，不能靠返回键溜进应用。
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                declineAndExit();
            }
        });
    }

    private void declineAndExit() {
        Toast.makeText(this, R.string.toast_declined_exit, Toast.LENGTH_SHORT).show();
        // 启动页已 finish，本页就是任务栈底 —— finishAffinity 即退出应用。
        finishAffinity();
    }
}
