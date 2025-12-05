package com.company.shenzhou.player.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
    
    // 视频尺寸
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    // 是否启用居中变换（用于 IJK 和系统内核）
    private boolean mCenterCropEnabled = true;
    // 是否是横屏模式（横屏时铺满屏幕）
    private boolean mIsLandscape = false;

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
            mVideoWidth = videoWidth;
            mVideoHeight = videoHeight;
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
            // 更新变换矩阵以居中显示视频（仅对非 VLC 内核生效）
            updateTextureViewTransform();
        }
    }

    @Override
    public void setAspectRatioType(@AspectRatioType int aspectRatioType) {
        mMeasureHelper.setAspectRatioType(aspectRatioType);
        requestLayout();
        updateTextureViewTransform();
    }
    
    /**
     * 设置是否启用居中变换
     * VLC 内核自己处理居中，不需要额外变换（传 false）
     * IJK 和系统内核需要通过 Matrix 变换来居中（传 true）
     */
    @Override
    public void setCenterTransformEnabled(boolean enabled) {
        mCenterCropEnabled = enabled;
        updateTextureViewTransform();
    }
    
    /**
     * 设置是否是横屏模式
     * 横屏时视频铺满整个屏幕（CENTER_CROP 效果）
     * 竖屏时保持宽高比居中显示（FIT_CENTER 效果）
     */
    @Override
    public void setLandscapeMode(boolean isLandscape) {
        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            requestLayout();
            updateTextureViewTransform();
        }
    }

    @Override
    public Surface getSurface() {
        return mSurface;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 如果启用了居中变换（IJK/系统内核），则使用父容器的完整尺寸
        // 视频居中通过 Matrix 变换实现
        // 如果未启用居中变换（VLC），则使用 MeasureHelper 计算的尺寸
        if (mCenterCropEnabled) {
            // IJK 和系统内核：使用父容器的完整尺寸，通过 Matrix 变换居中
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
        } else {
            // VLC：使用 MeasureHelper 计算的尺寸，VLC 内部处理居中
            int[] measuredSize = mMeasureHelper.measure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(measuredSize[0], measuredSize[1]);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 视图尺寸变化时更新变换矩阵
        updateTextureViewTransform();
    }
    
    /**
     * 更新 TextureView 的变换矩阵
     * 统一使用 FIT_CENTER 模式：保持宽高比居中显示（与 VLC 行为一致）
     * 仅对 IJK 和系统内核生效（mCenterCropEnabled = true）
     * VLC 内核自己处理，不需要额外变换
     */
    private void updateTextureViewTransform() {
        if (!mCenterCropEnabled || mVideoWidth <= 0 || mVideoHeight <= 0) {
            // VLC 内核或视频尺寸未知，重置变换
            setTransform(null);
            return;
        }
        
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        
        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }
        
        // 计算视频和视图的宽高比
        float videoRatio = (float) mVideoWidth / mVideoHeight;
        float viewRatio = (float) viewWidth / viewHeight;
        
        float scaleX, scaleY;
        float translateX = 0, translateY = 0;
        
        // FIT_CENTER 模式：保持宽高比，适应视图，居中显示（横竖屏统一）
        if (videoRatio > viewRatio) {
            // 视频更宽，以宽度为基准缩放，垂直居中
            scaleX = 1.0f;
            scaleY = viewRatio / videoRatio;
            translateY = (viewHeight - viewHeight * scaleY) / 2;
        } else {
            // 视频更高，以高度为基准缩放，水平居中
            scaleX = videoRatio / viewRatio;
            scaleY = 1.0f;
            translateX = (viewWidth - viewWidth * scaleX) / 2;
        }
        
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY);
        matrix.postTranslate(translateX, translateY);
        setTransform(matrix);
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

    /**
     * 截取当前画面
     * 临时移除变换矩阵获取原始视频帧，然后恢复变换
     * @return 当前画面的 Bitmap，如果失败返回 null
     */
    @Override
    public Bitmap captureFrame() {
        if (!mIsSurfaceAvailable) {
            return null;
        }
        try {
            // 如果没有启用居中变换，直接获取
            if (!mCenterCropEnabled || mVideoWidth <= 0 || mVideoHeight <= 0) {
                return getBitmap();
            }
            
            // 保存当前变换矩阵
            Matrix currentTransform = getTransform(null);
            
            // 临时移除变换矩阵，获取原始内容
            setTransform(null);
            
            // 获取原始 Bitmap（此时是未变换的，视频会填满整个 TextureView）
            Bitmap rawBitmap = getBitmap();
            
            // 恢复变换矩阵
            setTransform(currentTransform);
            
            if (rawBitmap == null) {
                return null;
            }
            
            // 原始 Bitmap 是视频帧拉伸到 TextureView 尺寸的结果
            // 需要将其缩放到正确的视频宽高比
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            
            if (viewWidth <= 0 || viewHeight <= 0) {
                return rawBitmap;
            }
            
            // 计算目标尺寸（保持视频宽高比）
            float videoRatio = (float) mVideoWidth / mVideoHeight;
            int targetWidth, targetHeight;
            
            // 使用视频的原始宽高比来确定输出尺寸
            // 以较大的边为基准
            if (viewWidth > viewHeight) {
                targetWidth = viewWidth;
                targetHeight = (int) (viewWidth / videoRatio);
            } else {
                targetHeight = viewHeight;
                targetWidth = (int) (viewHeight * videoRatio);
            }
            
            // 确保尺寸合理
            targetWidth = Math.max(1, targetWidth);
            targetHeight = Math.max(1, targetHeight);
            
            // 创建正确宽高比的 Bitmap
            Bitmap resultBitmap = Bitmap.createScaledBitmap(rawBitmap, targetWidth, targetHeight, true);
            
            // 释放原始 Bitmap
            if (resultBitmap != rawBitmap) {
                rawBitmap.recycle();
            }
            
            return resultBitmap;
        } catch (Exception e) {
            android.util.Log.e("TextureRenderView", "captureFrame failed: " + e.getMessage());
            return null;
        }
    }
}
