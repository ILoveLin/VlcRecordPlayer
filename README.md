# 统一播放器框架

# VLC播放器Demo（录像，截图等功能），可二次开发。
# ffmpeg-Kit （录像，截图,合流播放,合流推送(到本地手机服务器或者公司三方服务器),等一些列视频操作功能），可二次开发。
# ZLMediaKit 作为本地推流服务器的使用，可以直接提取so或者aar文件给自己二次开发。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q，3Q。

* vlc编译，源库地址是这个博主的：https://github.com/mengzhidaren/Vlc-sdk-lib 可以去star支持下。
* ZLMediaKit编译，源库地址是这个博主的：https://github.com/ZLMediaKit/ZLMediaKit 可以去star支持下。

* 基于VLC的播放器（Android
  录像，截图），可做二次开发，支持在点播或者直播，播放的时候：录像，截图等等功能。支持RTSP，RTMP，HTTP，HLS，HTTPS等等。支持所有CPU架构。

* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！
* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！！


* ffmpeg-Kit 可以执行任何ffmpeg命令行操作,Demo中对直播流(也可以本地文件)进行了：录像，截图等等功能，也可以做任何你想要的操作，比如将直播音频流和视频流合流播放，合流推流等等操作。

* ffmpeg-Kit，可以实现任何ffmpeg命令行操作, 特意花时间写了个Demo开源出来，希望能帮助到需要的人！
* ffmpeg-Kit，可以实现任何ffmpeg命令行操作, 特意花时间写了个Demo开源出来，希望能帮助到需要的人！！

## 动图欣赏

* 如果看不到gif动图，请科学上网查看gif效果图，或者下载项目之后本地打开。在picture文件夹/gif文件夹/vlc.gif和ffmpeg.gif，zmlkit.gif。

* VLCDemo测试，动图欣赏vlc.gif
  ![](picture/gif/vlc.gif)


* ffmpegDemo测试，动图欣赏ffmpeg.gif
  ![](picture/gif/ffmpeg.gif)


* ZLMediaKitDemo测试，动图欣赏zmlkit.gif
  ![](picture/gifzmlkit.gif)


## 架构说明

```
player/
├── core/                          # 核心层
│   ├── IPlayerEngine.java         # 播放器接口
│   ├── BasePlayerEngine.java      # 播放器基类
│   ├── PlayerType.java            # 类型定义
│   ├── PlayerState.java           # 状态枚举
│   ├── PlayerFactory.java         # 工厂类
│   └── VideoPlayerManager.java    # 播放器管理器
│
├── engine/                        # 内核实现
│   ├── VlcPlayerEngine.java       # VLC 内核
│   └── MediaPlayerEngine.java     # 原生 MediaPlayer 内核
│
├── render/                        # 渲染层
│   ├── IRenderView.java           # 渲染接口
│   ├── AspectRatioType.java       # 宽高比类型
│   ├── MeasureHelper.java         # 测量辅助类
│   ├── TextureRenderView.java     # TextureView 渲染
│   └── SurfaceRenderView.java     # SurfaceView 渲染
│
├── controller/                    # 控制器
│   ├── IController.java           # 控制器接口
│   ├── VideoController.java       # 播放控制UI
│   └── GestureController.java     # 手势控制
│
├── ui/                            # 视图层
│   ├── UniversalVideoView.java    # 统一视频视图
│   └── PlayerActivity.java        # 播放器 Activity
│
└── listener/                      # 监听器
    ├── OnPreparedListener.java
    ├── OnCompletionListener.java
    ├── OnErrorListener.java
    ├── OnBufferingUpdateListener.java
    ├── OnVideoSizeChangedListener.java
    ├── OnInfoListener.java
    └── PlayerStateListener.java
```

## 使用方法

### 1. 在布局中使用

```xml
<com.company.shenzhou.player.ui.UniversalVideoView
    android:id="@+id/video_view"
    android:layout_width="match_parent"
    android:layout_height="200dp" />
```

### 2. 在代码中使用

