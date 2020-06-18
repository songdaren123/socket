package tojoy.it.tvcontrol.net;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import tojoy.it.tvcontrol.R;
import tojoy.it.tvcontrol.utils.AppUtil;
import tojoy.it.tvcontrol.utils.LogUtil;
import tojoy.it.tvcontrol.utils.QRUtils;
import tojoy.it.tvcontrol.utils.RecoderUtils;

public class NetActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int MSG_RECONNECTD = 3;
    public static final int MSG_CONNECTED = 2;
    public static final int MSG_DISCONNECT = 1;
    private static final int CONNECT_MAX = 6;
    private static final int MSG_OCCPUTY = 7;//已被占用
    private static final int MSG_KiCK = 8;//强踢
    private TextView mConnectState;
    private TextView serverIp;
    private EditText editText;
    private LinearLayout mServerLayout;
    private LinearLayout mClientLayout;
    private ClientSocket clientSocket;
    private int reconnect_count = 0;
    private boolean disconnect = false;
    private ImageView qrImage;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_RECONNECTD:
                    if (clientSocket != null) {
                        new Thread(clientSocket).start();
                    }
                    if (reconnect_count < CONNECT_MAX) {
                        reconnect_count++;
                        mHandler.sendEmptyMessageDelayed(MSG_RECONNECTD, 50000);
                    } else {
                        mConnectState.setText("重连" + reconnect_count);
                    }
                    break;
                case MSG_CONNECTED:
                    mConnectState.setText("连接成功");
                    break;
                case MSG_DISCONNECT:
                    reconnect_count = 0;
                    if (!disconnect)
                        mHandler.sendEmptyMessageDelayed(MSG_RECONNECTD, 2000);
                    mConnectState.setText("链接断开");
                    break;
                case MSG_KiCK:
                    String str = (String) msg.obj;
                    mConnectState.setText(str);
                    break;
                case MSG_OCCPUTY:
                    mConnectState.setText("占用");
                    break;
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);
        mConnectState = findViewById(R.id.connect_state);
        mClientLayout = findViewById(R.id.net_client);
        mServerLayout = findViewById(R.id.net_server);
        RadioGroup mRadioGroup = findViewById(R.id.rg_radiogroup);
        Button mVoice = findViewById(R.id.button_voice);
        Button mConnect = findViewById(R.id.connect);
        Button scan = findViewById(R.id.bt_scan);
        scan.setOnClickListener(this);
        serverIp = findViewById(R.id.net_address);
        editText = findViewById(R.id.input_address);
        qrImage = findViewById(R.id.qr_image);
        mConnect.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_client:
                        mClientLayout.setVisibility(View.VISIBLE);
                        mServerLayout.setVisibility(View.GONE);
                        break;
                    case R.id.radio_server:
                        mClientLayout.setVisibility(View.GONE);
                        mServerLayout.setVisibility(View.VISIBLE);
                        disconnect = false;
                        initServer();
                        break;
                    case R.id.radio_disconnect:
                        if (clientSocket != null) {
                            disconnect = true;
                            clientSocket.disconnect();
                        }
                        break;
                }
            }
        });
        mVoice.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        RecoderUtils.newInstance().startRecoder();
                        if (clientSocket != null) {
                            clientSocket.startRecoder();
                        }


                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        RecoderUtils.newInstance().stopRecoder();
                        break;
                }
                return false;
            }
        });
    }

    //初始化客户端
    private void initClient() {
        final String address = editText.getText().toString().trim();
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "请输入ip", Toast.LENGTH_SHORT).show();
            return;
        }
        clientSocket = new ClientSocket(mHandler, address, TvSocket.port);
        new Thread(clientSocket).start();

    }


    private void initServer() {
        String ip = NetUtils.getLocalIpAddress(this);
        String qrURl = new StringBuilder().append("&QR_IP=").append(ip).append("&QR_PORT=").append(TvSocket.port).toString();
        Bitmap largeQrImage = QRUtils.createQRcodeImage("https://?" + qrURl, AppUtil.dip2px(NetActivity.this, 200), AppUtil.dip2px(NetActivity.this, 200));
        qrImage.setImageBitmap(largeQrImage);
        if (!TextUtils.isEmpty(ip)) {
            serverIp.setText(ip);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TvSocket tvSocket = new TvSocket();
                    tvSocket.init(mHandler);
                }
            }).start();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect) {
            initClient();

        } else if (v.getId() == R.id.bt_scan) {
            Intent intent = new Intent(this, CaptureActivity.class);
            startActivityForResult(intent, 1);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.logd("TAG", "resultCode" + resultCode);
        if (resultCode == 3) {
            int port = data.getIntExtra("port", 0);
            String address = data.getStringExtra("ip");
            clientSocket = new ClientSocket(mHandler, address, port);
            new Thread(clientSocket).start();
        }
    }
}