package com.company.shenzhou.player.listener;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放错误监听器
 */
public interface OnErrorListener {
    /**
     * 播放错误回调
     * @param errorCode 错误码
     * @param errorMsg 错误信息
     * @return true 表示已处理错误，false 表示未处理
     */
    boolean onError(int errorCode, String errorMsg);
}
