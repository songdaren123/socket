package tojoy.it.tvcontrol;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @ClassName: BlueClient
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 5:55 PM
 */
class BlueClient {
    private String TAG = "songmingzhan---->";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothDevice mBluetoothDevice;
    private BlueBroadcastReceiver receiver;
    private UUID mUUID = null;
    private Context context;
    private Handler handler;

    public BlueClient(BluetoothAdapter bluetoothAdapter, BluetoothManager bluetoothManager, Context context, Handler handler) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothManager = bluetoothManager;
        this.context = context;
        this.handler = handler;
    }

    public void registerReceiver() {
        receiver = new BlueBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        // 用BroadcastReceiver来取得搜索结果
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        // 两种情况会触发ACTION_DISCOVERY_FINISHED：1.系统结束扫描（约12秒）；2.调用cancelDiscovery()方法主动结束扫描
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter);
    }


    public void start() {
        Log.d(TAG, "start: ");
        if (bluetoothAdapter != null) {
            mUUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
            UUID uuids[] = new UUID[]{mUUID};
            bluetoothAdapter.startDiscovery();
            requestProfileConnectionState(context);
        }
    }

    private void requestProfileConnectionState(Context context) {
        //检测连接状态：
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int gatt = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT);
        int sap = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.SAP);
        //据是否有连接获取已连接的设备
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (gatt == BluetoothProfile.STATE_CONNECTED) {
            flag = gatt;
        } else if (sap == BluetoothProfile.STATE_CONNECTED) {
            flag = sap;
        }
        if (flag != -1) {
            ProxyListener mProxyListener = new ProxyListener();
            bluetoothAdapter.getProfileProxy(context, mProxyListener, flag);
        }
    }

    private class ProxyListener implements BluetoothProfile.ServiceListener {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {

            if (proxy != null) {
                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                if (mDevices.size() > 0) {
                    for (int i = 0; i < mDevices.size(); i++) {
                        //获取BluetoothDevice
                        mBluetoothDevice = mDevices.get(i);
                        //调用创建Socket连接
                        if (mBluetoothDevice.getName().equals("songdaren"))
                            Log.d(TAG, "onReceive: " + mBluetoothDevice.getUuids());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                connectDevice();
                            }
                        }).start();
                    }
                }
                bluetoothAdapter.closeProfileProxy(profile, proxy);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    }


    private class FoundDeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)
                    || BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {

                int a2dpState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
                int adapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                if (BluetoothA2dp.STATE_CONNECTED == a2dpState || BluetoothAdapter.STATE_CONNECTED == adapterState) {//连接成功
                    //获取BluetoothDevice
                    mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d("", mBluetoothDevice.getAddress());
                    //调用创建Socket连接
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connectDevice();
                        }
                    }).start();

                } else if (BluetoothA2dp.STATE_CONNECTING == a2dpState) {//正在连接
                } else if (BluetoothA2dp.STATE_DISCONNECTED == a2dpState) {//取消连接

                }
            }
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    }

    private void connectDevice() {
        bluetoothAdapter.cancelDiscovery();
        BluetoothSocket mmSocket = null;
        try {
            mmSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUUID);
            mmSocket.connect();
            mmSocket.getOutputStream();
            handler.sendEmptyMessage(mmSocket.isConnected() ? 2 : 0);
            Log.d(TAG, "connectDevice: " + mmSocket.isConnected());
            if (mmSocket.isConnected()) {
                DataOutputStream output = new DataOutputStream(mmSocket.getOutputStream());
                while (true) {
                    byte[] bt = new byte[1024 * 2];
                    int len = 0;
                    while ((len = RecoderUtils.newInstance().read(bt, 0, bt.length)) > -1) {
                        output.write(bt, 0, len);
                        output.flush();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class BlueBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "onReceive: " + device.getName());
                if (device.getName().equals("songdaren"))
                    bluetoothAdapter.cancelDiscovery();
                mBluetoothDevice = device;
                Log.d(TAG, "onReceive: " + device.getUuids());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectDevice();
                    }
                }).start();

            }

        }
    }
}

