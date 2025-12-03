package com.company.shenzhou.player.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import com.company.shenzhou.player.listener.OnBufferingUpdateListener;
import com.company.shenzhou.player.listener.OnCompletionListener;
import com.company.shenzhou.player.listener.OnErrorListener;
import com.company.shenzhou.player.listener.OnInfoListener;
import com.company.shenzhou.player.listener.OnPreparedListener;
import com.company.shenzhou.player.listener.OnRecordListener;
import com.company.shenzhou.player.listener.OnSnapshotListener;
import com.company.shenzhou.player.listener.OnVideoSizeChangedListener;

import java.util.Map;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器内核基类，提供通用的监听器管理和主线程回调
 */
public abstract class BasePlayerEngine implements IPlayerEngine {

    protected Context mContext;
    protected Surface mSurface;
    protected String mUrl;
    protected Map<String, String> mHeaders;
    protected Handler mMainHandler;
    
    protected boolean mIsLooping = false;
    protected float mSpeed = 1.0f;
    protected float mVolumeLeft = 1.0f;
    protected float mVolumeRight = 1.0f;
    
    protected int mVideoWidth;
    protected int mVideoHeight;
    protected int mBufferPercent;

    // 监听器
    protected OnPreparedListener mOnPreparedListener;
    protected OnCompletionListener mOnCompletionListener;
    protected OnErrorListener mOnErrorListener;
    protected OnBufferingUpdateListener mOnBufferingUpdateListener;
    protected OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    protected OnInfoListener mOnInfoListener;
    protected OnRecordListener mOnRecordListener;
    protected OnSnapshotListener mOnSnapshotListener;
    
    // 录像状态
    protected boolean mIsRecording = false;
    protected String mRecordFilePath;

    @Override
    public void init(Context context) {
        mContext = context.getApplicationContext();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    @Override
    public void setDataSource(String url) {
        mUrl = url;
        mHeaders = null;
    }

    @Override
    public void setDataSource(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }

    @Override
    public void setVolume(float left, float right) {
        mVolumeLeft = left;
        mVolumeRight = right;
    }

    @Override
    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    @Override
    public float getSpeed() {
        return mSpeed;
    }

    @Override
    public void setLooping(boolean looping) {
        mIsLooping = looping;
    }

    @Override
    public boolean isLooping() {
        return mIsLooping;
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercent;
    }

    // ==================== 监听器设置 ====================

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    @Override
    public void setOnRecordListener(OnRecordListener listener) {
        mOnRecordListener = listener;
    }

    @Override
    public void setOnSnapshotListener(OnSnapshotListener listener) {
        mOnSnapshotListener = listener;
    }

    // ==================== 录像和截图默认实现（不支持） ====================

    @Override
    public boolean isSupportRecord() {
        return false;
    }

    @Override
    public boolean isSupportSnapshot() {
        return false;
    }

    @Override
    public boolean startRecord(String directory, String fileName) {
        return false;
    }

    @Override
    public boolean stopRecord() {
        return false;
    }

    @Override
    public boolean isRecording() {
        return mIsRecording;
    }

    @Override
    public String getRecordFilePath() {
        return mRecordFilePath;
    }

    @Override
    public boolean takeSnapshot(String filePath, int width, int height) {
        return false;
    }

    // ==================== 回调通知（主线程） ====================

    protected void notifyOnPrepared() {
        if (mOnPreparedListener != null) {
            mMainHandler.post(() -> mOnPreparedListener.onPrepared());
        }
    }

    protected void notifyOnCompletion() {
        if (mOnCompletionListener != null) {
            mMainHandler.post(() -> mOnCompletionListener.onCompletion());
        }
    }

    protected void notifyOnError(int errorCode, String errorMsg) {
        if (mOnErrorListener != null) {
            mMainHandler.post(() -> mOnErrorListener.onError(errorCode, errorMsg));
        }
    }

    protected void notifyOnBufferingUpdate(int percent) {
        mBufferPercent = percent;
        if (mOnBufferingUpdateListener != null) {
            mMainHandler.post(() -> mOnBufferingUpdateListener.onBufferingUpdate(percent));
        }
    }

    protected void notifyOnVideoSizeChanged(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mOnVideoSizeChangedListener != null) {
            mMainHandler.post(() -> mOnVideoSizeChangedListener.onVideoSizeChanged(width, height));
        }
    }

    protected void notifyOnInfo(int what, int extra) {
        if (mOnInfoListener != null) {
            mMainHandler.post(() -> mOnInfoListener.onInfo(what, extra));
        }
    }

    protected void notifyOnRecordStart(String filePath) {
        mIsRecording = true;
        mRecordFilePath = filePath;
        if (mOnRecordListener != null) {
            mMainHandler.post(() -> mOnRecordListener.onRecordStart(filePath));
        }
    }

    protected void notifyOnRecordStop(String filePath) {
        mIsRecording = false;
        if (mOnRecordListener != null) {
            mMainHandler.post(() -> mOnRecordListener.onRecordStop(filePath));
        }
    }

    protected void notifyOnRecordError(String errorMsg) {
        mIsRecording = false;
        if (mOnRecordListener != null) {
            mMainHandler.post(() -> mOnRecordListener.onRecordError(errorMsg));
        }
    }

    protected void notifyOnSnapshotSuccess(String filePath) {
        if (mOnSnapshotListener != null) {
            mMainHandler.post(() -> mOnSnapshotListener.onSnapshotSuccess(filePath));
        }
    }

    protected void notifyOnSnapshotError(String errorMsg) {
        if (mOnSnapshotListener != null) {
            mMainHandler.post(() -> mOnSnapshotListener.onSnapshotError(errorMsg));
        }
    }
}
