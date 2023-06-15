//package com.company.shenzhou.ffmpeg;
//
//import android.graphics.Bitmap;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.arthenica.ffmpegkit.FFmpegKit;
//import com.arthenica.ffmpegkit.FFmpegKitConfig;
//import com.arthenica.ffmpegkit.FFmpegSession;
//import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
//import com.arthenica.ffmpegkit.LogCallback;
//import com.arthenica.ffmpegkit.ReturnCode;
//import com.arthenica.ffmpegkit.SessionState;
//import com.arthenica.ffmpegkit.Statistics;
//import com.arthenica.ffmpegkit.StatisticsCallback;
//import com.company.shenzhou.R;
//import com.company.shenzhou.util.FileUtil;
//import com.company.shenzhou.util.LogUtils;
//import com.github.chrisbanes.photoview.PhotoView;
//import com.hjq.permissions.OnPermissionCallback;
//import com.hjq.permissions.Permission;
//import com.hjq.permissions.XXPermissions;
//import com.tencent.mmkv.MMKV;
//import com.zhy.http.okhttp.OkHttpUtils;
//import com.zhy.http.okhttp.callback.StringCallback;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Random;
//
//import okhttp3.Call;
//import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;
//import xyz.doikki.videoplayer.player.BaseVideoView;
//import xyz.doikki.videoplayer.player.VideoView;
//
///**
// * company：江西神州医疗设备有限公司
// * author： LoveLin
// * time：2023/6/1 11:18
// * desc：测试http相关
// *
// */
//public class FFmpegHttpActivity extends AppCompatActivity implements View.OnClickListener {
//    private Button tv_click, tv_cancle;
//    private VideoView mVideoPlayer;
//    private VideoView mAudioPlayer;
//    private Button tv_shot;
//    private Button tv_16_9;
//    private Button tv_no_voice;
//    private PhotoView imageView;
//    private Button tv_open_voice;
//    private Button scale_default;
//    private Button scale_original;
//    private Button tv_save_cmd;
//    private Button tv_save_live;
//    private EditText tt_live;
//    private EditText tt_cmd;
//    //电视台直播流
//    //private String mVideoPath = "http://220.161.87.62:8800/hls/0/index.m3u8";
//    private String mVideoPath = "http://192.168.67.105:3333/api/stream/video?session=123456";       //默认 纯视频流地址
//    private String mAudioPath = "http://192.168.67.105:3333/api/stream/audio?session=123456";       //默认 纯音频流地址
//    //private String CMD = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -i http://192.168.67.105:3333/api/stream/audio?session=123456 -c copy ";
//    //公司视频流和音频流
//    private String CMD = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -i http://192.168.67.105:3333/api/stream/audio?session=123456 -c copy ";
//    //电视台直播流录像
//    private String CMD2 = "-i http://220.161.87.62:8800/hls/0/index.m3u8 -c copy ";
//    //ffmpeg -i http://192.168.67.105:3333/api/stream/video?session=123456 -y -t 0.001 -ss 1 -f image2 -r 1    截图   格式通过 MediaStore.Images.Media.MIME_TYPE, "image/jpeg"更改
//    private String CMD3 = "-i http://220.161.87.62:8800/hls/0/index.m3u8 -y -t 0.001 -ss 1 -f image2 -r 1 ";
//    //    private String CMD3 = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -y -t 0.001 -ss 1 -f image2 -r 1 ";
//    private Button tv_shot_ffmpeg;
//    private MMKV mmkv;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_ffmpeg);
//        getXXPermissions();
//        initView();
//        initData();
//        responseListener();
//    }
//
//    private void responseListener() {
//        tv_click.setOnClickListener(this);
//        tv_cancle.setOnClickListener(this);
//        tv_shot.setOnClickListener(this);
//        tv_16_9.setOnClickListener(this);
//        scale_default.setOnClickListener(this);
//        scale_original.setOnClickListener(this);
//        tv_no_voice.setOnClickListener(this);
//        tv_save_live.setOnClickListener(this);
//        tv_shot_ffmpeg.setOnClickListener(this);
//
//        mVideoPlayer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(FFmpegHttpActivity.this, "播放器-被点击了", Toast.LENGTH_SHORT).show();
//
//            }
//        });
//
//        //直播监听
//        mVideoPlayer.setOnStateChangeListener(new BaseVideoView.OnStateChangeListener() {
//            @Override
//            public void onPlayerStateChanged(int playerState) {
//
//            }
//
//            @Override
//            public void onPlayStateChanged(int playState) {
//                switch (playState) {
//                    case VideoView.STATE_ERROR:
//                        LogUtils.e("FFmpegActivity" + "播放器状态====错误==error");
//                        break;
//                    case VideoView.STATE_IDLE:
//                        LogUtils.e("FFmpegActivity" + "播放器状态====闲置==idle");
//                        break;
//                    case VideoView.STATE_PREPARED:
//                        LogUtils.e("FFmpegActivity" + "播放器状态====准备==prepared");
//                        break;
//                    case VideoView.STATE_PLAYING:
//                        int[] videoSize = mVideoPlayer.getVideoSize();
//                        LogUtils.e("FFmpegActivity" + "播放器状态====播放中==playing");
//                        LogUtils.e("FFmpegActivity" + "播放器状态====视频宽=" + videoSize[0]);
//                        LogUtils.e("FFmpegActivity" + "播放器状态====视频高=" + videoSize[1]);
//                        break;
//                    case VideoView.STATE_PAUSED:
//                        LogUtils.e("FFmpegActivity" + "播放器状态====暂停==paused");
//                        break;
//                    case VideoView.STATE_BUFFERING:
//                        LogUtils.e("FFmpegActivity" + "播放器状态====缓冲中==buffering");
//                        break;
//                    case VideoView.STATE_BUFFERED:
//                        LogUtils.e("FFmpegActivity" + "播放器状态====缓冲完毕==buffered");
//                        break;
//                    case VideoView.STATE_PLAYBACK_COMPLETED:
//                        LogUtils.e("FFmpegActivity" + "播放器状态====回放_已完成==playback_completed");
//                        break;
//                }
//
//            }
//        });
//
//    }
//
//    private void initData() {
//        mmkv = MMKV.defaultMMKV();
//        //使用IjkPlayer解码
//        mVideoPlayer.setPlayerFactory(IjkPlayerFactory.create());
//        mAudioPlayer.setPlayerFactory(IjkPlayerFactory.create());
//        mVideoPlayer.setUrl(mVideoPath); //设置视频地址
////        StandardVideoController controller = new StandardVideoController(this);
////        controller.addDefaultControlComponent("标题", false);
////        videoView.setVideoController(controller); //设置控制器
//        mVideoPlayer.start(); //开始播放，不调用则不自动播放
//
//        mAudioPlayer.setUrl(mAudioPath); //设置视频地址
////        StandardVideoController controller = new StandardVideoController(this);
////        controller.addDefaultControlComponent("标题", false);
////        videoView.setVideoController(controller); //设置控制器
//        mAudioPlayer.start(); //开始播放，不调用则不自动播放
//
//
//    }
//
//    private void initView() {
//        mVideoPlayer = findViewById(R.id.player_video);
//        mAudioPlayer = findViewById(R.id.player_audio);
//        tv_shot = findViewById(R.id.tv_shot);
//        tv_16_9 = findViewById(R.id.tv_16_9);
//        tv_no_voice = findViewById(R.id.tv_mute);
//        tv_cancle = findViewById(R.id.tv_cancle);
//        imageView = findViewById(R.id.tv_bg_shop);
//        tv_click = findViewById(R.id.tv_click);
//        scale_default = findViewById(R.id.scale_default);
//        scale_original = findViewById(R.id.scale_original);
//        tv_save_live = findViewById(R.id.tv_save_live);
//        tt_live = findViewById(R.id.tt_live);
//        tv_shot_ffmpeg = findViewById(R.id.tv_shot_ffmpeg);
//
//
//    }
//
//
//    private void startFFmpegShot(String XSession) {
//        String path = "/FFMPEG_CME";
//        //String CMD3 = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -y -t 0.001 -ss 1 -f image2 -r 1 ";
//
//        String mCurrentCMD = "-i http://192.168.67.105:3333/api/stream/video?session=" + XSession + " -y -t 0.001 -ss 1 -f image2 -r 1 ";
//
//        //获取uri地址
////        Uri uri = FileUtils.publicDirURI(FFmpegActivity.this, getVideoNameByTime(), path, false);
//        Uri uri = FileUtil.createImagePathUri(FFmpegHttpActivity.this);
//        //ffmpeg-kit 文档要求这么去获取 outputVideoPath  路径
//        String outputVideoPath = FFmpegKitConfig.getSafParameter(FFmpegHttpActivity.this, uri, "rw");
//        LogUtils.e("FFmpegActivity--截图" + "path====path=" + path);
//        LogUtils.e("FFmpegActivity--截图" + "uri====uri=" + uri);
//        LogUtils.e("FFmpegActivity--截图" + "CMD====CMD=" + mCurrentCMD);
//        LogUtils.e("FFmpegActivity--截图" + "CMD====outputVideoPath=全部 =" + mCurrentCMD + outputVideoPath);
//
//        FFmpegKit.executeAsync(mCurrentCMD + outputVideoPath, new FFmpegSessionCompleteCallback() {
//
//            @Override
//            public void apply(FFmpegSession session) {
//                SessionState state = session.getState();
//                ReturnCode returnCode = session.getReturnCode();
//                /**
//                 * 录像
//                 * returnCode=1         说明:录像,失败
//                 * returnCode=255       说明:录像,成功
//                 * returnCode=0         说明:截图,成功
//                 */
//                LogUtils.e("FFmpegActivity--截图" + "apply====state=" + state);
//                LogUtils.e("FFmpegActivity--截图" + "apply====returnCode=" + returnCode);
//                if ("1".equals(returnCode + "")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(FFmpegHttpActivity.this, "截图失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } else if ("0".equals(returnCode + "")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(FFmpegHttpActivity.this, "截图成功", Toast.LENGTH_SHORT).show();
//
////                            imageView.setImageBitmap(bitmap);
//                            imageView.setImageURI(uri);
//                        }
//                    });
//                }
//
//            }
//        }, new LogCallback() {
//
//            @Override
//            public void apply(com.arthenica.ffmpegkit.Log log) {
//
//            }
//        }, new StatisticsCallback() {
//
//            @Override
//            public void apply(Statistics statistics) {
//                LogUtils.e("FFmpegActivity" + "apply====statistics=" + statistics.toString());
//
//
//            }
//        });
//
//    }
//
//
//    private String startPath = "http://192.168.67.105:3333/api/begin";
//    private String endPath = "http://192.168.67.105:3333/api/end";
//    private void sendGetXSessionRequest(String type) {
//
//        startBeginRequest(type);
//
////
////        OkHttpUtils.post()
////                .addParams("Authorization", "Basic YWRtaW46ODI3Y2NiMGVlYThhNzA2YzRjMzRhMTY4OTFmODRlN2I=")
////                .url(endPath)
////                .build()
////                .execute(new StringCallback() {
////                    @Override
////                    public void onError(Call call, Exception e, int id) {
////                        LogUtils.e("FFmpegActivity--end-请求:" + "path====Exception=" + e);
////                        Toast.makeText(FFmpegHttpActivity.this, "end-请求-失败", Toast.LENGTH_SHORT).show();
////
////                    }
////
////                    @Override
////                    public void onResponse(String response, int id) {
////                        LogUtils.e("FFmpegActivity--end-请求:" + "path====response=" + response);
////
////
////                    }
////                });
//
//
//    }
//
//
//
//    private void startBeginRequest(String type) {
//        OkHttpUtils.post()
//                .addParams("Authorization", "Basic YWRtaW46ODI3Y2NiMGVlYThhNzA2YzRjMzRhMTY4OTFmODRlN2I=")
//                .url(startPath)
//                .build()
//                .execute(new StringCallback() {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        LogUtils.e("FFmpegActivity--begin-请求:" + "path====Exception=" + e);
//
//                        Toast.makeText(FFmpegHttpActivity.this, "begin-请求-失败", Toast.LENGTH_SHORT).show();
//
//                    }
//
//                    @Override
//                    public void onResponse(String response, int id) {
//                        LogUtils.e("FFmpegActivity--begin-请求:" + "path====response=" + response);
//                        String XSession = mmkv.decodeString("RC200XSession");
//                        LogUtils.e("FFmpegActivity--begin-请求:" + "path====XSession=" + XSession);
//                        if ("record".equals(type)) {
//                            startFFmpegRecord(XSession);
//                        } else {
//                            startFFmpegShot(XSession);
//
//                        }
//
//                    }
//                });
//    }
//    private void startFFmpegRecord(String XSession) {
//        String path = "/FFMPEG_CME";
//        //String CMD = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -i http://192.168.67.105:3333/api/stream/audio?session=123456 -c copy ";
//
//        String mCurrentCMD = "-i http://192.168.67.105:3333/api/stream/video?session=" + XSession + " -i http://192.168.67.105:3333/api/stream/audio?session=" + XSession + " -c copy ";
//        //获取uri地址
//        Uri uri = FileUtil.createVideoPathUri(FFmpegHttpActivity.this);
////        Uri uri = FileUtils.publicDirURI(FFmpegActivity.this, getVideoNameByTime(), path, true);
//        //ffmpeg-kit 文档要求这么去获取 outputVideoPath  路径
//        String outputVideoPath = FFmpegKitConfig.getSafParameter(FFmpegHttpActivity.this, uri, "rw");
//        LogUtils.e("FFmpegActivity--录像" + "path====path=" + path);
//        LogUtils.e("FFmpegActivity--录像" + "uri====uri=" + uri);
//        LogUtils.e("FFmpegActivity--录像" + "CMD====CMD=" + mCurrentCMD);
//        LogUtils.e("FFmpegActivity--录像" + "CMD====outputVideoPath=全部 =" + mCurrentCMD + outputVideoPath);
//
//        FFmpegKit.executeAsync(mCurrentCMD + outputVideoPath, new FFmpegSessionCompleteCallback() {
//
//            @Override
//            public void apply(FFmpegSession session) {
//                SessionState state = session.getState();
//                ReturnCode returnCode = session.getReturnCode();
//                LogUtils.e("FFmpegActivity" + "apply====state=" + state);            //COMPLETED  完成 并且255=成功
//                LogUtils.e("FFmpegActivity" + "apply====returnCode=" + returnCode);  //COMPLETED  完成 并且1=失败
//                if ("1".equals(returnCode + "")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(FFmpegHttpActivity.this, "录制失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } else if ("255".equals(returnCode + "")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(FFmpegHttpActivity.this, "录制成功", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } else if ("0".equals(returnCode + "")) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(FFmpegHttpActivity.this, "截图成功", Toast.LENGTH_SHORT).show();
//
////                            imageView.setImageBitmap(bitmap);
//                            imageView.setImageURI(uri);
//                        }
//                    });
//                }
//
//            }
//        }, new LogCallback() {
//
//            @Override
//            public void apply(com.arthenica.ffmpegkit.Log log) {
//
//
//            }
//        }, new StatisticsCallback() {
//
//            @Override
//            public void apply(Statistics statistics) {
//                LogUtils.e("FFmpegActivity--录像" + "apply====statistics=" + statistics.toString());
//
//                // CALLED WHEN SESSION GENERATES STATISTICS
//
//            }
//        });
//
//
//    }
//
//
//    private void getXXPermissions() {
//
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
////        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
//            XXPermissions.with(this)
////                .permission(Permission.READ_EXTERNAL_STORAGE)  //正式版本    android 10已下申请  READ_EXTERNAL_STORAGE   WRITE_EXTERNAL_STORAGE
////                .permission(Permission.WRITE_EXTERNAL_STORAGE)  //正式版本
//                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
//                    .request(new OnPermissionCallback() {
//
//                        @Override
//                        public void onGranted(List<String> permissions, boolean all) {
//                            LogUtils.e("FFmpegActivity" + "成功====all=" + all);
//
//
//                        }
//
//                        @Override
//                        public void onDenied(List<String> permissions, boolean never) {
//                            LogUtils.e("FFmpegActivity" + "失败=====" + never);
//
//
//                        }
//                    });
//
//        } else {
//
////        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
//            XXPermissions.with(this)
//                    .permission(Permission.READ_EXTERNAL_STORAGE)  //正式版本    android 10已下申请  READ_EXTERNAL_STORAGE   WRITE_EXTERNAL_STORAGE
//                    .permission(Permission.WRITE_EXTERNAL_STORAGE)  //正式版本
////                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
//                    .request(new OnPermissionCallback() {
//
//                        @Override
//                        public void onGranted(List<String> permissions, boolean all) {
//                            LogUtils.e("FFmpegActivity" + "成功====all=" + all);
//
//
//                        }
//
//                        @Override
//                        public void onDenied(List<String> permissions, boolean never) {
//                            LogUtils.e("FFmpegActivity" + "失败=====" + never);
//
//
//                        }
//                    });
//
//        }
//
//
//    }
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mAudioPlayer.pause();
//        mVideoPlayer.pause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mAudioPlayer.resume();
//        mVideoPlayer.resume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mAudioPlayer.release();
//        mVideoPlayer.release();
//    }
//
//
//    @Override
//    public void onBackPressed() {
//        if (!mAudioPlayer.onBackPressed()) {
//            super.onBackPressed();
//        }
//        if (!mVideoPlayer.onBackPressed()) {
//            super.onBackPressed();
//        }
//    }
//
//
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.tv_click:          //执行ffmpeg命令
//                sendGetXSessionRequest("record");
//                break;
//            case R.id.tv_cancle:         //取消ffmpeg命令
//                FFmpegKit.cancel();
//                break;
//            case R.id.tv_shot:          //截图
//                Toast.makeText(FFmpegHttpActivity.this, "截图", Toast.LENGTH_SHORT).show();
//                Bitmap bitmap = mVideoPlayer.doScreenShot();
//                imageView.setImageBitmap(bitmap);
//                MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, "IMG" + Calendar.getInstance().getTime(), null);
//                break;
//            case R.id.tv_shot_ffmpeg:          //截图
//                sendGetXSessionRequest("shot");
//                break;
//            case R.id.tv_16_9:          //16:9
//                Toast.makeText(FFmpegHttpActivity.this, "16:9", Toast.LENGTH_SHORT).show();
//                mVideoPlayer.setScreenScaleType(VideoView.SCREEN_SCALE_16_9);
//                break;
//            case R.id.scale_default:     //默认大小
//                Toast.makeText(FFmpegHttpActivity.this, "默认大小", Toast.LENGTH_SHORT).show();
//                mVideoPlayer.setScreenScaleType(VideoView.SCREEN_SCALE_DEFAULT);
//                break;
//            case R.id.scale_original:   //原始大小
//                Toast.makeText(FFmpegHttpActivity.this, "原始大小", Toast.LENGTH_SHORT).show();
//                mVideoPlayer.setScreenScaleType(VideoView.SCREEN_SCALE_ORIGINAL);
//                break;
//            case R.id.tv_mute:          //静音
//                boolean mute = mVideoPlayer.isMute();
//                if (mute) {
//                    mVideoPlayer.setMute(false);
//                    Toast.makeText(FFmpegHttpActivity.this, "开启-声音", Toast.LENGTH_SHORT).show();
//
//                } else {
//                    mVideoPlayer.setMute(true);
//                    Toast.makeText(FFmpegHttpActivity.this, "开启-静音", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.tv_save_live:       //切换直播源
//                mVideoPlayer.release();
//                this.mVideoPath = tt_live.getText().toString().trim();
//                mVideoPlayer.setUrl(mVideoPath); //设置视频地址
//                mVideoPlayer.start(); //开始播放，不调用则不自动播放
//                break;
//
//        }
//    }
//    public static String getVideoNameByTime() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//        String newDate = sdf.format(new Date());
//        String result = "";
//        Random random = new Random();
//        for (int i = 0; i < 5; i++) {
//            result += random.nextInt(10);
//        }
//        return newDate + result;
//    }
//
//}