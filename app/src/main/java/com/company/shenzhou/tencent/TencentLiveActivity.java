package com.company.shenzhou.tencent;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.R;
import com.company.shenzhou.util.LogUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.List;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/6/1 11:18
 * desc：腾讯直播界面 webrtc
 */
public class TencentLiveActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "腾讯直播:";
    //电视台直播流
    //private String mVideoPath = "http://220.161.87.62:8800/hls/0/index.m3u8";
    //默认 纯视频流地址---腾讯WebRTC测试流地址
    private String mVideoPath = "webrtc://www.cme8848.com/live/0fd7899cd217a995?txSecret=17d438b3ab8f3562d4bc0484406c57ac&txTime=66061733";
    private EditText mEditLivePath, mEditMicPath;
    private Button mBtnSavePaht2Play, mMicStartPush, mMicStopPush;
    private V2TXLivePlayer mLivePlayer;
    private TXCloudVideoView mPlayRenderView;
    private String mPath;
    private boolean mPlayFlag = false;   //播放的标识
    private String mStreamId;
    private int mStreamType = 3;    //0:RTMP  1：FLV 2:HLS 3:RTC
    private V2TXLivePlayerObserver mV2TXLivePlayerObserver;
    private V2TXLivePusherImpl mLivePusher;
    private TextView mMicPushStatue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tencent);
        initView();
        initData();
        responseListener();
    }

    private void initView() {
        mEditLivePath = findViewById(R.id.edit_live_path);
        mBtnSavePaht2Play = findViewById(R.id.bt_save_live);
        mEditMicPath = findViewById(R.id.edit_mic);
        mMicStartPush = findViewById(R.id.btn_mic_push);
        mMicStopPush = findViewById(R.id.btn_mic_stop);
        mMicPushStatue = findViewById(R.id.tv_mic_statue);
        mPlayRenderView = findViewById(R.id.play_tx_cloud_view);
        
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save_live:     //拉流播放
                mPath ="webrtc://www.cme8848.com/live/0fd7899cd217a995?txSecret=17d438b3ab8f3562d4bc0484406c57ac&txTime=66061733";
//                mPath = mEditLivePath.getText().toString().trim();
                if (null == mLivePlayer) {
                    mLivePlayer = new V2TXLivePlayerImpl(TencentLiveActivity.this);
                } else {
                    mLivePlayer.stopPlay();
                    mLivePlayer.setObserver(mV2TXLivePlayerObserver);
                    mLivePlayer.setRenderView(mPlayRenderView);
                    // 传⼊低延时协议播放地址，即可开始播放；
                    mLivePlayer.startLivePlay(mPath);
                    LogUtils.e(TAG + "播放地址:" + mPath);

                }
                break;
            case R.id.btn_mic_push:     //开启-音频推流
                getXXPermissions();
                break;
            case R.id.btn_mic_stop:     //关闭-音频推流
                if (null != mLivePusher) {
                    mLivePusher.stopMicrophone();
                    mMicPushStatue.setText("未-开启");
                    mLivePusher.stopPush();

                } else {
                    Toast.makeText(TencentLiveActivity.this, "还未开始推流,你关闭个啥??", Toast.LENGTH_SHORT).show();

                }
                break;


        }
    }

    private void initData() {
        mPath = mVideoPath;
//        mPath = mEditLivePath.getText().toString().trim();
        // 创建⼀个 V2TXLivePlayer 对象；
        mLivePlayer = new V2TXLivePlayerImpl(TencentLiveActivity.this);
        mLivePlayer.setObserver(mV2TXLivePlayerObserver);
        mLivePlayer.setRenderView(mPlayRenderView);//设置播放器的视频渲染 View，该控件负责显示视频内容
        /**
         * V2TXLivePlayer播放器使用地址
         * 文档地址 https://cloud.tencent.com/document/product/454/56045
         * V2TXLiveFillModeFill 【默认值】: 图像铺满屏幕，不留黑边，如果图像宽高比不同于屏幕宽高比，部分画面内容会被裁剪掉。
         *  V2TXLiveFillModeFit: 图像适应屏幕，保持画面完整，但如果图像宽高比不同于屏幕宽高比，会有黑边的存在。
         *  V2TXLiveFillModeScaleFill: 图像拉伸铺满，因此长度和宽度可能不会按比例变化。
         */
        mLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFit);
        // 传⼊低延时协议播放地址，即可开始播放；
        mLivePlayer.startLivePlay(mPath);
        //mLivePlayer.
        //mPlayRenderView


        LogUtils.e(TAG + "初始化播放地址:" + mVideoPath);
        mMicPushStatue.setText("未-开启");
        mEditMicPath.setText("rtmp://105950.livepush.myqcloud.com/live/apptest?txSecret=73fb3a653cc0559d372d2a270ccfda18&txTime=649E9ED4");
        mV2TXLivePlayerObserver = new V2TXLivePlayerObserver() {

            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                LogUtils.e(TAG +
                        "[Player] 1onError: player-" + player + " code-" + code + " msg-" + msg + " info-" + extraInfo);
            }

            @Override
            public void onVideoLoading(V2TXLivePlayer player, Bundle extraInfo) {
                LogUtils.e(TAG + "2[Player] onVideoLoading: player-" + player + ", extraInfo-" + extraInfo);
            }

            @Override
            public void onVideoPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
                LogUtils.e(TAG +
                        "[Player] 3onVideoPlaying: player-" + player + " firstPlay-" + firstPlay + " info-" + extraInfo);
            }

            @Override
            public void onVideoResolutionChanged(V2TXLivePlayer player, int width, int height) {
                LogUtils.e(TAG + "4[Player] onVideoResolutionChanged: player-" + player + " width-" + width + " height-"
                        + height);
            }

            @Override
            public void onWarning(V2TXLivePlayer v2TXLivePlayer, int i, String s, Bundle bundle) {
                LogUtils.e(TAG + "5[Player] Override: player-" + v2TXLivePlayer + ", i-" + i + ", s-" + s);
            }

            @Override
            public void onRenderVideoFrame(V2TXLivePlayer player, V2TXLiveDef.V2TXLiveVideoFrame v2TXLiveVideoFrame) {
                super.onRenderVideoFrame(player, v2TXLiveVideoFrame);
                LogUtils.e(TAG +
                        "[Player] 6onRenderVideoFrame: player-" + player + ", v2TXLiveVideoFrame-" + v2TXLiveVideoFrame);
            }
        };

    }

    private void responseListener() {
        //播放视频
        mBtnSavePaht2Play.setOnClickListener(this);
        mMicStartPush.setOnClickListener(this);
        mMicStopPush.setOnClickListener(this);

    }


    private void getXXPermissions() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
