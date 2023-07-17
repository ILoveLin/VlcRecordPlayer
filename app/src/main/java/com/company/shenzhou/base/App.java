package com.company.shenzhou.base;

import android.app.Application;

import com.company.shenzhou.util.LogUtils;
import com.didichuxing.doraemonkit.DoKit;
import com.tencent.live2.V2TXLivePremier;
import com.tencent.mmkv.MMKV;
import com.tencent.rtmp.TXLiveBase;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.cookie.CookieJarImpl;
import com.zhy.http.okhttp.cookie.store.MemoryCookieStore;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;


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
        // MMKV 初始化
        MMKV.initialize(this);

        initTencentLive();

        //请求工具的拦截器  ,可以设置证书,设置可访问所有的https网站,参考https://www.jianshu.com/p/64cc92c52650
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .cookieJar(new CookieJarImpl(new MemoryCookieStore()))                  //内存存储cookie
                .connectTimeout(5000L, TimeUnit.MILLISECONDS)
                .addInterceptor(new MyInterceptor(this))                      //拦截器,可以添加header 一些信息
                .readTimeout(5000L, TimeUnit.MILLISECONDS)
                .hostnameVerifier(new HostnameVerifier() {//允许访问https网站,并忽略证书
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

        OkHttpUtils.initClient(okHttpClientBuilder.build());


    }

    private void initTencentLive() {
        String licenceURL = "https://license.vod2.myqcloud.com/license/v2/1255750344_1/v_cube.license"; // 获取到的 licence url
        String licenceKey = "05fcb2597e0e53dfa98cd026c388455e"; // 获取到的 licence key
        V2TXLivePremier.setLicence(this, licenceURL, licenceKey);
        V2TXLivePremier.setObserver(new V2TXLivePremier.V2TXLivePremierObserver() {
            @Override
            public void onLicenceLoaded(int result, String reason) {
                LogUtils.e("MyApplication==腾讯直播初始化" + "onLicenceLoaded: result:" + result + ", reason:" + reason);
            }

            @Override
            public void onLog(int level, String log) {
                super.onLog(level, log);
                LogUtils.e("MyApplication==腾讯直播初始化" + "onLicenceLoaded: log:" + log );

            }
        });

        String licenceInfo = TXLiveBase.getInstance().getLicenceInfo(this);
        LogUtils.e("MyApplication==腾讯直播初始化" + "licenceInfo:" + licenceInfo );

    }


}










