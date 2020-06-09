package tojoy.it.tvcontrol;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class NetActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MSG_RECONNECTD = 1;
    private String TAG = "songmingzhan-NetActivity";
    private RadioGroup mRadioGroup;
    private TextView mConnectState;
    private TextView serverIp;
    private EditText editText;
    private LinearLayout mServerLayout;
    private LinearLayout mClientLayout;
    private Button mVoice;
    private Button mConnect;
    public static int COUNT = 0;
    ClientSocket clientSocket;
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    mConnectState.setText("连接成功");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);
        mConnectState = findViewById(R.id.connect_state);
        mClientLayout = findViewById(R.id.net_client);
        mServerLayout = findViewById(R.id.net_server);
        mRadioGroup = findViewById(R.id.rg_radiogroup);
        mVoice = findViewById(R.id.button_voice);
        mConnect = findViewById(R.id.connect);
        serverIp = findViewById(R.id.net_address);
        editText = findViewById(R.id.input_address);
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
                        initServer();
                        break;
                    case R.id.radio_disconnect:
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClientSocket clientSocket = new ClientSocket();
                clientSocket.createSocket(address, mHandler);
            }
        }).start();
    }


    private void initServer() {
        String ip = NetUtils.getLocalIpAddress(this);
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
        switch (v.getId()) {
            case R.id.connect:
                initClient();
                break;
        }
    }
}