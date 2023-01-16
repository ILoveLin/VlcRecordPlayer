package com.shenma.vlcrecordplayer.base;

import android.app.Application;
import com.didichuxing.doraemonkit.DoKit;


/**
 * Created by Lovelin on 2019/4/27
 * <p>
 * Describe:
 */
public class App extends Application {
    public static String FILE_DIR = "/sdcard/Downloads/test/";

    private static App app;
    public static String AppFilePath = "app";

    public App() {
        app = this;
    }

    public static synchronized App getInstance() {
        if (app == null) {
            app = new App();
        }
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * 做一些sdk等等的init
         */
        new DoKit.Builder(this)
                    .build();



    }


}










