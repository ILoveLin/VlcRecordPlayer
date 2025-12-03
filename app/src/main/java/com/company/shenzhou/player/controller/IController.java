package com.company.shenzhou.player.controller;

import com.company.shenzhou.player.core.VideoPlayerManager;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 控制器接口
 */
public interface IController {

    /**
     * 绑定播放器管理器
     */
    void bindPlayerManager(VideoPlayerManager playerManager);

    /**
     * 显示控制器
     */
    void show();

    /**
     * 隐藏控制器
     */
    void hide();

    /**
     * 是否正在显示
     */
    boolean isShowing();

    /**
     * 设置是否锁定
     */
    void setLocked(boolean locked);

    /**
     * 是否锁定
     */
    boolean isLocked();

    /**
     * 更新进度
     */
    void updateProgress();

    /**
     * 释放资源
     */
    void release();
}
