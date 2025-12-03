package com.company.shenzhou.player.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.company.shenzhou.R;
import com.company.shenzhou.player.core.PlayerState;
import com.company.shenzhou.player.core.PlayerType;
import com.company.shenzhou.player.core.VideoPlayerManager;
import com.company.shenzhou.player.listener.PlayerStateListener;
import com.company.shenzhou.player.widget.ENDownloadView;
import com.company.shenzhou.player.widget.ENPlayView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 视频控制器，四个方向控制栏 + 滑动动画
 */
public class VideoController extends FrameLayout implements IController, PlayerStateListener {

    private static final int DEFAULT_HIDE_TIMEOUT = 5000;
    private static final int PROGRESS_UPDATE_INTERVAL = 1000;
    private static final int ANIMATION_DURATION = 300;

    private VideoPlayerManager mPlayerManager;
    private Handler mHandler;
    private Context mContext;
    private ExecutorService mSaveExecutor;

    // 四个方向的控制栏
    private View mTopBar;
    private View mBottomBar;
    private View mLeftBar;
    private View mRightBar;
    private android.widget.LinearLayout mCustomButtonsContainer;

    // 控件
    private ImageView mBackBtn;
    private TextView mTitleTv;
    private ImageView mPlayPauseBtn;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private ImageView mMuteBtn;
    private ImageView mFullscreenBtn;
    private ImageView mLockBtn;
    private ImageView mRecordBtn;
    private ImageView mSnapshotBtn;
    private ENDownloadView mLoadingView;
    private ENPlayView mErrorPlayView;
    
    // 手势调节指示器
    private View mGestureIndicator;
    private ImageView mGestureIcon;
    private TextView mGesturePercent;
    private com.company.shenzhou.player.widget.ArcProgressView mArcProgress;

    private boolean mIsShowing = false;
    private boolean mIsLocked = false;
    private boolean mIsDragging = false;
    private boolean mIsAnimating = false;
    private boolean mIsLockBarShowing = false;  // 锁定状态下左侧栏是否显示
    private boolean mIsFullscreen = false;      // 是否全屏
    private boolean mIsMuted = false;           // 是否静音
    private boolean mIsLiveStream = false;      // 是否是直播流（无法获取时长）
    private boolean mIsSeeking = false;         // 是否正在 seek 中
    private long mSeekTargetPosition = -1;      // seek 目标位置
    private long mSeekStartTime = 0;            // seek 开始时间
    private static final long SEEK_TIMEOUT = 2000; // seek 超时时间（毫秒）

    // 可自定义的图标资源ID
    private int mPlayIconResId = R.drawable.ic_player_play;
    private int mPauseIconResId = R.drawable.ic_player_pause;
    private int mFullscreenEnterIconResId = R.drawable.ic_player_fullscreen;
    private int mFullscreenExitIconResId = R.drawable.ic_player_fullscreen_exit;
    private int mMuteIconResId = R.drawable.ic_player_volume_off;
    private int mUnmuteIconResId = R.drawable.ic_player_volume_on;
    private int mLockIconResId = R.drawable.ic_player_lock;
    private int mUnlockIconResId = R.drawable.ic_player_unlock;
    private int mRecordNormalIconResId = R.drawable.ic_player_record;
    private int mRecordActiveIconResId = R.drawable.ic_player_record_active;
    
    // 手势控制相关
    private static final int GESTURE_NONE = 0;
    private static final int GESTURE_BRIGHTNESS = 1;
    private static final int GESTURE_VOLUME = 2;
    private int mCurrentGesture = GESTURE_NONE;
    private float mDownX, mDownY;
    private float mBrightness = -1f;
    private int mVolume = -1;
    private int mMaxVolume;
    private AudioManager mAudioManager;
    private static final int GESTURE_THRESHOLD = 50; // 手势触发阈值

    // 回调
    private OnBackClickListener mOnBackClickListener;
    private OnFullscreenClickListener mOnFullscreenClickListener;
    private OnRecordClickListener mOnRecordClickListener;
    private OnSnapshotClickListener mOnSnapshotClickListener;

    // 保存路径
    private String mRecordDirectory;
    private String mSnapshotDirectory;

    public VideoController(Context context) {
        this(context, null);
    }

    public VideoController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        mHandler = new Handler(Looper.getMainLooper());
        mSaveExecutor = Executors.newSingleThreadExecutor();
        
