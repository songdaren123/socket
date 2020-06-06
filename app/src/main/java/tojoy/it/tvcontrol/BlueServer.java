package tojoy.it.tvcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @ClassName: BlueServer
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 5:55 PM
 */
class BlueServer {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private Handler handler;
    private String TAG = "songmingzhan--->" + this.getClass().getSimpleName();

    public BlueServer(BluetoothAdapter bluetoothAdapter, BluetoothManager bluetoothManager, Handler handler) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothManager = bluetoothManager;
        this.handler = handler;
    }

    public void initServer() {
        BluetoothServerSocket mmServerSocket = null;
        AudioTrackUtils audioTrackUtils = null;
        try {
            mmServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("老板智慧仓", UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
            BluetoothSocket socket = mmServerSocket.accept();
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            handler.sendEmptyMessage(4);
            byte[] bt = new byte[1024 * 2];
            int len = 0;
            while ((len = inputStream.read(bt, 0, bt.length)) != -1) {
                Log.d(TAG, "accept: " + bt.length);
                if (audioTrackUtils == null) {
                    audioTrackUtils = new AudioTrackUtils();
                    audioTrackUtils.play();
                } else {
                    audioTrackUtils.write(bt, 0, len);
                }
            }

            audioTrackUtils.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
