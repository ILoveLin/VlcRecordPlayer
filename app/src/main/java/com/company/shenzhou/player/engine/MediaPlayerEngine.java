package com.company.shenzhou.player.engine;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;

import com.company.shenzhou.player.core.BasePlayerEngine;
import com.company.shenzhou.player.core.PlayerType;

import java.io.IOException;
import java.util.Map;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : Android 原生 MediaPlayer 内核实现，适用于本地视频和简单的 HTTP 流
 */
public class MediaPlayerEngine extends BasePlayerEngine {

    private MediaPlayer mMediaPlayer;
    private boolean mIsPrepared = false;

    @Override
    public void init(Context context) {
        super.init(context);
        createMediaPlayer();
    }

    private void createMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        
        // 设置音频属性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();
            mMediaPlayer.setAudioAttributes(audioAttributes);
        }
        
        // 设置监听器
        mMediaPlayer.setOnPreparedListener(mp -> {
            mIsPrepared = true;
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            notifyOnPrepared();
        });
        
        mMediaPlayer.setOnCompletionListener(mp -> {
            notifyOnCompletion();
        });
        
        mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
            notifyOnError(what, "MediaPlayer error: " + what + ", " + extra);
            return true;
        });
        
        mMediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            notifyOnBufferingUpdate(percent);
        });
        
        mMediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
            mVideoWidth = width;
            mVideoHeight = height;
            notifyOnVideoSizeChanged(width, height);
        });
        
        mMediaPlayer.setOnInfoListener((mp, what, extra) -> {
            notifyOnInfo(what, extra);
            return false;
        });
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
        
        if (mMediaPlayer == null) return;
        
        try {
            mMediaPlayer.reset();
            mIsPrepared = false;
            
            if (headers != null && !headers.isEmpty()) {
                mMediaPlayer.setDataSource(mContext, Uri.parse(url), headers);
            } else {
                mMediaPlayer.setDataSource(url);
            }
        } catch (IOException e) {
            notifyOnError(-1, "Failed to set data source: " + e.getMessage());
        }
    }

    @Override
    public void prepare() {
        if (mMediaPlayer == null) return;
        
        try {
            mMediaPlayer.prepare();
            mIsPrepared = true;
        } catch (IOException e) {
            notifyOnError(-1, "Failed to prepare: " + e.getMessage());
        }
    }

    @Override
    public void prepareAsync() {
        if (mMediaPlayer == null) return;
        
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            notifyOnError(-1, "Failed to prepareAsync: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mIsPrepared && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mIsPrepared = false;
        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mIsPrepared = false;
        }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mMediaPlayer.seekTo(position, MediaPlayer.SEEK_CLOSEST);
            } else {
                mMediaPlayer.seekTo((int) position);
            }
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
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
        if (mMediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mMediaPlayer.setPlaybackParams(mMediaPlayer.getPlaybackParams().setSpeed(speed));
            } catch (Exception e) {
                // 某些设备可能不支持
            }
        }
    }

    @Override
    public float getSpeed() {
        if (mMediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return mMediaPlayer.getPlaybackParams().getSpeed();
            } catch (Exception e) {
                return mSpeed;
            }
        }
        return mSpeed;
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
        return PlayerType.MEDIA_PLAYER;
    }
}
