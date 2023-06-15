# VLC播放器Demo（录像，截图等功能），可二次开发。
# ffmpeg-Kit （录像，截图,合流播放,合流推送(到本地手机服务器或者公司三方服务器),等一些列视频操作功能），可二次开发。
# ZLMediaKit 作为本地推流服务器的使用，可以直接提取so或者aar文件给自己二次开发。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q。

#### 如果帮助的到了您，请您不要吝啬你的Star，先谢谢您的点赞（Star），3Q，3Q，3Q。

* vlc编译，源库地址是这个博主的：https://github.com/mengzhidaren/Vlc-sdk-lib 可以去star支持下。
* ffmpeg-kit编译，源库地址是这个博主的：https://github.com/arthenica/ffmpeg-kit 可以去star支持下。
* ZLMediaKit编译，源库地址是这个博主的：https://github.com/ZLMediaKit/ZLMediaKit 可以去star支持下。

* 基于VLC的播放器（Android
  录像，截图），可做二次开发，支持在点播或者直播，播放的时候：录像，截图等等功能。支持RTSP，RTMP，HTTP，HLS，HTTPS等等。支持所有CPU架构。

* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！
* 使用VLC，作为播放器：实现，录像，截图功能（点播,直播都支持!） 特意花时间写了个Demo开源出来，希望能帮助到需要的人！！


* ffmpeg-Kit 可以执行任何ffmpeg命令行操作,Demo中对直播流(也可以本地文件)进行了：录像，截图等等功能，也可以做任何你想要的操作，比如将直播音频流和视频流合流播放，合流推流等等操作。

* ffmpeg-Kit，可以实现任何ffmpeg命令行操作, 特意花时间写了个Demo开源出来，希望能帮助到需要的人！
* ffmpeg-Kit，可以实现任何ffmpeg命令行操作, 特意花时间写了个Demo开源出来，希望能帮助到需要的人！！

## 动图欣赏

* 如果看不到gif动图，请科学上网查看gif效果图，或者下载项目之后本地打开。在picture文件夹/gif文件夹/vlc.gif和ffmpeg.gif

* VLCDemo测试，动图欣赏vlc.gif
![](picture/gif/vlc.gif)


* ffmpegDemo测试，动图欣赏ffmpeg.gif
![](picture/gif/ffmpeg.gif)


* ZLMediaKit测试，动图欣赏zmlkit.gif
![](picture/gifzmlkit.gif)

#### VLC播放器，使用指南

* 请直接，下载Demo查看，通俗易懂，谢谢。

* 主要功能（录像，截图），我封装在MyControlVlcVideoView，这个自定义View里面去了，可以直接下载Demo搜索“截图”，“录像”，功能查看代码即可。

* 主要功能（录像，截图），我封装在MyControlVlcVideoView，这个自定义View里面去了，可以直接下载Demo搜索“截图”，“录像”，功能查看代码即可。

* VlcPlayerActivity：此界面是播放界面：只做视频的播放 mPlayerView.setStartLive(mPath01)；

* MyControlVlcVideoView：自己封装的播放控制View：我把所有功能都封装在这个View里面。如：截图，录像，设置静音播放，手势触摸调节亮度和声音，切换高清播放，打开相册，全屏显示，锁屏功能，设置播放标题，退出界面等等(
可以自行添加自己需要的功能)。

#### ffmpeg-kit，使用指南

* 请直接，下载Demo查看，通俗易懂，谢谢。

* 主要功能（录像）：
  ```java
       //电视台直播流
      //private String mVideoPath = "http://220.161.87.62:8800/hls/0/index.m3u8";
      //录像命令
      // private String CMD = "-i http://220.161.87.62:8800/hls/0/index.m3u8 -c copy ";
            //获取uri地址
            Uri uri = FileUtil.createVideoPathUri(MainActivity.this);
            //获取存储mp4文件地址
            String outputVideoPath = FFmpegKitConfig.getSafParameter(MainActivity.this, uri, "rw");
              FFmpegKit.executeAsync(CMD + outputVideoPath, new FFmpegSessionCompleteCallback() {
  
                @Override
                public void apply(FFmpegSession session) {
                    /**
                     * returnCode
                     * returnCode=1         说明:录像,失败
                     * returnCode=255       说明:录像,成功
                     * returnCode=0         说明:截图,成功
                     */
                    SessionState state = session.getState();
                    ReturnCode returnCode = session.getReturnCode();        
                    LogUtils.e("FFmpegKit" + "apply====state=" + state);         
                    LogUtils.e("FFmpegKit" + "apply====returnCode=" + returnCode);  
             
  
                }
            }, new LogCallback() {
  
                @Override
                public void apply(com.arthenica.ffmpegkit.Log log) {
  
  
                }
            }, new StatisticsCallback() {
  
                @Override
                public void apply(Statistics statistics) {
            
  
                    // CALLED WHEN SESSION GENERATES STATISTICS
  
                }
            });
    
    
     ```
     

