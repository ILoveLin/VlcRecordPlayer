package com.company.shenzhou.zlm;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.company.shenzhou.R;
import com.company.shenzhou.util.EnumConfig;
import com.company.shenzhou.util.FileUtil;
import com.company.shenzhou.util.LogUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zlmediakit.jni.ZLMediaKit;

import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import xyz.doikki.videoplayer.player.BaseVideoView;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/6/1 11:18
 * desc：ZlmMediaKit 作为手机服务器  测试界面
 * ffmpeg  可以使用任何ffmpeg命令行操作
 */
public class ZlmMediaKitTestActivity extends AppCompatActivity implements View.OnClickListener {
    private Button tv_click, tv_cancle;
    private VideoView mVideoPlayer;
    private VideoView mAudioPlayer;
    private Button tv_shot;
    private Button tv_16_9;
    private Button tv_no_voice;
    private PhotoView imageView;
    private Button tv_open_voice;
    private Button scale_default;
    private Button scale_original;
    private Button tv_save_cmd;
    private Button tv_save_live;
    private EditText tt_live;
    private EditText tt_cmd;
    //电视台直播流
//    private String mVideoPath = "http://sf1-hscdn-tos.pstatp.com/obj/media-fe/xgplayer_doc_video/flv/xgplayer-demo-360p.flv";        //西瓜视频点播地址
    //    private String mVideoPath = "rtsp://root:root@192.168.1.200:7788/session0.mpg";                 //公司直播地址(采集卡采集的视频然后推送过来的直播地址)
    private String mVideoPath = "http://220.161.87.62:8800/hls/0/index.m3u8";                 //电台直播源地址,这个直播地址推流到本地服务器一直失败不知道为何  请用你们公司自己的直播地址推送
    //    private String mVideoPath = "http://192.168.67.105:3333/api/stream/video?session=123456";     //默认 纯视频流地址
    //    private String mAudioPath = "http://220.161.87.62:8800/hls/0/index.m3u8";       //默认 纯音频流地址
    private String mAudioPath = "http://192.168.67.105:3333/api/stream/audio?session=123456";       //默认 纯音频流地址
    //private String CMD = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -i http://192.168.67.105:3333/api/stream/audio?session=123456 -c copy ";
    //公司视频流和音频流
    private String CMD = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -i http://192.168.67.105:3333/api/stream/audio?session=123456 -c copy ";
    //电视台直播流录像
    private String CMD2 = "-i http://220.161.87.62:8800/hls/0/index.m3u8 -c copy ";
    //ffmpeg -i http://192.168.67.105:3333/api/stream/video?session=123456 -y -t 0.001 -ss 1 -f image2 -r 1    截图   格式通过 MediaStore.Images.Media.MIME_TYPE, "image/jpeg"更改
    private String CMD3 = "-i http://220.161.87.62:8800/hls/0/index.m3u8 -y -t 0.001 -ss 1 -f image2 -r 1 ";
    //    private String CMD3 = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -y -t 0.001 -ss 1 -f image2 -r 1 ";
    private Button tv_shot_ffmpeg;
    private Button tv_merge_live;
    private TextView mCurrentUrl;
    private Button tv_start_push;
    private EditText et_current_server_url;
    private TextView tv_start_server;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zlm);
        getXXPermissions();
        initView();
        openPhoneZlmServer();
        initData();
        responseListener();
    }

    private void responseListener() {
        tv_click.setOnClickListener(this);
        tv_cancle.setOnClickListener(this);
        tv_shot.setOnClickListener(this);
        tv_16_9.setOnClickListener(this);
        scale_default.setOnClickListener(this);
        scale_original.setOnClickListener(this);
        tv_no_voice.setOnClickListener(this);
        tv_save_live.setOnClickListener(this);
        tv_shot_ffmpeg.setOnClickListener(this);
        tv_start_push.setOnClickListener(this);
        tv_start_server.setOnClickListener(this);

        mVideoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ZlmMediaKitTestActivity.this, "我被点击了", Toast.LENGTH_SHORT).show();

            }
        });

        //直播监听
        mVideoPlayer.setOnStateChangeListener(new BaseVideoView.OnStateChangeListener() {
            @Override
            public void onPlayerStateChanged(int playerState) {

            }

            @Override
            public void onPlayStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_ERROR:
                        LogUtils.e("ZlmActivity" + "播放器状态====错误==error");
                        Toast.makeText(ZlmMediaKitTestActivity.this, "播放器状态====错误==error", Toast.LENGTH_SHORT).show();

                        break;
                    case VideoView.STATE_IDLE:
                        LogUtils.e("ZlmActivity" + "播放器状态====闲置==idle");
                        break;
                    case VideoView.STATE_PREPARED:
                        LogUtils.e("ZlmActivity" + "播放器状态====准备==prepared");
                        break;
                    case VideoView.STATE_PLAYING:
                        int[] videoSize = mVideoPlayer.getVideoSize();
                        LogUtils.e("ZlmActivity" + "播放器状态====播放中==playing");
                        LogUtils.e("ZlmActivity" + "播放器状态====视频宽=" + videoSize[0]);
                        LogUtils.e("ZlmActivity" + "播放器状态====视频高=" + videoSize[1]);
                        break;
                    case VideoView.STATE_PAUSED:
                        LogUtils.e("ZlmActivity" + "播放器状态====暂停==paused");
                        break;
                    case VideoView.STATE_BUFFERING:
                        LogUtils.e("ZlmActivity" + "播放器状态====缓冲中==buffering");
                        break;
                    case VideoView.STATE_BUFFERED:
                        LogUtils.e("ZlmActivity" + "播放器状态====缓冲完毕==buffered");
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        LogUtils.e("ZlmActivity" + "播放器状态====回放_已完成==playback_completed");
                        break;
                }

            }
        });

    }

    private void initData() {
        //使用IjkPlayer解码
//        mVideoPlayer.setPlayerFactory(IjkPlayerFactory.create());
//        mAudioPlayer.setPlayerFactory(IjkPlayerFactory.create());
        mVideoPlayer.setUrl(mVideoPath); //设置视频地址
        mCurrentUrl.setText(mVideoPath);
//        StandardVideoController controller = new StandardVideoController(this);
//        controller.addDefaultControlComponent("标题", false);
//        videoView.setVideoController(controller); //设置控制器
        mVideoPlayer.start(); //开始播放，不调用则不自动播放

        mAudioPlayer.setUrl(mAudioPath); //设置视频地址
//        StandardVideoController controller = new StandardVideoController(this);
//        controller.addDefaultControlComponent("标题", false);   
//        videoView.setVideoController(controller); //设置控制器
        mAudioPlayer.start(); //开始播放，不调用则不自动播放

        Toast.makeText(ZlmMediaKitTestActivity.this, "测试手机服务器功能，必须替换成你们公司自己推送的直播流才可以!!!!!", Toast.LENGTH_SHORT).show();

    }


    private void initView() {
        mVideoPlayer = findViewById(R.id.player_video);
        mAudioPlayer = findViewById(R.id.player_audio);
        tv_shot = findViewById(R.id.tv_shot);
        tv_16_9 = findViewById(R.id.tv_16_9);
        tv_no_voice = findViewById(R.id.tv_mute);
        tv_cancle = findViewById(R.id.tv_cancle);
        mCurrentUrl = findViewById(R.id.tv_current_url_dec);
        imageView = findViewById(R.id.tv_bg_shop);
        tv_click = findViewById(R.id.tv_click);
        scale_default = findViewById(R.id.scale_default);
        scale_original = findViewById(R.id.scale_original);
        tv_save_live = findViewById(R.id.tv_save_live);
        tt_live = findViewById(R.id.tt_live);
        tv_shot_ffmpeg = findViewById(R.id.tv_shot_ffmpeg);
        tv_start_push = findViewById(R.id.tv_start_push);
        tv_start_server = findViewById(R.id.tv_start_server);
        et_current_server_url = findViewById(R.id.et_current_server_url);


    }

    private void startRequest() {
        String url = "https://zlmediakit.com/index/api/whip?app=live&stream=test&type=push";
//        String url = "https://zlmediakit.com/index/api/whip?app=live&stream=test";
        OkHttpUtils.post()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        LogUtils.e("ZlmActivity" + "==OkHttpUtils====Exception=" + e);
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        LogUtils.e("ZlmActivity" + "==OkHttpUtils====response=" + response);
                    }
                });

    }

    private void startFFmpegShot() {
        String path = "/FFMPEG_CME";
        //获取uri地址
//        Uri uri = FileUtils.publicDirURI(ZlmMediaKitTestActivity.this, getVideoNameByTime(), path, false);
        Uri uri = FileUtil.createImagePathUri(ZlmMediaKitTestActivity.this);
        //ffmpeg-kit 文档要求这么去获取 outputVideoPath  路径
        String outputVideoPath = FFmpegKitConfig.getSafParameter(ZlmMediaKitTestActivity.this, uri, "rw");
        LogUtils.e("ZlmActivity--截图" + "path====path=" + path);
        LogUtils.e("ZlmActivity--截图" + "uri====uri=" + uri);
        String strUrl = mCurrentUrl.getText().toString().trim();
        String mCurrentCMD = "-i " + strUrl + " -c copy ";
        LogUtils.e("ZlmActivity--截图" + "mCurrentCMD====mCurrentCMD=" + mCurrentCMD);
        LogUtils.e("ZlmActivity--截图" + "mCurrentCMD====outputVideoPath=全部 =" + mCurrentCMD + outputVideoPath);
        FFmpegKit.executeAsync(mCurrentCMD + outputVideoPath, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();
                /**
                 * 录像
                 * returnCode=1         说明:录像,失败
                 * returnCode=255       说明:录像,成功
                 * returnCode=0         说明:截图,成功
                 */
                LogUtils.e("ZlmActivity--截图" + "apply====state=" + state);
                LogUtils.e("ZlmActivity--截图" + "apply====returnCode=" + returnCode);
                if ("1".equals(returnCode + "")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ZlmMediaKitTestActivity.this, "截图失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("0".equals(returnCode + "")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ZlmMediaKitTestActivity.this, "截图成功", Toast.LENGTH_SHORT).show();

//                            imageView.setImageBitmap(bitmap);
                            imageView.setImageURI(uri);
                        }
                    });
                }

            }
        }, new LogCallback() {

            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {

            }
        }, new StatisticsCallback() {

            @Override
            public void apply(Statistics statistics) {
                LogUtils.e("ZlmActivity" + "apply====statistics=" + statistics.toString());


            }
        });

    }

    private void startFFmpegRecord() {
        String path = "/ffmpeg_kit";
        //获取uri地址
        Uri uri = FileUtil.createVideoPathUri(ZlmMediaKitTestActivity.this);
//        Uri uri = FileUtils.publicDirURI(ZlmActivity.this, getVideoNameByTime(), path, true);
        //ffmpeg-kit 文档要求这么去获取 outputVideoPath  路径
        String outputVideoPath = FFmpegKitConfig.getSafParameter(ZlmMediaKitTestActivity.this, uri, "rw");
        LogUtils.e("ZlmActivity--录像" + "path====path=" + path);
        LogUtils.e("ZlmActivity--录像" + "uri====uri=" + uri);
        String strUrl = mCurrentUrl.getText().toString().trim();
        String mCurrentCMD = "-i " + strUrl + " -c copy ";
        LogUtils.e("ZlmActivity--录像" + "mCurrentCMD====mCurrentCMD=" + mCurrentCMD);
        LogUtils.e("ZlmActivity--录像" + "mCurrentCMD====outputVideoPath=全部 =" + mCurrentCMD + outputVideoPath);
        FFmpegKit.executeAsync(mCurrentCMD + outputVideoPath, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();
                LogUtils.e("ZlmActivity" + "apply====state=" + state);            //COMPLETED  完成 并且255=成功
                LogUtils.e("ZlmActivity" + "apply====returnCode=" + returnCode);  //COMPLETED  完成 并且1=失败
                if ("1".equals(returnCode + "")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ZlmMediaKitTestActivity.this, "录制失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("255".equals(returnCode + "")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ZlmMediaKitTestActivity.this, "录制成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("0".equals(returnCode + "")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ZlmMediaKitTestActivity.this, "截图成功", Toast.LENGTH_SHORT).show();

//                            imageView.setImageBitmap(bitmap);
                            imageView.setImageURI(uri);
                        }
                    });
                }

            }
        }, new LogCallback() {

            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {


            }
        }, new StatisticsCallback() {

            @Override
            public void apply(Statistics statistics) {
                LogUtils.e("ZlmActivity--录像" + "apply====statistics=" + statistics.toString());

                // CALLED WHEN SESSION GENERATES STATISTICS

            }
        });


    }


    private void getXXPermissions() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
