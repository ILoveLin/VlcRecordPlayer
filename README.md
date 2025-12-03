# 统一播放器框架
## 一个功能完善的 Android 视频播放器框架， VLC播放器Demo（支持多种播放器内核切换、录像截图、手势控制等功能。）可二次开发。支持内核切换ijk或者系统自带播放器

> author : LoveLin  
> github : https://github.com/ILoveLin/VlcRecordPlayer.git  
> time   : 2025/12/3

##

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q，3Q。

* vlc编译，源库地址是这个博主的：https://github.com/mengzhidaren/Vlc-sdk-lib 可以去star支持下。16kb内存页过些天有空再去集成

* 基于VLC的播放器（Android
  录像，截图），可做二次开发，支持在点播或者直播，播放的时候：录像，截图等等功能。支持RTSP，RTMP，HTTP，HLS，HTTPS等等。支持所有CPU架构。

* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！
* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！！


## 动图欣赏

* 如果看不到gif动图，请科学上网查看gif效果图，或者下载项目之后本地打开。在picture文件夹/gif文件夹/player.gif。

* VLCDemo测试，动图欣赏player.gif

> ⚠️ GIF 文件较大（约18MB），如无法加载请直接查看：[picture/gif/player.gif](picture/gif/player.gif)

![player演示](picture/gif/player.gif)



---

## 目录

