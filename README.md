

# 统一播放器框架
> author : LoveLin  
> github : https://github.com/ILoveLin/VlcRecordPlayer.git  
> time   : 2025/12/3

# VLC播放器Demo（录像，截图等功能），可二次开发。支持内核切换ijk或者系统自带播放器

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q，3Q。

* vlc编译，源库地址是这个博主的：https://github.com/mengzhidaren/Vlc-sdk-lib 可以去star支持下。16kb内存页过些天有空再去集成

* 基于VLC的播放器（Android
  录像，截图），可做二次开发，支持在点播或者直播，播放的时候：录像，截图等等功能。支持RTSP，RTMP，HTTP，HLS，HTTPS等等。支持所有CPU架构。

* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！
* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！！


## 动图欣赏

* 如果看不到gif动图，请科学上网查看gif效果图，或者下载项目之后本地打开。在picture文件夹/gif文件夹/vlc.gif。

* VLCDemo测试，动图欣赏vlc.gif
  ![](../picture/gif/vlc.gif)




## 目录结构

```
player/
├── core/                          # 核心层
│   ├── IPlayerEngine.java         # 播放器内核接口
│   ├── BasePlayerEngine.java      # 播放器内核基类
│   ├── PlayerType.java            # 播放器类型定义（VLC/IJK/MEDIA_PLAYER/EXO/TENCENT）
│   ├── PlayerState.java           # 播放器状态枚举
│   ├── PlayerFactory.java         # 播放器工厂类
│   └── VideoPlayerManager.java    # 播放器管理器
│
├── engine/                        # 内核实现层
│   ├── VlcPlayerEngine.java       # VLC 内核（支持录像/截图）
│   ├── IjkPlayerEngine.java       # IJK 内核（支持硬解码）
│   └── MediaPlayerEngine.java     # Android 原生 MediaPlayer 内核
│
├── render/                        # 渲染层
│   ├── IRenderView.java           # 渲染视图接口
│   ├── AspectRatioType.java       # 宽高比类型定义
│   ├── MeasureHelper.java         # 视频尺寸测量辅助类
│   ├── TextureRenderView.java     # TextureView 渲染（支持动画）
│   └── SurfaceRenderView.java     # SurfaceView 渲染（性能更好）
│
├── controller/                    # 控制器层
│   ├── IController.java           # 控制器接口
│   ├── VideoController.java       # 视频控制器（四方向控制栏+手势）
│   ├── CustomButton.java          # 自定义按钮数据类
│   └── GestureController.java     # 手势控制器
│
├── widget/                        # 自定义控件
│   ├── ArcProgressView.java       # 弧形进度视图（亮度/音量调节显示）
│   ├── ENDownloadView.java        # 加载动画视图
│   └── ENPlayView.java            # 播放/暂停动画视图
│
├── ui/                            # 界面层
│   ├── UniversalVideoView.java    # 统一视频视图（整合内核+渲染+控制器）
│   ├── PlayerActivity.java        # 全屏播放器 Activity
│   └── PlayerSettingActivity.java # 播放器设置入口界面
│
└── listener/                      # 监听器
    ├── OnPreparedListener.java    # 准备完成监听
    ├── OnCompletionListener.java  # 播放完成监听
    ├── OnErrorListener.java       # 错误监听
    ├── OnBufferingUpdateListener.java # 缓冲更新监听
    ├── OnVideoSizeChangedListener.java # 视频尺寸变化监听
    ├── OnInfoListener.java        # 播放器信息监听
    ├── OnRecordListener.java      # 录像状态监听
    ├── OnSnapshotListener.java    # 截图监听
    └── PlayerStateListener.java   # 播放器状态监听
```

## 支持的播放器内核

| 内核 | 类型常量 | 状态 | 录像 | 截图 | 说明 |
|------|---------|------|------|------|------|
| VLC | `PlayerType.VLC` | ✅ 已实现 | ✅ | ✅ | 支持 RTSP/RTMP/HTTP |
| IJK | `PlayerType.IJK` | ✅ 已实现 | ❌ | ❌ | 支持硬解码 |
| MediaPlayer | `PlayerType.MEDIA_PLAYER` | ✅ 已实现 | ❌ | ❌ | Android 原生播放器 |
| ExoPlayer | `PlayerType.EXO` | ⏳ 待实现 | - | - | - |
| Tencent | `PlayerType.TENCENT` | ⏳ 待实现 | - | - | - |

## 快速开始

### 1. 布局中使用

```xml
<com.company.shenzhou.player.ui.UniversalVideoView
    android:id="@+id/video_view"
    android:layout_width="match_parent"
    android:layout_height="200dp" />
```

### 2. 代码中使用

```java
UniversalVideoView videoView = findViewById(R.id.video_view);

// 设置播放器内核（可选，默认 VLC）
videoView.setPlayerType(PlayerType.VLC);

// 设置标题
videoView.setTitle("视频标题");

// 设置监听器
videoView.setOnPreparedListener(() -> {
    // 准备完成
});

videoView.setOnCompletionListener(() -> {
    // 播放完成
});

videoView.setOnErrorListener((errorCode, errorMsg) -> {
    // 播放错误
    return false;
});

// 播放
videoView.play("rtsp://xxx.xxx.xxx/live");

// 释放资源（在 Activity onDestroy 中调用）
videoView.release();
```

### 3. 使用 PlayerActivity

```java
// 简单启动
PlayerActivity.start(context, "rtsp://xxx.xxx.xxx/live");

// 带标题
PlayerActivity.start(context, url, "视频标题");

// 指定播放器内核
PlayerActivity.start(context, url, "视频标题", PlayerType.VLC);

// 指定播放器内核和播放速度
PlayerActivity.start(context, url, "视频标题", PlayerType.IJK, 1.5f);
```

