package com.vlc.lib;


import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.vlc.lib.listener.MediaListenerEvent;
import com.vlc.lib.listener.MediaPlayerControl;
import com.vlc.lib.listener.VideoSizeChange;
import com.vlc.lib.listener.util.LogUtils;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.util.VLCUtil;

import java.util.ArrayList;

/**
 * 这个类实现了大部分播放器功能
 * 只是提供参考的实例代码
 * <p>
 * 如果有bug请在github留言
 */
public class VlcVideoView extends TextureView implements MediaPlayerControl, VideoSizeChange {
    private VlcPlayer videoMediaLogic;
    private final String tag = "VlcVideoView";

    public VlcVideoView(Context context) {
        this(context, null);
    }

    public VlcVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VlcVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        initPlayer(context);
        setSurfaceTextureListener(videoSurfaceListener);
    }

    //这里改为子类继承   方便修改libVLC参数
    public void initPlayer(Context context) {

//        videoMediaLogic = new VlcPlayer(VLCInstance.get(context));
//        videoMediaLogic.setVideoSizeChange(this);


        if (videoMediaLogic != null) {
            videoMediaLogic.onStop();
            videoMediaLogic.onDestroy();
        }
        ArrayList<String> options = new ArrayList<String>();
        //正式参数配置
        //值越大，缓存越大，延迟越大。这三项是延迟设置
        options.add("--network-caching=300");//网络缓存
        options.add(":clock-jitter=0");
        options.add(":clock-synchro=0");

        options.add("--rtsp-caching=300");//
        options.add("--tcp-caching=300");//TCP输入缓存值 (毫秒)
        options.add("--realrtsp-caching=300");//RTSP缓存值 (毫秒)
        options.add(":file-caching=300");//文件缓存
        options.add(":live-cacheing=300");//直播缓存
        options.add("--file-caching");//文件缓存
        options.add("--sout-mux-caching=300");//输出缓存
        options.add("--no-drop-late-frames");//关闭丢弃晚的帧 (默认打开)
        options.add("--no-skip-frames");//关闭跳过帧 (默认打开)
        options.add(":rtsp-frame-buffer-size=1000"); //RTSP帧缓冲大小，默认大小为100000
        options.add("--rtsp-tcp");
        options.add("--http-reconnect");    //: 重连
        options.add("--deinterlace");    //: 交错
        options.add("" + getDeblocking(-1));//这里太大了消耗性能   太小了会花屏
        options.add("--deinterlace-mode={discard,blend,mean,bob,linear,x}");// 视频译码器 解除交错模式
        options.add("--network-synchronisation");// 网络同步化 (默认关闭)


//        options.add(":sout-record-dst-prefix=yylpre.mp4");

//        options.add("--sub-source=marq{marquee=\"%Y-%m-%d,%H:%M:%S \",position=10,color=0xFFFFFF,size=40}");  //添加系统时间


//        options.add("--audio-time-stretch");
//        options.add("--sub-source=marq{marquee=\"%Y-%m-%d,%H:%M:%S\",position=10,color=0xFF0000,size=40}");//这行是可以再vlc窗口右下角添加当前时间的

//        =======================注释不执行参数============================
//        options.add(":file-caching=1500");
//        options.add("--codec=mediacodec,iomx,all");//文件缓存
//        options.add("--drop-late-frames");//关闭丢弃晚的帧 (默认打开)
//        options.add("--skip-frames");//关闭跳过帧 (默认打开)
//        options.add("--live-caching=1500");//直播缓存
//        options.add("--sout-mux-caching=1500");//输出缓存

//        --drop-late-frames, --no-drop-late-frames  // 丢弃晚的帧 (默认打开)
//        --skip-frames, --no-skip-frames   //    跳过帧 (默认打开)
//        ===================================================

//        options.add("--rtsp-tcp");   //强制rtsp-tcp，加快加载视频速度
//        options.add("--aout=opensles");
//        options.add(":network-caching=30");
//        options.add("--audio-time-stretch");//color=0xFF0000
//        options.add(":network-caching=30");//网络缓存
//        options.add(":file-caching=30");//文件缓存
//        options.add(":live-cacheing=30");//直播缓存
//        options.add(":latency=10");//直播缓存
//        options.add(":sout-mux-caching=30");//输出缓存
//        options.add("--no-drop-late-frames");//关闭丢弃晚的帧 (默认打开)
//        options.add("--no-skip-frames");//关闭跳过帧 (默认打开)
//        options.add(":codec=mediacodec,iomx,all");
//        options.add(":fullscreen");

        LibVLC libVLC = new LibVLC(context, options);

        videoMediaLogic = new VlcPlayer(libVLC);
        videoMediaLogic.setVideoSizeChange(this);
//        ===================================================

//        =========================案例--测试注释参数==========================
//        ArrayList<String> libOptions = VLCOptions.getLibOptions(getContext());
////        mMediaRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);
//        libOptions.add(":file-caching=100");//文件缓存
//        libOptions.add(":network-caching=100");//网络缓存
//        libOptions.add(":live-caching=100");//直播缓存
//        libOptions.add(":sout-mux-caching=100");//输出缓存
//        libOptions.add(":codec=mediacodec,iomx,all");
//        libOptions.add(":rtsp-frame-buffer-size=100"); //RTSP帧缓冲大小，默认大小为100000
//        libOptions.add(":rtsp-tcp");//RTSP采用TCP传输方式
//        libOptions.add(":sout-rtp-proto={dccp,sctp,tcp,udp,udplite}");//RTSP采用TCP传输方式
//        LibVLC libVLC = new LibVLC(getContext(), libOptions);
//        Media media = new Media(libVLC, Uri.parse(""));
//        media.setHWDecoderEnabled(true, true);
//
////        rtsp://root:root@192.168.129.39:7788/session0.mpg
//        //   media.addOption(":sout-record-dst-prefix=yylpre.mp4");
////        media.addOption(":network-caching=1000");
////        media.addOption(":rtsp-frame-buffer-size=100000");
////           media.addOption(":rtsp-tcp");
//        videoMediaLogic = new VlcPlayer(libVLC);
//        videoMediaLogic.setVideoSizeChange(this);

//        视频译码器
//        解除交错视频过滤器
//                --deinterlace-mode {discard,blend,mean,bob,linear,x}
//        解除交错模式
//                --sout-deinterlace-mode {discard,blend,mean,bob,linear,x}
//        串流解除交错模式

//        伪视频译码器
//        --fake-deinterlace, --no-fake-deinterlace
//        解除交错视频 (默认关闭)
//                --fake-deinterlace-module {deinterlace,ffmpeg-deinterlace}
    }


    public void setLibVLCPath(LibVLC libVLC) {
        videoMediaLogic.onDestroy();
        videoMediaLogic = new VlcPlayer(libVLC);
        videoMediaLogic.setVideoSizeChange(this);

//        rtsp://root:root@192.168.129.39:7788/session0.mpg
        //   media.addOption(":sout-record-dst-prefix=yylpre.mp4");
//        media.addOption(":network-caching=1000");
//        media.addOption(":rtsp-frame-buffer-size=100000");
//           media.addOption(":rtsp-tcp");
        Log.e("path=====Start:=====", "我是当前播放的url======setLibVLCPath======");


    }

    public void setMediaListenerEvent(MediaListenerEvent mediaListenerEvent) {
        videoMediaLogic.setMediaListenerEvent(mediaListenerEvent);
    }

    private static int getDeblocking(int deblocking) {
        int ret = deblocking;
        if (deblocking < 0) {
            /**
             * Set some reasonable sDeblocking defaults:
             *
             * Skip all (4) for armv6 and MIPS by default
             * Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
             * Skip non-key (3) for all devices that don't meet anything above
             */
            VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
            if (m == null)
                return ret;
            if ((m.hasArmV6 && !(m.hasArmV7)) || m.hasMips)
                ret = 4;
            else if (m.frequency >= 1200 && m.processors > 2)
                ret = 1;
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1;
                Log.d("TAG", "Used bogoMIPS due to lack of frequency info");
            } else
                ret = 3;
        } else if (deblocking > 4) { // sanity check
            ret = 3;
        }
        return ret;
    }

    @Override
    public boolean canControl() {//直播流不要用这个判断
        return videoMediaLogic.canControl();
    }

    @Override
    public void startPlay() {
        videoMediaLogic.startPlay();
    }

    @Override
    public void start() {
        videoMediaLogic.start();
    }

    @Override
    public void setPath(String path) {
        videoMediaLogic.setPath(path);
    }

    @Override
    public void seekTo(long pos) {
        videoMediaLogic.seekTo(pos);
    }

    /**
     * 关闭停止播放器时用
     */
    public void onStop() {
        videoMediaLogic.onStop();
    }
    /**
     * 暂停播放器时用
     */
    public void onPause() {
        videoMediaLogic.onPause();
    }

    /**
     * 退出界面时回收
     */
    public void onDestroy() {
        if (videoMediaLogic != null)
            videoMediaLogic.onDestroy();
        LogUtils.i(tag, "onDestory");
    }

    @Override
    public boolean isPrepare() {
        return videoMediaLogic.isPrepare();
    }


    @Override
    public void pause() {
        videoMediaLogic.pause();
    }

    public String getTime() {
        return videoMediaLogic.getTime();
    }

    @Override
    public int getDuration() {
        return videoMediaLogic.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return videoMediaLogic.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        videoMediaLogic.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return videoMediaLogic.isPlaying();
    }

    @Override
    public long getDrawingTime() {
        return super.getDrawingTime();
    }

    @Override
    public void setMirror(boolean mirror) {
        this.mirror = mirror;
        if (mirror) {
            setScaleX(-1f);
        } else {
            setScaleX(1f);
        }
    }

    private boolean mirror = false;

    @Override
    public boolean getMirror() {
        return mirror;
    }


    @Override
    public int getBufferPercentage() {
        return videoMediaLogic.getBufferPercentage();
    }


    @Override
    public boolean setPlaybackSpeedMedia(float speed) {
        return videoMediaLogic.setPlaybackSpeedMedia(speed);
    }

    @Override
    public float getPlaybackSpeed() {
        return videoMediaLogic.getPlaybackSpeed();
    }

    //切换parent时的2秒黑屏用seek恢复 注意：直播时要为false
    public void setClearVideoTrackCache(boolean clearVideoTrackCache) {
        videoMediaLogic.clearVideoTrackCache = clearVideoTrackCache;
    }

    @Override
    public void setLoop(boolean isLoop) {
        videoMediaLogic.setLoop(isLoop);
    }

    @Override
    public boolean isLoop() {
        return videoMediaLogic.isLoop();
    }


