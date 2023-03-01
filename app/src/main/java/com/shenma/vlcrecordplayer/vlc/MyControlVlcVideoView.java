package com.shenma.vlcrecordplayer.vlc;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.shenma.vlcrecordplayer.MainActivity;
import com.shenma.vlcrecordplayer.R;
import com.shenma.vlcrecordplayer.util.CoreUtil;
import com.shenma.vlcrecordplayer.util.EnumConfig;
import com.shenma.vlcrecordplayer.util.FileUtil;
import com.shenma.vlcrecordplayer.util.LogUtils;
import com.vlc.lib.RecordEvent;
import com.vlc.lib.VlcVideoView;
import com.vlc.lib.listener.MediaListenerEvent;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/1/12 15:13
 * desc：自定义View-播放器View-包含(控制布局-手势)
 */
public class MyControlVlcVideoView extends RelativeLayout implements GestureDetector.OnGestureListener, View.OnClickListener {
    private Context mContext;
    private RelativeLayout mRootLayout;
    private LinearLayout mVideoGestureLayout;
    private ImageView mVideoGestureImg;
    private TextView mVideoGestureText;

    //播放器手势
    private GestureDetector mGestureDetector;
    private boolean mIsProgressChange = false;    //是否为手势改变进度
    private boolean mIsFirstScroll = false;// 每次触摸屏幕后，第一次scroll的标志
    private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量,3.调节亮度
    private static final int GESTURE_MODIFY_PROGRESS = 1;
    private static final int GESTURE_MODIFY_VOLUME = 2;
    private static final int GESTURE_MODIFY_BRIGHT = 3;
    private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快

    //自新封装的控制布局
    private static final String TAG = "VlcVideoPlayerView,自定义View中:";

    public String mTitle = "我是标题";
    public static String mPath01 = "rtsp://root:root@192.168.66.31:7788/session0.mpg";
    public static String mPath02 = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
    //vlc录像的Event
    private RecordEvent recordEvent = new RecordEvent();
    //vlc截图文件地址
    private File mTakeSnapshotFile = new File(Environment.getExternalStorageDirectory(), "CME");
    private File mRecordFile = new File(Environment.getExternalStorageDirectory(), "CME");
    //    private File mRecordFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CME");
    private String mRecordDirectory = mRecordFile.getAbsolutePath();
    //自定义方法订阅,布局控制的订阅
    private Disposable mControllerDis;
    private static final int CONTROLLER_HIDE_DELAY = 3000;
    //时间定时器的订阅,刷新
    private Disposable mPlayerTimeDis;
    private LinearLayout mControlTopLayout;
    private RelativeLayout mControlBottomLayout;
    private LinearLayout mControlLeftLayout;
    private LinearLayout mControlRightLayout;
    private LinearLayout mControlMiddleLayout;
    private MyControlVlcVideoView mPlayerView;
    private RelativeLayout rootView;
    private RelativeLayout mRelativeAll;
    private View.OnTouchListener onTouchVideoListener;
    private VlcVideoView mVlcVideoPlayerView;
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
    //播放状态,默认未录像:false
    private Boolean mRecordType = false;
    //handler 控制变量
    private static final int SHOW_TIME = 100;
    private static final int SHOW_TOAST = 101;
    private static final int SHOW_HIDE_PLAYER_VIEW = 102;
    private static final int RECORD_START = 103;
    private static final int RECORD_STOP = 104;
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
                case RECORD_START://开始录像
                    setTextColor(getResources().getColor(R.color.colorAccent), getResources().getString(R.string.vlc_video));
                    Drawable record_start = getResources().getDrawable(R.drawable.icon_record_pre);
                    mRightRecord.setCompoundDrawablesWithIntrinsicBounds(null, record_start, null, null);
                    break;
                case RECORD_STOP://结束录像
                    setTextColor(getResources().getColor(R.color.white), getResources().getString(R.string.vlc_video));
                    Drawable record_end = getResources().getDrawable(R.drawable.icon_record_nore);
                    mRightRecord.setCompoundDrawablesWithIntrinsicBounds(null, record_end, null, null);
                    break;


            }
        }
    };
    private String rootPath;
    private String mRecordOppoDirectory;
    private ImageView mBottomVoice;

    public MyControlVlcVideoView(Context context) {
        super(context);
        initView(context);
    }

    public MyControlVlcVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MyControlVlcVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

    }


    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.vlc_video_palyer_control_view_layout, this);
        mVlcVideoPlayerView = findViewById(R.id.vlc_video_view);      //vlc播放器View

        //初始化控件
        initControlView();
        mVideoGestureLayout.getBackground().setAlpha(110);
