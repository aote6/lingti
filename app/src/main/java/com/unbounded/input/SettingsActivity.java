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
    private TextView layoutText, behaviorText, themeText, heightText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("灵体 设置 V3");
        title.setTextSize(22);
        title.setTextColor(0xFF22CC88);
        title.setPadding(0, 0, 0, 30);
        root.addView(title);

        // 布局
        addSection(root, "键盘布局");
        layoutText = addCurrent(root, layoutName(prefs.getString("default_layout", "ninekey")));
        root.addView(makeRow(
            makeBtn("九宫格", "ninekey", layoutText, "default_layout", true),
            makeBtn("26键", "qwerty26", layoutText, "default_layout", true),
            makeBtn("终端", "unexpected_terminal", layoutText, "default_layout", true)
        ));

        // 行为
        addSection(root, "输入行为");
        behaviorText = addCurrent(root, behaviorName(prefs.getString("default_behavior", "t9")));
        root.addView(makeRow(
            makeBtn("T9中文", "t9", behaviorText, "default_behavior", false),
            makeBtn("直接输入", "direct", behaviorText, "default_behavior", false),
            makeBtn("终端键码", "keyevent", behaviorText, "default_behavior", false)
        ));

        // 主题
        addSection(root, "主题");
        themeText = addCurrent(root, themeName(prefs.getString("theme", "Default")));
        root.addView(makeRow(
            makeThemeBtn("默认", "Default", themeText),
            makeThemeBtn("琥珀", "Amber", themeText),
            makeThemeBtn("IBM", "IBM", themeText)
        ));

        // 高度
        addSection(root, "键盘高度 (dp)");
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

    private void addSection(LinearLayout root, String label) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(16);
        tv.setTextColor(0xFF55EEAA);
        tv.setPadding(0, 25, 0, 10);
        root.addView(tv);
    }

    private TextView addCurrent(LinearLayout root, String text) {
        TextView tv = new TextView(this);
        tv.setText("当前: " + text);
        tv.setTextSize(14);
        tv.setTextColor(0xFF1C9E6A);
        tv.setPadding(0, 0, 0, 15);
        root.addView(tv);
        return tv;
    }

    private LinearLayout makeRow(Button... btns) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (Button b : btns) row.addView(b);
        return row;
    }

    private Button makeBtn(String label, final String value, final TextView display, final String key, final boolean isLayout) {
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
                display.setText("当前: " + (isLayout ? layoutName(value) : behaviorName(value)));
            }
        });
        return btn;
    }

    private Button makeThemeBtn(String label, final String value, final TextView display) {
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
                prefs.edit().putString("theme", value).apply();
                display.setText("当前: " + themeName(value));
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
    private String themeName(String val) {
        switch (val) {
            case "Amber": return "琥珀";
            case "IBM": return "IBM";
            default: return "默认";
        }
    }
}