//        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
            XXPermissions.with(this)
//                .permission(Permission.READ_EXTERNAL_STORAGE)  //正式版本    android 10已下申请  READ_EXTERNAL_STORAGE   WRITE_EXTERNAL_STORAGE
//                .permission(Permission.WRITE_EXTERNAL_STORAGE)  //正式版本
                    .permission(Permission.RECORD_AUDIO)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            LogUtils.e(TAG + "成功====all=" + all);
                            startMicPushSteam();

                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            LogUtils.e(TAG + "失败=====" + never);


                        }
                    });

        } else {

//        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
            XXPermissions.with(this)
                    .permission(Permission.RECORD_AUDIO)
//                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            LogUtils.e(TAG + "成功====all=" + all);
                            startMicPushSteam();


                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            LogUtils.e(TAG + "失败=====" + never);


                        }
                    });

        }


    }

    private void startMicPushSteam() {
        mLivePusher = new V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP);
        mLivePusher.startMicrophone();
        // 根据推流协议传入对应的 URL 即可启动推流， RTMP 协议以 rtmp:// 开头，该协议不支持连麦
        //String url = "rtmp://test.com/live/streamid?txSecret=xxxxx&txTime=xxxxxxxx";
        // 根据推流协议传入对应的 URL 即可启动推流， RTC 协议以 trtc:// 开头，该协议支持连麦
        //String url = "trtc://cloud.tencent.com/push/streamid?sdkappid=1400188888&userId=A&usersig=xxxxx";
        String mPushPath = mEditMicPath.getText().toString().trim();
        int ret = mLivePusher.startPush(mPushPath);
        LogUtils.e(TAG + "开始音频推流:code=" + ret);
        if (0 == ret) {
            mMicPushStatue.setText("已-开启");
        } else {
            mMicPushStatue.setText("错误");
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLivePlayer != null) {
            if (mPlayFlag) {
                mLivePlayer.stopPlay();
            }
            mLivePlayer = null;
        }
        if (mLivePusher != null) {
            mLivePusher.stopCamera();
            if (mLivePusher.isPushing() == 1) {
                mLivePusher.stopPush();
            }

            mLivePusher = null;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}