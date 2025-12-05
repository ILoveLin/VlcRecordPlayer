package com.company.shenzhou.player.engine;

import android.content.Context;
import android.view.Surface;

import com.company.shenzhou.player.core.BasePlayerEngine;
import com.company.shenzhou.player.core.PlayerType;
import com.vlc.lib.RecordEvent;
import com.vlc.lib.VlcPlayer;
import com.vlc.lib.listener.MediaListenerEvent;
import com.vlc.lib.listener.VideoSizeChange;

import org.videolan.libvlc.LibVLC;

import java.util.ArrayList;
import java.util.Map;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : VLC 播放器内核实现，基于项目中已有的 VlcPlayer 封装，支持 RTSP、RTMP、HTTP 等多种协议
 */
public class VlcPlayerEngine extends BasePlayerEngine {

    private LibVLC mLibVLC;
    private VlcPlayer mVlcPlayer;
    private boolean mIsPrepared = false;
    private boolean mIsSurfaceReady = false;

    @Override
    public void init(Context context) {
        super.init(context);
        initVlc();
    }

    private void initVlc() {
        ArrayList<String> options = getVlcOptions();
        mLibVLC = new LibVLC(mContext, options);
        mVlcPlayer = new VlcPlayer(mLibVLC);
        
        // 提前初始化 RecordEvent，确保 native 库已加载，避免第一次录像失败
        mRecordEvent = new RecordEvent();
        
        // 设置事件监听
        mVlcPlayer.setMediaListenerEvent(new MediaListenerEvent() {
            @Override
            public void eventBuffing(int event, float buffing) {
                notifyOnBufferingUpdate((int) buffing);
                // 当缓冲到100%时，才真正准备好可以显示画面
                if (buffing >= 100f && !mIsPrepared) {
                    mIsPrepared = true;
                    notifyOnPrepared();
                }
            }

            @Override
            public void eventPlayInit(boolean isPlaying) {
                // 初始化状态
            }

            @Override
            public void eventStop(boolean isPlayError) {
                if (isPlayError) {
                    // 播放错误，不触发完成回调，错误已在eventError中处理
                    return;
                }
                if (mIsLooping) {
                    // 循环播放
                    mMainHandler.post(() -> {
                        seekTo(0);
                        start();
                    });
                } else {
                    notifyOnCompletion();
                }
            }

            @Override
            public void eventError(int event, boolean show) {
                notifyOnError(event, "VLC playback error");
            }

            @Override
            public void eventPlay(boolean isPlaying) {
                // eventPlay 只表示播放状态变化，不再用于触发 prepared
                // prepared 改为在 buffing >= 100% 时触发，这样能确保画面已经准备好
            }
        });
        
        // 设置视频尺寸变化监听
        mVlcPlayer.setVideoSizeChange(new VideoSizeChange() {
            @Override
            public void onVideoSizeChanged(int width, int height, int visibleWidth, int visibleHeight) {
                mVideoWidth = width;
                mVideoHeight = height;
                notifyOnVideoSizeChanged(width, height);
            }
        });
    }

    /**
     * VLC 配置选项
     * 优化配置减少首屏加载时间
     */
    private ArrayList<String> getVlcOptions() {
        ArrayList<String> options = new ArrayList<>();
        
        // 时钟同步
        options.add(":clock-jitter=0");
        options.add(":clock-synchro=0");
        
        // 缓存配置 - 150ms 平衡配置
        options.add("--rtsp-caching=150");
        options.add("--tcp-caching=150");
        options.add("--realrtsp-caching=150");
        options.add("--network-caching=150");
        options.add(":live-caching=150");
        options.add(":file-caching=150");
        options.add("--sout-mux-caching=150");
        
        // 帧处理 - 允许丢帧以加快显示
        options.add("--drop-late-frames");
        options.add("--skip-frames");
        
        // RTSP 配置
        options.add(":rtsp-frame-buffer-size=200");
        options.add("--rtsp-tcp");
        
        // 网络配置
        options.add("--http-reconnect");
        
        // 解码器优化
        options.add("--avcodec-fast");
        options.add("--avcodec-skiploopfilter=4");
        
        return options;
    }

    // 保存窗口尺寸，用于 VLC 渲染
    private int mWindowWidth = 0;
    private int mWindowHeight = 0;

