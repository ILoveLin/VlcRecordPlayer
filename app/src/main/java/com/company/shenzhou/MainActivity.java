package com.company.shenzhou;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.player.ui.PlayerSettingActivity;


/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 主界面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private View m2UniversalPlayer;
    private Intent intent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mContext = MainActivity.this;
        initView();
        responseListener();
    }

    private void initView() {
        m2UniversalPlayer = findViewById(R.id.tv_to_universal_player);
    }

    /**
     * 跳转播放界面
     */
    private void responseListener() {
        m2UniversalPlayer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
         if (id == R.id.tv_to_universal_player) {
            //统一播放器Demo
            intent = new Intent(MainActivity.this, PlayerSettingActivity.class);
            startActivity(intent);
        }
    }
}
