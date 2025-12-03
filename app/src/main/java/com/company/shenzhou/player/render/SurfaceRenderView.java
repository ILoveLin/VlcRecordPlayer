package com.company.shenzhou.player.render;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : SurfaceView 渲染视图，性能更好，但不支持动画
 */
public class SurfaceRenderView extends SurfaceView implements IRenderView, SurfaceHolder.Callback {

    private SurfaceListener mSurfaceListener;
    private MeasureHelper mMeasureHelper;

    public SurfaceRenderView(Context context) {
        this(context, null);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMeasureHelper = new MeasureHelper();
        getHolder().addCallback(this);
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
        return getHolder().getSurface();
    }

    @Override
    public void release() {
        // SurfaceView 由系统管理，无需手动释放
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] measuredSize = mMeasureHelper.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredSize[0], measuredSize[1]);
    }

    // ==================== SurfaceHolder.Callback ====================
    
    // 保存尺寸，用于 surfaceCreated 时传递
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // SurfaceView 的 surfaceCreated 在 surfaceChanged 之前调用
        // 但此时可能还没有尺寸，需要等 surfaceChanged
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mSurfaceListener != null) {
            if (mSurfaceWidth == 0 && mSurfaceHeight == 0) {
                // 第一次获取尺寸，调用 onSurfaceAvailable
                mSurfaceWidth = width;
                mSurfaceHeight = height;
                mSurfaceListener.onSurfaceAvailable(holder.getSurface(), width, height);
            } else {
                // 尺寸变化
                mSurfaceWidth = width;
                mSurfaceHeight = height;
                mSurfaceListener.onSurfaceChanged(holder.getSurface(), width, height);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceWidth = 0;
        mSurfaceHeight = 0;
        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceDestroyed(holder.getSurface());
        }
    }
}