//        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
            XXPermissions.with(this)
//                .permission(Permission.READ_EXTERNAL_STORAGE)  //正式版本    android 10已下申请  READ_EXTERNAL_STORAGE   WRITE_EXTERNAL_STORAGE
//                .permission(Permission.WRITE_EXTERNAL_STORAGE)  //正式版本
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            LogUtils.e("ZlmActivity" + "成功====all=" + all);


                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            LogUtils.e("ZlmActivity" + "失败=====" + never);


                        }
                    });

        } else {

//        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
            XXPermissions.with(this)
                    .permission(Permission.READ_EXTERNAL_STORAGE)  //正式版本    android 10已下申请  READ_EXTERNAL_STORAGE   WRITE_EXTERNAL_STORAGE
                    .permission(Permission.WRITE_EXTERNAL_STORAGE)  //正式版本
//                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            LogUtils.e("ZlmActivity" + "成功====all=" + all);


                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            LogUtils.e("ZlmActivity" + "失败=====" + never);


                        }
                    });

        }


    }


    @Override
    protected void onPause() {
        super.onPause();
        mAudioPlayer.pause();
        mVideoPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAudioPlayer.resume();
        mVideoPlayer.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioPlayer.release();
        mVideoPlayer.release();
    }


    @Override
    public void onBackPressed() {
        if (!mAudioPlayer.onBackPressed()) {
            super.onBackPressed();
        }
        if (!mVideoPlayer.onBackPressed()) {
            super.onBackPressed();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_click:          //执行ffmpeg命令
                Toast.makeText(ZlmMediaKitTestActivity.this, "开始录像", Toast.LENGTH_SHORT).show();
                startFFmpegRecord();
                break;
            case R.id.tv_cancle:         //取消ffmpeg命令
                Toast.makeText(ZlmMediaKitTestActivity.this, "取消录像", Toast.LENGTH_SHORT).show();
                FFmpegKit.cancel();
                break;
            case R.id.tv_shot:          //截图
                Toast.makeText(ZlmMediaKitTestActivity.this, "截图", Toast.LENGTH_SHORT).show();
                Bitmap bitmap = mVideoPlayer.doScreenShot();
                imageView.setImageBitmap(bitmap);
                MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmap, "IMG" + Calendar.getInstance().getTime(), null);
                break;
            case R.id.tv_start_server:          //开启手机服务器--开启ZmlMediaKit服务器
                startRequest();
                break;
            case R.id.tv_start_push:          //推流到手机服务器
                startFFmpeg2PushSteam2PhoneServer();
                break;
            case R.id.tv_shot_ffmpeg:          //截图
                startFFmpegShot();
                break;
            case R.id.tv_16_9:          //16:9
                Toast.makeText(ZlmMediaKitTestActivity.this, "16:9", Toast.LENGTH_SHORT).show();
                mVideoPlayer.setScreenScaleType(VideoView.SCREEN_SCALE_16_9);
                break;
            case R.id.scale_default:     //默认大小
                Toast.makeText(ZlmMediaKitTestActivity.this, "默认大小", Toast.LENGTH_SHORT).show();
                mVideoPlayer.setScreenScaleType(VideoView.SCREEN_SCALE_DEFAULT);
                break;
            case R.id.scale_original:   //原始大小
                Toast.makeText(ZlmMediaKitTestActivity.this, "原始大小", Toast.LENGTH_SHORT).show();
                mVideoPlayer.setScreenScaleType(VideoView.SCREEN_SCALE_ORIGINAL);
                break;
            case R.id.tv_mute:          //静音
                boolean mute = mVideoPlayer.isMute();
                if (mute) {
                    mVideoPlayer.setMute(false);
                    Toast.makeText(ZlmMediaKitTestActivity.this, "开启-声音", Toast.LENGTH_SHORT).show();

                } else {
                    mVideoPlayer.setMute(true);
                    Toast.makeText(ZlmMediaKitTestActivity.this, "开启-静音", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_save_live:       //切换直播源
                mVideoPlayer.release();
                this.mVideoPath = tt_live.getText().toString().trim();
                mVideoPlayer.setUrl(mVideoPath); //设置视频地址
                mCurrentUrl.setText(mVideoPath);
                mVideoPlayer.start(); //开始播放，不调用则不自动播放
                break;

        }
    }

    private void openPhoneZlmServer() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
//        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
            XXPermissions.with(this)
//                .permission(Permission.READ_EXTERNAL_STORAGE)  //正式版本    android 10已下申请  READ_EXTERNAL_STORAGE   WRITE_EXTERNAL_STORAGE
//                .permission(Permission.WRITE_EXTERNAL_STORAGE)  //正式版本
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            LogUtils.e("ZlmActivity" + "权限申请成功====all=" + all);
                            /**
                             * 开启本地手机推流服务器
                             */
                            String sd_dir = Environment.getExternalStoragePublicDirectory("").toString();
                            //开启推流服务器
                            ZLMediaKit.startDemo(sd_dir);
                            LogUtils.e("ZlmActivity" + "服务器开启成功====all=");
                            Toast.makeText(ZlmMediaKitTestActivity.this, "服务器开启成功", Toast.LENGTH_SHORT).show();
                            LogUtils.e("ZlmActivity" + "服务器开启成功====服务器开启成功");

                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            LogUtils.e("ZlmActivity" + "服务器开启失败=====" + never);
                            Toast.makeText(ZlmMediaKitTestActivity.this, "服务器开启失败,请先申请读写权限", Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {

//        java.lang.UnsupportedOperationException: Unknown URI: content://media/external_primary/video/media
            XXPermissions.with(this)
                    .permission(Permission.READ_EXTERNAL_STORAGE)  //正式版本    android 10已下申请  READ_EXTERNAL_STORAGE   WRITE_EXTERNAL_STORAGE
                    .permission(Permission.WRITE_EXTERNAL_STORAGE)  //正式版本
//                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)  //正式版本    android 10以上申请  MANAGE_EXTERNAL_STORAGE
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            LogUtils.e("ZlmActivity" + "成功====all=" + all);
                            /**
                             * 开启本地手机推流服务器
                             */
                            String sd_dir = Environment.getExternalStoragePublicDirectory("").toString();
                            //开启推流服务器
                            ZLMediaKit.startDemo(sd_dir);
                            LogUtils.e("ZlmActivity" + "服务器开启成功====服务器开启成功");
                            Toast.makeText(ZlmMediaKitTestActivity.this, "服务器开启成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            LogUtils.e("ZlmActivity" + "服务器开启失败=====" + never);
                            Toast.makeText(ZlmMediaKitTestActivity.this, "服务器开启失败,请先申请读写权限", Toast.LENGTH_SHORT).show();

                        }
                    });

        }


    }

    /**
     * 开启ffmpeg推流到手机服务器
     */
    private void startFFmpeg2PushSteam2PhoneServer() {
        String path = "/ffmpeg_kit";
        //获取uri地址
        Uri uri = FileUtil.createVideoPathUri(ZlmMediaKitTestActivity.this);
//        Uri uri = FileUtils.publicDirURI(ZlmActivity.this, getVideoNameByTime(), path, true);
        //ffmpeg-kit 文档要求这么去获取 outputVideoPath  路径
        String outputVideoPath = FFmpegKitConfig.getSafParameter(ZlmMediaKitTestActivity.this, uri, "rw");

//        -i http://192.168.67.105:3333/api/stream/video?session=123456 -i http://192.168.67.105:3333/api/stream/audio?session=123456 -c copy -rtsp_transport tcp -f rtsp rtsp://127.0.0.1:8554/stream/live
        String strUrl = mCurrentUrl.getText().toString().trim();
        //rtsp://127.0.0.1:8554/stream/live--->本地地址,可以自行修改,但是必须是二级目录(/stream/live)不然推不倒服务器上去
        //UDP推流
        //ffmpeg -re -i input.mp4 -c copy -f rtsp rtsp://127.0.0.1:8554/stream
        //TCP推流
//        String mCurrentCMD = "-i " + strUrl + " -c copy -f rtsp rtsp://127.0.0.1:8554/stream/live";
        String mCurrentCMD = "-i " + strUrl + " -c copy -rtsp_transport tcp -f rtsp rtsp://127.0.0.1:8554/stream/live";
        LogUtils.e("ZlmActivity--推流到手机服务器" + "mCurrentCMD==" + mCurrentCMD);
        LogUtils.e("ZlmActivity--推流到手机服务器" + "outputVideoPath==" + outputVideoPath);
        FFmpegKit.executeAsync(mCurrentCMD, new FFmpegSessionCompleteCallback() {

            @Override
            public void apply(FFmpegSession session) {
                SessionState state = session.getState();
                ReturnCode returnCode = session.getReturnCode();
                LogUtils.e("ZlmActivity" + "apply====state=" + state);
                LogUtils.e("ZlmActivity" + "apply====returnCode=" + returnCode);  //COMPLETED  完成 并且1=失败
                if ("1".equals(returnCode + "")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ZlmMediaKitTestActivity.this, "推流到手机服务器--失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }, new LogCallback() {

            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {
//                LogUtils.e("ZlmActivity--推流到手机服务器" + "apply=日志--log=" + log);


            }
        }, new StatisticsCallback() {

            @Override
            public void apply(Statistics statistics) {
                LogUtils.e("ZlmActivity--推流到手机服务器" + "apply======" + statistics.toString());
                if (!isPushOK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ZlmMediaKitTestActivity.this, "推流到手机服务器--成功,直播地址:rtsp://127.0.0.1:8554/stream/live", Toast.LENGTH_SHORT).show();
                            et_current_server_url.setText("rtsp://127.0.0.1:8554/stream/live");
                        }
                    });
                }
                isPushOK = true;
                // CALLED WHEN SESSION GENERATES STATISTICS

            }
        });

    }

    //是否推送到本地服务器成功的标识 默认失败
    private boolean isPushOK = false;
}