//        mRootLayout.setOnTouchListener(mOnTouchVideoListener);
        mRootLayout.setLongClickable(true);  //手势需要
        //视频播放器手势
        mGestureDetector = new GestureDetector(mContext, this);
        mGestureDetector.setIsLongpressEnabled(true);
        mAudiomanager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量

        //自新封装的控制布局
        mPlayerView = findViewById(R.id.player);

        //控制相关
        mControlTopLayout = findViewById(R.id.layout_control_top);
        mTopTitle = findViewById(R.id.tv_top_title);
        mTopBack = findViewById(R.id.iv_back);

        mControlBottomLayout = findViewById(R.id.layout_control_bottom);
        mBottomTime = findViewById(R.id.tv_bottom_time);
        mBottomVoice = findViewById(R.id.iv_voice_type);
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
        mRelativeAll = findViewById(R.id.root_layout_vlc);

        mRelativeAll.postDelayed(mHideControllerRunnable, CONTROLLER_HIDE_DELAY);
        //设置播放样式
        setPageType(EnumConfig.PageType.SHRINK);

        responseListener();
    }


    public RelativeLayout getRootView() {
        return mRootLayout;
    }

    public OnTouchListener getOnTouchVideoListener() {
        return mOnTouchVideoListener;
    }


    private AudioManager mAudiomanager;

    private void initControlView() {
        mRootLayout = findViewById(R.id.root_layout_vlc);
        mVideoGestureLayout = findViewById(R.id.video_gesture_layout);
        mVideoGestureImg = findViewById(R.id.video_gesture_img);
        mVideoGestureText = findViewById(R.id.video_gesture_text);
    }


    /*视频播放 - Start*/
    private OnTouchListener mOnTouchVideoListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mDownX = motionEvent.getX();
                mDownY = motionEvent.getY();
                mIsFirstScroll = true;  // 设定是触摸屏幕后第一次scroll的标志
                mCurrentVolume = mAudiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前音量值
                //第一次进入，获取的当前亮度为系统目前亮度（此时getWindow().getAttributes().screenBrightness = -1.0）
                //未退出再次在该界面调节时，获取当前已调节的亮度
                if (((Activity) mContext).getWindow().getAttributes().screenBrightness < 0) {
                    mCurrentBrightness = android.provider.Settings.System.getInt(mContext.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 255) / mMaxBrightness;  // 获取当前系统亮度值,获取失败则返回255
                } else {
                    mCurrentBrightness = ((Activity) mContext).getWindow().getAttributes().screenBrightness;
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                float upX = motionEvent.getX();
                float upY = motionEvent.getY();
                GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
                mVideoGestureLayout.setVisibility(GONE);
                //通过down和up来判断手势是否移动，部分机型用MotionEvent.ACTION_MOVE判断会有误
                if (Math.abs(upX - mDownX) > 20 || Math.abs(upY - mDownY) > 20) {
                } else {  //非手势移动，才自动显示/隐藏状态栏
//                    /**
//                     * 此处做点击 是否显示或者隐藏控制布局
//                     */
//                    // 先移除之前发送的
//                    mRelativeAll.removeCallbacks(mShowControllerRunnable);
//                    mRelativeAll.removeCallbacks(mHideControllerRunnable);
//                    if (mControllerShow) {
//                        // 隐藏控制面板
//                        mRelativeAll.post(mHideControllerRunnable);
//                    } else {
//                        // 显示控制面板
//                        mRelativeAll.post(mShowControllerRunnable);
//                        mRelativeAll.postDelayed(mHideControllerRunnable, CONTROLLER_HIDE_DELAY);
//                    }

                }
                mIsProgressChange = false;
            }
            return mGestureDetector.onTouchEvent(motionEvent);
        }
    };


    /**
     * 控制布局监听回调
     */
    private onVlcControllerLayoutListener mListener;

    public void setVlcControllerLayoutListener(onVlcControllerLayoutListener mListener) {
        this.mListener = mListener;
    }


    //点击显示隐藏控制面板
    public interface onVlcControllerLayoutListener {

        void finishActivity();


    }

