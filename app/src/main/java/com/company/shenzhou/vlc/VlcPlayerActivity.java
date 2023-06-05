package com.company.shenzhou.vlc;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.R;
import com.company.shenzhou.util.LogUtils;


/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/1/16 10:13
 * desc：播放界面
 */
public class VlcPlayerActivity extends AppCompatActivity {
    //苹果点播视频链接,网络不好会一直loading,建议使用本地推流地址,如果没有去下载OBS(Open Broadcaster Software是一个免费的开源的视频录制和视频实时交流软件) 电脑屏幕推流即可
//    public static String mPath01 = "rtsp://root:root@192.168.66.31:7788/session0.mpg";
//    public static String mPath01 = "rtmp://58.200.131.2:1935/livetv/hunantv";
//    public static String mPath01 = "http://192.168.67.200:3333/api/stream/video?session=1234567";


//    public static String mPath01 = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
    //湖南卫视直播
//    public static String mPath01 = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
    public static String mPath01 = "http://220.161.87.62:8800/hls/0/index.m3u8";
//    public static String mPath01 = "https://www.cambridgeenglish.org/images/153149-movers-sample-listening-test-vol2.mp3";
private static final String TAG = "Activity,播放器界面中:";

    private RelativeLayout rootView;
    private MyControlVlcVideoView mPlayerView;
    private View.OnTouchListener onTouchVideoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc);
        //设置沉浸式观影模式体验
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //永远不息屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
    }

    private void initView() {
        mPlayerView = findViewById(R.id.player);
        //触摸控制亮度和声音,是否可触摸开关
        rootView = mPlayerView.getRootView();
        onTouchVideoListener = mPlayerView.getOnTouchVideoListener();
        rootView.setLongClickable(true);  //手势需要--能触摸
        rootView.setOnTouchListener(onTouchVideoListener);

        mPlayerView.setPlayerTitle("你好，我是手动设置传入的标题");

        mPlayerView.setVlcControllerLayoutListener(new MyControlVlcVideoView.onVlcControllerLayoutListener() {
            @Override
            public void finishActivity() {
                finish();
            }
        });

        mPlayerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //开始播放
                mPlayerView.setStartLive(mPath01);

            }
        }, 500);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.e(TAG + "==onResume");
        mPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.e(TAG + "==onPause");
        mPlayerView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.e(TAG + "==onStop");
        mPlayerView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.e(TAG + "==onDestroy");
        mPlayerView.onDestroy();

    }
}
