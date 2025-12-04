package com.company.shenzhou.player.render;

import android.graphics.Bitmap;
import android.view.Surface;
import android.view.View;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 渲染视图接口
 */
public interface IRenderView {

    /**
     * 获取视图
     * @return View
     */
    View getView();

    /**
     * 设置Surface监听器
     * @param listener 监听器
     */
    void setSurfaceListener(SurfaceListener listener);

    /**
     * 设置视频尺寸
     * @param videoWidth 视频宽度
     * @param videoHeight 视频高度
     */
    void setVideoSize(int videoWidth, int videoHeight);

    /**
     * 设置宽高比类型
     * @param aspectRatioType 宽高比类型
     */
    void setAspectRatioType(@AspectRatioType int aspectRatioType);

    /**
     * 获取Surface
     * @return Surface
     */
    Surface getSurface();

    /**
     * 释放资源
     */
    void release();
    
    /**
     * 设置是否启用居中变换
     * VLC 内核自己处理居中，不需要额外变换（传 false）
     * IJK 和系统内核需要通过变换来居中（传 true）
     * @param enabled 是否启用
     */
    default void setCenterTransformEnabled(boolean enabled) {
        // 默认空实现，子类可以覆盖
    }

    /**
     * 截取当前画面
     * @return 当前画面的 Bitmap，如果不支持或失败返回 null
     */
    default Bitmap captureFrame() {
        return null;
    }

    /**
     * Surface监听器
     */
    interface SurfaceListener {
        /**
         * Surface 可用时调用（包含尺寸信息）
         * 参考 VlcVideoView 的实现，VLC 需要在同一个回调中先设置 WindowSize 再设置 Surface
         * @param surface Surface
         * @param width 宽度
         * @param height 高度
         */
        void onSurfaceAvailable(Surface surface, int width, int height);
        
        /**
         * Surface 尺寸变化时调用
         */
        void onSurfaceChanged(Surface surface, int width, int height);
        
        /**
         * Surface 销毁时调用
         */
        void onSurfaceDestroyed(Surface surface);
    }
}
