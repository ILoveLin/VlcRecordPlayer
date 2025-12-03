package com.company.shenzhou.player.listener;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器信息监听器
 */
public interface OnInfoListener {
    /**
     * 播放器信息回调
     * @param what 信息类型
     * @param extra 额外信息
     */
    void onInfo(int what, int extra);
}
