package com.company.shenzhou.player.core;

import android.content.Context;
import android.view.Surface;

import com.company.shenzhou.player.listener.OnBufferingUpdateListener;
import com.company.shenzhou.player.listener.OnCompletionListener;
import com.company.shenzhou.player.listener.OnErrorListener;
import com.company.shenzhou.player.listener.OnInfoListener;
import com.company.shenzhou.player.listener.OnPreparedListener;
import com.company.shenzhou.player.listener.OnVideoSizeChangedListener;
import com.company.shenzhou.player.listener.PlayerStateListener;
import com.company.shenzhou.player.render.IRenderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 视频播放器管理器，统一管理播放器内核、渲染视图和播放状态
 */
public class VideoPlayerManager implements IRenderView.SurfaceListener {

    private Context mContext;
    private IPlayerEngine mEngine;
    private IRenderView mRenderView;
    private Surface mSurface;
    
    private PlayerState mState = PlayerState.IDLE;
    private List<PlayerStateListener> mStateListeners = new ArrayList<>();
    
    private String mUrl;
    private Map<String, String> mHeaders;
    private boolean mPendingPlay = false;
    
    // Surface 尺寸（VLC 需要）
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;
    
    @PlayerType
    private int mPlayerType = PlayerType.VLC;

    public VideoPlayerManager(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 设置播放器内核类型
     */
    public void setPlayerType(@PlayerType int type) {
        android.util.Log.d("VideoPlayerManager", "setPlayerType: type=" + type + ", mPlayerType=" + mPlayerType + ", mEngine=" + mEngine);
        
        if (mPlayerType == type && mEngine != null) {
            return;
        }
        
        // 释放旧内核
        if (mEngine != null) {
            mEngine.release();
        }
        
        mPlayerType = type;
        mEngine = PlayerFactory.create(mContext, type);
        bindEngineListeners();
        
        // 重新绑定 Surface（VLC 需要先设置 WindowSize）
        android.util.Log.d("VideoPlayerManager", "setPlayerType: mSurface=" + mSurface + ", mSurfaceWidth=" + mSurfaceWidth + ", mSurfaceHeight=" + mSurfaceHeight);
        if (mSurface != null) {
            if (type == PlayerType.VLC) {
                if (mEngine instanceof com.company.shenzhou.player.engine.VlcPlayerEngine) {
                    // 总是设置窗口大小，即使是 0（VlcPlayer 内部会处理）
                    ((com.company.shenzhou.player.engine.VlcPlayerEngine) mEngine).setWindowSize(mSurfaceWidth, mSurfaceHeight);
                }
            }
            mEngine.setSurface(mSurface);
        }
        
        setState(PlayerState.IDLE);
    }

    /**
     * 设置渲染视图
     */
    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            mRenderView.release();
        }
        mRenderView = renderView;
        mRenderView.setSurfaceListener(this);
    }

    /**
     * 设置数据源
     */
    public void setDataSource(String url) {
        setDataSource(url, null);
    }

    /**
     * 设置数据源（带请求头）
     */
    public void setDataSource(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
        
        ensureEngine();
        mEngine.setDataSource(url, headers);
        setState(PlayerState.INITIALIZED);
    }

    /**
     * 播放
     */
    public void play(String url) {
        play(url, null);
    }

    /**
     * 播放（带请求头）
     */
    public void play(String url, Map<String, String> headers) {
        setDataSource(url, headers);
        prepareAsync();
    }

    /**
     * 重试播放（使用上次的地址）
     */
    public void retry() {
        if (mUrl != null) {
            reset();
            play(mUrl, mHeaders);
        }
    }

    /**
     * 异步准备
     */
    public void prepareAsync() {
        if (mEngine == null || mUrl == null) return;
        
        if (mSurface == null) {
            // Surface 还未准备好，等待
            mPendingPlay = true;
            return;
        }
        
        mEngine.prepareAsync();
        setState(PlayerState.PREPARING);
    }

    /**
     * 开始播放
     */
    public void start() {
        if (mEngine != null) {
            mEngine.start();
            setState(PlayerState.PLAYING);
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mEngine != null && isPlaying()) {
            mEngine.pause();
            setState(PlayerState.PAUSED);
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (mEngine != null) {
            mEngine.stop();
            setState(PlayerState.STOPPED);
        }
    }

    /**
     * 重置
     */
    public void reset() {
        if (mEngine != null) {
            mEngine.reset();
            setState(PlayerState.IDLE);
        }
        mUrl = null;
        mHeaders = null;
        mPendingPlay = false;
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mEngine != null) {
            mEngine.release();
            mEngine = null;
        }
        if (mRenderView != null) {
            mRenderView.release();
            mRenderView = null;
        }
        mStateListeners.clear();
        setState(PlayerState.RELEASED);
    }

    /**
     * 跳转
     */
    public void seekTo(long position) {
        if (mEngine != null) {
            mEngine.seekTo(position);
        }
    }

    /**
     * 获取当前位置
     */
    public long getCurrentPosition() {
        return mEngine != null ? mEngine.getCurrentPosition() : 0;
    }

    /**
     * 获取总时长
     */
    public long getDuration() {
        return mEngine != null ? mEngine.getDuration() : 0;
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return mEngine != null && mEngine.isPlaying();
    }

    /**
     * 获取缓冲百分比
     */
    public int getBufferPercentage() {
        return mEngine != null ? mEngine.getBufferPercentage() : 0;
    }

    /**
     * 设置音量
     */
    public void setVolume(float left, float right) {
        if (mEngine != null) {
            mEngine.setVolume(left, right);
        }
    }

    /**
     * 设置播放速度
     */
    public void setSpeed(float speed) {
        if (mEngine != null) {
            mEngine.setSpeed(speed);
        }
    }

    /**
     * 获取播放速度
     */
    public float getSpeed() {
        return mEngine != null ? mEngine.getSpeed() : 1.0f;
    }

    /**
     * 设置循环播放
     */
    public void setLooping(boolean looping) {
        if (mEngine != null) {
            mEngine.setLooping(looping);
        }
    }

    /**
     * 获取当前状态
     */
    public PlayerState getState() {
        return mState;
    }

    /**
     * 获取播放器类型
     */
    @PlayerType
    public int getPlayerType() {
        return mPlayerType;
    }

    /**
     * 添加状态监听器
     */
    public void addStateListener(PlayerStateListener listener) {
        if (listener != null && !mStateListeners.contains(listener)) {
            mStateListeners.add(listener);
        }
    }

    /**
     * 移除状态监听器
     */
    public void removeStateListener(PlayerStateListener listener) {
        mStateListeners.remove(listener);
    }

    // ==================== 录像和截图功能 ====================

    /**
     * 是否支持录像
     */
    public boolean isSupportRecord() {
        return mEngine != null && mEngine.isSupportRecord();
    }

    /**
     * 是否支持截图
     */
    public boolean isSupportSnapshot() {
        return mEngine != null && mEngine.isSupportSnapshot();
    }

    /**
     * 开始录像
     */
    public boolean startRecord(String directory, String fileName) {
        if (mEngine != null) {
            return mEngine.startRecord(directory, fileName);
        }
        return false;
    }

    /**
     * 停止录像
     */
    public boolean stopRecord() {
        if (mEngine != null) {
            return mEngine.stopRecord();
        }
        return false;
    }

    /**
     * 是否正在录像
     */
    public boolean isRecording() {
        return mEngine != null && mEngine.isRecording();
    }

    /**
     * 获取录像文件路径
     */
    public String getRecordFilePath() {
        return mEngine != null ? mEngine.getRecordFilePath() : null;
    }

    /**
     * 截图
     */
    public boolean takeSnapshot(String filePath, int width, int height) {
        if (mEngine != null) {
            return mEngine.takeSnapshot(filePath, width, height);
        }
        return false;
    }

    /**
     * 截图（原始尺寸）
     */
    public boolean takeSnapshot(String filePath) {
        return takeSnapshot(filePath, 0, 0);
    }

    // ==================== IRenderView.SurfaceListener ====================

    @Override
    public void onSurfaceAvailable(Surface surface, int width, int height) {
        // 参考 VlcVideoView 的实现：在 onSurfaceTextureAvailable 中
        // 先设置 WindowSize，再设置 Surface
        mSurface = surface;
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        
        android.util.Log.d("VideoPlayerManager", "onSurfaceAvailable: width=" + width + ", height=" + height + ", surface valid=" + (surface != null && surface.isValid()) + ", mEngine=" + mEngine);
        
        if (mEngine != null) {
            // VLC 需要先设置 WindowSize 再设置 Surface（与 VlcVideoView 保持一致）
            if (mPlayerType == PlayerType.VLC) {
                if (mEngine instanceof com.company.shenzhou.player.engine.VlcPlayerEngine) {
                    com.company.shenzhou.player.engine.VlcPlayerEngine vlcEngine = 
                        (com.company.shenzhou.player.engine.VlcPlayerEngine) mEngine;
                    // 先设置窗口大小，再设置 Surface
                    vlcEngine.setWindowSize(width, height);
                    mEngine.setSurface(surface);
                } else {
                    mEngine.setSurface(surface);
                }
            } else {
                mEngine.setSurface(surface);
            }
        }
        
        // 如果有待播放的任务
        if (mPendingPlay && mUrl != null) {
            mPendingPlay = false;
            prepareAsync();
        }
    }

    @Override
    public void onSurfaceChanged(Surface surface, int width, int height) {
        // 保存 Surface 尺寸
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        
        // VLC 需要设置窗口大小
        if (mEngine != null && mPlayerType == PlayerType.VLC && width > 0 && height > 0) {
            if (mEngine instanceof com.company.shenzhou.player.engine.VlcPlayerEngine) {
                ((com.company.shenzhou.player.engine.VlcPlayerEngine) mEngine).setWindowSize(width, height);
            }
        }
    }

    @Override
    public void onSurfaceDestroyed(Surface surface) {
        mSurface = null;
        // VLC 需要通知 Surface 销毁
        if (mEngine != null && mPlayerType == PlayerType.VLC) {
            if (mEngine instanceof com.company.shenzhou.player.engine.VlcPlayerEngine) {
                ((com.company.shenzhou.player.engine.VlcPlayerEngine) mEngine).onSurfaceDestroyed();
            }
        }
    }

    // ==================== 私有方法 ====================

    private void ensureEngine() {
        if (mEngine == null) {
            mEngine = PlayerFactory.create(mContext, mPlayerType);
            bindEngineListeners();
            
            // 如果 Surface 已经准备好，设置到新创建的引擎
            if (mSurface != null) {
                if (mPlayerType == PlayerType.VLC && mEngine instanceof com.company.shenzhou.player.engine.VlcPlayerEngine) {
                    com.company.shenzhou.player.engine.VlcPlayerEngine vlcEngine = 
                        (com.company.shenzhou.player.engine.VlcPlayerEngine) mEngine;
                    // 总是设置窗口大小
                    vlcEngine.setWindowSize(mSurfaceWidth, mSurfaceHeight);
                }
                mEngine.setSurface(mSurface);
            }
        }
    }

    private void bindEngineListeners() {
        if (mEngine == null) return;
        
        mEngine.setOnPreparedListener(() -> {
            setState(PlayerState.PREPARED);
            start();
        });
        
        mEngine.setOnCompletionListener(() -> {
            setState(PlayerState.COMPLETED);
        });
        
        mEngine.setOnErrorListener((errorCode, errorMsg) -> {
            setState(PlayerState.ERROR);
            return false;
        });
        
        mEngine.setOnBufferingUpdateListener(percent -> {
            if (percent < 100) {
                setState(PlayerState.BUFFERING);
            } else if (mState == PlayerState.BUFFERING) {
                setState(PlayerState.PLAYING);
            }
        });
        
        mEngine.setOnVideoSizeChangedListener((width, height) -> {
            if (mRenderView != null) {
                mRenderView.setVideoSize(width, height);
            }
        });
    }

    private void setState(PlayerState state) {
        if (mState != state) {
            mState = state;
            for (PlayerStateListener listener : mStateListeners) {
                listener.onStateChanged(state);
            }
        }
    }
}
