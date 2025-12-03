package com.company.shenzhou.player.controller;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.company.shenzhou.player.core.VideoPlayerManager;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 手势控制器，支持滑动调节亮度、音量、进度
 */
public class GestureController implements View.OnTouchListener {

    private Context mContext;
    private VideoPlayerManager mPlayerManager;
    private Window mWindow;
    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    
    // 手势类型
    private static final int GESTURE_NONE = 0;
    private static final int GESTURE_BRIGHTNESS = 1;
    private static final int GESTURE_VOLUME = 2;
    private static final int GESTURE_PROGRESS = 3;
    
    private int mCurrentGesture = GESTURE_NONE;
    private float mDownX, mDownY;
    private int mViewWidth, mViewHeight;
    
    // 亮度和音量
    private float mBrightness;
    private int mVolume;
    private int mMaxVolume;
    
    // 进度
    private long mSeekPosition;
    
    // 回调
    private OnGestureListener mOnGestureListener;

    public GestureController(Context context, Window window) {
        mContext = context;
        mWindow = window;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void bindPlayerManager(VideoPlayerManager playerManager) {
        mPlayerManager = playerManager;
    }

    public void setOnGestureListener(OnGestureListener listener) {
        mOnGestureListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mViewWidth = v.getWidth();
        mViewHeight = v.getHeight();
        
        mGestureDetector.onTouchEvent(event);
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                mCurrentGesture = GESTURE_NONE;
                
                // 获取当前亮度
                mBrightness = mWindow.getAttributes().screenBrightness;
                if (mBrightness < 0) {
                    try {
                        mBrightness = Settings.System.getInt(mContext.getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS) / 255f;
                    } catch (Settings.SettingNotFoundException e) {
                        mBrightness = 0.5f;
                    }
                }
                
                // 获取当前音量
                mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                
                // 获取当前进度
                if (mPlayerManager != null) {
                    mSeekPosition = mPlayerManager.getCurrentPosition();
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - mDownX;
                float deltaY = event.getY() - mDownY;
                
                // 判断手势类型
                if (mCurrentGesture == GESTURE_NONE) {
                    if (Math.abs(deltaX) > 50 || Math.abs(deltaY) > 50) {
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            // 水平滑动 - 调节进度
                            mCurrentGesture = GESTURE_PROGRESS;
                        } else {
                            // 垂直滑动
                            if (mDownX < mViewWidth / 2) {
                                // 左侧 - 调节亮度
                                mCurrentGesture = GESTURE_BRIGHTNESS;
                            } else {
                                // 右侧 - 调节音量
                                mCurrentGesture = GESTURE_VOLUME;
                            }
                        }
                    }
                }
                
                // 处理手势
                switch (mCurrentGesture) {
                    case GESTURE_BRIGHTNESS:
                        handleBrightnessGesture(deltaY);
                        break;
                    case GESTURE_VOLUME:
                        handleVolumeGesture(deltaY);
                        break;
                    case GESTURE_PROGRESS:
                        handleProgressGesture(deltaX);
                        break;
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrentGesture == GESTURE_PROGRESS && mPlayerManager != null) {
                    mPlayerManager.seekTo(mSeekPosition);
                }
                
                if (mOnGestureListener != null) {
                    mOnGestureListener.onGestureEnd();
                }
                
                mCurrentGesture = GESTURE_NONE;
                break;
        }
        
        return true;
    }

    /**
     * 处理亮度手势
     */
    private void handleBrightnessGesture(float deltaY) {
        float percent = -deltaY / mViewHeight;
        float newBrightness = mBrightness + percent;
        newBrightness = Math.max(0.01f, Math.min(1f, newBrightness));
        
        WindowManager.LayoutParams params = mWindow.getAttributes();
        params.screenBrightness = newBrightness;
        mWindow.setAttributes(params);
        
        if (mOnGestureListener != null) {
            mOnGestureListener.onBrightnessChanged((int) (newBrightness * 100));
        }
    }

    /**
     * 处理音量手势
     */
    private void handleVolumeGesture(float deltaY) {
        float percent = -deltaY / mViewHeight;
        int deltaVolume = (int) (percent * mMaxVolume);
        int newVolume = mVolume + deltaVolume;
        newVolume = Math.max(0, Math.min(mMaxVolume, newVolume));
        
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        
        if (mOnGestureListener != null) {
            mOnGestureListener.onVolumeChanged(newVolume * 100 / mMaxVolume);
        }
    }

    /**
     * 处理进度手势
     */
    private void handleProgressGesture(float deltaX) {
        if (mPlayerManager == null) return;
        
        long duration = mPlayerManager.getDuration();
        if (duration <= 0) return;
        
        // 滑动整个屏幕宽度 = 总时长的 1/3
        long deltaPosition = (long) (deltaX / mViewWidth * duration / 3);
        mSeekPosition = mPlayerManager.getCurrentPosition() + deltaPosition;
        mSeekPosition = Math.max(0, Math.min(duration, mSeekPosition));
        
        if (mOnGestureListener != null) {
            mOnGestureListener.onProgressChanged(mSeekPosition, duration);
        }
    }

    /**
     * 手势监听器
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mOnGestureListener != null) {
                mOnGestureListener.onSingleTap();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mOnGestureListener != null) {
                mOnGestureListener.onDoubleTap();
            }
            return true;
        }
    }

    /**
     * 手势回调接口
     */
    public interface OnGestureListener {
        void onSingleTap();
        void onDoubleTap();
        void onBrightnessChanged(int percent);
        void onVolumeChanged(int percent);
        void onProgressChanged(long position, long duration);
        void onGestureEnd();
    }
}
