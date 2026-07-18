package com.unbounded.input;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(40, 60, 40, 60);

        TextView title = new TextView(this);
        title.setText("灵体 输入法");
        title.setTextSize(24);
        title.setTextColor(0xFF22CC88);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("复古终端风中文输入平台");
        desc.setTextSize(14);
        desc.setTextColor(0xFF1C9E6A);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 0, 0, 40);
        root.addView(desc);

        Button enableBtn = new Button(this);
        enableBtn.setText("启用输入法");
        enableBtn.setTextColor(0xFF000000);
        enableBtn.setBackgroundColor(0xFF22CC88);
        enableBtn.setPadding(30, 15, 30, 15);
        enableBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 0, 20);
        enableBtn.setLayoutParams(btnParams);
        root.addView(enableBtn);

        Button settingsBtn = new Button(this);
        settingsBtn.setText("键盘设置");
        settingsBtn.setTextColor(0xFF000000);
        settingsBtn.setBackgroundColor(0xFF1C9E6A);
        settingsBtn.setPadding(30, 15, 30, 15);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SimpleImeService.openSettings(v.getContext());
            }
        });
        settingsBtn.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(settingsBtn);

        setContentView(root);
    }
}
