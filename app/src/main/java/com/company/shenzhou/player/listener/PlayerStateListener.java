package com.company.shenzhou.player.listener;

import com.company.shenzhou.player.core.PlayerState;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器状态监听器
 */
public interface PlayerStateListener {
    /**
     * 播放器状态变化回调
     * @param state 当前状态
     */
    void onStateChanged(PlayerState state);
}
