package tojoy.it.tvcontrol;

import android.os.Handler;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @ClassName: ClientSocket
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 1:08 PM
 */
public class ClientSocket {
    private String TAG = "songmingzhan-ClientSocket";
    private Socket mSocket;
    private boolean isConnected = false;
    private DataOutputStream output = null;
    //操作类型 0 空闲
    private volatile int operator = 0;
    private KeepLive mKeepLive;

    public void createSocket(String ip, Handler handler) {
        try {
            if (mSocket == null || mSocket.isClosed())
                mSocket = new Socket();
            LogUtil.logd(TAG, "isClosed:" + mSocket.isClosed());
            mSocket.connect(new InetSocketAddress(ip, TvSocket.port));
            mSocket.setSoTimeout(50000);
            mSocket.setKeepAlive(true);
            handler.sendEmptyMessage(mSocket.isConnected() ? 2 : 0);
            output = new DataOutputStream(mSocket.getOutputStream());
            mKeepLive = new KeepLive();
            mKeepLive.start();
        } catch (UnknownHostException e) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e1) {
                e1.printStackTrace();
                LogUtil.logd(TAG, "createSocket: " + e1);
            }
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            LogUtil.logd(TAG, "IOException1:" + e);
        }
    }

    public void startRecoder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudio();
            }
        }).start();
    }

    public void writeAudio() {
        LogUtil.logd(TAG, "writeAudio--1: ");
        try {
            LogUtil.logd(TAG, "writeAudio--mSocket: " + mSocket + "-----" + mSocket.isConnected());
            if (mSocket != null && mSocket.isConnected()) {
                operator = 1;
                byte[] cmd = new byte[2];
                cmd[0] = 1;
                output.write(cmd, 0, cmd.length);
                output.flush();
                byte[] bt = new byte[1024 * 2];
                int len = 0;
                while ((len = RecoderUtils.newInstance().read(bt, 0, bt.length)) > 0) {
                    output.write(bt, 0, len);
                    output.flush();
                    LogUtil.logd(TAG, "writeAudio--->len: " + len);
                }
                operator = 0;
            }

        } catch (IOException e) {
            LogUtil.logd(TAG, "writeAudio: " + e);
            e.printStackTrace();
            try {
                mSocket.close();
                output.close();
                isConnected = false;

            } catch (IOException e1) {
                e.printStackTrace();
                LogUtil.logd(TAG, "writeAudio: " + e1);
            }
        } catch (Exception e) {
            LogUtil.logd(TAG, "writeAudio: " + e);
        }
    }

    private class KeepLive extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    if (output != null && operator == 0) {
                        byte[] bt = new byte[2];
                        bt[0] = 2;
                        output.write(bt, 0, bt.length);
                        output.flush();
                        LogUtil.logd(TAG, "KeepLive--1: ");
                    }
                    Thread.sleep(2000);
                }
            } catch (IOException e) {
                e.printStackTrace();
                LogUtil.logd(TAG, "KeepLive1:" + e);
                try {
                    mSocket.close();
                    output.close();
                    isConnected = false;

                } catch (Exception e1) {
                    e.printStackTrace();
                    LogUtil.logd(TAG, "KeepLive2: " + e1);
                }
            } catch (Exception e) {
                LogUtil.logd(TAG, "KeepLive3: " + e);
            }
        }
    }

}
