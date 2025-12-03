package com.company.shenzhou.player.render;

import android.view.View;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 视频尺寸测量辅助类
 */
public class MeasureHelper {

    private int mVideoWidth;
    private int mVideoHeight;
    @AspectRatioType
    private int mAspectRatioType = AspectRatioType.FIT_PARENT;

    public void setVideoSize(int videoWidth, int videoHeight) {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }

    public void setAspectRatioType(@AspectRatioType int aspectRatioType) {
        mAspectRatioType = aspectRatioType;
    }

    /**
     * 测量视图尺寸
     * @return [width, height]
     */
    public int[] measure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // 对于固定比例类型，即使没有视频尺寸也要应用比例
        boolean isFixedRatio = (mAspectRatioType == AspectRatioType.RATIO_16_9 
                || mAspectRatioType == AspectRatioType.RATIO_4_3 
                || mAspectRatioType == AspectRatioType.RATIO_19_6);

        if ((mVideoWidth <= 0 || mVideoHeight <= 0) && !isFixedRatio) {
            width = widthSize;
            height = heightSize;
            return new int[]{width, height};
        }

        switch (mAspectRatioType) {
            case AspectRatioType.FILL_PARENT:
                width = widthSize;
                height = heightSize;
                break;

            case AspectRatioType.WRAP_CONTENT:
                width = mVideoWidth;
                height = mVideoHeight;
                break;

            case AspectRatioType.FIT_WIDTH:
                width = widthSize;
                height = (int) (widthSize * 1.0f / mVideoWidth * mVideoHeight);
                break;

            case AspectRatioType.FIT_HEIGHT:
                height = heightSize;
                width = (int) (heightSize * 1.0f / mVideoHeight * mVideoWidth);
                break;

            case AspectRatioType.RATIO_16_9:
                if (widthSize * 9 > heightSize * 16) {
                    width = heightSize * 16 / 9;
                    height = heightSize;
                } else {
                    width = widthSize;
                    height = widthSize * 9 / 16;
                }
                break;

            case AspectRatioType.RATIO_4_3:
                if (widthSize * 3 > heightSize * 4) {
                    width = heightSize * 4 / 3;
                    height = heightSize;
                } else {
                    width = widthSize;
                    height = widthSize * 3 / 4;
                }
                break;

            case AspectRatioType.RATIO_19_6:
                // 19:6 比例
                if (widthSize * 6 > heightSize * 19) {
                    width = heightSize * 19 / 6;
                    height = heightSize;
                } else {
                    width = widthSize;
                    height = widthSize * 6 / 19;
                }
                break;

            case AspectRatioType.FIT_PARENT:
            default:
                // 保持宽高比适应父容器
                float videoRatio = mVideoWidth * 1.0f / mVideoHeight;
                float containerRatio = widthSize * 1.0f / heightSize;

                if (videoRatio > containerRatio) {
                    width = widthSize;
                    height = (int) (widthSize / videoRatio);
                } else {
                    height = heightSize;
                    width = (int) (heightSize * videoRatio);
                }
                break;
        }

        return new int[]{width, height};
    }
}
