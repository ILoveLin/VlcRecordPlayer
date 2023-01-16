package com.shenma.vlcrecordplayer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.shenma.vlcrecordplayer.util.CoreUtil;
import com.shenma.vlcrecordplayer.util.EnumConfig;
import com.shenma.vlcrecordplayer.util.FileUtil;
import com.shenma.vlcrecordplayer.util.LogUtils;
import com.shenma.vlcrecordplayer.vlc.ENDownloadView;
import com.shenma.vlcrecordplayer.vlc.ENPlayView;
import com.shenma.vlcrecordplayer.vlc.MyVlcVideoView;
import com.vlc.lib.RecordEvent;
import com.vlc.lib.VlcVideoView;
import com.vlc.lib.listener.MediaListenerEvent;

import org.videolan.libvlc.Media;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * author : LoveLin
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2023/1/13
 * desc   : vlc录像直播Demo初始版本    控制布局写在主界面 以后有时间再优化,封装到播放界面去
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    public static final String mPath01 = "rtsp://root:root@192.168.66.31:7788/session0.mpg";
    public static final String mPath01 = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
    //vlc录像的Event
    private RecordEvent recordEvent = new RecordEvent();
    //vlc截图文件地址
    private File takeSnapshotFile = new File(Environment.getExternalStorageDirectory(), "CME");

    //自定义方法订阅,布局控制的订阅
    private Disposable mControllerDis;
    private static final int CONTROLLER_HIDE_DELAY = 3000;
    //时间定时器的订阅,刷新
    private Disposable mPlayerTimeDis;
    private Context mContext;
    private LinearLayout mControlTopLayout;
    private RelativeLayout mControlBottomLayout;
    private LinearLayout mControlLeftLayout;
    private LinearLayout mControlRightLayout;
    private LinearLayout mControlMiddleLayout;
    private MyVlcVideoView mPlayerView;
    private RelativeLayout rootView;
    private RelativeLayout mRelativeAll;
    private View.OnTouchListener onTouchVideoListener;
    private VlcVideoView mVlcVideoView;
    private TextView mVlvErrorTextView;
    private ENDownloadView mVlvLoadingView;
    private ENPlayView mVlvPlayView;
    private ImageView mBottomVideoFull;
    private ImageView mLockView;
    private TextView mRightPathType;
    private TextView mRightMic;
    private TextView mRightPhotos;
    private TextView mRightRecord;
    private TextView mRightShot;
    private TextView mBottomTime;
    private TextView mTopTitle;
    private ImageView mTopBack;
    //全屏(展开=0),半屏(收缩=1),默认==半屏(收缩=1)
    private int mCurrPageType;
    //播放状态
    private int mPlayStatueType = EnumConfig.PlayState.STATE_STOP;
    //handler 控制变量
    private static final int SHOW_TIME = 100;
    private static final int SHOW_TOAST = 101;
    private static final int SHOW_HIDE_PLAYER_VIEW = 102;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case SHOW_TIME://设置播放器时间
                    mBottomTime.setText(msg.obj + "");
                    break;
                case SHOW_TOAST://toast提示
                    ToastUtils.make().setNotUseSystemToast().setGravity(Gravity.CENTER, 0, 0)
                            .setTextColor(getResources().getColor(R.color.color_ffffff))
                            .setBgResource(R.drawable.shape_toast_000000_98)
                            .show((String) msg.obj);
                    break;
                case SHOW_HIDE_PLAYER_VIEW://播放器,加载,播放,错误view的显示与隐藏
                    showHidePlayLoadingView((int) msg.obj);
                    break;


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置沉浸式观影模式体验
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //永远不息屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.mContext = MainActivity.this;
        initView();
        responseListener();
    }

    private void initView() {
        mPlayerView = findViewById(R.id.player);
        //触摸控制亮度和声音,是否可触摸开关
        rootView = mPlayerView.getRootView();
        onTouchVideoListener = mPlayerView.getOnTouchVideoListener();
        //VLC播放的View
        mVlcVideoView = findViewById(R.id.vlc_video_view);
        //控制相关
        mControlTopLayout = findViewById(R.id.layout_control_top);
        mTopTitle = findViewById(R.id.tv_top_title);
        mTopBack = findViewById(R.id.iv_back);

        mControlBottomLayout = findViewById(R.id.layout_control_bottom);
        mBottomTime = findViewById(R.id.tv_bottom_time);
        mBottomVideoFull = findViewById(R.id.iv_bottom_video_full);

        mControlLeftLayout = findViewById(R.id.layout_control_left);
        mLockView = findViewById(R.id.iv_left_lock);

        mControlRightLayout = findViewById(R.id.layout_control_right);
        mRightPathType = findViewById(R.id.change_live);
        mRightMic = findViewById(R.id.pusher_mic);
        mRightPhotos = findViewById(R.id.photos);
        mRightRecord = findViewById(R.id.recordStart);
        mRightShot = findViewById(R.id.snapShot);
        mControlMiddleLayout = findViewById(R.id.layout_control_middle);
        //错误提示
        mVlvErrorTextView = findViewById(R.id.vlc_error_text);
        //加载的loading
        mVlvLoadingView = findViewById(R.id.vlc_loading);
        //点击重新加载的view
        mVlvPlayView = findViewById(R.id.vlc_play);
        mRelativeAll = findViewById(R.id.relative_all);
        //发送延迟消息(4s)
//        resetHideController(CONTROLLER_HIDE_DELAY, false);

        mRelativeAll.postDelayed(mHideControllerRunnable, CONTROLLER_HIDE_DELAY);

        //设置播放样式
        setPageType(EnumConfig.PageType.SHRINK);

        startLive(mPath01);
    }

    private void responseListener() {
        //重置触摸事件
//        rootView.setOnTouchListener(null);
//        rootView.setLongClickable(false);  //手势不需要需要--不能触摸

        rootView.setLongClickable(true);  //手势需要--能触摸
        rootView.setOnTouchListener(onTouchVideoListener);
        mBottomVideoFull.setOnClickListener(this);
        mTopBack.setOnClickListener(this);
        mLockView.setOnClickListener(this);
        mRightPathType.setOnClickListener(this);
        mRightMic.setOnClickListener(this);
        mRightPhotos.setOnClickListener(this);
        mRightRecord.setOnClickListener(this);
        mRightShot.setOnClickListener(this);

        //点击事情,控制主动显示或者隐藏所有的控制布局
        mPlayerView.setVlcControllerLayoutListener(new MyVlcVideoView.onVlcControllerLayoutListener() {
            @Override
            public void finishActivity() {

            }
        });
//
//        mPlayerView.setVlcControllerLayoutListener(new MyVlcVideoView.onClickResetHideControllerListener() {
//            @Override
//            public void onClickResetHideController() {
//                LogUtils.e("onClick==========onClickResetHideController===mControllerShow:" + mControllerShow);
////                resetHideController(CONTROLLER_HIDE_IMDT, false);
//                // 先移除之前发送的
//                mRelativeAll.removeCallbacks(mShowControllerRunnable);
//                mRelativeAll.removeCallbacks(mHideControllerRunnable);
//                if (mControllerShow) {
//                    // 隐藏控制面板
//                    mRelativeAll.post(mHideControllerRunnable);
//                } else {
//                    // 显示控制面板
//                    mRelativeAll.post(mShowControllerRunnable);
//                    mRelativeAll.postDelayed(mHideControllerRunnable, CONTROLLER_HIDE_DELAY);
//                }
//
//            }
//        });

        mVlcVideoView.setMediaListenerEvent(new MediaListenerEvent() {
            @Override
            public void eventBuffing(int event, float buffing) {
                if (buffing < 100) {
                    handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_SHOW_LOADING_VIEW);

                    //缓冲时不允许手势操作
//                    mPlayerView.setGestureEnable(false);
                } else if (buffing == 100) {
//                    mPlayerView.setGestureEnable(true);
                    mPlayStatueType = EnumConfig.PlayState.STATE_PLAY;
                    handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_HIDE_LOADING_PLAY_VIEW);

                }
            }

            @Override
            public void eventStop(boolean isPlayError) {
                LogUtils.e("mVlcVideoView==========eventStop");
            }

            @Override
            public void eventError(int event, boolean show) {
                mPlayStatueType = EnumConfig.PlayState.STATE_STOP;
                handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_SHOW_ERROR_VIEW);
                LogUtils.e("mVlcVideoView==========eventError");

            }

            @Override
            public void eventPlay(boolean isPlaying) {
                LogUtils.e("mVlcVideoView==========eventPlay:" + isPlaying);

            }

            @Override
            public void eventSystemEnd(String isStringed) {
                LogUtils.e("mVlcVideoView==========eventSystemEnd:" + isStringed);

            }

            @Override
            public void eventCurrentTime(String time) {

            }

            @Override
            public void eventPlayInit(boolean openClose) {
                LogUtils.e("mVlcVideoView==========eventPlayInit:" + openClose);
                if (openClose) {
                    createPlayerTimeSub();
                }

            }
        });


    }

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                mVlcVideoView.setAddSlave(null);
                mVlcVideoView.onStop();
                finish();
                break;
            case R.id.iv_left_lock: //锁屏
                LogUtils.e("onClick==========锁屏:");
                if (mLockMode) {
                    unlock();
                } else {
                    lock();
                }

                break;
            case R.id.iv_bottom_video_full: //全屏
                setVideoWindowType();
                break;
            case R.id.change_live: //清晰度
                showToast("清晰度");
                break;
            case R.id.pusher_mic:   //语音
                showToast("语音");
                break;
            case R.id.photos:       //相册

                showToast("相册");
                break;
            case R.id.recordStart:  //录像

                showToast("录像");
                break;
            case R.id.snapShot:     //截图
                getStoragePermission("Shot");

                break;


        }
    }

    /**
     * 录像,截图获取权限
     *
     * @param type
     */
    private void getStoragePermission(String type) {
        XXPermissions.with(this)
                // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            //直播状态
                            if (mPlayStatueType == EnumConfig.PlayState.STATE_PLAY) {

                                if (mVlcVideoView.isPrepare()) {
                                    Media.VideoTrack mVideoTrack = mVlcVideoView.getVideoTrack();
                                    if (mVideoTrack != null) {
                                        showToast("截图成功");
                                        //原图
                                        LogUtils.e("path=====录像(截图的地址):=====" + takeSnapshotFile.getAbsolutePath()); //   /storage/emulated/0/1604026573438.mp4
                                        File localFile = new File(takeSnapshotFile.getAbsolutePath());
                                        if (!localFile.exists()) {
                                            localFile.mkdir();
                                        }
                                        recordEvent.takeSnapshot(mVlcVideoView.getMediaPlayer(), takeSnapshotFile.getAbsolutePath(), 0, 0);
                                        //插入相册01,有些设备刷新会出问题 01,02都行
//                                       MediaStore.Images.Media.insertImage(getContentResolver(), mVlcVideoView.getBitmap(), "", "");
                                        String nowString = TimeUtils.getNowString().trim();
                                        LogUtils.e("path=====录像(nowString):=====" + nowString); //   /storage/emulated/0/1604026573438.mp4

                                        MediaStore.Images.Media.insertImage(getContentResolver(), mVlcVideoView.getBitmap(), nowString, null);

//                                        MediaStore.Images.Media.insertImage(getContentResolver(), mVlcVideoView.getBitmap(), "", "");
                                        //刷新相册02,以下解决,(最好的效果)此问题在android 10.0 的版本上会出现。图库不刷新问题java.lang.IllegalStateException: Failed to build unique file
                                        FileUtil.RefreshAlbum(takeSnapshotFile.getAbsolutePath(), false, MainActivity.this);
                                        //recordEvent.takeSnapshot(vlcVideoView.getMediaPlayer(), takeSnapshotFile.getAbsolutePath(), videoTrack.width / 2, 0);
                                    }
                                }

                            }

                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            showToast("被永久拒绝授权，请手动授予存储权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(getApplicationContext(), permissions);
                        } else {
                            showToast("获取存储权限失败");


                        }
                    }
                });

    }

    /**
     * 开始直播
     *
     * @param path
     */
    private void startLive(String path) {
        handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_SHOW_LOADING_VIEW);
        mVlcVideoView.setPath(path);
        mVlcVideoView.startPlay();
    }
