package com.company.shenzhou.player.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.company.shenzhou.player.controller.VideoController;
import com.company.shenzhou.player.core.PlayerState;
import com.company.shenzhou.player.core.PlayerType;
import com.company.shenzhou.player.core.VideoPlayerManager;
import com.company.shenzhou.player.listener.OnCompletionListener;
import com.company.shenzhou.player.listener.OnErrorListener;
import com.company.shenzhou.player.listener.OnPreparedListener;
import com.company.shenzhou.player.listener.PlayerStateListener;
import com.company.shenzhou.player.render.AspectRatioType;
import com.company.shenzhou.player.render.IRenderView;
import com.company.shenzhou.player.render.TextureRenderView;

import java.util.Map;

/**
 *
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 统一视频视图，整合播放器内核、渲染视图和控制器
 */
public class UniversalVideoView extends FrameLayout {

    private VideoPlayerManager mPlayerManager;
    private IRenderView mRenderView;
    private VideoController mController;
    
    // 全屏相关
    private boolean mIsFullscreen = false;
    private ViewGroup mOriginalParent;
    private int mOriginalIndex;
    private ViewGroup.LayoutParams mOriginalParams;
    
    // 监听器
    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;

    public UniversalVideoView(Context context) {
        this(context, null);
    }

    public UniversalVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UniversalVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(0xFF000000);
        
        // 创建播放器管理器
        mPlayerManager = new VideoPlayerManager(context);
        