//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        if (isInEditMode()) {
//            return;
//        }
//        setKeepScreenOn(true);// 上层决定开关
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        if (isInEditMode()) {
//            return;
//        }
//        setKeepScreenOn(false);
//    }

    /**
     * 这里计算方法是以16:9为基础
     * 在当前view中显示video的最大videoWidth或者video的最大videoHeight
     * 只供参考 跟距产品需求适配
     *
     * @param videoWidth
     * @param videoHeight
     */
    public void adjustAspectRatio(int videoWidth, int videoHeight) {
        if (videoWidth * videoHeight == 0) {
            return;
        }
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        double videoRatio = (double) viewWidth / (double) viewHeight;//显示比例 (小屏16：9 大屏厂商手机比例  真乱)
        double aspectRatio = (double) videoWidth / (double) videoHeight;//视频比例
        int newWidth, newHeight;
        if (videoWidth > videoHeight) {//正常比例16：9
            if (videoRatio > aspectRatio) {//16:9>16:10
                newWidth = (int) (viewHeight * aspectRatio);
                newHeight = viewHeight;
            } else {//16:9<16:8
                newWidth = viewWidth;
                newHeight = (int) (viewWidth / aspectRatio);
            }
        } else {//非正常可能是 90度
            //16:9>1:9
            newWidth = (int) (viewHeight * aspectRatio);
            newHeight = viewHeight;
        }
        float xoff = (viewWidth - newWidth) / 2f;
        float yoff = (viewHeight - newHeight) / 2f;
        Matrix txform = new Matrix();
        getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight
                / viewHeight);
        // txform.postRotate(10); // just for fun
        txform.postTranslate(xoff, yoff);
        setTransform(txform);
        if (rotation == 180) {
            setRotation(180);
        } else {
            setRotation(0);
        }
        LogUtils.i(tag, "onVideoSizeChanged   newVideo=" + newWidth + "x" + newHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && mVideoWidth * mVideoHeight > 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    adjustAspectRatio(mVideoWidth, mVideoHeight);
                }
            });
        }
    }


    private int mVideoWidth;
    private int mVideoHeight;
    private int rotation = 0;

