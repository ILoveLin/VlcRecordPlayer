package com.shenma.vlcrecordplayer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.shenma.vlcrecordplayer.util.CoreUtil;
import com.shenma.vlcrecordplayer.util.EnumConfig;
import com.shenma.vlcrecordplayer.util.FileUtil;
import com.shenma.vlcrecordplayer.util.LogUtils;
import com.shenma.vlcrecordplayer.vlc.ENDownloadView;
import com.shenma.vlcrecordplayer.vlc.ENPlayView;
import com.shenma.vlcrecordplayer.vlc.MyControlVlcVideoView;
import com.vlc.lib.RecordEvent;
import com.vlc.lib.VlcVideoView;
import com.vlc.lib.listener.MediaListenerEvent;

import org.videolan.libvlc.Media;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * author : LoveLin
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2023/1/13
 * desc   : 主界面
 */
public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private TextView m2VLCPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mContext = MainActivity.this;
        initView();
        responseListener();
    }

    private void initView() {
        m2VLCPlayer = findViewById(R.id.tv_to_vlc);
    }

    /**
     * 跳转播放界面
     */
    private void responseListener() {
        m2VLCPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VlcPlayerActivity.class);
                startActivity(intent);
            }
        });
    }
}




