
#### 如果帮助的到了您，请您不要吝啬你的Star，谢谢您的点赞（Star），3Q
#### 如果帮助的到了您，请您不要吝啬你的Star，谢谢您的点赞（Star），3Q


#### 使用VLC，作为播放器来实现，录像，截图功能(点播,直播都支持!) 所以在此我觉得把这个Demo开源出来，希望能帮助到需要的人！


#### 动图欣赏

![](picture/dialog/1.jpg) ![](picture/dialog/2.jpg)
![image](https://github.com/ILoveLin/VlcRecordPlayer/blob/main/picture/gif/1.gif )
![](https://github.com/ILoveLin/VlcRecordPlayer/blob/main/picture/gif/1.gif)
#### 项目使用
* 备注：您不需要编译任何东西，只需要下载Demo，拷贝main目录下的 jniLibs包下的所有so依赖库 和app/libs目录下 ijkplayer-java-release.aar   （这个是ijk工程里面的java代码）到你自己的项目里面.
*
* 第一步：(只支持直播噢!)
* 只需要下载Demo，拷贝main目录下的 jniLibs包下的所有so依赖库 和app目录下 ijkplayer-java-release.aar   （这个是ijk工程里面的java代码）
* 您在app的build.gradle中android节点里面找到-->defaultConfig节点--->然后添加
*    ndk {abiFilters 'arm64-v8a','armeabi' ,'armeabi-v7a', 'x86', 'x86_64'}     // 设置支持的SO库架构
*  然后在 dependencies中添加    implementation files('libs/ijkplayer-java-release.aar')     //第一步拷贝的aar的依赖
*
* 第二步：(记得动态获取权限在调用以下API,不然会失败哦)
* 项目中如何使用？(只支持直播噢!)
*
* 直播中：开始录像：  mPlayer.startRecord(mRecordPath);   //mRecordPath  是录像存入的文件路径
* 直播中：结束录像：  mPlayer.stopRecord();
* 直播中：截图：      Bitmap srcBitmap = Bitmap.createBitmap(1920,1080, Bitmap.Config.ARGB_8888);    mPlayer.getCurrentFrame(srcBitmap);   //PS：异步的哦，记得开线程！！！！
*

* 重要的事情说三遍！！！
* What ？还是不懂?请您下载Demo,查看MainActivity里面的简单代码，我相信您一看就会~~~！
* What ？还是不懂?请您下载Demo,查看MainActivity里面的简单代码，我相信您一看就会~~~！
* What ？还是不懂?请您下载Demo,查看MainActivity里面的简单代码，我相信您一看就会~~~！
*
#### 简单说明
* 网上有很多修改底层C的代码，实现录像和截图，但是好多都会程序奔溃和闪退，这个Demo做了优化

* 修改底层C代码增加了三个native的API：
* 我们使用的其实都是底层调用这三个方法：
* public native int startRecord(String var1);           //开始录像
* public native int stopRecord();                       //结束录像
* public native boolean getCurrentFrame(Bitmap var1);   //截图



