package com.company.shenzhou.base;

import android.content.Context;
import android.util.Log;


import com.company.shenzhou.util.LogUtils;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lovelin on 2019/5/10
 * <p>
 * Describe:拦截器  添加header
 */
public class MyInterceptor implements Interceptor {
    private Context mContext;

    public MyInterceptor(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
//                .addHeader("device", "android")
//                .addHeader("Authorization", "Basic YWRtaW46ZTEwYWRjMzk0OWJhNTlhYmJlNTZlMDU3ZjIwZjg4M2U=")

//                .addHeader("token", token)
//                .addHeader("userid", userid)
                .build();

        Response response = chain.proceed(request);
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            LogUtils.e("跳转播放界面" + "拦截器=:" + headers.name(i));
            LogUtils.e("跳转播放界面" + "拦截器=:" + headers.get(headers.name(i)));


        }
        return response;
//        return chain.proceed(request);

    }
}
























