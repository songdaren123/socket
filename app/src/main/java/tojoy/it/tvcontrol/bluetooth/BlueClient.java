package tojoy.it.tvcontrol.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import tojoy.it.tvcontrol.utils.LogUtil;
import tojoy.it.tvcontrol.utils.RecoderUtils;

/**
 * @ClassName: BlueClient
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 5:55 PM
 */
class BlueClient {
    private String TAG = this.getClass().getSimpleName();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothDevice mBluetoothDevice;
    private BlueBroadcastReceiver receiver;
    private UUID mUUID = null;
    private Context context;
    private Handler handler;
    private String name = "songdaren";//

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
        LogUtil.logd(TAG, "start: ");
        if (bluetoothAdapter != null) {
            mUUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
            bluetoothAdapter.startDiscovery();
            requestProfileConnectionState(context);
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : devices) {
                LogUtil.logd(TAG, "start: " + device.getName());
                if (device.getName().equals(name)) {
                    mBluetoothDevice = device;
                    LogUtil.logd(TAG, "start: 去连接");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connectDevice();
                        }
                    }).start();
                    break;
                }

            }
        }
    }

    private void requestProfileConnectionState(Context context) {
        //检测连接状态：
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int gatt = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT);
        int sap = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            sap = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.SAP);
        }
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
                        LogUtil.logd(TAG, "ProxyListener: " + mBluetoothDevice.getName());
                        if (mBluetoothDevice.getName().equals(name)) {
                            LogUtil.logd(TAG, "ProxyListener: " + mBluetoothDevice.getUuids());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    connectDevice();
                                }
                            }).start();
                        }

                    }
                }
                bluetoothAdapter.closeProfileProxy(profile, proxy);
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    }


    private void connectDevice() {
        bluetoothAdapter.cancelDiscovery();
        BluetoothSocket mmSocket;
        try {
            mmSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUUID);
            mmSocket.connect();
            mmSocket.getOutputStream();
            handler.sendEmptyMessage(mmSocket.isConnected() ? 2 : 0);
            LogUtil.logd(TAG, "connectDevice: " + mmSocket.isConnected());
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
        } catch (Exception e) {
            LogUtil.logd(TAG, "connectDevice: " + e);
        }
    }

    private class BlueBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogUtil.logd(TAG, "onReceive: " + Objects.requireNonNull(device).getName());
                if (device.getName().equals(name)) {
                    bluetoothAdapter.cancelDiscovery();
                    mBluetoothDevice = device;
//                    LogUtil.logd(TAG, String.format("onReceive: %s", device.getUuids()));
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
}

