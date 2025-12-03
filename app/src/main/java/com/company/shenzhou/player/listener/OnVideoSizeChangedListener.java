package com.company.shenzhou.player.listener;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 视频尺寸变化监听器
 */
public interface OnVideoSizeChangedListener {
    /**
     * 视频尺寸变化回调
     * @param width 视频宽度
     * @param height 视频高度
     */
    void onVideoSizeChanged(int width, int height);
}
