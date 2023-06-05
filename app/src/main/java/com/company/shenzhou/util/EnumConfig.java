package com.company.shenzhou.util;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/1/12 16:31
 * desc：
 */
public class EnumConfig {
    //播放模式
    public class VoiceType {
        public final static int HAVE_VOICE = 0;
        public final static int HAVE_NO_VOICE = 1;
    }
    //播放样式 展开、缩放
    public class PageType {
        public final static int EXPAND = 0;
        public final static int SHRINK = 1;
    }

    //进度条状态
    public class LockState {
        public final static int LOCK = 0;   //锁定
        public final static int UNLOCK = 1; //未锁定
    }

    //播放状态
    public class PlayState {
        public static final int STATE_PLAY = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_LOAD = 3;
        public static final int STATE_RESUME = 4;
        public static final int STATE_STOP = 5;
    }

    //播放状态显示加载,播放,错误view
    //播放状态
    public class PlayerState {
        public static final int PLAYER_SHOW_LOADING_VIEW = 1;        //显示加载view
        public static final int PLAYER_SHOW_PLAY_VIEW = 2;           //显示播放view
        public static final int PLAYER_SHOW_ERROR_VIEW = 3;          //显示错误view

        public static final int PLAYER_HIDE_LOADING_PLAY_VIEW = 4;   //隐藏加载view

    }
}
