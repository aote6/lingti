package com.unbounded.input;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private SharedPreferences prefs;
    private TextView layoutText, behaviorText, heightText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);

        // 标题
        TextView title = new TextView(this);
        title.setText("灵体 设置 V3");
        title.setTextSize(22);
        title.setTextColor(0xFF22CC88);
        title.setPadding(0, 0, 0, 30);
        root.addView(title);

        // ===== 布局选择 =====
        TextView layoutLabel = new TextView(this);
        layoutLabel.setText("键盘布局");
        layoutLabel.setTextSize(16);
        layoutLabel.setTextColor(0xFF55EEAA);
        layoutLabel.setPadding(0, 10, 0, 10);
        root.addView(layoutLabel);

        layoutText = new TextView(this);
        String currentLayout = prefs.getString("default_layout", "ninekey");
        layoutText.setText("当前: " + layoutName(currentLayout));
        layoutText.setTextSize(14);
        layoutText.setTextColor(0xFF1C9E6A);
        layoutText.setPadding(0, 0, 0, 15);
        root.addView(layoutText);

        LinearLayout layoutRow = new LinearLayout(this);
        layoutRow.setOrientation(LinearLayout.HORIZONTAL);
        layoutRow.addView(makeBtn("九宫格", "ninekey", layoutText, "default_layout"));
        layoutRow.addView(makeBtn("26键", "qwerty26", layoutText, "default_layout"));
        layoutRow.addView(makeBtn("终端", "unexpected_terminal", layoutText, "default_layout"));
        root.addView(layoutRow);

        // ===== 行为选择 =====
        TextView behaviorLabel = new TextView(this);
        behaviorLabel.setText("输入行为");
        behaviorLabel.setTextSize(16);
        behaviorLabel.setTextColor(0xFF55EEAA);
        behaviorLabel.setPadding(0, 25, 0, 10);
        root.addView(behaviorLabel);

        behaviorText = new TextView(this);
        String currentBehavior = prefs.getString("default_behavior", "t9");
        behaviorText.setText("当前: " + behaviorName(currentBehavior));
        behaviorText.setTextSize(14);
        behaviorText.setTextColor(0xFF1C9E6A);
        behaviorText.setPadding(0, 0, 0, 15);
        root.addView(behaviorText);

        LinearLayout behaviorRow = new LinearLayout(this);
        behaviorRow.setOrientation(LinearLayout.HORIZONTAL);
        behaviorRow.addView(makeBtn("T9中文", "t9", behaviorText, "default_behavior"));
        behaviorRow.addView(makeBtn("直接输入", "direct", behaviorText, "default_behavior"));
        behaviorRow.addView(makeBtn("终端键码", "keyevent", behaviorText, "default_behavior"));
        root.addView(behaviorRow);

        // ===== 键盘高度 =====
        TextView heightLabel = new TextView(this);
        heightLabel.setText("键盘高度 (dp)");
        heightLabel.setTextSize(16);
        heightLabel.setTextColor(0xFF55EEAA);
        heightLabel.setPadding(0, 25, 0, 10);
        root.addView(heightLabel);

        heightText = new TextView(this);
        int currentHeight = prefs.getInt("keyboard_height", 280);
        heightText.setText(currentHeight + " dp");
        heightText.setTextSize(14);
        heightText.setTextColor(0xFF1C9E6A);
        heightText.setPadding(0, 0, 0, 10);
        root.addView(heightText);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(200);
        seekBar.setProgress(currentHeight - 200);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                int val = p + 200;
                heightText.setText(val + " dp");
                prefs.edit().putInt("keyboard_height", val).apply();
            }
            public void onStartTrackingTouch(SeekBar sb) {}
            public void onStopTrackingTouch(SeekBar sb) {}
        });
        root.addView(seekBar);

        // 关闭按钮
        Button closeBtn = new Button(this);
        closeBtn.setText("关闭");
        closeBtn.setTextColor(0xFF000000);
        closeBtn.setBackgroundColor(0xFF22CC88);
        closeBtn.setPadding(30, 15, 30, 15);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 30, 0, 0);
        btnParams.gravity = Gravity.CENTER_HORIZONTAL;
        closeBtn.setLayoutParams(btnParams);
        root.addView(closeBtn);

        scroll.addView(root);
        setContentView(scroll);
    }

    private Button makeBtn(String label, final String value, final TextView display, final String key) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextColor(0xFF000000);
        btn.setBackgroundColor(0xFF1C9E6A);
        btn.setPadding(20, 10, 20, 10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 10, 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prefs.edit().putString(key, value).apply();
                if (key.equals("default_layout")) {
                    display.setText("当前: " + layoutName(value));
                } else {
                    display.setText("当前: " + behaviorName(value));
                }
            }
        });
        return btn;
    }

    private String layoutName(String val) {
        switch (val) {
            case "qwerty26": return "26键";
            case "unexpected_terminal": return "终端";
            default: return "九宫格";
        }
    }

    private String behaviorName(String val) {
        switch (val) {
            case "direct": return "直接输入";
            case "keyevent": return "终端键码";
            default: return "T9中文";
        }
    }
}
