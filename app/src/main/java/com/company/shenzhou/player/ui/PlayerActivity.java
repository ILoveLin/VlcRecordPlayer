package com.company.shenzhou.player.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.R;
import com.company.shenzhou.player.controller.CustomButton;
import com.company.shenzhou.player.core.PlayerType;

/**
 *
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器示例 Activity
 */
public class PlayerActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_PLAYER_TYPE = "extra_player_type";
    private static final String EXTRA_SPEED = "extra_speed";

    private UniversalVideoView mVideoView;
    private String mUrl;
    private String mTitle;
    @PlayerType
    private int mPlayerType = PlayerType.VLC;
    private float mSpeed = 1.0f;
    
    // 自定义按钮ID
    private static final int BUTTON_ID_MIC = 1001;
    private boolean mIsMicOn = true;

    /**
     * 启动播放器
     */
    public static void start(Context context, String url) {
        start(context, url, "", PlayerType.VLC, 1.0f);
    }

    /**
     * 启动播放器
     */
    public static void start(Context context, String url, String title) {
        start(context, url, title, PlayerType.VLC, 1.0f);
    }

    /**
     * 启动播放器
     */
    public static void start(Context context, String url, String title, @PlayerType int playerType) {
        start(context, url, title, playerType, 1.0f);
    }

    /**
     * 启动播放器（带播放速度）
     */
    public static void start(Context context, String url, String title, @PlayerType int playerType, float speed) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_PLAYER_TYPE, playerType);
        intent.putExtra(EXTRA_SPEED, speed);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_player);
        
        // 获取参数
        mUrl = getIntent().getStringExtra(EXTRA_URL);
        mTitle = getIntent().getStringExtra(EXTRA_TITLE);
        mPlayerType = getIntent().getIntExtra(EXTRA_PLAYER_TYPE, PlayerType.VLC);
        mSpeed = getIntent().getFloatExtra(EXTRA_SPEED, 1.0f);
        
        initViews();
        startPlay();
    }

    private void initViews() {
        mVideoView = findViewById(R.id.video_view);
        
        // 设置播放器类型
        mVideoView.setPlayerType(mPlayerType);
        
        // 设置标题
        if (mTitle != null && !mTitle.isEmpty()) {
            mVideoView.setTitle(mTitle);
        }
        
        // 设置返回按钮监听
        mVideoView.setOnBackClickListener(() -> finish());
        
        // 设置监听器
        mVideoView.setOnPreparedListener(() -> {
            // 准备完成
        });
        
        mVideoView.setOnCompletionListener(() -> {
            // 播放完成，不自动退出，让用户点击返回
        });
        
        mVideoView.setOnErrorListener((errorCode, errorMsg) -> {
            // 播放错误，显示错误重试按钮（由 VideoController 处理）
            // 返回 true 表示已处理错误，不需要其他处理
            return true;
        });
        
        // 添加自定义麦克风按钮示例
        addMicButton();
    }
    
    /**
     * 添加麦克风按钮示例
     */
    private void addMicButton() {
        CustomButton micButton = new CustomButton(BUTTON_ID_MIC, R.drawable.ic_player_mic)
                .setTag("mic")
                .setClickListener(button -> {
                    // 切换麦克风状态
                    mIsMicOn = !mIsMicOn;
                    
                    // 更新图标
                    mVideoView.updateCustomButtonIcon(BUTTON_ID_MIC, 
                            mIsMicOn ? R.drawable.ic_player_mic : R.drawable.ic_player_mic_off);
                    
                    // 显示提示
                    Toast.makeText(this, mIsMicOn ? "麦克风已开启" : "麦克风已关闭", Toast.LENGTH_SHORT).show();
                    
                    // TODO: 在这里添加你的麦克风控制逻辑
                });
        
        mVideoView.addCustomButton(micButton);
    }

    private void startPlay() {
        if (mUrl != null && !mUrl.isEmpty()) {
            // 设置播放速度
            if (mSpeed != 1.0f) {
                mVideoView.setSpeed(mSpeed);
            }
            mVideoView.play(mUrl);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null && !mVideoView.isPlaying()) {
            mVideoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
        }
    }

    @Override
    public void onBackPressed() {
        if (mVideoView != null && mVideoView.isFullscreen()) {
            mVideoView.exitFullscreen();
            return;
        }
        super.onBackPressed();
    }
}