- [功能特性](#功能特性)
- [目录结构](#目录结构)
- [支持的播放器内核](#支持的播放器内核)
- [快速开始](#快速开始)
- [详细使用说明](#详细使用说明)
- [API 参考](#api-参考)
- [自定义配置](#自定义配置)
- [注意事项](#注意事项)

---

## 功能特性

### 核心功能
- ✅ 多播放器内核支持（VLC、IJK、MediaPlayer）
- ✅ 支持 RTSP、RTMP、HTTP、HLS 等多种协议
- ✅ 录像功能（仅 VLC 内核）
- ✅ 截图功能（仅 VLC 内核）
- ✅ 进度条拖动（点播流）
- ✅ 倍速播放
- ✅ 循环播放

### 控制器功能
- ✅ 四方向控制栏（顶部、底部、左侧、右侧）
- ✅ 滑动动画效果
- ✅ 锁屏功能
- ✅ 全屏切换
- ✅ 静音控制
- ✅ 自定义按钮支持

### 手势控制
- ✅ 左侧上下滑动：调节亮度
- ✅ 右侧上下滑动：调节音量
- ✅ 单击屏幕：显示/隐藏控制栏

### 点播视频 vs 直播视频

| 功能 | 点播视频 | 直播视频 |
|------|---------|---------|
| 进度条 | ✅ 显示 | ❌ 隐藏 |
| 拖动进度 | ✅ 支持 | ❌ 不支持 |
| 倍速播放 | ✅ 支持 | ❌ 不支持 |
| 时间显示 | 当前时间/总时长 | "直播中" |

播放器会自动检测视频类型：
- 通过 `duration > 0` 判断是否有时长
- 通过 `isSeekable()` 判断是否支持进度拖动

---

## 目录结构

```
player/
├── core/                          # 核心层
│   ├── IPlayerEngine.java         # 播放器内核接口
│   ├── BasePlayerEngine.java      # 播放器内核基类
│   ├── PlayerType.java            # 播放器类型定义
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
│   ├── TextureRenderView.java     # TextureView 渲染
│   └── SurfaceRenderView.java     # SurfaceView 渲染
│
├── controller/                    # 控制器层
│   ├── IController.java           # 控制器接口
│   ├── VideoController.java       # 视频控制器（四方向控制栏）
│   ├── CustomButton.java          # 自定义按钮数据类
│   └── GestureController.java     # 手势控制器
│
├── widget/                        # 自定义控件
│   ├── ArcProgressView.java       # 弧形进度视图（亮度/音量指示器）
│   ├── ENDownloadView.java        # 加载动画视图
│   └── ENPlayView.java            # 播放动画视图
│
├── ui/                            # 界面层
│   ├── UniversalVideoView.java    # 统一视频视图（推荐使用）
│   ├── PlayerActivity.java        # 全屏播放器 Activity
│   └── PlayerSettingActivity.java # 播放器设置界面
│
└── listener/                      # 监听器
    ├── OnPreparedListener.java        # 准备完成监听
    ├── OnCompletionListener.java      # 播放完成监听
    ├── OnErrorListener.java           # 错误监听
    ├── OnBufferingUpdateListener.java # 缓冲更新监听
    ├── OnVideoSizeChangedListener.java# 视频尺寸变化监听
    ├── OnInfoListener.java            # 信息监听
    ├── OnRecordListener.java          # 录像状态监听
    ├── OnSnapshotListener.java        # 截图监听
    └── PlayerStateListener.java       # 播放器状态监听
```

---

## 支持的播放器内核

| 内核 | 类型常量 | 录像 | 截图 | 硬解码 | 说明 |
|------|---------|------|------|--------|------|
| VLC | `PlayerType.VLC` | ✅ | ✅ | ✅ | 支持 RTSP/RTMP/HTTP/HLS，功能最全 |
| IJK | `PlayerType.IJK` | ❌ | ❌ | ✅ | B站开源播放器，支持硬解码 |
| MediaPlayer | `PlayerType.MEDIA_PLAYER` | ❌ | ❌ | ✅ | Android 原生，兼容性最好 |
| ExoPlayer | `PlayerType.EXO` | ❌ | ❌ | ✅ | Google 官方（待实现） |
| Tencent | `PlayerType.TENCENT` | ❌ | ❌ | ✅ | 腾讯云播放器（待实现） |

---

## 快速开始

### 方式一：使用 UniversalVideoView（推荐）

#### 1. 布局文件

```xml
<com.company.shenzhou.player.ui.UniversalVideoView
    android:id="@+id/video_view"
    android:layout_width="match_parent"
    android:layout_height="200dp" />
```

#### 2. 代码使用

```java
UniversalVideoView videoView = findViewById(R.id.video_view);

// 设置播放器内核（可选，默认 VLC）
videoView.setPlayerType(PlayerType.VLC);

// 设置标题
videoView.setTitle("视频标题");

// 设置返回按钮监听
videoView.setOnBackClickListener(() -> finish());

// 播放
videoView.play("rtsp://xxx.xxx.xxx/live");

// 释放资源（必须在 onDestroy 中调用）
@Override
protected void onDestroy() {
    super.onDestroy();
    videoView.release();
}
```

### 方式二：使用 PlayerActivity

```java
// 简单启动
PlayerActivity.start(context, "rtsp://xxx/live");

// 带标题
PlayerActivity.start(context, url, "视频标题");

// 指定播放器类型
PlayerActivity.start(context, url, "标题", PlayerType.VLC);

// 指定播放速度
PlayerActivity.start(context, url, "标题", PlayerType.VLC, 1.5f);
```

---

## 详细使用说明

### 播放控制

```java
// 播放
videoView.play("http://example.com/video.mp4");

// 带请求头播放
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer xxx");
videoView.play(url, headers);

// 暂停
videoView.pause();

// 继续播放
videoView.start();

// 停止
videoView.stop();

// 重置
videoView.reset();

// 跳转到指定位置（毫秒）
videoView.seekTo(10000);

// 获取当前位置
long position = videoView.getCurrentPosition();

// 获取总时长
long duration = videoView.getDuration();

// 是否正在播放
boolean isPlaying = videoView.isPlaying();
```

### 倍速播放

```java
// 设置播放速度（0.5 - 2.0）
// 注意：直播流不支持倍速播放
videoView.setSpeed(1.5f);

// 获取当前速度
float speed = videoView.getSpeed();
```

### 循环播放

```java
videoView.setLooping(true);
```

### 音量控制

```java
// 设置音量（0.0 - 1.0）
videoView.setVolume(0.5f, 0.5f);
```

### 宽高比设置

```java
// 适应父容器（保持宽高比，默认）
videoView.setAspectRatioType(AspectRatioType.FIT_PARENT);

// 填充父容器（可能裁剪）
videoView.setAspectRatioType(AspectRatioType.FILL_PARENT);

// 原始尺寸
videoView.setAspectRatioType(AspectRatioType.WRAP_CONTENT);

// 16:9
videoView.setAspectRatioType(AspectRatioType.RATIO_16_9);

// 4:3
videoView.setAspectRatioType(AspectRatioType.RATIO_4_3);
```

### 全屏控制

```java
// 进入全屏
videoView.enterFullscreen();

// 退出全屏
videoView.exitFullscreen();

// 切换全屏
videoView.toggleFullscreen();

// 是否全屏
boolean isFullscreen = videoView.isFullscreen();

// 处理返回键
@Override
public void onBackPressed() {
    if (videoView.isFullscreen()) {
        videoView.exitFullscreen();
        return;
    }
    super.onBackPressed();
}
```

### 录像功能（仅 VLC）

```java
// 检查是否支持录像
if (videoView.isSupportRecord()) {
    // 开始录像
    videoView.startRecord("/sdcard/records", "video_001");
    
    // 停止录像
    videoView.stopRecord();
    
    // 是否正在录像
    boolean isRecording = videoView.isRecording();
}

// 设置录像保存目录
videoView.setRecordDirectory("/sdcard/MyApp/records");
```

### 截图功能（仅 VLC）

```java
// 检查是否支持截图
if (videoView.isSupportSnapshot()) {
    // 截图（原始尺寸）
    videoView.takeSnapshot("/sdcard/snapshots/img_001.jpg");
    
    // 截图（指定尺寸）
    videoView.takeSnapshot("/sdcard/snapshots/img_001.jpg", 1920, 1080);
}

// 设置截图保存目录
videoView.setSnapshotDirectory("/sdcard/MyApp/snapshots");
```

### 状态监听

```java
// 准备完成
videoView.setOnPreparedListener(() -> {
    Log.d("Player", "准备完成，开始播放");
});

// 播放完成
videoView.setOnCompletionListener(() -> {
    Log.d("Player", "播放完成");
});

// 播放错误
videoView.setOnErrorListener((errorCode, errorMsg) -> {
    Log.e("Player", "播放错误: " + errorMsg);
    return true; // 返回 true 表示已处理
});
```

### 直播流检测

```java
// 检查是否是直播流
if (videoView.isLiveStream()) {
    // 直播流处理
    Log.d("Player", "当前是直播流");
}

// 检查是否支持进度拖动
if (videoView.isSeekable()) {
    videoView.seekTo(position);
}
```

---

## API 参考

### UniversalVideoView 主要方法

| 方法 | 说明 |
|------|------|
| `setPlayerType(int type)` | 设置播放器内核类型 |
| `play(String url)` | 播放视频 |
| `play(String url, Map headers)` | 带请求头播放 |
| `start()` | 开始/继续播放 |
| `pause()` | 暂停 |
| `stop()` | 停止 |
| `reset()` | 重置 |
| `release()` | 释放资源 |
| `seekTo(long position)` | 跳转到指定位置 |
| `getCurrentPosition()` | 获取当前位置 |
| `getDuration()` | 获取总时长 |
| `isPlaying()` | 是否正在播放 |
| `isLiveStream()` | 是否是直播流 |
| `isSeekable()` | 是否支持进度拖动 |
| `setSpeed(float speed)` | 设置播放速度 |
| `setVolume(float left, float right)` | 设置音量 |
| `setLooping(boolean looping)` | 设置循环播放 |
| `setAspectRatioType(int type)` | 设置宽高比 |
| `enterFullscreen()` | 进入全屏 |
| `exitFullscreen()` | 退出全屏 |
| `isSupportRecord()` | 是否支持录像 |
| `startRecord(String dir, String name)` | 开始录像 |
| `stopRecord()` | 停止录像 |
| `isSupportSnapshot()` | 是否支持截图 |
| `takeSnapshot(String path)` | 截图 |

### PlayerState 状态枚举

| 状态 | 说明 |
|------|------|
| `IDLE` | 空闲 |
| `INITIALIZED` | 已初始化 |
| `PREPARING` | 准备中 |
| `PREPARED` | 准备完成 |
| `PLAYING` | 播放中 |
| `PAUSED` | 暂停 |
| `BUFFERING` | 缓冲中 |
| `COMPLETED` | 播放完成 |
| `STOPPED` | 已停止 |
| `ERROR` | 错误 |
| `RELEASED` | 已释放 |

---

## 自定义配置

### 控制器可见性设置

```java
// 隐藏返回按钮
videoView.setBackVisible(false);

// 隐藏全屏按钮
videoView.setFullscreenVisible(false);

// 隐藏静音按钮
videoView.setMuteVisible(false);

// 隐藏锁屏按钮
videoView.setLockVisible(false);

// 隐藏录像按钮
videoView.setRecordVisible(false);

// 隐藏截图按钮
videoView.setSnapshotVisible(false);

// 隐藏进度条
videoView.setSeekBarVisible(false);

// 隐藏时间显示
videoView.setTimeVisible(false);

// 隐藏整个控制栏
videoView.setTopBarVisible(false);
videoView.setBottomBarVisible(false);
videoView.setLeftBarVisible(false);
videoView.setRightBarVisible(false);
```

### 自定义图标

```java
// 播放/暂停按钮图标
videoView.setPlayPauseIcon(R.drawable.my_play, R.drawable.my_pause);

// 全屏按钮图标
videoView.setFullscreenIcon(R.drawable.my_fullscreen, R.drawable.my_exit_fullscreen);

// 静音按钮图标
videoView.setMuteIcon(R.drawable.my_mute, R.drawable.my_unmute);

// 锁屏按钮图标
videoView.setLockIcon(R.drawable.my_lock, R.drawable.my_unlock);

// 录像按钮图标
videoView.setRecordIcon(R.drawable.my_record, R.drawable.my_record_active);

// 截图按钮图标
videoView.setSnapshotIcon(R.drawable.my_snapshot);

// 返回按钮图标
videoView.setBackIcon(R.drawable.my_back);
```

### 添加自定义按钮

```java
// 创建自定义按钮
CustomButton micButton = new CustomButton(1001, R.drawable.ic_mic)
        .setTag("mic")
        .setOrder(15)  // 排序值，数字越小越靠上（录像=10，截图=20）
        .setClickListener(button -> {
            // 处理点击事件
            Toast.makeText(this, "麦克风按钮点击", Toast.LENGTH_SHORT).show();
        });

// 添加按钮
videoView.addCustomButton(micButton);

// 更新按钮图标
videoView.updateCustomButtonIcon(1001, R.drawable.ic_mic_off);

// 设置按钮可见性
videoView.setCustomButtonVisible(1001, false);

// 移除按钮
videoView.removeCustomButton(1001);

// 清除所有自定义按钮
videoView.clearCustomButtons();
```

### 按钮排序

```java
// 设置录像按钮排序（默认10）
videoView.setRecordOrder(5);

// 设置截图按钮排序（默认20）
videoView.setSnapshotOrder(25);

// 设置自定义按钮排序
videoView.setCustomButtonOrder(1001, 15);
```

---

## 注意事项

### 生命周期管理

```java
@Override
protected void onResume() {
    super.onResume();
    if (videoView != null && !videoView.isPlaying()) {
        videoView.start();
    }
}

@Override
protected void onPause() {
    super.onPause();
    if (videoView != null && videoView.isPlaying()) {
        videoView.pause();
    }
}

@Override
protected void onDestroy() {
    super.onDestroy();
    if (videoView != null) {
        videoView.release();  // 必须调用！
    }
}
```

### 横竖屏切换优化

```java
// 在 AndroidManifest.xml 中配置
android:configChanges="orientation|screenSize|keyboardHidden"

// 在 Activity 中处理
@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (videoView != null) {
        videoView.onOrientationChanged();
    }
}
```

### 权限要求

```xml
<!-- 网络权限 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 存储权限（录像/截图需要） -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Android 11+ 存储权限 -->
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
```

### 常见问题

1. **直播流不支持进度拖动和倍速播放**
   - 这是正常的，直播流没有总时长概念

2. **录像和截图功能不可用**
   - 确保使用 VLC 内核：`videoView.setPlayerType(PlayerType.VLC)`
   - 确保已授予存储权限

3. **进度条拖动后回弹**
   - 已优化，HLS 流 seek 需要时间，框架会自动处理

4. **VLC 播放黑屏**
   - 确保在 Surface 准备好后再播放
   - 检查视频格式是否支持

5. **内存泄漏**
   - 确保在 `onDestroy()` 中调用 `release()`

---

## 依赖库

- VLC: `org.videolan.android:libvlc-all`
- IJK: `tv.danmaku.ijk:ijkplayer-java`
- 权限: `com.github.getActivity:XXPermissions`

---

## License

```
Copyright 2025 LoveLin

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
