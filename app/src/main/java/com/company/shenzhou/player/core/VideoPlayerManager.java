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
    
    // 待应用的播放速度（在 prepared 后应用）
    private float mPendingSpeed = 1.0f;
    // 标记是否已经应用了待设置的速度
    private boolean mSpeedApplied = false;

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
        
        // 根据播放器类型设置渲染视图的居中变换
        // VLC 内核自己处理视频居中，不需要额外变换
        // IJK 和系统内核需要通过 Matrix 变换来居中显示
        if (mRenderView != null) {
            boolean needCenterTransform = (type != PlayerType.VLC);
            mRenderView.setCenterTransformEnabled(needCenterTransform);
        }
        
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
        
        // 根据当前播放器类型设置居中变换
        // VLC 内核自己处理视频居中，不需要额外变换
        // IJK 和系统内核需要通过 Matrix 变换来居中显示
        boolean needCenterTransform = (mPlayerType != PlayerType.VLC);
        mRenderView.setCenterTransformEnabled(needCenterTransform);
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
        mPendingSpeed = 1.0f;
        mSpeedApplied = false;
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
     * 注意：如果在播放前调用，速度会在 prepared 后自动应用
     */
    public void setSpeed(float speed) {
        mPendingSpeed = speed;
        if (mEngine != null && mState == PlayerState.PLAYING) {
            // 已经在播放中，直接设置
            mEngine.setSpeed(speed);
        }
        // 如果还没开始播放，速度会在 onPrepared 后的 start() 中应用
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
     * VLC 内核使用原生截图 API
     * IJK 和系统内核通过 TextureView 截图
     */
    public boolean takeSnapshot(String filePath, int width, int height) {
        if (mEngine == null) {
            return false;
        }
        
        // VLC 内核使用原生截图
        if (mPlayerType == PlayerType.VLC) {
            return mEngine.takeSnapshot(filePath, width, height);
        }
        
        // IJK 和系统内核通过 RenderView 截图
        if (mRenderView != null) {
            android.graphics.Bitmap bitmap = mRenderView.captureFrame();
            if (bitmap != null) {
                try {
                    // 如果指定了尺寸，缩放 Bitmap
                    if (width > 0 && height > 0 && (bitmap.getWidth() != width || bitmap.getHeight() != height)) {
                        android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true);
                        bitmap.recycle();
                        bitmap = scaledBitmap;
                    }
                    
                    // 保存到文件
                    java.io.File file = new java.io.File(filePath);
                    java.io.File parentDir = file.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.flush();
                    fos.close();
                    bitmap.recycle();
                    return true;
                } catch (Exception e) {
                    android.util.Log.e("VideoPlayerManager", "takeSnapshot failed: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 截图（原始尺寸）
     */
    public boolean takeSnapshot(String filePath) {
        return takeSnapshot(filePath, 0, 0);
    }
    
    /**
     * 获取当前帧的 Bitmap（不保存到文件）
     * @return 当前帧的 Bitmap，失败返回 null
     */
    public android.graphics.Bitmap captureFrame() {
        if (mEngine == null) {
            return null;
        }
        
        // 通过 RenderView 截图（适用于所有内核）
        if (mRenderView != null) {
            return mRenderView.captureFrame();
        }
        return null;
    }

    /**
     * 判断当前流是否支持进度拖动（seek）
     * 点播流返回 true，直播流返回 false
     * @return true 支持进度拖动
     */
    public boolean isSeekable() {
        return mEngine != null && mEngine.isSeekable();
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

    /**
     * 横竖屏切换时调用，优化 VLC 切换体验
     * 在 Activity 的 onConfigurationChanged 中调用
     */
    public void onOrientationChanged() {
        if (mEngine != null && mPlayerType == PlayerType.VLC) {
            if (mEngine instanceof com.company.shenzhou.player.engine.VlcPlayerEngine) {
                ((com.company.shenzhou.player.engine.VlcPlayerEngine) mEngine).onOrientationChanged();
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
            mSpeedApplied = false; // 重置标记
            start();
            // 对于非 IJK 内核，在播放开始后延迟应用速度
            // IJK 内核需要等待 MEDIA_INFO_VIDEO_RENDERING_START 事件
            if (mPendingSpeed != 1.0f && mEngine != null && mPlayerType != PlayerType.IJK) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (mEngine != null && !mSpeedApplied && (mState == PlayerState.PLAYING || mState == PlayerState.BUFFERING)) {
                        mEngine.setSpeed(mPendingSpeed);
                        mSpeedApplied = true;
                    }
                }, 200);
            }
        });
        
        mEngine.setOnCompletionListener(() -> {
            setState(PlayerState.COMPLETED);
        });
        
        mEngine.setOnErrorListener((errorCode, errorMsg) -> {
            setState(PlayerState.ERROR);
            return false;
        });
        
        mEngine.setOnBufferingUpdateListener(percent -> {
            // 系统 MediaPlayer 在低倍速播放时可能不会正确触发缓冲事件
            // 只在非播放状态时才切换到 BUFFERING 状态
            if (percent < 100 && mState != PlayerState.PLAYING) {
                setState(PlayerState.BUFFERING);
            } else if (percent >= 100 && mState == PlayerState.BUFFERING) {
                setState(PlayerState.PLAYING);
            }
        });
        
        // 添加 Info 监听器，用于更准确地判断播放状态
        mEngine.setOnInfoListener((what, extra) -> {
            // MEDIA_INFO_BUFFERING_START = 701, MEDIA_INFO_BUFFERING_END = 702
            // MEDIA_INFO_VIDEO_RENDERING_START = 3
            if (what == 701) {
                setState(PlayerState.BUFFERING);
            } else if (what == 702 || what == 3) {
                // 缓冲结束或开始渲染视频，切换到播放状态
                if (mState == PlayerState.BUFFERING) {
                    setState(PlayerState.PLAYING);
                }
                // IJK 内核在收到渲染开始事件后设置速度
                if (what == 3 && mPlayerType == PlayerType.IJK && mPendingSpeed != 1.0f && !mSpeedApplied) {
                    // 延迟一点确保播放器完全就绪
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (mEngine != null && !mSpeedApplied) {
                            android.util.Log.d("VideoPlayerManager", "IJK: Applying speed " + mPendingSpeed + " after rendering start");
                            mEngine.setSpeed(mPendingSpeed);
                            mSpeedApplied = true;
                        }
                    }, 100);
                }
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
