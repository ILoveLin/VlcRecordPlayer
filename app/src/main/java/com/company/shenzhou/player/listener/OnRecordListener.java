package com.company.shenzhou.player.listener;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 录像状态监听器
 */
public interface OnRecordListener {
    /**
     * 录像开始
     * @param filePath 录像文件路径
     */
    void onRecordStart(String filePath);

    /**
     * 录像停止
     * @param filePath 录像文件路径
     */
    void onRecordStop(String filePath);

    /**
     * 录像错误
     * @param errorMsg 错误信息
     */
    void onRecordError(String errorMsg);
}