        // 创建渲染视图
        mRenderView = new TextureRenderView(context);
        LayoutParams renderParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mRenderView.getView(), renderParams);
        
        // 绑定渲染视图
        mPlayerManager.setRenderView(mRenderView);
        
        // 创建控制器
        mController = new VideoController(context);
        LayoutParams controllerParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mController, controllerParams);
        
        // 绑定控制器
        mController.bindPlayerManager(mPlayerManager);
        
        // 设置全屏按钮点击监听
        mController.setOnFullscreenClickListener(() -> toggleFullscreen());
        
        // 监听状态变化
        mPlayerManager.addStateListener(mInternalStateListener);
    }

    // ==================== 播放控制 ====================

    /**
     * 设置播放器内核类型
     */
    public void setPlayerType(@PlayerType int type) {
        mPlayerManager.setPlayerType(type);
    }

    /**
     * 播放视频
     */
    public void play(String url) {
        play(url, null);
    }

    /**
     * 播放视频（带请求头）
     */
    public void play(String url, Map<String, String> headers) {
        mPlayerManager.play(url, headers);
    }

    /**
     * 开始播放
     */
    public void start() {
        mPlayerManager.start();
    }

    /**
     * 暂停
     */
    public void pause() {
        mPlayerManager.pause();
    }

    /**
     * 停止
     */
    public void stop() {
        mPlayerManager.stop();
    }

    /**
     * 重置
     */
    public void reset() {
        mPlayerManager.reset();
    }

    /**
     * 释放资源
     */
    public void release() {
        mController.release();
        mPlayerManager.release();
    }

    /**
     * 跳转
     */
    public void seekTo(long position) {
        mPlayerManager.seekTo(position);
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return mPlayerManager.isPlaying();
    }

    /**
     * 获取当前位置
     */
    public long getCurrentPosition() {
        return mPlayerManager.getCurrentPosition();
    }

    /**
     * 获取总时长
     */
    public long getDuration() {
        return mPlayerManager.getDuration();
    }

    /**
     * 设置音量
     */
    public void setVolume(float left, float right) {
        mPlayerManager.setVolume(left, right);
    }

    /**
     * 设置播放速度
     * 注意：直播流不支持倍速播放，会被忽略
     */
    public void setSpeed(float speed) {
        // 直播流不支持倍速播放
        if (isLiveStream()) {
            return;
        }
        mPlayerManager.setSpeed(speed);
    }

    /**
     * 获取播放速度
     */
    public float getSpeed() {
        return mPlayerManager.getSpeed();
    }

    /**
     * 是否是直播流
     * 直播流特征：无法获取总时长（duration <= 0）
     */
    public boolean isLiveStream() {
        if (mController != null) {
            return mController.isLiveStream();
        }
        // 备用判断：duration <= 0 表示直播流
        return mPlayerManager.getDuration() <= 0;
    }

    /**
     * 判断当前流是否支持进度拖动（seek）
     * 点播流返回 true，直播流返回 false
     * @return true 支持进度拖动
     */
    public boolean isSeekable() {
        return mPlayerManager != null && mPlayerManager.isSeekable();
    }

    /**
     * 设置循环播放
     */
    public void setLooping(boolean looping) {
        mPlayerManager.setLooping(looping);
    }

    /**
     * 设置宽高比类型
     */
    public void setAspectRatioType(@AspectRatioType int type) {
        if (mRenderView != null) {
            mRenderView.setAspectRatioType(type);
        }
    }

    /**
     * 获取当前状态
     */
    public PlayerState getState() {
        return mPlayerManager.getState();
    }

    // ==================== 录像和截图功能 ====================

    /**
     * 是否支持录像
     */
    public boolean isSupportRecord() {
        return mPlayerManager.isSupportRecord();
    }

    /**
     * 是否支持截图
     */
    public boolean isSupportSnapshot() {
        return mPlayerManager.isSupportSnapshot();
    }

    /**
     * 开始录像
     * @param directory 保存目录
     * @param fileName 文件名（不含扩展名）
     */
    public boolean startRecord(String directory, String fileName) {
        return mPlayerManager.startRecord(directory, fileName);
    }

    /**
     * 停止录像
     */
    public boolean stopRecord() {
        return mPlayerManager.stopRecord();
    }

    /**
     * 是否正在录像
     */
    public boolean isRecording() {
        return mPlayerManager.isRecording();
    }

    /**
     * 截图
     * @param filePath 保存路径
     */
    public boolean takeSnapshot(String filePath) {
        return mPlayerManager.takeSnapshot(filePath);
    }

    /**
     * 截图（指定尺寸）
     */
    public boolean takeSnapshot(String filePath, int width, int height) {
        return mPlayerManager.takeSnapshot(filePath, width, height);
    }

    /**
     * 设置录像保存目录
     */
    public void setRecordDirectory(String directory) {
        if (mController != null) {
            mController.setRecordDirectory(directory);
        }
    }

    /**
     * 设置截图保存目录
     */
    public void setSnapshotDirectory(String directory) {
        if (mController != null) {
            mController.setSnapshotDirectory(directory);
        }
    }

    // ==================== 全屏控制 ====================

    /**
     * 进入全屏
     */
    public void enterFullscreen() {
        if (mIsFullscreen) return;
        
        Activity activity = getActivity();
        if (activity == null) return;
        
        // 保存原始状态
        mOriginalParent = (ViewGroup) getParent();
        mOriginalIndex = mOriginalParent.indexOfChild(this);
        mOriginalParams = getLayoutParams();
        
        // 从原父容器移除
        mOriginalParent.removeView(this);
        
        // 添加到 DecorView
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        LayoutParams fullscreenParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        decorView.addView(this, fullscreenParams);
        
        // 设置横屏
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        mIsFullscreen = true;
        
        // 同步控制器状态
        if (mController != null) {
            mController.setFullscreen(true);
        }
    }

    /**
     * 退出全屏
     */
    public void exitFullscreen() {
        if (!mIsFullscreen) return;
        
        Activity activity = getActivity();
        if (activity == null) return;
        
        // 从 DecorView 移除
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        decorView.removeView(this);
        
        // 恢复到原父容器
        mOriginalParent.addView(this, mOriginalIndex, mOriginalParams);
        
        // 恢复竖屏
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mIsFullscreen = false;
        
        // 同步控制器状态
        if (mController != null) {
            mController.setFullscreen(false);
        }
    }

    /**
     * 切换全屏状态
     */
    public void toggleFullscreen() {
        if (mIsFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    /**
     * 是否全屏
     */
    public boolean isFullscreen() {
        return mIsFullscreen;
    }

    /**
     * 横竖屏切换时调用，优化 VLC 切换体验
     * 在 Activity 的 onConfigurationChanged 中调用
     */
    public void onOrientationChanged() {
        if (mPlayerManager != null) {
            mPlayerManager.onOrientationChanged();
        }
    }

    // ==================== 控制器设置 ====================

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        if (mController != null) {
            mController.setTitle(title);
        }
    }

    /**
     * 设置返回按钮点击监听
     */
    public void setOnBackClickListener(VideoController.OnBackClickListener listener) {
        if (mController != null) {
            mController.setOnBackClickListener(listener);
        }
    }

    // ==================== 自定义按钮管理 ====================

    /**
     * 添加自定义按钮到右侧栏
     * @param button 自定义按钮配置
     */
    public void addCustomButton(com.company.shenzhou.player.controller.CustomButton button) {
        if (mController != null) {
            mController.addCustomButton(button);
        }
    }

    /**
     * 移除自定义按钮
     * @param buttonId 按钮ID
     */
    public void removeCustomButton(int buttonId) {
        if (mController != null) {
            mController.removeCustomButton(buttonId);
        }
    }

    /**
     * 更新自定义按钮图标
     * @param buttonId 按钮ID
     * @param iconResId 新图标资源ID
     */
    public void updateCustomButtonIcon(int buttonId, int iconResId) {
        if (mController != null) {
            mController.updateCustomButtonIcon(buttonId, iconResId);
        }
    }

    /**
     * 设置自定义按钮可见性
     * @param buttonId 按钮ID
     * @param visible 是否可见
     */
    public void setCustomButtonVisible(int buttonId, boolean visible) {
        if (mController != null) {
            mController.setCustomButtonVisible(buttonId, visible);
        }
    }

    /**
     * 清除所有自定义按钮
     */
    public void clearCustomButtons() {
        if (mController != null) {
            mController.clearCustomButtons();
        }
    }

    /**
     * 设置自定义按钮排序值
     * @param buttonId 按钮ID
     * @param order 排序值（数字越小越靠上）
     */
    public void setCustomButtonOrder(int buttonId, int order) {
        if (mController != null) {
            mController.setCustomButtonOrder(buttonId, order);
        }
    }

    /**
     * 设置录像按钮排序值
     * @param order 排序值（数字越小越靠上，默认10）
     */
    public void setRecordOrder(int order) {
        if (mController != null) {
            mController.setRecordOrder(order);
        }
    }

    /**
     * 设置截图按钮排序值
     * @param order 排序值（数字越小越靠上，默认20）
     */
    public void setSnapshotOrder(int order) {
        if (mController != null) {
            mController.setSnapshotOrder(order);
        }
    }

    /**
     * 获取控制器（用于高级自定义）
     */
    public VideoController getController() {
        return mController;
    }

    // ==================== 图标自定义接口 ====================

    /** 设置返回按钮图标 */
    public void setBackIcon(int resId) {
        if (mController != null) mController.setBackIcon(resId);
    }

    /** 设置返回按钮可见性 */
    public void setBackVisible(boolean visible) {
        if (mController != null) mController.setBackVisible(visible);
    }

    /** 设置播放/暂停按钮图标 */
    public void setPlayPauseIcon(int playResId, int pauseResId) {
        if (mController != null) mController.setPlayPauseIcon(playResId, pauseResId);
    }

    /** 设置播放/暂停按钮可见性 */
    public void setPlayPauseVisible(boolean visible) {
        if (mController != null) mController.setPlayPauseVisible(visible);
    }

    /** 设置全屏按钮图标 */
    public void setFullscreenIcon(int enterResId, int exitResId) {
        if (mController != null) mController.setFullscreenIcon(enterResId, exitResId);
    }

    /** 设置全屏按钮可见性 */
    public void setFullscreenVisible(boolean visible) {
        if (mController != null) mController.setFullscreenVisible(visible);
    }

    /** 设置静音按钮图标 */
    public void setMuteIcon(int muteResId, int unmuteResId) {
        if (mController != null) mController.setMuteIcon(muteResId, unmuteResId);
    }

    /** 设置静音按钮可见性 */
    public void setMuteVisible(boolean visible) {
        if (mController != null) mController.setMuteVisible(visible);
    }

    /** 设置锁屏按钮图标 */
    public void setLockIcon(int lockResId, int unlockResId) {
        if (mController != null) mController.setLockIcon(lockResId, unlockResId);
    }

    /** 设置锁屏按钮可见性 */
    public void setLockVisible(boolean visible) {
        if (mController != null) mController.setLockVisible(visible);
    }

    /** 设置录像按钮图标 */
    public void setRecordIcon(int normalResId, int activeResId) {
        if (mController != null) mController.setRecordIcon(normalResId, activeResId);
    }

    /** 设置录像按钮可见性 */
    public void setRecordVisible(boolean visible) {
        if (mController != null) mController.setRecordVisible(visible);
    }

    /** 设置截图按钮图标 */
    public void setSnapshotIcon(int resId) {
        if (mController != null) mController.setSnapshotIcon(resId);
    }

    /** 设置截图按钮可见性 */
    public void setSnapshotVisible(boolean visible) {
        if (mController != null) mController.setSnapshotVisible(visible);
    }

    /** 设置顶部栏可见性 */
    public void setTopBarVisible(boolean visible) {
        if (mController != null) mController.setTopBarVisible(visible);
    }

    /** 设置底部栏可见性 */
    public void setBottomBarVisible(boolean visible) {
        if (mController != null) mController.setBottomBarVisible(visible);
    }

    /** 设置左侧栏可见性 */
    public void setLeftBarVisible(boolean visible) {
        if (mController != null) mController.setLeftBarVisible(visible);
    }

    /** 设置右侧栏可见性 */
    public void setRightBarVisible(boolean visible) {
        if (mController != null) mController.setRightBarVisible(visible);
    }

    /** 设置进度条可见性 */
    public void setSeekBarVisible(boolean visible) {
        if (mController != null) mController.setSeekBarVisible(visible);
    }

    /** 设置时间显示可见性 */
    public void setTimeVisible(boolean visible) {
        if (mController != null) mController.setTimeVisible(visible);
    }

    // ==================== 监听器设置 ====================

    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    // ==================== 私有方法 ====================

    private Activity getActivity() {
        Context context = getContext();
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;
    }

    private final PlayerStateListener mInternalStateListener = state -> {
        switch (state) {
            case PREPARED:
                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPrepared();
                }
                break;
            case COMPLETED:
                if (mOnCompletionListener != null) {
                    mOnCompletionListener.onCompletion();
                }
                break;
            case ERROR:
                if (mOnErrorListener != null) {
                    mOnErrorListener.onError(-1, "Playback error");
                }
                break;
        }
    };
}