* 主要功能（截图）：
  ```java
       //电视台直播流
         //private String mVideoPath = "http://220.161.87.62:8800/hls/0/index.m3u8";
         //截图命令
          private String CMD2 = "-i http://220.161.87.62:8800/hls/0/index.m3u8 -y -t 0.001 -ss 1 -f image2 -r 1 ";
         //获取uri地址
         Uri uri = FileUtil.createVideoPathUri(MainActivity.this);
         //获取存储mp4文件地址
         String outputVideoPath = FFmpegKitConfig.getSafParameter(MainActivity.this, uri, "rw");
         FFmpegKit.executeAsync(CMD2 + outputVideoPath, new FFmpegSessionCompleteCallback() {
       
                       @Override
                       public void apply(FFmpegSession session) {
                           SessionState state = session.getState();
                           ReturnCode returnCode = session.getReturnCode();
                           LogUtils.e("FFmpegKit" + "apply====state=" + state);            
                           LogUtils.e("FFmpegKit" + "apply====returnCode=" + returnCode);  
                    
       
                       }
                   }, new LogCallback() {
       
                       @Override
                       public void apply(com.arthenica.ffmpegkit.Log log) {
       
       
                       }
                   }, new StatisticsCallback() {
       
                       @Override
                       public void apply(Statistics statistics) {
                   
       
                           // CALLED WHEN SESSION GENERATES STATISTICS
       
                       }
                   });
    
    
     ```
  
* 其他ffmpeg命令行功能：可以使用任何ffmpeg命令行功能实现你想要的功能。

#### ZLMediaKit，作为手机端本地服务器的使用指南

* 两种方式：1：直接依赖libs的so(太大了 我直接打包放在这里要使用请下载后自己解压把so放入libs)。
*          2：使用lib是里面的aar(分debug版本和release版本按需索取)，然后在app.gradle中引用此aar即可。

* 1，开启服务器：
    ```java
          //使用之前请自己申请读写权限 谢谢(PackageManager.PERMISSION_GRANTED)
           String sd_dir = Environment.getExternalStoragePublicDirectory("").toString();
  
          ZLMediaKit.startDemo(sd_dir);

    ```
  
  
* 2，使用ffmpeg-kit 对纯音频和纯视频流进行合流(或者可以播放的直播流)，推流到本地ZLMediaKit服务器上
    ```java
      private void startFFmpegPushSteam() {
            //http://192.168.67.105:3333/api/stream/video?session=123456          //纯视频流地址(我公司的,请替换可以播放的流地址即可)。
            String steam = "-i http://192.168.67.105:3333/api/stream/video?session=123456 -i http://192.168.67.105:3333/api/stream/audio?session=123456";
            String CMD = steam + " -c copy -rtsp_transport tcp -f rtsp rtsp://127.0.0.1:8554/stream/live";
            //rtsp://127.0.0.1:8554/stream/live   //此地址是，推送到本地服务器的地址，必须两级目录不然推送失败(/steam/live)，目录名字可以随便改动。
            LogUtils.e("ffmpeg-kit--推流CMD" + "apply====CMD=" + CMD);
            FFmpegKit.executeAsync(CMD, new FFmpegSessionCompleteCallback() {
    
                @Override
                public void apply(FFmpegSession session) {
                    SessionState state = session.getState();
                    ReturnCode returnCode = session.getReturnCode();
                    /**
                     * 录像
                     * returnCode=1         说明：推流，失败
                     * returnCode=255       说明：推流，成功
                     */
                    LogUtils.e("ffmpeg-kit--推流" + "apply====state=" + state);
                    LogUtils.e("ffmpeg-kit--推流" + "apply====returnCode=" + returnCode);
    
                }
            }, new LogCallback() {
    
                @Override
                public void apply(com.arthenica.ffmpegkit.Log log) {
    
                }
            }, new StatisticsCallback() {
    
                @Override
                public void apply(Statistics statistics) {
                    LogUtils.e("ffmpeg-kit" + "推流=====" + statistics.toString());
    
    
                }
            });
    
    
        }
   ```
    
* 3，使用VLC播放即可：传入url：rtsp://127.0.0.1:8554/stream/live  即可播放。

* 4，PS：ZLMediaKit自带播放器有录像功能，等我后续更新吧。


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