## 播放控制

```java
videoView.play(url);           // 播放
videoView.start();             // 开始/恢复
videoView.pause();             // 暂停
videoView.stop();              // 停止
videoView.seekTo(5000);        // 跳转到指定位置（毫秒）
videoView.setSpeed(1.5f);      // 设置播放速度
videoView.setVolume(0.5f, 0.5f); // 设置音量
videoView.setLooping(true);    // 设置循环播放
videoView.release();           // 释放资源
```

## 全屏控制

```java
videoView.enterFullscreen();   // 进入全屏
videoView.exitFullscreen();    // 退出全屏
videoView.toggleFullscreen();  // 切换全屏状态
videoView.isFullscreen();      // 是否全屏
```

## 录像和截图（仅 VLC 内核支持）

```java
// 检查是否支持
if (videoView.isSupportRecord()) {
    videoView.startRecord("/sdcard/records", "video_001"); // 开始录像
    videoView.stopRecord();                                 // 停止录像
    videoView.isRecording();                                // 是否正在录像
}

if (videoView.isSupportSnapshot()) {
    videoView.takeSnapshot("/sdcard/snapshots/img.jpg");           // 截图
    videoView.takeSnapshot("/sdcard/snapshots/img.jpg", 1280, 720); // 指定尺寸截图
}

// 设置保存目录
videoView.setRecordDirectory("/sdcard/MyApp/records");
videoView.setSnapshotDirectory("/sdcard/MyApp/snapshots");
```

## 宽高比设置

```java
videoView.setAspectRatioType(AspectRatioType.FIT_PARENT);   // 适应父容器（默认）
videoView.setAspectRatioType(AspectRatioType.FILL_PARENT);  // 填充父容器
videoView.setAspectRatioType(AspectRatioType.WRAP_CONTENT); // 原始尺寸
videoView.setAspectRatioType(AspectRatioType.FIT_WIDTH);    // 适应宽度
videoView.setAspectRatioType(AspectRatioType.FIT_HEIGHT);   // 适应高度
videoView.setAspectRatioType(AspectRatioType.RATIO_16_9);   // 16:9
videoView.setAspectRatioType(AspectRatioType.RATIO_4_3);    // 4:3
videoView.setAspectRatioType(AspectRatioType.RATIO_19_6);   // 19:6
```

## 控制器自定义

### 控制栏可见性

```java
videoView.setTopBarVisible(true);      // 顶部栏
videoView.setBottomBarVisible(true);   // 底部栏
videoView.setLeftBarVisible(true);     // 左侧栏
videoView.setRightBarVisible(true);    // 右侧栏
```

### 按钮可见性

```java
videoView.setBackVisible(true);        // 返回按钮
videoView.setPlayPauseVisible(true);   // 播放/暂停按钮
videoView.setFullscreenVisible(true);  // 全屏按钮
videoView.setMuteVisible(true);        // 静音按钮
videoView.setLockVisible(true);        // 锁屏按钮
videoView.setRecordVisible(true);      // 录像按钮
videoView.setSnapshotVisible(true);    // 截图按钮
videoView.setSeekBarVisible(true);     // 进度条
videoView.setTimeVisible(true);        // 时间显示
```

### 自定义图标

```java
videoView.setBackIcon(R.drawable.ic_back);
videoView.setPlayPauseIcon(R.drawable.ic_play, R.drawable.ic_pause);
videoView.setFullscreenIcon(R.drawable.ic_fullscreen, R.drawable.ic_fullscreen_exit);
videoView.setMuteIcon(R.drawable.ic_mute, R.drawable.ic_unmute);
videoView.setLockIcon(R.drawable.ic_lock, R.drawable.ic_unlock);
videoView.setRecordIcon(R.drawable.ic_record, R.drawable.ic_record_active);
videoView.setSnapshotIcon(R.drawable.ic_snapshot);
```

### 添加自定义按钮

```java
CustomButton button = new CustomButton(1001, R.drawable.ic_custom)
    .setTag("custom")
    .setOrder(30)  // 排序值，数字越小越靠上
    .setClickListener(btn -> {
        // 处理点击
    });

videoView.addCustomButton(button);
videoView.updateCustomButtonIcon(1001, R.drawable.ic_custom_new);
videoView.setCustomButtonVisible(1001, true);
videoView.removeCustomButton(1001);
videoView.clearCustomButtons();
```

## 控制器功能说明

### 四方向控制栏
- **顶部栏**：返回按钮、标题
- **底部栏**：播放/暂停、进度条、时间、静音、全屏
- **左侧栏**：锁屏按钮
- **右侧栏**：录像、截图、自定义按钮

### 手势控制
- **左侧上下滑动**：调节亮度
- **右侧上下滑动**：调节音量
- **单击屏幕**：显示/隐藏控制栏
- **锁定状态**：只显示锁屏按钮，禁用其他手势

## 播放器状态

```java
PlayerState state = videoView.getState();
// IDLE        - 空闲
// INITIALIZED - 已初始化
// PREPARING   - 准备中
// PREPARED    - 准备完成
// PLAYING     - 播放中
// PAUSED      - 暂停
// BUFFERING   - 缓冲中
// COMPLETED   - 播放完成
// STOPPED     - 已停止
// ERROR       - 错误
// RELEASED    - 已释放
```

## 注意事项

1. 在 Activity 的 `onDestroy()` 中必须调用 `videoView.release()` 释放资源
2. VLC 内核需要项目中的 libvlc 模块支持
3. IJK 内核需要添加 ijkplayer 依赖
4. 录像和截图功能仅 VLC 内核支持
5. 全屏播放时会自动切换横屏，退出全屏恢复竖屏

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