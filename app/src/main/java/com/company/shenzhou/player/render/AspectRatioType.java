package com.company.shenzhou.player.render;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 视频宽高比类型
 */
@IntDef({
    AspectRatioType.FIT_PARENT,
    AspectRatioType.FILL_PARENT,
    AspectRatioType.WRAP_CONTENT,
    AspectRatioType.FIT_WIDTH,
    AspectRatioType.FIT_HEIGHT,
    AspectRatioType.RATIO_16_9,
    AspectRatioType.RATIO_4_3,
    AspectRatioType.RATIO_19_6
})
@Retention(RetentionPolicy.SOURCE)
public @interface AspectRatioType {
    int FIT_PARENT = 0;     // 适应父容器，保持宽高比
    int FILL_PARENT = 1;    // 填充父容器，可能裁剪
    int WRAP_CONTENT = 2;   // 原始尺寸
    int FIT_WIDTH = 3;      // 适应宽度
    int FIT_HEIGHT = 4;     // 适应高度
    int RATIO_16_9 = 5;     // 16:9
    int RATIO_4_3 = 6;      // 4:3
    int RATIO_19_6 = 7;     // 19:6
}
