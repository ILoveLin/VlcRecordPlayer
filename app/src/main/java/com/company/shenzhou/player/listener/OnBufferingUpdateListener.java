package com.company.shenzhou.player.listener;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 缓冲进度更新监听器
 */
public interface OnBufferingUpdateListener {
    /**
     * 缓冲进度更新
     * @param percent 缓冲百分比 0-100
     */
    void onBufferingUpdate(int percent);
}
