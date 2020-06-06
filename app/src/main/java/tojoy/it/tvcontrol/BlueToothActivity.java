package tojoy.it.tvcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

public class BlueToothActivity extends AppCompatActivity implements View.OnClickListener {
    BlueClient client;
    private int REQUEST_ENABLE_BT = 10;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

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
    private BlueServer server;
    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    mConnectState.setText("成功连接服务起");
                    break;
                case 3:
                    mConnectState.setText("连接客户端成功");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);
        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothManager.getAdapter();
        bluetoothAdapter = bluetoothManager.getAdapter();
        openBlue();
        initView();
    }

    private void initView() {
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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                client = new BlueClient(bluetoothAdapter, bluetoothManager, BlueToothActivity.this, mHandler);
                                client.start();
                                client.registerReceiver();
                            }
                        }).start();

                        break;
                    case R.id.radio_server:
                        mClientLayout.setVisibility(View.GONE);
                        mServerLayout.setVisibility(View.VISIBLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                server = new BlueServer(bluetoothAdapter, bluetoothManager, mHandler);
                                server.initServer();
                            }
                        }).start();

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

    private void openBlue() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (REQUEST_ENABLE_BT == requestCode) {

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

    private void initClient() {

    }
}