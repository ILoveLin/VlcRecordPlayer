package com.company.shenzhou.player.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.R;
import com.company.shenzhou.player.core.PlayerType;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器入口设置界面，选择播放器内核，输入地址，跳转到全屏播放界面
 */
public class PlayerSettingActivity extends AppCompatActivity {

    private EditText mEtUrl;
    private RadioGroup mRgPlayerType;
    private RadioGroup mRgSpeed;
    private Button mBtnPlay;
    private TextView mTvTip;

    private static final String DEFAULT_URL = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";

    @PlayerType
    private int mSelectedPlayerType = PlayerType.VLC;
    private float mSelectedSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_demo);
        initViews();
        initListeners();
    }

    private void initViews() {
        mEtUrl = findViewById(R.id.et_url);
        mRgPlayerType = findViewById(R.id.rg_player_type);
        mRgSpeed = findViewById(R.id.rg_speed);
        mBtnPlay = findViewById(R.id.btn_play);
        mTvTip = findViewById(R.id.tv_tip);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        mEtUrl.setText(DEFAULT_URL);
        updateTip();
    }

    private void initListeners() {
        mRgPlayerType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_vlc) {
                mSelectedPlayerType = PlayerType.VLC;
            } else if (checkedId == R.id.rb_ijk) {
                mSelectedPlayerType = PlayerType.IJK;
            } else if (checkedId == R.id.rb_media_player) {
                mSelectedPlayerType = PlayerType.MEDIA_PLAYER;
            }
            updateTip();
        });

        mRgSpeed.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_speed_05) {
                mSelectedSpeed = 0.5f;
            } else if (checkedId == R.id.rb_speed_075) {
                mSelectedSpeed = 0.75f;
            } else if (checkedId == R.id.rb_speed_10) {
                mSelectedSpeed = 1.0f;
            } else if (checkedId == R.id.rb_speed_15) {
                mSelectedSpeed = 1.5f;
            } else if (checkedId == R.id.rb_speed_20) {
                mSelectedSpeed = 2.0f;
            }
        });

        mBtnPlay.setOnClickListener(v -> {
            String url = mEtUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, "请输入播放地址", Toast.LENGTH_SHORT).show();
                return;
            }
            // 跳转到全屏播放界面，传递播放速度
            PlayerActivity.start(this, url, "视频播放", mSelectedPlayerType, mSelectedSpeed);
        });
    }

    private void updateTip() {
        String tip;
        switch (mSelectedPlayerType) {
            case PlayerType.VLC:
                tip = "VLC 内核：支持 RTSP/RTMP/HTTP，支持录像和截图(16kb内存页)";
                break;
            case PlayerType.IJK:
                tip = "IJK 内核：支持 RTSP/RTMP/HTTP，硬解码，支持截图";
                break;
            case PlayerType.MEDIA_PLAYER:
            default:
                tip = "MediaPlayer 内核：支持 HTTP，支持截图";
                break;
        }
        mTvTip.setText(tip);
    }
}
