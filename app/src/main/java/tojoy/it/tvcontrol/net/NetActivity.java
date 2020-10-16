package tojoy.it.tvcontrol.net;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import tojoy.it.tvcontrol.R;
import tojoy.it.tvcontrol.net.client.ClientChannel;
import tojoy.it.tvcontrol.net.server.ServerChannel;
import tojoy.it.tvcontrol.task.MsgReceived;
import tojoy.it.tvcontrol.utils.AppUtil;
import tojoy.it.tvcontrol.utils.LogUtil;
import tojoy.it.tvcontrol.utils.QRUtils;
import tojoy.it.tvcontrol.utils.RecoderUtils;

public class NetActivity extends AppCompatActivity implements View.OnClickListener, MsgReceived {
    private String TAG = "songmingzhan";
    public static final int MSG_RECONNECTD = 3;
    public static final int MSG_CONNECTED = 2;

    public static final int MSG_OCCPUTY = 7;//已被占用
    public static final int MSG_KiCK = 8;//强踢
    private TextView messageContent;
    private TextView serverIp;
    private EditText editText;
    private LinearLayout mServerLayout;
    private LinearLayout mClientLayout;
    private ClientChannel clientChannel;

    private ImageView qrImage;
    private Button bt_kick;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);
        messageContent = findViewById(R.id.received_msg);
        mClientLayout = findViewById(R.id.net_client);
        mServerLayout = findViewById(R.id.net_server);
        RadioGroup mRadioGroup = findViewById(R.id.rg_radiogroup);
        Button mVoice = findViewById(R.id.button_voice);
        Button send = findViewById(R.id.btn_send);
        Button scan = findViewById(R.id.bt_scan);
        scan.setOnClickListener(this);
        serverIp = findViewById(R.id.net_address);
        editText = findViewById(R.id.message_content);
        qrImage = findViewById(R.id.qr_image);
        bt_kick = findViewById(R.id.bt_kikt);
        bt_kick.setOnClickListener(this);
        send.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
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


    private void initServer() {
        String ip = NetUtils.getLocalIpAddress(this);
        String qrURl = new StringBuilder().append("&QR_IP=").append(ip).append("&QR_PORT=").append(TvSocket.port).toString();
        Bitmap largeQrImage = QRUtils.createQRcodeImage("https://?" + qrURl, AppUtil.dip2px(NetActivity.this, 200), AppUtil.dip2px(NetActivity.this, 200));
        qrImage.setImageBitmap(largeQrImage);
        ServerChannel serverChannel = new ServerChannel();
        serverChannel.initServer();
        new Thread(serverChannel).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_send) {
            if (clientChannel != null) {
                clientChannel.sendMessage(editText.getText().toString().trim());
            }

        } else if (v.getId() == R.id.bt_scan) {
            Intent intent = new Intent(this, CaptureActivity.class);
            startActivityForResult(intent, 1);
        } else if (v.getId() == R.id.bt_kikt) {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.logd("TAG", "resultCode" + resultCode);
        if (resultCode == 3) {
            int port = data.getIntExtra("port", 0);
            String address = data.getStringExtra("ip");
            clientChannel = new ClientChannel(address, port);
            clientChannel.setMsgReceived(this);
            new Thread(clientChannel).start();
        }
    }

    @Override
    public void onReceived(String message) {
        runOnUiThread(() -> messageContent.setText(message));
    }
}