    @Override
    public void setSurface(Surface surface) {
        super.setSurface(surface);
        mIsSurfaceReady = surface != null && surface.isValid();
        if (mVlcPlayer != null && surface != null && surface.isValid()) {
            // 总是设置窗口尺寸（VlcPlayer 内部会保存这些值）
            mVlcPlayer.setWindowSize(mWindowWidth, mWindowHeight);
            mVlcPlayer.setSurface(surface, null);
            android.util.Log.d("VlcPlayerEngine", "setSurface: surface is valid, width=" + mWindowWidth + ", height=" + mWindowHeight);
        } else {
            android.util.Log.w("VlcPlayerEngine", "setSurface: surface is null or invalid, surface=" + surface);
        }
    }

    /**
     * 设置 Surface 和尺寸（VLC 需要同时设置）
     */
    public void setSurfaceWithSize(Surface surface, int width, int height) {
        mWindowWidth = width;
        mWindowHeight = height;
        mIsSurfaceReady = surface != null;
        if (mVlcPlayer != null && surface != null) {
            mVlcPlayer.setWindowSize(width, height);
            mVlcPlayer.setSurface(surface, null);
        }
    }

    /**
     * 设置 Surface 尺寸
     */
    public void setWindowSize(int width, int height) {
        mWindowWidth = width;
        mWindowHeight = height;
        if (mVlcPlayer != null) {
            mVlcPlayer.setWindowSize(width, height);
        }
    }

    @Override
    public void setDataSource(String url, Map<String, String> headers) {
        super.setDataSource(url, headers);
        if (mVlcPlayer != null) {
            mVlcPlayer.setPath(url);
        }
    }

    @Override
    public void prepare() {
        prepareAsync();
    }

    @Override
    public void prepareAsync() {
        if (mVlcPlayer == null || mUrl == null) return;
        mIsPrepared = false;
        // VLC 的 prepare 和 play 是一起的，直接调用 startPlay
        // VlcPlayer.startPlay() 内部会处理 Surface：
        // - 如果 Surface 已设置（isSurfaceAvailable=true），会直接播放并 attachSurface
        // - 如果 Surface 未设置，会设置 isSufaceDelayerPlay=true，等 setSurface 时再播放
        mVlcPlayer.startPlay();
    }

    @Override
    public void start() {
        if (mVlcPlayer != null) {
            if (mIsPrepared) {
                // 已经在播放中，调用 start 恢复播放（用于暂停后恢复）
                mVlcPlayer.start();
            }
            // 如果未准备好，不做任何操作，prepareAsync 中的 startPlay 会处理
        }
    }

