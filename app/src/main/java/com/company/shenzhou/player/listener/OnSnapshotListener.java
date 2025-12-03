package com.company.shenzhou.player.listener;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 截图监听器
 */
public interface OnSnapshotListener {
    /**
     * 截图成功
     * @param filePath 截图文件路径
     */
    void onSnapshotSuccess(String filePath);

    /**
     * 截图失败
     * @param errorMsg 错误信息
     */
    void onSnapshotError(String errorMsg);
}