//    /**
//     * activity生命周期监听回调
//     * Life cycle
//     */
//    private onLifeCycle mLifeCycleListener;
//
//    public void setonLifeCycle(onLifeCycle mLifeCycleListener) {
//        this.mLifeCycleListener = mLifeCycleListener;
//    }
//
//
//    //点击显示隐藏控制面板
//    public interface onLifeCycle {
//
//        void onLifeCycleResume();
//        void onLifeCyclePause();
//        void onLifeCycleDestroy();
//
//
//    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    private boolean mIsAllowGesture = true; //默认允许手势操作


    public void setGestureEnable(boolean mIsAllowGesture) {
        this.mIsAllowGesture = mIsAllowGesture;
    }

    //播放器手势
//
    private int mMaxVolume = -1;
    private int mCurrentVolume = -1;
    private float mCurrentBrightness = 0.5f;
    private float mMaxBrightness = 255.0f;
    private static final int MIN_BRIGHTNESS = 10;
    //private long mCurDownPlayingTime = 0;
    private float mDownX = 0;
    private float mDownY = 0;

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!mIsAllowGesture) {
            return false;
        }
        float ex = e1.getX(), ey = e1.getY();
        int varX = (int) (e2.getX() - e1.getX());
        int varY = (int) (e2.getY() - e1.getY());
        if (mIsFirstScroll) {// 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
            mVideoGestureLayout.setVisibility(VISIBLE);
            // 横向的距离变化大则调整进度，纵向的变化大则调整音量
            if (Math.abs(distanceX) >= Math.abs(distanceY)) {    //调节进度
                mVideoGestureLayout.setVisibility(GONE);

            } else {
                if (ex < mRootLayout.getWidth() / 2) {     //左半边亮度
                    GESTURE_FLAG = GESTURE_MODIFY_BRIGHT;
                } else {    //右半边音量
                    GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
                }
            }
        }
        if (GESTURE_FLAG == GESTURE_MODIFY_BRIGHT) {
            mVideoGestureImg.setImageResource(R.drawable.brightness);
            int slideHeight = mRootLayout.getHeight() / 2;
            int midLevelPx = slideHeight / 15;
            int slideDistance = -varY;
            int slideLevel = slideDistance / midLevelPx;
            if (mCurrentBrightness == -1 || mCurrentBrightness < 0) {
                mCurrentBrightness = 0;
            }
            WindowManager.LayoutParams lpa = ((Activity) mContext).getWindow().getAttributes();
            float midLevelBright = (mMaxBrightness - MIN_BRIGHTNESS) / 15.0f;
            float realBright = midLevelBright * slideLevel + mCurrentBrightness
                    * (mMaxBrightness - MIN_BRIGHTNESS) + MIN_BRIGHTNESS;
            if (realBright < MIN_BRIGHTNESS) {
                realBright = MIN_BRIGHTNESS;
            }
            if (realBright > mMaxBrightness) {
                realBright = mMaxBrightness;
            }
            lpa.screenBrightness = realBright / mMaxBrightness;
            ((Activity) mContext).getWindow().setAttributes(lpa);
            mVideoGestureText.setText((int) (lpa.screenBrightness * 100) + "%");
        } else if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
            if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
                int slideHeight = mRootLayout.getHeight() / 2;
                int midLevelPx = slideHeight / 15;
                int slideDistance = -varY;
                int slideLevel = slideDistance / midLevelPx;
                int midLevelVolume = mMaxVolume / 15;
                int realVolume = midLevelVolume * slideLevel + mCurrentVolume;
                if (realVolume <= 0) {
                    realVolume = 0;
                    mVideoGestureImg.setImageResource(R.drawable.volume_slience);
                } else {
                    mVideoGestureImg.setImageResource(R.drawable.volume_not_slience);
                }
                if (realVolume > mMaxVolume) {
                    realVolume = mMaxVolume;
                }
                int percentage = (realVolume * 100) / mMaxVolume;
                mVideoGestureText.setText(percentage + "%");
                //设置音量大小，第一个参数：STREAM_VOICE_CALL(通话)、STREAM_SYSTEM(系统声音)、STREAM_RING(铃声)、STREAM_MUSIC(音乐)和STREAM_ALARM(闹铃)；
                // 第二个参数：音量值，取值范围为0-7；
                // 第三个参数：可选标志位，用于显示出音量调节UI(AudioManager.FLAG_SHOW_UI)。
                mAudiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, realVolume, 0);
            }
        }
        mIsFirstScroll = false;// 第一次scroll执行完成，修改标志
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }


    /**
     * **********************************************************************************************
     * **********************************************************************************************
     * 自己新增控制布局的代码
     * **********************************************************************************************
     * **********************************************************************************************
     */

    private void responseListener() {
        mBottomVideoFull.setOnClickListener(this);
        mTopBack.setOnClickListener(this);
        mLockView.setOnClickListener(this);
        mRightPathType.setOnClickListener(this);
        mBottomVoice.setOnClickListener(this);
        mRightMic.setOnClickListener(this);
        mRightPhotos.setOnClickListener(this);
        mRightRecord.setOnClickListener(this);
        mRightShot.setOnClickListener(this);
        mVlvPlayView.setOnClickListener(this);
        mVlcVideoPlayerView.setMediaListenerEvent(new MediaListenerEvent() {
            @Override
            public void eventBuffing(int event, float buffing) {
                if (buffing < 100) {
                    mPlayStatueType = EnumConfig.PlayState.STATE_LOAD;
                    if (mVlvLoadingView.getVisibility() == VISIBLE) {
                        return;
                    }
                    handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_SHOW_LOADING_VIEW);
                    LogUtils.e(TAG + "mMediaListenerEvent====eventBuffing方法==buffing < 100==:" + buffing);
                    //缓冲时不允许手势操作
//                    mPlayerView.setGestureEnable(false);
                } else if (buffing == 100) {
                    LogUtils.e(TAG + "mMediaListenerEvent====eventBuffing方法==buffing == 100==:" + buffing);

//                    mPlayerView.setGestureEnable(true);
                    mPlayStatueType = EnumConfig.PlayState.STATE_PLAY;
                    handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_HIDE_LOADING_PLAY_VIEW);

                }
            }

            @Override
            public void eventStop(boolean isPlayError) {
                if (null != mPlayerTimeDis) {
                    mPlayerTimeDis.dispose();
                    mPlayerTimeDis = null;
                }
                mPlayStatueType = EnumConfig.PlayState.STATE_STOP;
                LogUtils.e(TAG + "mMediaListenerEvent====eventStop方法==isPlayError:" + isPlayError);
                handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_SHOW_ERROR_VIEW);

            }

            @Override
            public void eventError(int event, boolean show) {
                mPlayStatueType = EnumConfig.PlayState.STATE_STOP;
                handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_SHOW_ERROR_VIEW);
                LogUtils.e(TAG + "mMediaListenerEvent====eventError方法==show:" + show);

            }

            @Override
            public void eventPlay(boolean isPlaying) {
                LogUtils.e(TAG + "mMediaListenerEvent====eventPlay方法==isPlaying:" + isPlaying);

            }

            @Override
            public void eventSystemEnd(String isStringed) {
                LogUtils.e(TAG + "mMediaListenerEvent====eventSystemEnd方法==isStringed:" + isStringed);

            }

            @Override
            public void eventCurrentTime(String time) {

            }

            @Override
            public void eventPlayInit(boolean openClose) {//openClose 当前界面是否可见,推入后台,就是不可见=false
                LogUtils.e(TAG + "mMediaListenerEvent====eventPlayInit方法==openClose:" + openClose);
                if (openClose) {
                    createPlayerTimeSub();
                }

            }
        });

        /**
         * 点击响应触摸事件,显示/隐藏控制布局
         */
        getRootView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.e(TAG + "onClick控制不仅是否显示？：" + mControllerShow);

                // 先移除之前发送的
                mRelativeAll.removeCallbacks(mShowControllerRunnable);
                mRelativeAll.removeCallbacks(mHideControllerRunnable);
                if (mControllerShow) {
                    // 隐藏控制面板
                    mRelativeAll.post(mHideControllerRunnable);
                } else {
                    // 显示控制面板
                    mRelativeAll.post(mShowControllerRunnable);
                    mRelativeAll.postDelayed(mHideControllerRunnable, CONTROLLER_HIDE_DELAY);
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                mVlcVideoPlayerView.setAddSlave(null);
                mVlcVideoPlayerView.onStop();
                mListener.finishActivity();
                break;
            case R.id.iv_left_lock: //锁屏
                if (mLockMode) {
                    unlock();
                } else {
                    lock();
                }
                break;
            case R.id.iv_bottom_video_full: //全屏
                setVideoWindowType();
                break;
            case R.id.vlc_play: //点击重新加载
                startLive(mPath01);
                LogUtils.e(TAG + "点击重新加载");

                break;
            case R.id.iv_voice_type: //控制,是否静音
                if (mVlcVideoPlayerView.getMediaPlayer().isPlaying() && mVlcVideoPlayerView.getMediaPlayer() != null) {
                    //当前有声音,设置音量为0.
                    if (mVlcVideoPlayerView.getMediaPlayer().getVolume() != 0) {
                        mVlcVideoPlayerView.getMediaPlayer().setVolume(0);
                        mBottomVoice.setImageDrawable(getResources().getDrawable(R.drawable.ic_player_have_no_voice));
                        //静音模式,不能响应触摸手势
                        getRootView().setOnTouchListener(null);
                        getRootView().setLongClickable(false);  //手势不需要需要--不能触摸
                        showToast("设置为:静音模式");

                    } else if (mVlcVideoPlayerView.getMediaPlayer().getVolume() == 0) {
                        mVlcVideoPlayerView.getMediaPlayer().setVolume(60);
                        mBottomVoice.setImageDrawable(getResources().getDrawable(R.drawable.ic_player_have_voice));
                        //播放模式,正常响应触摸手势
                        getRootView().setLongClickable(true);  //手势需要--能触摸
                        getRootView().setOnTouchListener(getOnTouchVideoListener());

                        showToast("设置为:播放模式");

                    }
                }
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
                getStoragePermission("record");

                break;
            case R.id.snapShot:     //截图
                getStoragePermission("shot");
                break;


        }
    }

    //handler消息,控制显示:错误,播放,加载view
    private void handlerMsgShowHidePlayLoadingView(int toastStr) {
        Message tempMsg = mHandler.obtainMessage();
        tempMsg.what = SHOW_HIDE_PLAYER_VIEW;
        tempMsg.obj = toastStr;
        mHandler.sendMessageDelayed(tempMsg, SHOW_HIDE_PLAYER_VIEW);
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
//                    LogUtils.e("显示定时器====currentTime：" + currentTime);
//                    LogUtils.e("显示定时器====count：" + count);

                });
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

    /**
     * 录像,截图获取权限
     *
     * @param type
     */
    private void getStoragePermission(String type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            XXPermissions.with(mContext)
                    // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
//                .permission(Permission.READ_EXTERNAL_STORAGE)
//                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                //直播状态
                                if (mPlayStatueType == EnumConfig.PlayState.STATE_PLAY) {
                                    //截图,功能
                                    if ("shot".equals(type)) {
                                        if (mVlcVideoPlayerView.isPrepare()) {
                                            Media.VideoTrack mVideoTrack = mVlcVideoPlayerView.getVideoTrack();
                                            if (mVideoTrack != null) {
                                                showToast("截图成功");
                                                //原图
                                                LogUtils.e(TAG + "录像(截图的地址)====count：" + mTakeSnapshotFile.getAbsolutePath());
                                                File localFile = new File(mTakeSnapshotFile.getAbsolutePath());
                                                if (!localFile.exists()) {
                                                    localFile.mkdir();
                                                }
                                                recordEvent.takeSnapshot(mVlcVideoPlayerView.getMediaPlayer(), mTakeSnapshotFile.getAbsolutePath(), 0, 0);
                                                //插入相册01,有些设备刷新会出问题 01,02都行  VIVO能刷截图,华为能刷新
//                                                MediaStore.Images.Media.insertImage(mContext.getContentResolver(), mVlcVideoPlayerView.getBitmap(), "", "");

//                                                MediaStore.Images.Media.insertImage(getContentResolver(), mVlcVideoView.getBitmap(), "", "");
                                                //刷新相册02,以下解决,(最好的效果)此问题在android 10.0 的版本上会出现。图库不刷新问题java.lang.IllegalStateException: Failed to build unique file
                                                MediaStore.Images.Media.insertImage(mContext.getContentResolver(), mVlcVideoPlayerView.getBitmap(), "IMG" + Calendar.getInstance().getTime(), null);

//                                                FileUtil.RefreshAlbum(mTakeSnapshotFile.getAbsolutePath(), false, mContext);  //刷新相册
                                                try {
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                                //recordEvent.takeSnapshot(vlcVideoView.getMediaPlayer(), takeSnapshotFile.getAbsolutePath(), videoTrack.width / 2, 0);
                                            }
                                        }
                                    } else {
                                        //录像,功能
                                        if (mRecordType) {
                                            LogUtils.e(TAG + "录像--结束录像:==2==="); //   /storage/emulated/0/1604026573438.mp4
                                            vlcRecordOver();
                                        } else {
                                            if (mVlcVideoPlayerView.isPrepare()) {
                                                mRecordType = true;
                                                mHandler.sendEmptyMessage(RECORD_START);
                                                //vlcVideoView.getMediaPlayer().record(directory);
                                                LogUtils.e(TAG + "录像--开始录像:==1==="); //   /storage/emulated/0/1604026573438.mp4
                                                LogUtils.e(TAG + "录像--文件路径:=====" + mRecordDirectory);
                                                recordEvent.startRecord(mVlcVideoPlayerView.getMediaPlayer(), mRecordDirectory, "");
                                            }
                                        }
                                    }
                                } else {
                                    showToast("暂未开启直播");
                                }

                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                showToast("被永久拒绝授权，请手动授予存储权限");
                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.startPermissionActivity(mContext, permissions);
                            } else {
                                showToast("获取存储权限失败");
                            }
                        }
                    });
        } else {
            XXPermissions.with(mContext)
                    //  Permission.Group.STORAGE
                    .permission(Permission.Group.STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                //直播状态
                                if (mPlayStatueType == EnumConfig.PlayState.STATE_PLAY) {
                                    //截图,功能
                                    if ("shot".equals(type)) {
                                        if (mVlcVideoPlayerView.isPrepare()) {
                                            Media.VideoTrack mVideoTrack = mVlcVideoPlayerView.getVideoTrack();
                                            if (mVideoTrack != null) {
                                                showToast("截图成功");
                                                //原图
                                                LogUtils.e(TAG + "录像(截图的地址)====count：" + mTakeSnapshotFile.getAbsolutePath());
                                                File localFile = new File(mTakeSnapshotFile.getAbsolutePath());
                                                if (!localFile.exists()) {
                                                    localFile.mkdir();
                                                }
                                                recordEvent.takeSnapshot(mVlcVideoPlayerView.getMediaPlayer(), mTakeSnapshotFile.getAbsolutePath(), 0, 0);
                                                //插入相册01,有些设备刷新会出问题 01,02都行  VIVO能刷截图,华为能刷新
                                                MediaStore.Images.Media.insertImage(mContext.getContentResolver(), mVlcVideoPlayerView.getBitmap(), "", "");

//                                        MediaStore.Images.Media.insertImage(getContentResolver(), mVlcVideoView.getBitmap(), "", "");
                                                //刷新相册02,以下解决,(最好的效果)此问题在android 10.0 的版本上会出现。图库不刷新问题java.lang.IllegalStateException: Failed to build unique file
//                                            FileUtil.RefreshAlbum(mTakeSnapshotFile.getAbsolutePath(), false, mContext);
                                                //recordEvent.takeSnapshot(vlcVideoView.getMediaPlayer(), takeSnapshotFile.getAbsolutePath(), videoTrack.width / 2, 0);
                                            }
                                        }
                                    } else {
                                        //录像,功能
                                        if (mRecordType) {
                                            LogUtils.e(TAG + "录像--结束录像:==2==="); //   /storage/emulated/0/1604026573438.mp4
                                            vlcRecordOver();
                                        } else {
                                            if (mVlcVideoPlayerView.isPrepare()) {
                                                mRecordType = true;
                                                mHandler.sendEmptyMessage(RECORD_START);
                                                //vlcVideoView.getMediaPlayer().record(directory);
                                                LogUtils.e(TAG + "录像--开始录像:==1==="); //   /storage/emulated/0/1604026573438.mp4
                                                LogUtils.e(TAG + "录像--文件路径:=====" + mRecordDirectory);
                                                recordEvent.startRecord(mVlcVideoPlayerView.getMediaPlayer(), mRecordDirectory, "");
                                            }
                                        }
                                    }
                                } else {
                                    showToast("暂未开启直播");
                                }

                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                showToast("被永久拒绝授权，请手动授予存储权限");
                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.startPermissionActivity(mContext, permissions);
                            } else {
                                showToast("获取存储权限失败");
                            }
                        }
                    });
        }


    }


    /**
     * 录像结束
     */
    private void vlcRecordOver() {
        mRecordType = false;
        mHandler.sendEmptyMessage(RECORD_STOP);
        showToast(getResources().getString(R.string.vlc_toast09));
        mVlcVideoPlayerView.getMediaPlayer().record(null);
        FileUtil.RefreshAlbum(mRecordDirectory, true, mContext);
    }

    public void setTextColor(int color, String message) {
        mRightRecord.setText(message);
        mRightRecord.setTextColor(color);
    }

    /**
     * 设置播放器标题
     *
     * @param title
     */
    public void setPlayerTitle(String title) {
        this.mTitle = title;
        LogUtils.e(TAG + "设置的标题=" + mTitle);
        mTopTitle.setText(mTitle + "");
    }

    /**
     * 设置播放器url，做切换分辨率的时候使用
     *
     * @param urlA 地址1
     * @param urlB 地址2
     */
    public void setPlayerPath(String urlA, String urlB) {
        this.mPath01 = urlA;
        this.mPath02 = urlB;
        LogUtils.e(TAG + "传入的mPath01=" + mPath01);
        LogUtils.e(TAG + "传入的mPath02=" + mPath02);
    }


    public void onResume() {
        LogUtils.e(TAG + "==onResume");
    }

    public void onPause() {
        LogUtils.e(TAG + "==onPause");

        //手动清空字幕
        if (null != mVlcVideoPlayerView) {
            mVlcVideoPlayerView.setAddSlave(null);
            //直接调用stop 或者onPause(自己新增的方法),不然回ANR
            // vlcVideoView.onStop();
            mVlcVideoPlayerView.onPause();
        }
    }

    public void onStop() {
        LogUtils.e(TAG + "==onStop");
    }

    /**
     * 获取播放器
     * 可以控制播放器声音
     * mMediaPlayer.getVolume();
     * mMediaPlayer.setVolume(100);
     *
     * @return
     */
    public MediaPlayer getMediaPlayer() {
        return mVlcVideoPlayerView.getMediaPlayer();
    }

    public void onDestroy() {
        LogUtils.e(TAG + "==onDestroy");

        //手动清空字幕
        if (null != mVlcVideoPlayerView) {
            mVlcVideoPlayerView.setAddSlave(null);
            //直接调用stop 不然回ANR
            mVlcVideoPlayerView.onStop();
            mVlcVideoPlayerView.onDestroy();
        }
        if (null != mVlcVideoPlayerView) {
            mVlcVideoPlayerView.setMediaListenerEvent(null);
        }
        if (null != mPlayerTimeDis) {
            mPlayerTimeDis.dispose();
            mPlayerTimeDis = null;
        }
        //录像状态恢复默认值
        mRecordType = false;
        mRelativeAll.removeCallbacks(mShowControllerRunnable);
        mRelativeAll.removeCallbacks(mHideControllerRunnable);
        mRelativeAll.removeAllViews();
    }

    /**
     * 开始直播
     *
     * @param path
     */
    public void setStartLive(String path) {
        handlerMsgShowHidePlayLoadingView(EnumConfig.PlayerState.PLAYER_SHOW_LOADING_VIEW);
        startLive(path);

    }

    private void startLive(String path) {
        mVlcVideoPlayerView.setPath(path);
        mVlcVideoPlayerView.startPlay();


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
        ObjectAnimator.ofFloat(mControlRightLayout, "translationX", mControlRightLayout.getWidth() + getResources().getDimension(R.dimen.padding_5), 0).start();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            mControlLeftLayout.setAlpha(alpha);

            if ((int) alpha != 1) {
                return;
            }
            if (mControlLeftLayout.getVisibility() == INVISIBLE) {
                mControlLeftLayout.setVisibility(VISIBLE);
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
        ObjectAnimator.ofFloat(mControlRightLayout, "translationX", 0, mControlRightLayout.getWidth() + getResources().getDimension(R.dimen.padding_5)).start();
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            mControlLeftLayout.setAlpha(alpha);
            if (alpha != 0f) {
                return;
            }

            if (mControlLeftLayout.getVisibility() == VISIBLE) {
                mControlLeftLayout.setVisibility(INVISIBLE);
            }
        });
        animator.start();
    }


}