    @Override
    public void pause() {
        if (mVlcPlayer != null && mVlcPlayer.isPlaying()) {
            mVlcPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (mVlcPlayer != null) {
            mVlcPlayer.onStop();
        }
        mIsPrepared = false;
    }

    @Override
    public void reset() {
        stop();
        mUrl = null;
        mIsPrepared = false;
    }

    @Override
    public void release() {
        if (mVlcPlayer != null) {
            mVlcPlayer.onDestroy();
            mVlcPlayer = null;
        }
        
        if (mLibVLC != null) {
            mLibVLC.release();
            mLibVLC = null;
        }
    }

    @Override
    public void seekTo(long position) {
        if (mVlcPlayer != null && mIsPrepared) {
            // 对于点播流，即使 VLC 的 canSeek 未设置也尝试 seek
            // 因为某些 HLS 流可能延迟触发 SeekableChanged 事件
            mVlcPlayer.seekTo(position);
            android.util.Log.d("VlcPlayerEngine", "seekTo: position=" + position + 
                    ", duration=" + getDuration() + ", isSeekable=" + isSeekable());
        }
    }

    /**
     * 判断当前流是否支持进度拖动
     * 点播流（有时长）返回 true，直播流返回 false
     * @return true 支持进度拖动
     */
    public boolean isSeekable() {
        if (mVlcPlayer != null) {
            // 使用 canControl() 方法判断是否可以控制（包含 canSeek）
            return mVlcPlayer.canControl();
        }
        // 如果有时长，认为是点播流
        return getDuration() > 0;
    }

    @Override
    public long getCurrentPosition() {
        if (mVlcPlayer != null) {
            return mVlcPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mVlcPlayer != null) {
            return mVlcPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mVlcPlayer != null && mVlcPlayer.isPlaying();
    }

    @Override
    public void setVolume(float left, float right) {
        super.setVolume(left, right);
        // VlcPlayer 没有直接的音量设置方法，需要通过 MediaPlayer 设置
        if (mVlcPlayer != null && mVlcPlayer.getMediaPlayer() != null) {
            int volume = (int) ((left + right) / 2 * 100);
            mVlcPlayer.getMediaPlayer().setVolume(volume);
        }
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(speed);
        if (mVlcPlayer != null) {
            mVlcPlayer.setPlaybackSpeedMedia(speed);
        }
    }

    @Override
    public float getSpeed() {
        if (mVlcPlayer != null) {
            return mVlcPlayer.getPlaybackSpeed();
        }
        return 1.0f;
    }

    @Override
    public void setLooping(boolean looping) {
        super.setLooping(looping);
        if (mVlcPlayer != null) {
            mVlcPlayer.setLoop(looping);
        }
    }

    @Override
    public boolean isLooping() {
        if (mVlcPlayer != null) {
            return mVlcPlayer.isLoop();
        }
        return mIsLooping;
    }

    @Override
    public int getPlayerType() {
        return PlayerType.VLC;
    }

    // ==================== 录像和截图功能 ====================
    
    // RecordEvent 实例，用于录像和截图
    private RecordEvent mRecordEvent;
    // 录像目录
    private String mRecordDirectory;
    // 录像开始时间，用于筛选录像文件
    private long mRecordStartTime;

    @Override
    public boolean isSupportRecord() {
        return RecordEvent.isSport();
    }

    @Override
    public boolean isSupportSnapshot() {
        return RecordEvent.isSport();
    }

    @Override
    public boolean startRecord(String directory, String fileName) {
        if (!isSupportRecord()) {
            notifyOnRecordError("录像功能不可用");
            return false;
        }
        
        if (mVlcPlayer == null || mVlcPlayer.getMediaPlayer() == null) {
            notifyOnRecordError("播放器未初始化");
            return false;
        }
        
        if (mIsRecording) {
            notifyOnRecordError("正在录像中");
            return false;
        }
        
        try {
            // 确保目录存在
            java.io.File dir = new java.io.File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存录像目录，用于后续查找录像文件
            mRecordDirectory = directory;
            
            // 使用 RecordEvent 的录制方法
            // mRecordEvent 已在 initVlc() 中初始化，确保 native 库已加载
            boolean result = mRecordEvent.startRecord(mVlcPlayer.getMediaPlayer(), directory, fileName);
            
            if (result) {
                // VLC 录像文件名是自动生成的，格式类似 vlc-record-yyyy-MM-dd-HHhMMmSSs-xxx.mpg
                // 记录开始时间，用于后续筛选录像文件
                mRecordStartTime = System.currentTimeMillis();
                mRecordFilePath = directory;
                mIsRecording = true;
                if (mOnRecordListener != null) {
                    mMainHandler.post(() -> mOnRecordListener.onRecordStart(directory));
                }
                return true;
            } else {
                notifyOnRecordError("开始录像失败");
                return false;
            }
        } catch (Exception e) {
            notifyOnRecordError("录像异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean stopRecord() {
        if (!mIsRecording) {
            notifyOnRecordError("当前没有在录像");
            return false;
        }
        
        if (mVlcPlayer == null || mVlcPlayer.getMediaPlayer() == null) {
            notifyOnRecordError("播放器未初始化");
            return false;
        }
        
        if (mRecordEvent == null) {
            notifyOnRecordError("录像组件未初始化");
            return false;
        }
        
        try {
            // 使用 RecordEvent 停止录像
            boolean result = mRecordEvent.stopRecord(mVlcPlayer.getMediaPlayer());
            android.util.Log.d("VlcPlayerEngine", "stopRecord result: " + result + ", directory: " + mRecordDirectory);
            
            // 无论 result 如何，都标记为停止录像
            mIsRecording = false;
            
            // 同步等待一小段时间让文件写入完成
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {}
            
            // 立即查找录像文件
            String recordFile = findLatestRecordFile(mRecordDirectory, mRecordStartTime);
            if (recordFile != null) {
                mRecordFilePath = recordFile;
                android.util.Log.d("VlcPlayerEngine", "findLatestRecordFile: " + recordFile);
                notifyOnRecordStop(recordFile);
            } else {
                // 如果立即找不到，再延迟一次查找
                final String directory = mRecordDirectory;
                final long startTime = mRecordStartTime;
                mMainHandler.postDelayed(() -> {
                    String file = findLatestRecordFile(directory, startTime);
                    if (file != null) {
                        mRecordFilePath = file;
                        android.util.Log.d("VlcPlayerEngine", "findLatestRecordFile delayed: " + file);
                        if (mOnRecordListener != null) {
                            mOnRecordListener.onRecordStop(file);
                        }
                    } else {
                        android.util.Log.w("VlcPlayerEngine", "录像文件未找到");
                        if (mOnRecordListener != null) {
                            mOnRecordListener.onRecordError("录像文件未找到");
                        }
                    }
                }, 500);
            }
            
            return true;
        } catch (Exception e) {
            android.util.Log.e("VlcPlayerEngine", "stopRecord exception", e);
            mIsRecording = false;
            notifyOnRecordError("停止录像异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 查找目录中最新的录像文件
     * @param directory 录像目录
     * @param startTime 录像开始时间，用于筛选在此时间之后创建的文件
     */
    private String findLatestRecordFile(String directory, long startTime) {
        if (directory == null) return null;
        
        java.io.File dir = new java.io.File(directory);
        if (!dir.exists() || !dir.isDirectory()) return null;
        
        java.io.File[] files = dir.listFiles((d, name) -> 
            name.endsWith(".mpg") || name.endsWith(".ts") || name.endsWith(".mp4"));
        
        if (files == null || files.length == 0) return null;
        
        // 找到在录像开始时间之后修改的最新文件
        java.io.File latestFile = null;
        for (java.io.File file : files) {
            // 只考虑在录像开始时间之后修改的文件（允许1秒误差）
            if (file.lastModified() >= startTime - 1000) {
                if (latestFile == null || file.lastModified() > latestFile.lastModified()) {
                    latestFile = file;
                }
            }
        }
        
        return latestFile != null ? latestFile.getAbsolutePath() : null;
    }

    @Override
    public boolean takeSnapshot(String filePath, int width, int height) {
        if (!isSupportSnapshot()) {
            notifyOnSnapshotError("截图功能不可用");
            return false;
        }
        
        if (mVlcPlayer == null || mVlcPlayer.getMediaPlayer() == null) {
            notifyOnSnapshotError("播放器未初始化");
            return false;
        }
        
        try {
            // 确保目录存在
            java.io.File file = new java.io.File(filePath);
            java.io.File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            
            RecordEvent recordEvent = new RecordEvent();
            int result = recordEvent.takeSnapshot(mVlcPlayer.getMediaPlayer(), filePath, width, height);
            
            if (result == 0) {
                notifyOnSnapshotSuccess(filePath);
                return true;
            } else {
                notifyOnSnapshotError("截图失败，错误码: " + result);
                return false;
            }
        } catch (Exception e) {
            notifyOnSnapshotError("截图异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取内部 VlcPlayer 实例
     * 用于高级操作
     */
    public VlcPlayer getVlcPlayer() {
        return mVlcPlayer;
    }

    /**
     * Surface 销毁时调用
     */
    public void onSurfaceDestroyed() {
        mIsSurfaceReady = false;
        if (mVlcPlayer != null) {
            mVlcPlayer.onSurfaceTextureDestroyedUI();
        }
    }

    /**
     * 设置是否启用视频轨道缓存清理
     * 开启后可以减少横竖屏切换时的黑屏时间
     * @param enable true 启用（推荐），false 禁用
     */
    public void setClearVideoTrackCache(boolean enable) {
        if (mVlcPlayer != null) {
            mVlcPlayer.clearVideoTrackCache = enable;
        }
    }

    /**
     * 横竖屏切换时调用，用于优化切换体验
     * 在 Activity 的 onConfigurationChanged 中调用此方法
     */
    public void onOrientationChanged() {
        if (mVlcPlayer != null && mVlcPlayer.isPrepare()) {
            // 启用缓存清理，减少黑屏
            mVlcPlayer.clearVideoTrackCache = true;
        }
    }
    
    /**
     * 设置是否是横屏模式
     * 横屏时视频铺满整个屏幕（SURFACE_FILL 模式）
     * 竖屏时保持宽高比居中显示（SURFACE_BEST_FIT 模式）
     * @param isLandscape 是否横屏
     */
    public void setLandscapeMode(boolean isLandscape) {
        if (mVlcPlayer != null && mVlcPlayer.getMediaPlayer() != null) {
            org.videolan.libvlc.MediaPlayer mediaPlayer = mVlcPlayer.getMediaPlayer();
            if (isLandscape) {
                // 横屏：铺满屏幕
                mediaPlayer.setVideoScale(org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_FILL);
            } else {
                // 竖屏：保持宽高比
                mediaPlayer.setVideoScale(org.videolan.libvlc.MediaPlayer.ScaleType.SURFACE_BEST_FIT);
            }
        }
    }
}
