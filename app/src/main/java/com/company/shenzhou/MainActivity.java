package com.company.shenzhou;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.player.ui.PlayerSettingActivity;
import com.company.shenzhou.tencent.TencentLiveActivity;


/**
 * author : LoveLin
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2023/1/13
 * desc   : 主界面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private TextView m2Tencent;
    private TextView m2UniversalPlayer;
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
        m2Tencent = findViewById(R.id.tv_to_tencent);
        m2UniversalPlayer = findViewById(R.id.tv_to_universal_player);
    }

    /**
     * 跳转播放界面
     */
    private void responseListener() {
        m2Tencent.setOnClickListener(this);
        m2UniversalPlayer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.tv_to_zlmkit:     //ZlmMediaKit手机服务器界面
//                intent = new Intent(MainActivity.this, ZlmMediaKitTestActivity.class);
//                startActivity(intent);
//                break;
            case R.id.tv_to_tencent:     //测试腾讯云直播
                intent = new Intent(MainActivity.this, TencentLiveActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_to_universal_player:     //统一播放器Demo
                intent = new Intent(MainActivity.this, PlayerSettingActivity.class);
                startActivity(intent);
                break;
        }

    }
}
