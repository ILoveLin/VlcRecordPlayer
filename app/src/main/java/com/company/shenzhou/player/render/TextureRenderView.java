package com.company.shenzhou.player.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : TextureView 渲染视图，支持动画、截图等操作
 */
public class TextureRenderView extends TextureView implements IRenderView, TextureView.SurfaceTextureListener {

    private SurfaceListener mSurfaceListener;
    private Surface mSurface;
    private MeasureHelper mMeasureHelper;

    public TextureRenderView(Context context) {
        this(context, null);
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMeasureHelper = new MeasureHelper();
        setSurfaceTextureListener(this);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setSurfaceListener(SurfaceListener listener) {
        mSurfaceListener = listener;
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setAspectRatioType(@AspectRatioType int aspectRatioType) {
        mMeasureHelper.setAspectRatioType(aspectRatioType);
        requestLayout();
    }

    @Override
    public Surface getSurface() {
        return mSurface;
    }

    @Override
    public void release() {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] measuredSize = mMeasureHelper.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredSize[0], measuredSize[1]);
    }

    // ==================== SurfaceTextureListener ====================

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        // 参考 VlcVideoView 的实现：每次都创建新的 Surface
        // 因为 SurfaceTexture 可能被重新创建，旧的 Surface 可能无效
        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = new Surface(surfaceTexture);
        
        if (mSurfaceListener != null) {
            // 参考 VlcVideoView 的实现：在 onSurfaceTextureAvailable 中
            // 先通知尺寸变化，再通知 Surface 创建
            // 这样 VLC 可以在 setSurface 之前先设置 WindowSize
            mSurfaceListener.onSurfaceAvailable(mSurface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceChanged(mSurface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        android.util.Log.d("TextureRenderView", "onSurfaceTextureDestroyed called");
        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceDestroyed(mSurface);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // 每帧更新时调用，通常不需要处理
    }
}