        // 初始化音频管理器
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            mRecordDirectory = externalDir.getAbsolutePath() + "/records";
            mSnapshotDirectory = externalDir.getAbsolutePath() + "/snapshots";
        }

        LayoutInflater.from(context).inflate(R.layout.layout_video_controller, this, true);
        initViews();
        initListeners();
        hideImmediately();
    }

    private void initViews() {
        mTopBar = findViewById(R.id.controller_top_bar);
        mBottomBar = findViewById(R.id.controller_bottom_bar);
        mLeftBar = findViewById(R.id.controller_left_bar);
        mRightBar = findViewById(R.id.controller_right_bar);
        mCustomButtonsContainer = findViewById(R.id.custom_buttons_container);

        mBackBtn = findViewById(R.id.btn_back);
        mTitleTv = findViewById(R.id.tv_title);
        mPlayPauseBtn = findViewById(R.id.btn_play_pause);
        mSeekBar = findViewById(R.id.seek_bar);
        mCurrentTime = findViewById(R.id.tv_current_time);
        mTotalTime = findViewById(R.id.tv_total_time);
        mMuteBtn = findViewById(R.id.btn_mute);
        mFullscreenBtn = findViewById(R.id.btn_fullscreen);
        mLockBtn = findViewById(R.id.btn_lock);
        mRecordBtn = findViewById(R.id.btn_record);
        mSnapshotBtn = findViewById(R.id.btn_snapshot);
        mLoadingView = findViewById(R.id.loading_view);
        mErrorPlayView = findViewById(R.id.error_play_view);
        
        // 手势调节指示器
        mGestureIndicator = findViewById(R.id.gesture_indicator);
        mGestureIcon = findViewById(R.id.iv_gesture_icon);
        mGesturePercent = findViewById(R.id.tv_gesture_percent);
        mArcProgress = findViewById(R.id.arc_progress);
    }

    private void initListeners() {
        // 返回按钮
        if (mBackBtn != null) {
            mBackBtn.setOnClickListener(v -> {
                if (mOnBackClickListener != null) {
                    mOnBackClickListener.onBackClick();
                }
            });
        }

        // 播放/暂停
        if (mPlayPauseBtn != null) {
            mPlayPauseBtn.setOnClickListener(v -> togglePlayPause());
        }

        // 进度条
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mPlayerManager != null) {
                        long duration = mPlayerManager.getDuration();
                        long position = duration * progress / 100;
                        mCurrentTime.setText(formatTime(position));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mIsDragging = true;
                    removeHideRunnable();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mPlayerManager != null) {
                        long duration = mPlayerManager.getDuration();
                        // 使用 isSeekable() 判断是否支持进度拖动
                        // 对于点播流（有时长且可 seek），执行 seek 操作
                        if (duration > 0 && mPlayerManager.isSeekable()) {
                            long position = duration * seekBar.getProgress() / 100;
                            android.util.Log.d("VideoController", "onStopTrackingTouch: seeking to " + position +
                                    ", duration=" + duration + ", progress=" + seekBar.getProgress());
                            
                            // 记录 seek 目标位置和时间，防止进度条回弹
                            mSeekTargetPosition = position;
                            mSeekStartTime = System.currentTimeMillis();
                            mIsSeeking = true;
                            
                            mPlayerManager.seekTo(position);
                            
                            // 延迟恢复进度更新，给 VLC 时间完成 seek
                            // HLS 流可能需要更长时间，使用 1500ms
                            mHandler.postDelayed(() -> {
                                mIsDragging = false;
                                // 检查 seek 是否已经完成（位置接近目标位置）
                                checkSeekComplete();
                            }, 1500);
                        } else {
                            android.util.Log.d("VideoController", "onStopTrackingTouch: seek not supported, duration=" +
                                    duration + ", isSeekable=" + (mPlayerManager != null ? mPlayerManager.isSeekable() : false));
                            mIsDragging = false;
                        }
                    } else {
                        mIsDragging = false;
                    }
                    postHideRunnable();
                }
            });
        }

        // 全屏按钮
        if (mFullscreenBtn != null) {
            mFullscreenBtn.setOnClickListener(v -> {
                toggleFullscreen();
                if (mOnFullscreenClickListener != null) {
                    mOnFullscreenClickListener.onFullscreenClick();
                }
            });
        }

        // 锁屏按钮
        if (mLockBtn != null) {
            mLockBtn.setOnClickListener(v -> toggleLock());
        }

        // 静音按钮
        if (mMuteBtn != null) {
            mMuteBtn.setOnClickListener(v -> toggleMute());
        }

        // 录像按钮
        if (mRecordBtn != null) {
            mRecordBtn.setOnClickListener(v -> onRecordClick());
        }

        // 截图按钮
        if (mSnapshotBtn != null) {
            mSnapshotBtn.setOnClickListener(v -> onSnapshotClick());
        }

        // 错误重试按钮
        if (mErrorPlayView != null) {
            mErrorPlayView.setOnClickListener(v -> onRetryClick());
        }

        // 触摸事件在 onTouchEvent 中处理
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 锁定状态下只响应点击
        if (mIsLocked) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                toggleVisibility();
            }
            return true;
        }

        float x = event.getX();
        float y = event.getY();
        int width = getWidth();
        int height = getHeight();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mDownY = y;
                mCurrentGesture = GESTURE_NONE;
                // 记录当前亮度和音量
                mBrightness = getActivityBrightness();
                mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = x - mDownX;
                float deltaY = y - mDownY;

                // 判断手势类型
                if (mCurrentGesture == GESTURE_NONE) {
                    if (Math.abs(deltaY) > GESTURE_THRESHOLD && Math.abs(deltaY) > Math.abs(deltaX)) {
                        // 垂直滑动
                        if (mDownX < width / 2) {
                            // 左侧：亮度
                            mCurrentGesture = GESTURE_BRIGHTNESS;
                        } else {
                            // 右侧：音量
                            mCurrentGesture = GESTURE_VOLUME;
                        }
                    }
                }

                // 处理手势
                if (mCurrentGesture == GESTURE_BRIGHTNESS) {
                    handleBrightnessGesture(deltaY, height);
                } else if (mCurrentGesture == GESTURE_VOLUME) {
                    handleVolumeGesture(deltaY, height);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrentGesture == GESTURE_NONE) {
                    // 没有手势，当作点击处理
                    if (!mIsAnimating) {
                        toggleVisibility();
                    }
                } else {
                    // 隐藏指示器
                    hideGestureIndicator();
                }
                mCurrentGesture = GESTURE_NONE;
                break;
        }
        return true;
    }

    /**
     * 处理亮度手势
     */
    private void handleBrightnessGesture(float deltaY, int height) {
        float percent = -deltaY / height;
        float newBrightness = mBrightness + percent;
        newBrightness = Math.max(0.01f, Math.min(1f, newBrightness));

        // 设置亮度
        Activity activity = getActivity();
        if (activity != null) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.screenBrightness = newBrightness;
            activity.getWindow().setAttributes(lp);
        }

        // 显示指示器
        showGestureIndicator(true, (int) (newBrightness * 100));
    }

    /**
     * 处理音量手势
     */
    private void handleVolumeGesture(float deltaY, int height) {
        float percent = -deltaY / height;
        int newVolume = (int) (mVolume + percent * mMaxVolume);
        newVolume = Math.max(0, Math.min(mMaxVolume, newVolume));

        // 设置音量
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);

        // 显示指示器
        showGestureIndicator(false, newVolume * 100 / mMaxVolume);
    }

    /**
     * 显示手势指示器
     */
    private void showGestureIndicator(boolean isBrightness, int progress) {
        if (mGestureIndicator != null) {
            mGestureIndicator.setVisibility(VISIBLE);
        }
        if (mGestureIcon != null) {
            mGestureIcon.setImageResource(isBrightness ?
                    R.drawable.ic_player_brightness : R.drawable.ic_player_volume_on);
        }
        if (mGesturePercent != null) {
            mGesturePercent.setText(progress + "%");
        }
        if (mArcProgress != null) {
            mArcProgress.setProgress(progress);
        }
    }

    /**
     * 隐藏手势指示器
     */
    private void hideGestureIndicator() {
        if (mGestureIndicator != null) {
            mGestureIndicator.setVisibility(GONE);
        }
    }

    /**
     * 获取当前Activity亮度
     */
    private float getActivityBrightness() {
        Activity activity = getActivity();
        if (activity != null) {
            float brightness = activity.getWindow().getAttributes().screenBrightness;
            if (brightness < 0) {
                // 使用系统亮度
                try {
                    brightness = Settings.System.getInt(mContext.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS) / 255f;
                } catch (Settings.SettingNotFoundException e) {
                    brightness = 0.5f;
                }
            }
            return brightness;
        }
        return 0.5f;
    }

    /**
     * 获取Activity
     */
    private Activity getActivity() {
        Context context = mContext;
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    /**
     * 重试播放
     */
    private void onRetryClick() {
        if (mPlayerManager != null) {
            hideError();
            showLoading();
            mPlayerManager.retry();
        }
    }

    // ==================== 动画控制 ====================

    @Override
    public void show() {
        if (mIsAnimating) return;
        
        // 锁定状态：只显示左侧锁屏按钮
        if (mIsLocked) {
            showLockBarOnly();
            return;
        }
        
        if (mIsShowing) return;
        mIsShowing = true;
        mIsAnimating = true;

        // 先设置可见
        setAllBarsVisible(true);

        // 顶部从上方滑入
        if (mTopBar != null) {
            mTopBar.setTranslationY(-mTopBar.getHeight());
            mTopBar.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
        }

        // 底部从下方滑入
        if (mBottomBar != null) {
            mBottomBar.setTranslationY(mBottomBar.getHeight());
            mBottomBar.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
        }

        // 左侧从左方滑入
        if (mLeftBar != null) {
            mLeftBar.setTranslationX(-mLeftBar.getWidth());
            mLeftBar.animate().translationX(0).setDuration(ANIMATION_DURATION).start();
        }

        // 右侧从右方滑入
        if (mRightBar != null) {
            mRightBar.setTranslationX(mRightBar.getWidth());
            mRightBar.animate().translationX(0).setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsAnimating = false;
                        }
                    }).start();
        } else {
            mHandler.postDelayed(() -> mIsAnimating = false, ANIMATION_DURATION);
        }

        updateProgress();
        startProgressUpdate();
        postHideRunnable();
    }

    @Override
    public void hide() {
        if (mIsAnimating) return;
        
        // 锁定状态：只隐藏左侧锁屏按钮
        if (mIsLocked) {
            hideLockBarOnly();
            return;
        }
        
        if (!mIsShowing) return;
        mIsAnimating = true;

        // 顶部向上滑出
        if (mTopBar != null) {
            mTopBar.animate().translationY(-mTopBar.getHeight()).setDuration(ANIMATION_DURATION).start();
        }

        // 底部向下滑出
        if (mBottomBar != null) {
            mBottomBar.animate().translationY(mBottomBar.getHeight()).setDuration(ANIMATION_DURATION).start();
        }

        // 左侧向左滑出
        if (mLeftBar != null) {
            mLeftBar.animate().translationX(-mLeftBar.getWidth()).setDuration(ANIMATION_DURATION).start();
        }

        // 右侧向右滑出
        if (mRightBar != null) {
            mRightBar.animate().translationX(mRightBar.getWidth()).setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsShowing = false;
                            mIsAnimating = false;
                            setAllBarsVisible(false);
                        }
                    }).start();
        } else {
            mHandler.postDelayed(() -> {
                mIsShowing = false;
                mIsAnimating = false;
                setAllBarsVisible(false);
            }, ANIMATION_DURATION);
        }

        stopProgressUpdate();
        removeHideRunnable();
    }
    
    /**
     * 锁定状态下只显示左侧锁屏按钮
     */
    private void showLockBarOnly() {
        if (mIsLockBarShowing) return;
        mIsLockBarShowing = true;
        mIsAnimating = true;
        
        if (mLeftBar != null) {
            mLeftBar.setVisibility(VISIBLE);
            mLeftBar.setTranslationX(-mLeftBar.getWidth());
            mLeftBar.animate().translationX(0).setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsAnimating = false;
                        }
                    }).start();
        } else {
            mHandler.postDelayed(() -> mIsAnimating = false, ANIMATION_DURATION);
        }
        
        postHideRunnable();
    }
    
    /**
     * 锁定状态下只隐藏左侧锁屏按钮
     */
    private void hideLockBarOnly() {
        if (!mIsLockBarShowing) return;
        mIsAnimating = true;
        
        if (mLeftBar != null) {
            mLeftBar.animate().translationX(-mLeftBar.getWidth()).setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsLockBarShowing = false;
                            mIsAnimating = false;
                            mLeftBar.setVisibility(INVISIBLE);
                        }
                    }).start();
        } else {
            mHandler.postDelayed(() -> {
                mIsLockBarShowing = false;
                mIsAnimating = false;
            }, ANIMATION_DURATION);
        }
        
        removeHideRunnable();
    }

    /**
     * 立即隐藏（无动画）
     */
    private void hideImmediately() {
        mIsShowing = false;
        setAllBarsVisible(false);
        if (mTopBar != null) mTopBar.setTranslationY(-200);
        if (mBottomBar != null) mBottomBar.setTranslationY(200);
        if (mLeftBar != null) mLeftBar.setTranslationX(-200);
        if (mRightBar != null) mRightBar.setTranslationX(200);
    }

    private void setAllBarsVisible(boolean visible) {
        int visibility = visible ? VISIBLE : INVISIBLE;
        if (mTopBar != null) mTopBar.setVisibility(visibility);
        if (mBottomBar != null) mBottomBar.setVisibility(visibility);
        if (mLeftBar != null) mLeftBar.setVisibility(visibility);
        if (mRightBar != null) mRightBar.setVisibility(visibility);
    }

    // ==================== 锁屏功能 ====================

    private void toggleLock() {
        mIsLocked = !mIsLocked;
        updateLockButton();

        if (mIsLocked) {
            // 锁定时：隐藏上、下、右栏，只保留左侧锁屏按钮
            if (mTopBar != null) {
                mTopBar.animate().translationY(-mTopBar.getHeight()).setDuration(ANIMATION_DURATION).start();
            }
            if (mBottomBar != null) {
                mBottomBar.animate().translationY(mBottomBar.getHeight()).setDuration(ANIMATION_DURATION).start();
            }
            if (mRightBar != null) {
                mRightBar.animate().translationX(mRightBar.getWidth()).setDuration(ANIMATION_DURATION).start();
            }
            // 左侧栏保持显示
            mIsShowing = false;
            mIsLockBarShowing = true;
            stopProgressUpdate();
        } else {
            // 解锁时：显示所有控制栏
            mIsLockBarShowing = false;
            mIsShowing = true;
            
            if (mTopBar != null) {
                mTopBar.setVisibility(VISIBLE);
                mTopBar.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
            }
            if (mBottomBar != null) {
                mBottomBar.setVisibility(VISIBLE);
                mBottomBar.animate().translationY(0).setDuration(ANIMATION_DURATION).start();
            }
            if (mRightBar != null) {
                mRightBar.setVisibility(VISIBLE);
                mRightBar.animate().translationX(0).setDuration(ANIMATION_DURATION).start();
            }
            // 左侧栏已经显示
            
            updateProgress();
            startProgressUpdate();
        }
        postHideRunnable();
    }

    private void updateLockButton() {
        if (mLockBtn != null) {
            mLockBtn.setImageResource(mIsLocked ? mLockIconResId : mUnlockIconResId);
        }
    }
    
    // ==================== 全屏功能 ====================
    
    /**
     * 切换全屏状态
     */
    private void toggleFullscreen() {
        mIsFullscreen = !mIsFullscreen;
        updateFullscreenButton();
    }
    
    /**
     * 更新全屏按钮图标
     */
    private void updateFullscreenButton() {
        if (mFullscreenBtn != null) {
            mFullscreenBtn.setImageResource(mIsFullscreen ? 
                    mFullscreenExitIconResId : mFullscreenEnterIconResId);
        }
    }
    
    /**
     * 设置全屏状态（外部调用）
     */
    public void setFullscreen(boolean fullscreen) {
        mIsFullscreen = fullscreen;
        updateFullscreenButton();
    }
    
    /**
     * 是否全屏
     */
    public boolean isFullscreen() {
        return mIsFullscreen;
    }

    // ==================== 静音功能 ====================

    /**
     * 切换静音状态
     */
    private void toggleMute() {
        mIsMuted = !mIsMuted;
        updateMuteButton();
        if (mPlayerManager != null) {
            if (mIsMuted) {
                mPlayerManager.setVolume(0f, 0f);
            } else {
                mPlayerManager.setVolume(1f, 1f);
            }
        }
    }

    /**
     * 更新静音按钮图标
     */
    private void updateMuteButton() {
        if (mMuteBtn != null) {
            mMuteBtn.setImageResource(mIsMuted ? mMuteIconResId : mUnmuteIconResId);
        }
    }

    /**
     * 设置静音状态（外部调用）
     */
    public void setMuted(boolean muted) {
        if (mIsMuted != muted) {
            mIsMuted = muted;
            updateMuteButton();
            if (mPlayerManager != null) {
                if (mIsMuted) {
                    mPlayerManager.setVolume(0f, 0f);
                } else {
                    mPlayerManager.setVolume(1f, 1f);
                }
            }
        }
    }

    /**
     * 是否静音
     */
    public boolean isMuted() {
        return mIsMuted;
    }

    @Override
    public void setLocked(boolean locked) {
        if (mIsLocked != locked) {
            mIsLocked = locked;
            updateLockButton();
        }
    }

    @Override
    public boolean isLocked() {
        return mIsLocked;
    }

    @Override
    public boolean isShowing() {
        return mIsShowing;
    }

    // ==================== 播放器绑定 ====================

    @Override
    public void bindPlayerManager(VideoPlayerManager playerManager) {
        mPlayerManager = playerManager;
        mPlayerManager.addStateListener(this);
        updateRecordButtonVisibility();
    }

    public void updateRecordButtonVisibility() {
        if (mPlayerManager == null) {
            hideRecordButtons();
            return;
        }

        if (mPlayerManager.getPlayerType() == PlayerType.VLC) {
            if (mPlayerManager.isSupportRecord() && mRecordBtn != null) {
                mRecordBtn.setVisibility(VISIBLE);
            }
            if (mPlayerManager.isSupportSnapshot() && mSnapshotBtn != null) {
                mSnapshotBtn.setVisibility(VISIBLE);
            }
        } else {
            hideRecordButtons();
        }
    }

    private void hideRecordButtons() {
        if (mRecordBtn != null) mRecordBtn.setVisibility(GONE);
        if (mSnapshotBtn != null) mSnapshotBtn.setVisibility(GONE);
    }

    // ==================== 录像截图功能 ====================

    private void onRecordClick() {
        if (mPlayerManager == null || !mPlayerManager.isSupportRecord()) {
            showToast("当前播放器不支持录像功能");
            return;
        }
        if (mOnRecordClickListener != null) {
            mOnRecordClickListener.onRecordClick(mPlayerManager.isRecording());
            return;
        }
        requestStoragePermission("record");
    }

    private void onSnapshotClick() {
        if (mPlayerManager == null || !mPlayerManager.isSupportSnapshot()) {
            showToast("当前播放器不支持截图功能");
            return;
        }
        if (mOnSnapshotClickListener != null) {
            mOnSnapshotClickListener.onSnapshotClick();
            return;
        }
        requestStoragePermission("snapshot");
    }

    private void requestStoragePermission(String type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            XXPermissions.with(mContext)
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) executeMediaOperation(type);
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                showToast("被永久拒绝授权，请手动授予存储权限");
                                XXPermissions.startPermissionActivity(mContext, permissions);
                            } else {
                                showToast("获取存储权限失败");
                            }
                        }
                    });
        } else {
            XXPermissions.with(mContext)
                    .permission(Permission.Group.STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) executeMediaOperation(type);
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                showToast("被永久拒绝授权，请手动授予存储权限");
                                XXPermissions.startPermissionActivity(mContext, permissions);
                            } else {
                                showToast("获取存储权限失败");
                            }
                        }
                    });
        }
    }

    private void executeMediaOperation(String type) {
        if ("record".equals(type)) {
            doRecord();
        } else if ("snapshot".equals(type)) {
            doSnapshot();
        }
    }

    private void doRecord() {
        if (mPlayerManager.isRecording()) {
            boolean result = mPlayerManager.stopRecord();
            if (result) {
                updateRecordButton(false);
                String recordFilePath = mPlayerManager.getRecordFilePath();
                saveVideoToGallery(recordFilePath);
            } else {
                showToast("停止录像失败");
            }
        } else {
            File dir = new File(mRecordDirectory);
            if (!dir.exists()) dir.mkdirs();
            String fileName = generateFileName("REC");
            boolean result = mPlayerManager.startRecord(mRecordDirectory, fileName);
            if (result) {
                updateRecordButton(true);
                showToast("开始录像");
            } else {
                showToast("录像启动失败");
            }
        }
    }

    private void doSnapshot() {
        String fileName = generateFileName("IMG") + ".jpg";
        String filePath = mSnapshotDirectory + "/" + fileName;
        File dir = new File(mSnapshotDirectory);
        if (!dir.exists()) dir.mkdirs();

        boolean result = mPlayerManager.takeSnapshot(filePath);
        if (result) {
            showToast("截图成功");
            saveImageToGallery(filePath);
        } else {
            showToast("截图失败");
        }
    }

    public void updateRecordButton(boolean isRecording) {
        if (mRecordBtn != null) {
            mRecordBtn.setImageResource(isRecording ? mRecordActiveIconResId : mRecordNormalIconResId);
        }
    }

    private void saveImageToGallery(String filePath) {
        mSaveExecutor.execute(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists()) return;

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/VideoPlayer");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                }

                Uri uri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = mContext.getContentResolver().openOutputStream(uri);
                         FileInputStream in = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        mContext.getContentResolver().update(uri, values, null, null);
                    }
                    mHandler.post(() -> showToast("截图已保存到相册"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void saveVideoToGallery(String filePath) {
        if (filePath == null) {
            showToast("录像文件路径为空");
            return;
        }
        showToast("正在保存录像...");

        mSaveExecutor.execute(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    mHandler.post(() -> showToast("录像文件不存在"));
                    return;
                }

                String mimeType = "video/mpeg";
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".ts")) mimeType = "video/mp2t";
                else if (fileName.endsWith(".mp4")) mimeType = "video/mp4";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DISPLAY_NAME, file.getName());
                values.put(MediaStore.Video.Media.MIME_TYPE, mimeType);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/VideoPlayer");
                    values.put(MediaStore.Video.Media.IS_PENDING, 1);
                }

                Uri uri = mContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = mContext.getContentResolver().openOutputStream(uri);
                         FileInputStream in = new FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Video.Media.IS_PENDING, 0);
                        mContext.getContentResolver().update(uri, values, null, null);
                    }
                    mHandler.post(() -> showToast("录像已保存到相册"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.post(() -> showToast("保存录像异常"));
            }
        });
    }

    // ==================== 状态监听 ====================

    @Override
    public void onStateChanged(PlayerState state) {
        mHandler.post(() -> {
            switch (state) {
                case PREPARING:
                case BUFFERING:
                    showLoading();
                    break;
                case PREPARED:
                    hideLoading();
                    updatePlayPauseButton(true);
                    updateRecordButtonVisibility();
                    break;
                case PLAYING:
                    hideLoading();
                    updatePlayPauseButton(true);
                    break;
                case PAUSED:
                    hideLoading();
                    updatePlayPauseButton(false);
                    break;
                case COMPLETED:
                case STOPPED:
                    hideLoading();
                    hideError();
                    updatePlayPauseButton(false);
                    if (mPlayerManager != null && mPlayerManager.isRecording()) {
                        mPlayerManager.stopRecord();
                        updateRecordButton(false);
                    }
                    show();
                    break;
                case ERROR:
                    hideLoading();
                    showError();
                    updatePlayPauseButton(false);
                    if (mPlayerManager != null && mPlayerManager.isRecording()) {
                        mPlayerManager.stopRecord();
                        updateRecordButton(false);
                    }
                    break;
            }
        });
    }

    @Override
    public void updateProgress() {
        if (mPlayerManager == null || mIsDragging) return;

        long position = mPlayerManager.getCurrentPosition();
        long duration = mPlayerManager.getDuration();
        int bufferPercent = mPlayerManager.getBufferPercentage();
        boolean isSeekable = mPlayerManager.isSeekable();

        // 检测是否是直播流（duration <= 0 或不可 seek 表示直播）
        boolean isLive = duration <= 0 || !isSeekable;
        if (isLive != mIsLiveStream) {
            mIsLiveStream = isLive;
            updateLiveStreamUI();
        }

        // 如果正在 seek 中，使用目标位置而不是实际位置，防止进度条回弹
        if (mIsSeeking && mSeekTargetPosition >= 0) {
            // 检查是否超时
            if (System.currentTimeMillis() - mSeekStartTime > SEEK_TIMEOUT) {
                // 超时，重置 seek 状态
                mIsSeeking = false;
                mSeekTargetPosition = -1;
                android.util.Log.d("VideoController", "updateProgress: seek timeout, reset seek state");
            } else {
                // 检查实际位置是否已经接近目标位置（误差在 2 秒内）
                if (Math.abs(position - mSeekTargetPosition) < 2000) {
                    // seek 完成
                    mIsSeeking = false;
                    mSeekTargetPosition = -1;
                    android.util.Log.d("VideoController", "updateProgress: seek complete, position=" + position);
                } else {
                    // 仍在 seek 中，使用目标位置
                    position = mSeekTargetPosition;
                    android.util.Log.d("VideoController", "updateProgress: still seeking, use target position=" + position);
                }
            }
        }

        if (!mIsLiveStream && mSeekBar != null && duration > 0) {
            int progress = (int) (position * 100 / duration);
            mSeekBar.setProgress(progress);
            mSeekBar.setSecondaryProgress(bufferPercent);
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(mIsLiveStream ? "直播中" : formatTime(position));
        }
        if (mTotalTime != null) {
            mTotalTime.setVisibility(mIsLiveStream ? GONE : VISIBLE);
            if (!mIsLiveStream) {
                mTotalTime.setText(formatTime(duration));
            }
        }
    }

    /**
     * 检查 seek 是否完成
     */
    private void checkSeekComplete() {
        if (!mIsSeeking || mPlayerManager == null) return;
        
        long currentPosition = mPlayerManager.getCurrentPosition();
        // 如果当前位置接近目标位置（误差在 3 秒内），认为 seek 完成
        if (mSeekTargetPosition >= 0 && Math.abs(currentPosition - mSeekTargetPosition) < 3000) {
            mIsSeeking = false;
            mSeekTargetPosition = -1;
            android.util.Log.d("VideoController", "checkSeekComplete: seek complete, position=" + currentPosition);
        } else {
            // 如果还没完成，继续等待
            android.util.Log.d("VideoController", "checkSeekComplete: still seeking, current=" + currentPosition + 
                    ", target=" + mSeekTargetPosition);
            // 再等待 500ms 后再次检查
            mHandler.postDelayed(this::checkSeekComplete, 500);
        }
    }

    /**
     * 更新直播流/点播流的 UI 显示
     * 直播流：隐藏进度条和时间
     * 点播流：显示进度条和时间
     */
    private void updateLiveStreamUI() {
        if (mSeekBar != null) {
            mSeekBar.setVisibility(mIsLiveStream ? GONE : VISIBLE);
            mSeekBar.setEnabled(!mIsLiveStream);
        }
        if (mTotalTime != null) {
            mTotalTime.setVisibility(mIsLiveStream ? GONE : VISIBLE);
        }
    }

    /**
     * 是否是直播流
     */
    public boolean isLiveStream() {
        return mIsLiveStream;
    }

    @Override
    public void release() {
        if (mPlayerManager != null && mPlayerManager.isRecording()) {
            mPlayerManager.stopRecord();
        }
        if (mPlayerManager != null) {
            mPlayerManager.removeStateListener(this);
        }
        stopProgressUpdate();
        removeHideRunnable();
        mHandler.removeCallbacksAndMessages(null);
        if (mSaveExecutor != null && !mSaveExecutor.isShutdown()) {
            mSaveExecutor.shutdown();
        }
    }

    // ==================== 辅助方法 ====================

    private void togglePlayPause() {
        if (mPlayerManager == null) return;
        if (mPlayerManager.isPlaying()) {
            mPlayerManager.pause();
        } else {
            mPlayerManager.start();
        }
    }

    private void toggleVisibility() {
        if (mIsLocked) {
            // 锁定状态：只切换左侧锁屏栏
            if (mIsLockBarShowing) {
                hideLockBarOnly();
            } else {
                showLockBarOnly();
            }
        } else {
            // 非锁定状态：切换所有控制栏
            if (mIsShowing) {
                hide();
            } else {
                show();
            }
        }
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        if (mPlayPauseBtn != null) {
            mPlayPauseBtn.setImageResource(isPlaying ? mPauseIconResId : mPlayIconResId);
        }
    }

    private void showLoading() {
        hideError();
        if (mLoadingView != null) {
            mLoadingView.setVisibility(VISIBLE);
            mLoadingView.start();
        }
    }

    private void hideLoading() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(INVISIBLE);
            mLoadingView.release();
        }
    }

    private void showError() {
        hideLoading();
        if (mErrorPlayView != null) {
            mErrorPlayView.setVisibility(VISIBLE);
        }
    }

    private void hideError() {
        if (mErrorPlayView != null) {
            mErrorPlayView.setVisibility(INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private String formatTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private String generateFileName(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return prefix + "_" + sdf.format(new Date());
    }

    // ==================== 定时任务 ====================

    private final Runnable mHideRunnable = this::hide;

    private final Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            if (mIsShowing && mPlayerManager != null && mPlayerManager.isPlaying()) {
                mHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL);
            }
        }
    };

    private void postHideRunnable() {
        removeHideRunnable();
        mHandler.postDelayed(mHideRunnable, DEFAULT_HIDE_TIMEOUT);
    }

    private void removeHideRunnable() {
        mHandler.removeCallbacks(mHideRunnable);
    }

    private void startProgressUpdate() {
        mHandler.removeCallbacks(mProgressRunnable);
        mHandler.post(mProgressRunnable);
    }

    private void stopProgressUpdate() {
        mHandler.removeCallbacks(mProgressRunnable);
    }

    // ==================== 设置方法 ====================

    public void setTitle(String title) {
        if (mTitleTv != null) mTitleTv.setText(title);
    }

    public void setRecordDirectory(String directory) {
        mRecordDirectory = directory;
    }

    public void setSnapshotDirectory(String directory) {
        mSnapshotDirectory = directory;
    }

    public void setOnBackClickListener(OnBackClickListener listener) {
        mOnBackClickListener = listener;
    }

    public void setOnFullscreenClickListener(OnFullscreenClickListener listener) {
        mOnFullscreenClickListener = listener;
    }

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        mOnRecordClickListener = listener;
    }

    public void setOnSnapshotClickListener(OnSnapshotClickListener listener) {
        mOnSnapshotClickListener = listener;
    }

    // ==================== 图标自定义接口 ====================

    /**
     * 设置返回按钮图标
     */
    public void setBackIcon(int resId) {
        if (mBackBtn != null) mBackBtn.setImageResource(resId);
    }

    /**
     * 设置返回按钮可见性
     */
    public void setBackVisible(boolean visible) {
        if (mBackBtn != null) mBackBtn.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置播放/暂停按钮图标
     */
    public void setPlayPauseIcon(int playResId, int pauseResId) {
        mPlayIconResId = playResId;
        mPauseIconResId = pauseResId;
        updatePlayPauseButton(mPlayerManager != null && mPlayerManager.isPlaying());
    }

    /**
     * 设置播放/暂停按钮可见性
     */
    public void setPlayPauseVisible(boolean visible) {
        if (mPlayPauseBtn != null) mPlayPauseBtn.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置全屏按钮图标
     */
    public void setFullscreenIcon(int enterResId, int exitResId) {
        mFullscreenEnterIconResId = enterResId;
        mFullscreenExitIconResId = exitResId;
        updateFullscreenButton();
    }

    /**
     * 设置全屏按钮可见性
     */
    public void setFullscreenVisible(boolean visible) {
        if (mFullscreenBtn != null) mFullscreenBtn.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置静音按钮图标
     */
    public void setMuteIcon(int muteResId, int unmuteResId) {
        mMuteIconResId = muteResId;
        mUnmuteIconResId = unmuteResId;
        updateMuteButton();
    }

    /**
     * 设置静音按钮可见性
     */
    public void setMuteVisible(boolean visible) {
        if (mMuteBtn != null) mMuteBtn.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置锁屏按钮图标
     */
    public void setLockIcon(int lockResId, int unlockResId) {
        mLockIconResId = lockResId;
        mUnlockIconResId = unlockResId;
        updateLockButton();
    }

    /**
     * 设置锁屏按钮可见性
     */
    public void setLockVisible(boolean visible) {
        if (mLockBtn != null) mLockBtn.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置录像按钮图标
     */
    public void setRecordIcon(int normalResId, int activeResId) {
        mRecordNormalIconResId = normalResId;
        mRecordActiveIconResId = activeResId;
        updateRecordButton(mPlayerManager != null && mPlayerManager.isRecording());
    }

    /**
     * 设置录像按钮可见性
     */
    public void setRecordVisible(boolean visible) {
        if (mRecordBtn != null) mRecordBtn.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置截图按钮图标
     */
    public void setSnapshotIcon(int resId) {
        if (mSnapshotBtn != null) mSnapshotBtn.setImageResource(resId);
    }

    /**
     * 设置截图按钮可见性
     */
    public void setSnapshotVisible(boolean visible) {
        if (mSnapshotBtn != null) mSnapshotBtn.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置顶部栏可见性
     */
    public void setTopBarVisible(boolean visible) {
        if (mTopBar != null) mTopBar.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置底部栏可见性
     */
    public void setBottomBarVisible(boolean visible) {
        if (mBottomBar != null) mBottomBar.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置左侧栏可见性
     */
    public void setLeftBarVisible(boolean visible) {
        if (mLeftBar != null) mLeftBar.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置右侧栏可见性
     */
    public void setRightBarVisible(boolean visible) {
        if (mRightBar != null) mRightBar.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置进度条可见性
     */
    public void setSeekBarVisible(boolean visible) {
        if (mSeekBar != null) mSeekBar.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * 设置时间显示可见性
     */
    public void setTimeVisible(boolean visible) {
        int visibility = visible ? VISIBLE : GONE;
        if (mCurrentTime != null) mCurrentTime.setVisibility(visibility);
        if (mTotalTime != null) mTotalTime.setVisibility(visibility);
    }

    // ==================== 右侧栏按钮排序 ====================

    // 内置按钮的排序值
    private int mRecordOrder = 10;
    private int mSnapshotOrder = 20;

    /**
     * 设置录像按钮排序值（数字越小越靠上）
     */
    public void setRecordOrder(int order) {
        mRecordOrder = order;
        reorderRightBarButtons();
    }

    /**
     * 设置截图按钮排序值（数字越小越靠上）
     */
    public void setSnapshotOrder(int order) {
        mSnapshotOrder = order;
        reorderRightBarButtons();
    }

    /**
     * 重新排序右侧栏所有按钮
     */
    public void reorderRightBarButtons() {
        if (mRightBar == null || !(mRightBar instanceof android.widget.LinearLayout)) return;
        
        android.widget.LinearLayout rightBar = (android.widget.LinearLayout) mRightBar;
        
        // 收集所有按钮及其排序值
        java.util.List<ButtonOrderInfo> buttons = new java.util.ArrayList<>();
        
        // 添加内置按钮
        if (mRecordBtn != null) {
            buttons.add(new ButtonOrderInfo(mRecordBtn, mRecordOrder));
        }
        if (mSnapshotBtn != null) {
            buttons.add(new ButtonOrderInfo(mSnapshotBtn, mSnapshotOrder));
        }
        
        // 添加自定义按钮
        for (CustomButton cb : mCustomButtons) {
            ImageView view = mCustomButtonViews.get(cb.getId());
            if (view != null) {
                buttons.add(new ButtonOrderInfo(view, cb.getOrder()));
            }
        }
        
        // 按order排序
        java.util.Collections.sort(buttons, (a, b) -> Integer.compare(a.order, b.order));
        
        // 移除所有子View
        rightBar.removeAllViews();
        
        // 按排序后的顺序重新添加
        boolean isFirst = true;
        for (ButtonOrderInfo info : buttons) {
            android.widget.LinearLayout.LayoutParams params = 
                (android.widget.LinearLayout.LayoutParams) info.view.getLayoutParams();
            if (params == null) {
                params = new android.widget.LinearLayout.LayoutParams(dp2px(40), dp2px(40));
            }
            params.topMargin = isFirst ? 0 : dp2px(16);
            info.view.setLayoutParams(params);
            rightBar.addView(info.view);
            isFirst = false;
        }
        
        // 重新添加自定义按钮容器
        if (mCustomButtonsContainer != null && mCustomButtonsContainer.getParent() == null) {
            rightBar.addView(mCustomButtonsContainer);
        }
    }

    /**
     * 按钮排序信息
     */
    private static class ButtonOrderInfo {
        View view;
        int order;
        
        ButtonOrderInfo(View view, int order) {
            this.view = view;
            this.order = order;
        }
    }

    // ==================== 回调接口 ====================

    public interface OnBackClickListener {
        void onBackClick();
    }

    public interface OnFullscreenClickListener {
        void onFullscreenClick();
    }

    public interface OnRecordClickListener {
        void onRecordClick(boolean isRecording);
    }

    public interface OnSnapshotClickListener {
        void onSnapshotClick();
    }

    // ==================== 自定义按钮管理 ====================

    private java.util.List<CustomButton> mCustomButtons = new java.util.ArrayList<>();
    private java.util.Map<Integer, ImageView> mCustomButtonViews = new java.util.HashMap<>();

    /**
     * 添加自定义按钮到右侧栏
     * @param button 自定义按钮配置
     */
    public void addCustomButton(CustomButton button) {
        if (button == null) return;

        // 检查是否已存在
        for (CustomButton b : mCustomButtons) {
            if (b.getId() == button.getId()) {
                return;
            }
        }

        mCustomButtons.add(button);

        // 创建按钮视图
        ImageView imageView = new ImageView(mContext);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                dp2px(40), dp2px(40));
        params.topMargin = dp2px(16);
        imageView.setLayoutParams(params);
        imageView.setBackgroundResource(R.drawable.bg_controller_button);
        imageView.setPadding(dp2px(8), dp2px(8), dp2px(8), dp2px(8));

        if (button.getIconDrawable() != null) {
            imageView.setImageDrawable(button.getIconDrawable());
        } else if (button.getIconResId() != 0) {
            imageView.setImageResource(button.getIconResId());
        }

        imageView.setOnClickListener(v -> {
            if (button.getClickListener() != null) {
                button.getClickListener().onClick(button);
            }
        });

        mCustomButtonViews.put(button.getId(), imageView);
        
        // 重新排序所有按钮
        reorderRightBarButtons();
    }

    /**
     * 移除自定义按钮
     * @param buttonId 按钮ID
     */
    public void removeCustomButton(int buttonId) {
        ImageView view = mCustomButtonViews.get(buttonId);
        if (view != null) {
            // 从父容器移除
            if (view.getParent() != null) {
                ((android.view.ViewGroup) view.getParent()).removeView(view);
            }
            mCustomButtonViews.remove(buttonId);
        }

        for (int i = mCustomButtons.size() - 1; i >= 0; i--) {
            if (mCustomButtons.get(i).getId() == buttonId) {
                mCustomButtons.remove(i);
                break;
            }
        }
        
        // 重新排序
        reorderRightBarButtons();
    }

    /**
     * 设置自定义按钮排序值
     * @param buttonId 按钮ID
     * @param order 排序值（数字越小越靠上）
     */
    public void setCustomButtonOrder(int buttonId, int order) {
        for (CustomButton button : mCustomButtons) {
            if (button.getId() == buttonId) {
                button.setOrder(order);
                reorderRightBarButtons();
                break;
            }
        }
    }

    /**
     * 更新自定义按钮图标
     * @param buttonId 按钮ID
     * @param iconResId 新图标资源ID
     */
    public void updateCustomButtonIcon(int buttonId, int iconResId) {
        ImageView view = mCustomButtonViews.get(buttonId);
        if (view != null) {
            view.setImageResource(iconResId);
        }
    }

    /**
     * 设置自定义按钮可见性
     * @param buttonId 按钮ID
     * @param visible 是否可见
     */
    public void setCustomButtonVisible(int buttonId, boolean visible) {
        ImageView view = mCustomButtonViews.get(buttonId);
        if (view != null) {
            view.setVisibility(visible ? VISIBLE : GONE);
        }
    }

    /**
     * 清除所有自定义按钮
     */
    public void clearCustomButtons() {
        if (mCustomButtonsContainer != null) {
            mCustomButtonsContainer.removeAllViews();
        }
        mCustomButtons.clear();
        mCustomButtonViews.clear();
    }

    /**
     * 获取自定义按钮
     * @param buttonId 按钮ID
     * @return 自定义按钮，不存在返回null
     */
    public CustomButton getCustomButton(int buttonId) {
        for (CustomButton button : mCustomButtons) {
            if (button.getId() == buttonId) {
                return button;
            }
        }
        return null;
    }

    private int dp2px(float dp) {
        return (int) (dp * mContext.getResources().getDisplayMetrics().density + 0.5f);
    }
}
