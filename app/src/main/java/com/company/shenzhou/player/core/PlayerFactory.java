package com.company.shenzhou.player.core;

import android.content.Context;

import com.company.shenzhou.player.engine.IjkPlayerEngine;
import com.company.shenzhou.player.engine.MediaPlayerEngine;
import com.company.shenzhou.player.engine.VlcPlayerEngine;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 播放器工厂，根据类型创建对应的播放器内核
 */
public class PlayerFactory {

    /**
     * 创建播放器内核
     * @param context 上下文
     * @param type 播放器类型
     * @return 播放器内核实例
     */
    public static IPlayerEngine create(Context context, @PlayerType int type) {
        IPlayerEngine engine;
        
        switch (type) {
            case PlayerType.VLC:
                engine = new VlcPlayerEngine();
                break;
            case PlayerType.IJK:
                engine = new IjkPlayerEngine();
                break;
            case PlayerType.EXO:
                // TODO: 实现 ExoPlayerEngine
                engine = new VlcPlayerEngine(); // 暂时使用 VLC
                break;
            case PlayerType.TENCENT:
                // TODO: 实现 TencentPlayerEngine
                engine = new VlcPlayerEngine(); // 暂时使用 VLC
                break;
            case PlayerType.MEDIA_PLAYER:
            default:
                engine = new MediaPlayerEngine();
                break;
        }
        
        engine.init(context);
        return engine;
    }
}
