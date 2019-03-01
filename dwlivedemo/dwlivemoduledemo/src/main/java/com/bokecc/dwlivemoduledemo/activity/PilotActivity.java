package com.bokecc.dwlivemoduledemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;

/**
 * 观看直播 & 观看回放 入口选择页
 */
public class PilotActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        requestFullScreenFeature();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pilot);

        findViewById(R.id.btn_start_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PilotActivity.this, LoginActivity.class);
                intent.putExtra("fragmentIndex", 0);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_start_replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PilotActivity.this, LoginActivity.class);
                intent.putExtra("fragmentIndex", 1);
                startActivity(intent);
            }
        });
    }
}
