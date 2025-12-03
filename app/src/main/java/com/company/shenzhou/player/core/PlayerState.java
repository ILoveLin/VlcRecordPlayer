package com.company.shenzhou.player.core;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器状态枚举
 */
public enum PlayerState {
    IDLE,           // 空闲
    INITIALIZED,    // 已初始化
    PREPARING,      // 准备中
    PREPARED,       // 准备完成
    PLAYING,        // 播放中
    PAUSED,         // 暂停
    BUFFERING,      // 缓冲中
    COMPLETED,      // 播放完成
    STOPPED,        // 已停止
    ERROR,          // 错误
    RELEASED        // 已释放
}
