package com.company.shenzhou.signalr;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.company.shenzhou.R;
import com.company.shenzhou.util.LogUtils;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/6/1 11:18
 * desc：Signal服务器和移动端（Android ios）双向通讯Demo
 *
 */
public class SignalRActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "SignalR:";
    private TextView tv_send;
    private EditText et_send;
    private TextView tv_receive;
    private EditText et_receive;
    private HubConnection hubConnection;
    private TextView tv_back;
    private boolean connected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signalr);
        initView();
        initData();
        initSignalR();
        responseListener();
    }

    private void initSignalR() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                //1，创建连接   填写服务器地址
                hubConnection = HubConnectionBuilder.create("http://192.168.72.123:50000/hub").build();

                //2，接收消息方法设置。
                hubConnection.on("receiveMessage", (json) -> {
                    LogUtils.e("jochen+hubConnection SDKSig:New " + ",json: " + json);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            et_receive.setText("");
                            et_receive.setText("" + json);
                        }
                    });

                }, String.class);

                //3，进行连接
                try {
                    hubConnection.start().blockingAwait(); //进行连接
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("jochen+hubConnection link fail 1," + e.getMessage());
                }

            }
        }.start();


    }

    private void initView() {
        tv_send = findViewById(R.id.tv_send);
        et_send = findViewById(R.id.et_send);
        tv_receive = findViewById(R.id.tv_receive);
        et_receive = findViewById(R.id.et_receive);
        tv_back = findViewById(R.id.tv_back2);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back2:
                //发送数据
                this.finish();
                break;
            case R.id.tv_send:
                //4，发送数据
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        hubConnection.invoke("receiveMessage", "Android发送的数据" + et_send.getText().toString().trim());
                    }
                }.start();
                break;

        }
    }

    private void initData() {


    }

    private void responseListener() {
        tv_send.setOnClickListener(this);
        tv_back.setOnClickListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread() {
            @Override
            public void run() {
                super.run();
                //5，销毁
                hubConnection.stop();
                if (null != hubConnection) {
                    hubConnection = null;
                }
            }
        }.start();

    }


    @Override
    public void onBackPressed() {

    }
}