//    public int getVideoRotation() {
//        return rotation;
//    }
//
//    public int getVideoWidth() {
//        return mVideoWidth;
//    }
//
//    public int getVideoHeight() {
//        return mVideoHeight;
//    }

    public Media.VideoTrack getVideoTrack() {
        return videoMediaLogic.getVideoTrack();
    }

    /**
     * 如果设备支持opengl这里就不会调用
     *
     * @param width         w
     * @param height        h
     * @param visibleWidth  w
     * @param visibleHeight h
     */
    @Override
    public void onVideoSizeChanged(int width, int height, int visibleWidth, int visibleHeight) {
        LogUtils.i(tag, "onVideoSizeChanged   video=" + width + "x" + height + " visible="
                + visibleWidth + "x" + visibleHeight);
        if (width * height == 0) return;
        this.mVideoWidth = visibleWidth;
        this.mVideoHeight = visibleHeight;
        post(new Runnable() {
            @Override
            public void run() {
                adjustAspectRatio(mVideoWidth, mVideoHeight);
            }
        });
    }


    //设置字幕
    public void setAddSlave(String addSlave) {
        videoMediaLogic.setAddSlave(addSlave);
    }

    //定制播放器
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        videoMediaLogic.setMediaPlayer(mediaPlayer);
    }

    //定制media
    public void setMedia(IMedia iMedia) {
        videoMediaLogic.setMedia(iMedia);
    }

    public MediaPlayer getMediaPlayer() {
        return videoMediaLogic.getMediaPlayer();
    }

    public VlcPlayer getVlcPlayer() {
        return videoMediaLogic;
    }

    @Override
    public boolean canPause() {
        return videoMediaLogic.canPause();
    }

    @Override
    public boolean canSeekBackward() {
        return videoMediaLogic.canSeekBackward();
    }

    @Override
    public boolean canSeekForward() {
        return videoMediaLogic.canSeekForward();
    }

    @Override
    public int getAudioSessionId() {
        return videoMediaLogic.getAudioSessionId();
    }

    public int widthSurface = 0;
    public int heightSurface = 0;

    private TextureView.SurfaceTextureListener videoSurfaceListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            widthSurface = width;
            heightSurface = height;
            videoMediaLogic.setWindowSize(width, height);
            videoMediaLogic.setSurface(new Surface(surface), null);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            videoMediaLogic.setWindowSize(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            videoMediaLogic.onSurfaceTextureDestroyedUI();
            return true;//回收掉Surface
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

}