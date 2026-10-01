package com.leonardofox346.io;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * 「隐私与条款」正文页（MD3）。标题栏由布局里的 {@link MaterialToolbar} 承担。
 *
 * <p>15 条正文共用一份 {@code item_privacy_section.xml} 模板（标题 + 正文两个 TextView），
 * 文案在这里按顺序填。之所以不在布局里写死：15 条会让 XML 膨胀到 400 行以上，
 * 且译文/改文都要动布局。</p>
 *
 * <p>体例对齐博客站 {@code /privacy/}：两部分（隐私政策 / 使用条款）+ 连贯的「第 N 条」编号。</p>
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    /** 正文所包含的小节数量（= 布局里 sec1..secN 的个数）。 */
    private static final int SECTION_COUNT = 15;

    private static final int[] SECTION_TITLES = {
            R.string.privacy_s1_title,
            R.string.privacy_s2_title,
            R.string.privacy_s3_title,
            R.string.privacy_s4_title,
            R.string.privacy_s5_title,
            R.string.privacy_s6_title,
            R.string.privacy_s7_title,
            R.string.privacy_s8_title,
            R.string.privacy_s9_title,
            R.string.privacy_s10_title,
            R.string.privacy_s11_title,
            R.string.privacy_s12_title,
            R.string.privacy_s13_title,
            R.string.privacy_s14_title,
            R.string.privacy_s15_title,
    };

    private static final int[] SECTION_BODIES = {
            R.string.privacy_s1_body,
            R.string.privacy_s2_body,
            R.string.privacy_s3_body,
            R.string.privacy_s4_body,
            R.string.privacy_s5_body,
            R.string.privacy_s6_body,
            R.string.privacy_s7_body,
            R.string.privacy_s8_body,
            R.string.privacy_s9_body,
            R.string.privacy_s10_body,
            R.string.privacy_s11_body,
            R.string.privacy_s12_body,
            R.string.privacy_s13_body,
            R.string.privacy_s14_body,
            R.string.privacy_s15_body,
    };

    private static final int[] SECTION_IDS = {
            R.id.sec1, R.id.sec2, R.id.sec3, R.id.sec4, R.id.sec5, R.id.sec6, R.id.sec7,
            R.id.sec8, R.id.sec9, R.id.sec10, R.id.sec11, R.id.sec12, R.id.sec13, R.id.sec14,
            R.id.sec15,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_privacy_policy);
        UiInsets.applyTo(findViewById(R.id.privacyRoot));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindSections();
    }

    /**
     * 把三条平行数组按序号填进布局里的 15 个 {@code secN}。
     *
     * <p>⛔ 三张表必须等长 —— 长度不一致说明加/删小节时漏改了一张表，
     * 这里直接抛出来，免得静默少显示一条。</p>
     */
    private void bindSections() {
        if (SECTION_TITLES.length != SECTION_COUNT
                || SECTION_BODIES.length != SECTION_COUNT
                || SECTION_IDS.length != SECTION_COUNT) {
            throw new IllegalStateException("隐私条款小节表长度不一致："
                    + SECTION_TITLES.length + "/" + SECTION_BODIES.length + "/"
                    + SECTION_IDS.length + "，应为 " + SECTION_COUNT);
        }
        for (int i = 0; i < SECTION_COUNT; i++) {
            // 每个 <include> 都是独立子树，所以在子树内 findViewById 不会串到别的小节。
            TextView title = findViewById(SECTION_IDS[i]).findViewById(R.id.tvSectionTitle);
            TextView body = findViewById(SECTION_IDS[i]).findViewById(R.id.tvSectionBody);
            title.setText(SECTION_TITLES[i]);
            body.setText(SECTION_BODIES[i]);
        }
    }
}
