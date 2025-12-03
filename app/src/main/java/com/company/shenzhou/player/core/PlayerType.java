package com.company.shenzhou.player.core;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器内核类型定义
 */
@IntDef({PlayerType.VLC, PlayerType.MEDIA_PLAYER, PlayerType.EXO, PlayerType.IJK, PlayerType.TENCENT})
@Retention(RetentionPolicy.SOURCE)
public @interface PlayerType {
    int VLC = 0;
    int MEDIA_PLAYER = 1;
    int EXO = 2;
    int IJK = 3;
    int TENCENT = 4;
}
