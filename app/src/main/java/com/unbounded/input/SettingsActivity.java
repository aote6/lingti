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
    private TextView heightText, modeText;

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
        title.setText("灵体 设置");
        title.setTextSize(22);
        title.setTextColor(0xFF22CC88);
        title.setPadding(0, 0, 0, 30);
        root.addView(title);

        // 默认输入模式
        TextView modeLabel = new TextView(this);
        modeLabel.setText("默认输入模式");
        modeLabel.setTextSize(16);
        modeLabel.setTextColor(0xFF55EEAA);
        modeLabel.setPadding(0, 10, 0, 10);
        root.addView(modeLabel);

        modeText = new TextView(this);
        String currentMode = prefs.getString("default_mode", "chinese");
        modeText.setText("当前: " + modeName(currentMode));
        modeText.setTextSize(14);
        modeText.setTextColor(0xFF1C9E6A);
        modeText.setPadding(0, 0, 0, 15);
        root.addView(modeText);

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.addView(makeModeButton("中", "chinese"));
        modeRow.addView(makeModeButton("EN", "english"));
        root.addView(modeRow);

        // 键盘高度
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

    private Button makeModeButton(String label, final String mode) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextColor(0xFF000000);
        btn.setBackgroundColor(0xFF1C9E6A);
        btn.setPadding(25, 12, 25, 12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 15, 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                prefs.edit().putString("default_mode", mode).apply();
                modeText.setText("当前: " + modeName(mode));
            }
        });
        return btn;
    }

    private String modeName(String mode) {
        switch (mode) {
            case "english": return "英文";
            case "terminal": return "终端";
            default: return "中文";
        }
    }
}
