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
    private SurfaceTexture mSavedSurfaceTexture;
    private MeasureHelper mMeasureHelper;
    private boolean mIsSurfaceAvailable = false;

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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] measuredSize = mMeasureHelper.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredSize[0], measuredSize[1]);
    }

    // ==================== SurfaceTextureListener ====================

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        // 如果有保存的 SurfaceTexture，复用它以避免切换屏幕时的黑屏
        if (mSavedSurfaceTexture != null) {
            setSurfaceTexture(mSavedSurfaceTexture);
            // 尺寸可能变化，通知监听器
            if (mSurfaceListener != null && mSurface != null) {
                mSurfaceListener.onSurfaceChanged(mSurface, width, height);
            }
        } else {
            mSavedSurfaceTexture = surfaceTexture;
            if (mSurface != null) {
                mSurface.release();
            }
            mSurface = new Surface(surfaceTexture);
            mIsSurfaceAvailable = true;
            
            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceAvailable(mSurface, width, height);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (mSurfaceListener != null && mSurface != null) {
            mSurfaceListener.onSurfaceChanged(mSurface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        android.util.Log.d("TextureRenderView", "onSurfaceTextureDestroyed called");
        // 返回 false 表示我们自己管理 SurfaceTexture 的生命周期
        // 这样切换屏幕时不会销毁 SurfaceTexture，避免黑屏
        // 只有在 release() 时才真正销毁
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // 每帧更新时调用，通常不需要处理
    }

    @Override
    public void release() {
        mIsSurfaceAvailable = false;
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mSavedSurfaceTexture != null) {
            mSavedSurfaceTexture.release();
            mSavedSurfaceTexture = null;
        }
        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceDestroyed(null);
        }
    }
}
