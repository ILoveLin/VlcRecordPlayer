package com.company.shenzhou;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.ffmpeg.FFmpegActivity;
import com.company.shenzhou.vlc.VlcPlayerActivity;
import com.company.shenzhou.zlm.ZlmMediaKitTestActivity;

/**
 * author : LoveLin
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2023/1/13
 * desc   : 主界面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private TextView m2VLCPlayer, m2FFmpegKit,m2ZLMKit;
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
        m2VLCPlayer = findViewById(R.id.tv_to_vlc);
        m2FFmpegKit = findViewById(R.id.tv_to_ffmpeg);
        m2ZLMKit = findViewById(R.id.tv_to_zlmkit);

    }

    /**
     * 跳转播放界面
     */
    private void responseListener() {
        m2VLCPlayer.setOnClickListener(this);
        m2FFmpegKit.setOnClickListener(this);
        m2ZLMKit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_to_vlc:        //VLC播放界面
                intent = new Intent(MainActivity.this, VlcPlayerActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_to_ffmpeg:     //ffmpeg测试界面
//                intent = new Intent(MainActivity.this, FFmpegHttpActivity.class);
                intent = new Intent(MainActivity.this, FFmpegActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_to_zlmkit:     //ZlmMediaKit手机服务器界面
                intent = new Intent(MainActivity.this, ZlmMediaKitTestActivity.class);
                startActivity(intent);
                break;
        }

    }
}