```java
UniversalVideoView videoView = findViewById(R.id.video_view);

// 设置播放器内核（可选，默认 VLC）
videoView.setPlayerType(PlayerType.VLC);
// videoView.setPlayerType(PlayerType.MEDIA_PLAYER);

// 设置监听器
videoView.setOnPreparedListener(() -> {
    Log.d("Player", "准备完成");
});

videoView.setOnCompletionListener(() -> {
    Log.d("Player", "播放完成");
});

videoView.setOnErrorListener((errorCode, errorMsg) -> {
    Log.e("Player", "播放错误: " + errorMsg);
    return false;
});

// 播放视频
videoView.play("rtsp://xxx.xxx.xxx/live");
// 或
videoView.play("http://xxx.xxx.xxx/video.mp4");

// 控制播放
videoView.pause();
videoView.start();
videoView.seekTo(5000);
videoView.setSpeed(1.5f);
videoView.setVolume(0.5f, 0.5f);

// 全屏
videoView.enterFullscreen();
videoView.exitFullscreen();

// 释放资源（在 Activity onDestroy 中调用）
videoView.release();
```

### 3. 使用 PlayerActivity

```java
// 简单启动
PlayerActivity.start(context, "rtsp://xxx.xxx.xxx/live");

// 带标题
PlayerActivity.start(context, "rtsp://xxx.xxx.xxx/live", "直播标题");

// 指定播放器内核
PlayerActivity.start(context, "rtsp://xxx.xxx.xxx/live", "直播标题", PlayerType.VLC);
```

## 支持的播放器内核

| 内核 | 类型常量 | 状态 | 录像/截图 | 说明 |
|------|---------|------|----------|------|
| VLC | PlayerType.VLC | ✅ 已实现 | ✅ 支持 | 支持 RTSP/RTMP/HTTP 等 |
| MediaPlayer | PlayerType.MEDIA_PLAYER | ✅ 已实现 | ❌ 不支持 | Android 原生播放器 |
| ExoPlayer | PlayerType.EXO | ⏳ 待实现 | ❌ 不支持 | Google 推荐播放器 |
| IjkPlayer | PlayerType.IJK | ⏳ 待实现 | ❌ 不支持 | B站开源播放器 |
| Tencent | PlayerType.TENCENT | ⏳ 待实现 | ❌ 不支持 | 腾讯云播放器 |

## 录像和截图功能

录像和截图功能仅 VLC 内核支持，其他内核会自动隐藏相关按钮。

### 使用方法

```java
// 检查是否支持
if (videoView.isSupportRecord()) {
    // 开始录像
    videoView.startRecord("/sdcard/records", "video_001");
    
    // 停止录像
    videoView.stopRecord();
    
    // 检查是否正在录像
    boolean isRecording = videoView.isRecording();
}

if (videoView.isSupportSnapshot()) {
    // 截图
    videoView.takeSnapshot("/sdcard/snapshots/img_001.jpg");
    
    // 指定尺寸截图
    videoView.takeSnapshot("/sdcard/snapshots/img_002.jpg", 1280, 720);
}

// 设置保存目录（可选，有默认值）
videoView.setRecordDirectory("/sdcard/MyApp/records");
videoView.setSnapshotDirectory("/sdcard/MyApp/snapshots");
```

### 注意事项

1. VLC 录像需要关闭硬件解码才能正常工作
2. 录像文件格式为 .ts
3. 截图文件格式为 .jpg
4. 需要存储权限

## 宽高比类型

```java
videoView.setAspectRatioType(AspectRatioType.FIT_PARENT);   // 适应父容器
videoView.setAspectRatioType(AspectRatioType.FILL_PARENT);  // 填充父容器
videoView.setAspectRatioType(AspectRatioType.WRAP_CONTENT); // 原始尺寸
videoView.setAspectRatioType(AspectRatioType.FIT_WIDTH);    // 适应宽度
videoView.setAspectRatioType(AspectRatioType.FIT_HEIGHT);   // 适应高度
videoView.setAspectRatioType(AspectRatioType.RATIO_16_9);   // 16:9
videoView.setAspectRatioType(AspectRatioType.RATIO_4_3);    // 4:3
```

## 注意事项

1. 在 Activity 的 `onDestroy()` 中调用 `videoView.release()` 释放资源
2. VLC 内核需要项目中的 libvlc 模块支持
3. 如需录制或截图功能，VLC 需要关闭硬件解码


## License

```text
Copyright 2023 LoveLin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```