package com.company.shenzhou.player.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 弧形进度视图，用于显示亮度和音量调节，支持平滑动画过渡
 */
public class ArcProgressView extends View {

    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private RectF mArcRect;
    
    private int mTargetProgress = 50;
    private float mCurrentProgress = 50f;
    private int mMax = 100;
    
    private int mBackgroundColor = 0x4DFFFFFF;
    private int mProgressColor = 0xFFFFFFFF;
    private float mStrokeWidth;
    
    // 弧形起始角度和扫过角度
    private static final float START_ANGLE = 135f;
    private static final float SWEEP_ANGLE = 270f;
    
    // 动画相关
    private ValueAnimator mAnimator;
    private static final int ANIMATION_DURATION = 100; // 动画时长ms
    private boolean mEnableAnimation = true;

    public ArcProgressView(Context context) {
        this(context, null);
    }

    public ArcProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStrokeWidth = dp2px(6);
        
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mStrokeWidth);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        mArcRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = mStrokeWidth / 2;
        mArcRect.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制背景弧
        canvas.drawArc(mArcRect, START_ANGLE, SWEEP_ANGLE, false, mBackgroundPaint);
        
        // 绘制进度弧（使用当前动画进度值）
        float progressSweep = SWEEP_ANGLE * mCurrentProgress / mMax;
        if (progressSweep > 0) {
            canvas.drawArc(mArcRect, START_ANGLE, progressSweep, false, mProgressPaint);
        }
    }

    /**
     * 设置进度（带动画）
     */
    public void setProgress(int progress) {
        int newProgress = Math.max(0, Math.min(mMax, progress));
        
        if (mTargetProgress == newProgress) {
            return;
        }
        
        mTargetProgress = newProgress;
        
        if (mEnableAnimation) {
            animateToProgress(newProgress);
        } else {
            mCurrentProgress = newProgress;
            invalidate();
        }
    }

    /**
     * 设置进度（无动画，直接设置）
     */
    public void setProgressImmediate(int progress) {
        mTargetProgress = Math.max(0, Math.min(mMax, progress));
        mCurrentProgress = mTargetProgress;
        cancelAnimation();
        invalidate();
    }

    /**
     * 动画过渡到目标进度
     */
    private void animateToProgress(int targetProgress) {
        cancelAnimation();
        
        mAnimator = ValueAnimator.ofFloat(mCurrentProgress, targetProgress);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mCurrentProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    /**
     * 取消动画
     */
    private void cancelAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    /**
     * 获取进度
     */
    public int getProgress() {
        return mTargetProgress;
    }

    /**
     * 设置最大值
     */
    public void setMax(int max) {
        mMax = max;
        invalidate();
    }

    /**
     * 设置进度颜色
     */
    public void setProgressColor(int color) {
        mProgressColor = color;
        mProgressPaint.setColor(color);
        invalidate();
    }

    /**
     * 设置背景颜色
     */
    public void setBackgroundArcColor(int color) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(color);
        invalidate();
    }

    /**
     * 设置是否启用动画
     */
    public void setAnimationEnabled(boolean enabled) {
        mEnableAnimation = enabled;
    }

    /**
     * 设置弧线宽度
     */
    public void setStrokeWidth(float widthDp) {
        mStrokeWidth = dp2px(widthDp);
        mBackgroundPaint.setStrokeWidth(mStrokeWidth);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
        if (getWidth() > 0 && getHeight() > 0) {
            float padding = mStrokeWidth / 2;
            mArcRect.set(padding, padding, getWidth() - padding, getHeight() - padding);
        }
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
    }

    private float dp2px(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
