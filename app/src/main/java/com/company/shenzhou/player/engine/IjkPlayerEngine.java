package com.company.shenzhou.player.engine;

import android.content.Context;
import android.view.Surface;

import com.company.shenzhou.player.core.BasePlayerEngine;
import com.company.shenzhou.player.core.PlayerType;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : IJK 播放器内核实现，支持 RTSP、RTMP、HTTP 等多种协议，支持硬解码
 */
public class IjkPlayerEngine extends BasePlayerEngine {

    private IjkMediaPlayer mMediaPlayer;
    private boolean mIsPrepared = false;

    @Override
    public void init(Context context) {
        super.init(context);
        initPlayer();
    }

    private void initPlayer() {
        mMediaPlayer = new IjkMediaPlayer();
        
        // 设置IJK选项
        setIjkOptions();
        
        // 设置监听器
        mMediaPlayer.setOnPreparedListener(mp -> {
            mIsPrepared = true;
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            notifyOnPrepared();
        });
        
        mMediaPlayer.setOnCompletionListener(mp -> {
            if (mIsLooping) {
                mMediaPlayer.seekTo(0);
                mMediaPlayer.start();
            } else {
                notifyOnCompletion();
            }
        });
        
        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            notifyOnError(what, "IJK error: " + what + ", extra: " + extra);
            return true;
        });
        
        mMediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            notifyOnBufferingUpdate(percent);
        });
        
        mMediaPlayer.setOnVideoSizeChangedListener((mp, width, height, sarNum, sarDen) -> {
            mVideoWidth = width;
            mVideoHeight = height;
            notifyOnVideoSizeChanged(width, height);
        });
        
        mMediaPlayer.setOnInfoListener((mp, what, extra) -> {
            notifyOnInfo(what, extra);
            return true;
        });
    }

    /**
     * 设置IJK播放器选项
     */
    private void setIjkOptions() {
        // 设置播放前的探测时间
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10);
        
        // 设置播放前的最大探测时间
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
        
        // 关闭播放器缓冲
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
        
        // 跳帧处理
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
        
        // 最大缓冲大小
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 1024);
        
        // 设置无限读取
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1L);
        
        // 设置不限制拉流缓存大小
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 2L);
        
        // 启用硬解码
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1L);
        
        // 设置是否开启环路过滤
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48L);
        
        // 设置播放重连次数
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1L);
        
        // RTSP设置
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
        
        // 设置超时
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10000000L);
        
        // 设置是否开启变调
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1L);
        
        // 设置seekTo能够快速seek到指定位置并播放
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1L);
    }

    @Override
    public void setSurface(Surface surface) {
        super.setSurface(surface);
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
        }
    }

    @Override
    public void setDataSource(String url, Map<String, String> headers) {
        super.setDataSource(url, headers);
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDataSource(url, headers);
            }
        } catch (Exception e) {
            e.printStackTrace();
            notifyOnError(-1, "setDataSource error: " + e.getMessage());
        }
    }

    @Override
    public void prepare() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void prepareAsync() {
        prepare();
    }

    @Override
    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mIsPrepared = false;
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            setIjkOptions();
        }
        mUrl = null;
        mIsPrepared = false;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void seekTo(long position) {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public void setVolume(float left, float right) {
        super.setVolume(left, right);
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(left, right);
        }
    }

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(speed);
        if (mMediaPlayer != null) {
            mMediaPlayer.setSpeed(speed);
        }
    }

    @Override
    public float getSpeed() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getSpeed(1.0f);
        }
        return 1.0f;
    }

    @Override
    public void setLooping(boolean looping) {
        super.setLooping(looping);
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        }
    }

    @Override
    public boolean isLooping() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isLooping();
        }
        return mIsLooping;
    }

    @Override
    public int getPlayerType() {
        return PlayerType.IJK;
    }

    @Override
    public boolean isSupportRecord() {
        return false; // IJK不支持录像
    }

    @Override
    public boolean isSupportSnapshot() {
        return false; // IJK不支持截图
    }

    /**
     * 获取内部IjkMediaPlayer实例
     */
    public IjkMediaPlayer getIjkMediaPlayer() {
        return mMediaPlayer;
    }
}
