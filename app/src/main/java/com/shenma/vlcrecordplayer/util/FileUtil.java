package com.shenma.vlcrecordplayer.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {

    private static MediaScannerConnection mMediaScanner;

    public static String getROMTotalSize(final Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        String availMemStr = formateFileSize(context, blockSize * totalBlocks);
        return availMemStr;

    }

    //调用系统函数，字符串转换 long -String KB/MB
    public static String formateFileSize(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }

    public static String getROMAvailableSize(final Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        String availMemStr = formateFileSize(context, blockSize * availableBlocks);
        return availMemStr;

    }

    static final int ERROR = -1;

    /**
     * 外部存储是否可用
     *
     * @return
     */
    static public boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取手机内部可用空间大小
     *
     * @return
     */
    static public String getAvailableInternalMemorySize(final Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return formateFileSize(context, availableBlocks * blockSize);
    }

    /**
     * 获取手机内部空间大小
     *
     * @return
     */
    static public String getTotalInternalMemorySize(final Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return formateFileSize(context, totalBlocks * blockSize);
    }

    /**
     * 获取手机外部可用空间大小
     *
     * @return
     */
    static public String getAvailableExternalMemorySize(final Context context) {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return formateFileSize(context, availableBlocks * blockSize);
        } else {
            return "ERROR";
        }
    }

    /**
     * 获取手机外部空间大小
     *
     * @return
     */
    static public String getTotalExternalMemorySize(final Context context) {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return formateFileSize(context, totalBlocks * blockSize);
        } else {
            return "ERROR";
        }
    }

    static public String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KiB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MiB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null)
            resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    //针对非系统影音资源文件夹
    public static void insertIntoMediaStore(Context context, boolean isVideo, File saveFile, long createTime) {
        ContentResolver mContentResolver = context.getContentResolver();
        if (createTime == 0)
            createTime = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        //值一样，但是还是用常量区分对待
        values.put(isVideo ? MediaStore.Video.VideoColumns.DATE_TAKEN
                : MediaStore.Images.ImageColumns.DATE_TAKEN, createTime);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        if (!isVideo)
            values.put(MediaStore.Images.ImageColumns.ORIENTATION, 0);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        values.put(MediaStore.MediaColumns.MIME_TYPE, isVideo);
        //插入
        mContentResolver.insert(isVideo
                ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    // 获取video的mine_type,暂时只支持mp4,3gp
    private static String getVideoMimeType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith("mp4") || lowerPath.endsWith("mpeg4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith("3gp")) {
            return "video/3gp";
        }
        return "video/mp4";
    }

    public static void RefreshAlbum(String fileAbsolutePath, boolean isVideo, Context mContext) {
        mMediaScanner = new MediaScannerConnection(mContext, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                if (mMediaScanner.isConnected()) {
                    LogUtils.e("相册更新问题:连接成功 ");
                    LogUtils.e("相册更新问题:连接成功 isVideo:" + isVideo);

                    if (isVideo) {
                        mMediaScanner.scanFile(fileAbsolutePath, "video/mp4");
                    } else {
                        mMediaScanner.scanFile(fileAbsolutePath, "image/png");
//                        mMediaScanner.scanFile(fileAbsolutePath, "image/jpeg");
                    }
                } else {
                    LogUtils.e("相册更新问题:无法更新图库，未连接，广播通知更新图库，异常情况下 ");
                }
            }

            @Override

            public void onScanCompleted(String path, Uri uri) {
                LogUtils.e("相册更新问题:扫描完成 path: " + path + "uri:" + uri);
            }

        });

        mMediaScanner.connect();

    }

    public static void scanFile(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(intent);
    }

    /**
     * 保存视频
     *
     * @param context
     * @param file
     */
    public static void saveVideo(Context context, File file) {
        //是否添加到相册
        ContentResolver localContentResolver = context.getContentResolver();
        ContentValues localContentValues = getVideoContentValues(context, file, System.currentTimeMillis());
        Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
    }

    public static ContentValues getVideoContentValues(Context paramContext, File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "video/mp4");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }


    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @return
     */
    public static boolean deleteSDFile(String path) {
        return deleteSDFile(path, false);
    }

    /**
     * 删除SD卡中的文件或目录
     *
     * @param path
     * @param deleteParent true为删除父目录
     * @return
     */
    public static boolean deleteSDFile(String path, boolean deleteParent) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        File file = new File(path);
        if (!file.exists()) {
            //不存在
            return true;
        }
        return deleteFile(file, deleteParent);
    }

    /**
     * @param file
     * @param deleteParent true为删除父目录
     * @return
     */
    public static boolean deleteFile(File file, boolean deleteParent) {
        boolean flag = false;
        if (file == null) {
            return flag;
        }
        if (file.isDirectory()) {
            //是文件夹
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    flag = deleteFile(files[i], true);
                    if (!flag) {
                        return flag;
                    }
                }
            }
            if (deleteParent) {
                flag = file.delete();
            }
        } else {
            flag = file.delete();
        }
        file = null;
        return flag;
    }

    /**
     * 添加到媒体数据库
     *
     * @param context 上下文
     */
    public static Uri fileScanVideo(Context context, String videoPath, int videoWidth, int videoHeight,
                                    int videoTime) {

        File file = new File(videoPath);
        if (file.exists()) {

            Uri uri = null;

            long size = file.length();
            String fileName = file.getName();
            long dateTaken = System.currentTimeMillis();

            ContentValues values = new ContentValues(11);
            values.put(MediaStore.Video.Media.DATA, videoPath); // 路径;
            values.put(MediaStore.Video.Media.TITLE, fileName); // 标题;
            values.put(MediaStore.Video.Media.DURATION, videoTime * 1000); // 时长
            values.put(MediaStore.Video.Media.WIDTH, videoWidth); // 视频宽
            values.put(MediaStore.Video.Media.HEIGHT, videoHeight); // 视频高
            values.put(MediaStore.Video.Media.SIZE, size); // 视频大小;
            values.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken); // 插入时间;
            values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);// 文件名;
            values.put(MediaStore.Video.Media.DATE_MODIFIED, dateTaken / 1000);// 修改时间;
            values.put(MediaStore.Video.Media.DATE_ADDED, dateTaken / 1000); // 添加时间;
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

            ContentResolver resolver = context.getContentResolver();

            if (resolver != null) {
                try {
                    uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                } catch (Exception e) {
                    e.printStackTrace();
                    uri = null;
                }
            }

            if (uri == null) {
                MediaScannerConnection.scanFile(context, new String[]{videoPath}, new String[]{"video/*"}, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
            }

            return uri;
        }

        return null;
    }

    /**
     * SD卡存在并可以使用
     */
    public static boolean isSDExists() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡的剩余容量，单位是Byte
     *
     * @return
     */
    public static long getSDFreeMemory() {
        try {
            if (isSDExists()) {
                File pathFile = Environment.getExternalStorageDirectory();
                // Retrieve overall information about the space on a filesystem.
                // This is a Wrapper for Unix statfs().
                StatFs statfs = new StatFs(pathFile.getPath());
                // 获取SDCard上每一个block的SIZE
                long nBlockSize = statfs.getBlockSize();
                // 获取可供程序使用的Block的数量
                // long nAvailBlock = statfs.getAvailableBlocksLong();
                long nAvailBlock = statfs.getAvailableBlocks();
                // 计算SDCard剩余大小Byte
                long nSDFreeSize = nAvailBlock * nBlockSize;
                return nSDFreeSize;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     *******************************************已下是ffmpegactivity界面用到的方法******************************************
     */

    /**
     * 制作图片的路径地址
     *
     * @param context
     * @return
     */
    public static String createPath(Context context) {
        String path = null;
        File file = null;
        long tag = System.currentTimeMillis();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//SDCard是否可用
            //最好把images替换成你的项目名称，避免有重复文件夹
            path = Environment.getExternalStorageDirectory() + File.separator + "images/";
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = Environment.getExternalStorageDirectory() + File.separator + "images/" + tag + ".png";
        } else {
            path = context.getFilesDir() + File.separator + "images/";
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }

            path = context.getFilesDir() + File.separator + "images/" + tag + ".png";
        }
        return path;
    }


    /**
     * 方式一:获取uri 图片路径
     * <p>
     * 创建一条图片地址uri,用于保存拍照后的照片
     *
     * @param context
     * @return 图片的uri
     */
    public static Uri createImagePathUri(Context context) {
        Uri imageFilePath = null;
        String status = Environment.getExternalStorageState();
        SimpleDateFormat timeFormatter = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            imageFilePath = context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            imageFilePath = context.getContentResolver().insert(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
        Log.i("", "生成的照片输出路径：" + imageFilePath.toString());
        return imageFilePath;
    }

    /**
     * 方式一:获取视频uri路径
     *
     * @param context
     * @return @return 视频uri路径
     */
    public static Uri createVideoPathUri(Context context) {
        Uri imageFilePath = null;
        String status = Environment.getExternalStorageState();
        SimpleDateFormat timeFormatter = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.CHINA);
        long time = System.currentTimeMillis();
        String imageName = timeFormatter.format(new Date(time));
        // ContentValues是我们希望这条记录被创建时包含的数据信息
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.DISPLAY_NAME, imageName);
        values.put(MediaStore.Video.Media.DATE_TAKEN, time);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
            imageFilePath = context.getContentResolver().insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            imageFilePath = context.getContentResolver().insert(
                    MediaStore.Video.Media.INTERNAL_CONTENT_URI, values);
        }
        Log.i("", "生成的照片输出路径：" + imageFilePath.toString());
        return imageFilePath;
    }

    /**
     * 方式二:获取uri路径
     *
     * @param context
     * @param fileName 文件名字
     * @param dir      文件夹名字
     * @param isVideo  是否是视频格式  true=是
     * @return isVideo=true 返回视频uri路径,isVideo=false 返回图片uri路径
     */
    public static Uri publicDirURI(Context context, String fileName, String dir, boolean isVideo) {
        ContentValues valuesVideos;
        valuesVideos = new ContentValues();
        valuesVideos.put(MediaStore.Video.Media.RELATIVE_PATH, dir);
        valuesVideos.put(MediaStore.Video.Media.TITLE, fileName);
        valuesVideos.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        if (isVideo) {
            valuesVideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        } else {
            valuesVideos.put(MediaStore.Images.Media.MIME_TYPE, "image/png");//331.6kb     生成png质量各大
//            valuesVideos.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");//21.86kb  生成png质量各大
        }
        valuesVideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesVideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesVideos.put(MediaStore.Video.Media.IS_PENDING, 1);//设置独占使用
        // 如要要对ContentProvider中的数据进行操作，可以通过ContentResolver(数据调用者) 对象然后结合Uri进行调用 来实现
        //https://blog.csdn.net/mr_lee1314/article/details/127918121  使用方法
        ContentResolver resolver = context.getContentResolver();
        Uri collection = null;
        if (isVideo) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        }

        Uri uriSavedVideo = resolver.insert(collection, valuesVideos);
        return saveFileToPublicMovies(context, valuesVideos, uriSavedVideo, fileName);
    }


    private static Uri saveFileToPublicMovies(Context context, ContentValues contentValues, Uri uriSavedVideo, String fileName) {
        ParcelFileDescriptor pfd;
        try {
            //加载媒体文件使用 openFileDescriptor
            pfd = context.getContentResolver().openFileDescriptor(uriSavedVideo, "rw");
            FileOutputStream out = null;
            if (pfd != null) {
                out = new FileOutputStream(pfd.getFileDescriptor());
                File videoFile = new File(context.getExternalFilesDir("AppName"), fileName);
                FileInputStream in = new FileInputStream(videoFile);
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.flush();
                out.getFD().sync();
                out.close();
                in.close();
                pfd.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        contentValues.clear();
        //如果您的应用程序执行一些可能非常耗时的操作，比如写入媒体文件，那么在文件被处理时对其进行独占访问是非常有用的。
        //在运行Android 10或更高版本的设备上，您的应用程序可以通过将IS_PENDING标志的值设置为1来获得这种独占访问。
        // 只有您的应用程序可以查看该文件，直到您的应用程序将IS_PENDING的值更改回0
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);   //回复 默认状态
        context.getContentResolver().update(uriSavedVideo, contentValues, null, null);
        return uriSavedVideo;
    }
//
//    private static Uri saveFileToPublicMovies(Context context, ContentValues contentValues, Uri uriSavedVideo, String fileName) {
//        ParcelFileDescriptor pfd;
//        try {
//            pfd = context.getContentResolver().openFileDescriptor(uriSavedVideo, "w");
//            FileOutputStream out = null;
//            if (pfd != null) {
//                out = new FileOutputStream(pfd.getFileDescriptor());
//                File videoFile = new File(context.getExternalFilesDir("FOLDER"), fileName);
//                FileInputStream in = new FileInputStream(videoFile);
//                byte[] buf = new byte[8192];
//                int len;
//                while ((len = in.read(buf)) > 0) {
//                    out.write(buf, 0, len);
//                }
//                out.close();
//                in.close();
//                pfd.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        contentValues.clear();
//        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
//        context.getContentResolver().update(uriSavedVideo, contentValues, null, null);
//        return uriSavedVideo;
//    }

    /**
     *******************************************ffmpegactivity界面用到的方法**到处结束****************************************
     */
}