//
    /**
     * 创建播放器时间显示定时器
     */
    private void createPlayerTimeSub() {
        //currentTime为当前时间的格式化显示,为字符串类型
        mPlayerTimeDis = Observable
                .interval(1, TimeUnit.SECONDS)//定时器操作符，这里1秒打印一个log,刷新一次时间
                //取消任务时取消定时唤醒
                .doOnDispose(() -> {
                })
                .subscribe(count -> {
                    String currentTime = CoreUtil.secToTime(Integer.parseInt(count + ""));
                    showPlayerTime(currentTime);
                    LogUtils.e("mPlayerTimeDis==========currentTime:" + currentTime);
                    LogUtils.e("mPlayerTimeDis==========count:" + count);

                });
    }



    /**
     * 锁定控制面板
     */
    public void lock() {
        mLockMode = true;
        mLockView.setImageResource(R.drawable.video_lock_close_ic);
        mControlTopLayout.setVisibility(GONE);
        mControlBottomLayout.setVisibility(GONE);
        mControlRightLayout.setVisibility(GONE);
        // 延迟隐藏控制面板
        mRelativeAll.removeCallbacks(mHideControllerRunnable);
        mRelativeAll.postDelayed(mHideControllerRunnable, CONTROLLER_HIDE_DELAY);
    }

    /**
     * 解锁控制面板
     */
    public void unlock() {
        mLockMode = false;
        mLockView.setImageResource(R.drawable.video_lock_open_ic);
        mControlTopLayout.setVisibility(VISIBLE);
        mControlBottomLayout.setVisibility(VISIBLE);
        mControlRightLayout.setVisibility(VISIBLE);
        // 延迟隐藏控制面板
        mRelativeAll.removeCallbacks(mHideControllerRunnable);
        mRelativeAll.postDelayed(mHideControllerRunnable, CONTROLLER_HIDE_DELAY);
    }


    /**
     * 锁定面板
     */
    private boolean mLockMode;
    /**
     * 显示面板
     */
    private boolean mControllerShow = true;

    /**
     * 显示控制面板
     */
    private final Runnable mShowControllerRunnable = () -> {
        if (!mControllerShow) {
            showController();
        }
    };

    /**
     * 隐藏控制面板
     */
    private final Runnable mHideControllerRunnable = () -> {
        if (mControllerShow) {
            hideController();
        }
    };


    /**
     * 显示面板
     */
    public void showController() {
        if (mControllerShow) {
            return;
        }

        mControllerShow = true;
        ObjectAnimator.ofFloat(mControlTopLayout, "translationY", -mControlTopLayout.getHeight(), 0).start();
        ObjectAnimator.ofFloat(mControlBottomLayout, "translationY", mControlBottomLayout.getHeight(), 0).start();
        ObjectAnimator.ofFloat(mControlRightLayout, "translationX", mControlRightLayout.getWidth(), 0).start();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            mLockView.setAlpha(alpha);
            if ((int) alpha != 1) {
                return;
            }
            if (mLockView.getVisibility() == INVISIBLE) {
                mLockView.setVisibility(VISIBLE);
            }

        });
        animator.start();
    }


    /**
     * 隐藏面板
     */
    public void hideController() {
        if (!mControllerShow) {
            return;
        }

        mControllerShow = false;
        ObjectAnimator.ofFloat(mControlTopLayout, "translationY", 0, -mControlTopLayout.getHeight()).start();
        ObjectAnimator.ofFloat(mControlBottomLayout, "translationY", 0, mControlBottomLayout.getHeight()).start();
        ObjectAnimator.ofFloat(mControlRightLayout, "translationX", 0, mControlRightLayout.getWidth()).start();

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            mLockView.setAlpha(alpha);
            if (alpha != 0f) {
                return;
            }

            if (mLockView.getVisibility() == VISIBLE) {
                mLockView.setVisibility(INVISIBLE);
            }
        });
        animator.start();
    }


    //handler消息,控制显示:错误,播放,加载view
    private void handlerMsgShowHidePlayLoadingView(int toastStr) {
        Message tempMsg = mHandler.obtainMessage();
        tempMsg.what = SHOW_HIDE_PLAYER_VIEW;
        tempMsg.obj = toastStr;
        mHandler.sendMessage(tempMsg);
    }

    //handler消息,显示播放器时间
    private void showPlayerTime(String data) {
        Message timeMessage = mHandler.obtainMessage();
        timeMessage.what = SHOW_TIME;
        timeMessage.obj = data;
        mHandler.sendMessage(timeMessage);

    }

    //handler消息,显示toast
    private void showToast(String toastStr) {
        Message tempMsg = mHandler.obtainMessage();
        tempMsg.what = SHOW_TOAST;
        tempMsg.obj = toastStr;
        mHandler.sendMessage(tempMsg);
    }

    /**
     * 设置播放器:全屏/半屏,显示
     */
    public void setVideoWindowType() {
        int orientation = ((Activity) mContext).getRequestedOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //竖屏
            setPageType(EnumConfig.PageType.SHRINK);
        } else {
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
            setPageType(EnumConfig.PageType.EXPAND);
        }
    }

    /**
     * 设置播放样式:全屏或者半屏
     *
     * @param pageType
     */
    public void setPageType(int pageType) {
        mCurrPageType = pageType;
        if (pageType == EnumConfig.PageType.SHRINK) {
            mBottomVideoFull.setImageResource(R.drawable.nur_ic_fangda);
        } else {
            mBottomVideoFull.setImageResource(R.drawable.nur_ic_fangxiao);
        }
    }

    /**
     * 显示或者隐藏 加载/开始 View
     *
     * @param type 传入需要显示View的类型
     */
    public void showHidePlayLoadingView(int type) {
        //显示loadingView,隐藏playView
        if (EnumConfig.PlayerState.PLAYER_SHOW_LOADING_VIEW == type) {
            mVlvLoadingView.setVisibility(View.VISIBLE);
            mVlvPlayView.setVisibility(INVISIBLE);
            mVlvErrorTextView.setVisibility(INVISIBLE);
            mVlvLoadingView.start();
        } else if (EnumConfig.PlayerState.PLAYER_SHOW_PLAY_VIEW == type) {
            mVlvPlayView.setVisibility(VISIBLE);
            mVlvLoadingView.setVisibility(INVISIBLE);
            mVlvErrorTextView.setVisibility(INVISIBLE);
            mVlvLoadingView.release();
        } else if (EnumConfig.PlayerState.PLAYER_HIDE_LOADING_PLAY_VIEW == type) {
            //全部隐藏
            mVlvLoadingView.release();
            mVlvPlayView.setVisibility(INVISIBLE);
            mVlvLoadingView.setVisibility(INVISIBLE);
            mVlvErrorTextView.setVisibility(INVISIBLE);
        } else if (EnumConfig.PlayerState.PLAYER_SHOW_ERROR_VIEW == type) {
            //显示错误文字和加载view
            mVlvLoadingView.release();
            mVlvLoadingView.setVisibility(INVISIBLE);
            mVlvPlayView.setVisibility(VISIBLE);
            mVlvErrorTextView.setVisibility(VISIBLE);
        }

    }


    public void setPageTypeByBoolean(boolean isShrink) {
        if (isShrink) {
            mCurrPageType = EnumConfig.PageType.SHRINK;
            mBottomVideoFull.setImageResource(R.drawable.nur_ic_fangda);
        } else {
            mCurrPageType = EnumConfig.PageType.EXPAND;
            mBottomVideoFull.setImageResource(R.drawable.nur_ic_fangxiao);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();
        LogUtils.e("path=====录像--onPause:=====");
        //手动清空字幕
        if (null != mVlcVideoView) {
            mVlcVideoView.setAddSlave(null);
            //直接调用stop 或者onPause(自己新增的方法),不然回ANR
            // vlcVideoView.onStop();
            mVlcVideoView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //手动清空字幕
        if (null != mVlcVideoView) {
            mVlcVideoView.setAddSlave(null);
            //直接调用stop 不然回ANR
            mVlcVideoView.onStop();
            mVlcVideoView.onDestroy();
        }
        if (null != mVlcVideoView) {
            mVlcVideoView.setMediaListenerEvent(null);
        }
        if (null != mPlayerTimeDis) {
            mPlayerTimeDis.dispose();
            mPlayerTimeDis = null;
        }

        mRelativeAll.removeCallbacks(mShowControllerRunnable);
        mRelativeAll.removeCallbacks(mHideControllerRunnable);
        mRelativeAll.removeAllViews();
    }
}




