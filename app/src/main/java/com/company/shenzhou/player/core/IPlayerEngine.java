package com.company.shenzhou.player.core;

import android.content.Context;
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
 * desc   : 播放器内核接口，所有播放器内核（VLC、ExoPlayer、IjkPlayer等）都需要实现此接口
 */
public interface IPlayerEngine {

    /**
     * 初始化播放器
     * @param context 上下文
     */
    void init(Context context);

    /**
     * 设置渲染Surface
     * @param surface 渲染表面
     */
    void setSurface(Surface surface);

    /**
     * 设置数据源
     * @param url 播放地址
     */
    void setDataSource(String url);

    /**
     * 设置数据源（带请求头）
     * @param url 播放地址
     * @param headers 请求头
     */
    void setDataSource(String url, Map<String, String> headers);

    /**
     * 同步准备
     */
    void prepare();

    /**
     * 异步准备
     */
    void prepareAsync();

    /**
     * 开始播放
     */
    void start();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 重置播放器
     */
    void reset();

    /**
     * 释放播放器资源
     */
    void release();

    /**
     * 跳转到指定位置
     * @param position 位置（毫秒）
     */
    void seekTo(long position);

    /**
     * 获取当前播放位置
     * @return 当前位置（毫秒）
     */
    long getCurrentPosition();

    /**
     * 获取视频总时长
     * @return 总时长（毫秒）
     */
    long getDuration();

    /**
     * 是否正在播放
     * @return true 正在播放
     */
    boolean isPlaying();

    /**
     * 获取缓冲百分比
     * @return 缓冲百分比 0-100
     */
    int getBufferPercentage();

    /**
     * 设置音量
     * @param left 左声道 0.0-1.0
     * @param right 右声道 0.0-1.0
     */
    void setVolume(float left, float right);

    /**
     * 设置播放速度
     * @param speed 速度 0.5-2.0
     */
    void setSpeed(float speed);

    /**
     * 获取当前播放速度
     * @return 播放速度
     */
    float getSpeed();

    /**
     * 设置是否循环播放
     * @param looping 是否循环
     */
    void setLooping(boolean looping);

    /**
     * 是否循环播放
     * @return true 循环播放
     */
    boolean isLooping();

    /**
     * 获取视频宽度
     * @return 视频宽度
     */
    int getVideoWidth();

    /**
     * 获取视频高度
     * @return 视频高度
     */
    int getVideoHeight();

    /**
     * 获取播放器内核类型
     * @return 内核类型
     */
    @PlayerType
    int getPlayerType();

    /**
     * 判断当前流是否支持进度拖动（seek）
     * 点播流返回 true，直播流返回 false
     * @return true 支持进度拖动
     */
    boolean isSeekable();

    // ==================== 监听器设置 ====================

    void setOnPreparedListener(OnPreparedListener listener);

    void setOnCompletionListener(OnCompletionListener listener);

    void setOnErrorListener(OnErrorListener listener);

    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

    void setOnInfoListener(OnInfoListener listener);

    // ==================== 录像和截图功能 ====================

    /**
     * 是否支持录像功能
     * @return true 支持录像
     */
    boolean isSupportRecord();

    /**
     * 是否支持截图功能
     * @return true 支持截图
     */
    boolean isSupportSnapshot();

    /**
     * 开始录像
     * @param directory 录像保存目录
     * @param fileName 文件名（不含扩展名）
     * @return true 开始成功
     */
    boolean startRecord(String directory, String fileName);

    /**
     * 停止录像
     * @return true 停止成功
     */
    boolean stopRecord();

    /**
     * 是否正在录像
     * @return true 正在录像
     */
    boolean isRecording();

    /**
     * 获取录像文件路径
     * @return 录像文件路径
     */
    String getRecordFilePath();

    /**
     * 截图
     * @param filePath 截图保存路径
     * @param width 截图宽度（0 表示原始宽度）
     * @param height 截图高度（0 表示原始高度）
     * @return true 截图成功
     */
    boolean takeSnapshot(String filePath, int width, int height);

    /**
     * 设置录像监听器
     */
    void setOnRecordListener(OnRecordListener listener);

    /**
     * 设置截图监听器
     */
    void setOnSnapshotListener(OnSnapshotListener listener);
}
