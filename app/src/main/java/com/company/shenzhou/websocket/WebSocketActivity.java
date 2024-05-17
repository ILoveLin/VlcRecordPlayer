package com.company.shenzhou.websocket;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.company.shenzhou.R;
import com.company.shenzhou.util.LogUtils;
import com.microsoft.signalr.HubConnection;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/6/1 11:18
 * desc：WebSocket服务器和移动端（Android ios）双向通讯Demo
 */
public class WebSocketActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "WebSocket:";
    private TextView tv_send;
    private EditText et_send;
    private TextView tv_receive;
    private EditText et_receive;
    private EditText et_websocket_address;
    private HubConnection hubConnection;
    private TextView tv_back;
    private TextView tv_connect;
    private WebSocketClient webSocketClient;
    private boolean connected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket);
        initView();
        responseListener();
    }

    /**
     * 初始化WebSocket和连接到服务器
     */
    private void initWebSocket2ConnectServer() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String address = et_websocket_address.getText().toString().trim();
                ToastUtils.showShort("WebSocket 连接address=" + address);
                try {
                    //1，初始化WebSocket
                    webSocketClient = new WebSocketClient(new URI(address)) {
                        @Override
                        public void onOpen(ServerHandshake handshake) {
                            LogUtils.e(TAG + "onOpen,WebSocket连接已成功建立");
                            //  WebSocket连接已成功建立
                            // 在此执行任何必要的操作
                            connected = true;
                        }

                        @Override
                        public void onMessage(String message) {
                            //  4，处理来自服务器的传入消息
                            LogUtils.e(TAG + "onMessage,接受到服务器数据 message : " + message);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    et_receive.setText("");
                                    et_receive.setText("" + message);
                                }
                            });
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {
                            LogUtils.e(TAG + "onClose ");
                            //  WebSocket连接已关闭
                            //  在此执行任何必要的清理操作
                            connected = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showShort("WebSocket onClose ");
                                }
                            });

                        }

                        @Override
                        public void onError(Exception e) {
                            LogUtils.e(TAG + "onError Exception:" + e);
                            // 处理WebSocket连接期间发生的任何错误
                            connected = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showShort("WebSocket onError Exception:" + e);
                                }
                            });
                        }
                    };
                    //2，连接服务器
                    webSocketClient.connect();
                } catch (URISyntaxException e) {
                    connected = false;
                    ToastUtils.showShort("WebSocket 连接异常 e=" + e);
                    e.printStackTrace();
                }

            }
        }.start();

    }

    private void initView() {
        tv_connect = findViewById(R.id.tv_connect);
        et_websocket_address = findViewById(R.id.et_websocket_address);
        tv_send = findViewById(R.id.tv_send);
        et_send = findViewById(R.id.et_send);
        tv_receive = findViewById(R.id.tv_receive);
        et_receive = findViewById(R.id.et_receive);
        tv_back = findViewById(R.id.tv_back2);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_connect: //连接WebSocket
                initWebSocket2ConnectServer();
                break;
            case R.id.tv_back2:
                this.finish();
                break;
            case R.id.tv_send:
                //3，发送数据
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        if (null != webSocketClient && connected) {
                            webSocketClient.send("Android发送的数据" + et_send.getText().toString().trim());
                        } else {
                            ToastUtils.showShort(TAG + "==连接状态==connected：" + connected);
                        }
                    }
                }.start();
                break;

        }
    }


    private void responseListener() {
        tv_send.setOnClickListener(this);
        tv_back.setOnClickListener(this);
        tv_connect.setOnClickListener(this);
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
        connected = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                //5，销毁
                if (null != webSocketClient) {
                    webSocketClient.close();
                }
            }
        }.start();

    }


    @Override
    public void onBackPressed() {

    }
}