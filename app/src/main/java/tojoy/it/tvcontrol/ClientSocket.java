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
public class ClientSocket implements Runnable {
    private String TAG = "songmingzhan-ClientSocket";
    private Socket mSocket;
    private DataOutputStream output = null;
    private volatile int operator = 0;//操作类型 0 空闲
    private KeepLive mKeepLive;
    private Handler mHandler;
    private String ip;

    public ClientSocket(Handler mHandler, String ip) {
        this.mHandler = mHandler;
        this.ip = ip;
    }

    public void connect() {
        try {
            if (mSocket == null || mSocket.isClosed())
                mSocket = new Socket();
            LogUtil.logd(TAG, "isClosed:" + mSocket.isClosed());
            mSocket.connect(new InetSocketAddress(ip, TvSocket.port));
            mSocket.setSoTimeout(50000);
            mSocket.setKeepAlive(true);
            mHandler.removeMessages(3);
            mHandler.sendEmptyMessage(mSocket.isConnected() ? 2 : 0);
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
            mHandler.sendEmptyMessage(1);
            try {
                mSocket.close();
                output.close();

            } catch (IOException e1) {
                e.printStackTrace();
                LogUtil.logd(TAG, "writeAudio: " + e1);
            }
        } catch (Exception e) {
            LogUtil.logd(TAG, "writeAudio: " + e);
        }
    }

    @Override
    public void run() {
        connect();
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
                        LogUtil.logd(TAG, "KeepLive--心跳: ");
                    }
                    Thread.sleep(2000);
                }
            } catch (IOException e) {
                mHandler.sendEmptyMessage(1);
                e.printStackTrace();
                LogUtil.logd(TAG, "KeepLive1---心跳异常" + e);
                try {
                    mSocket.close();
                    output.close();
                } catch (Exception e1) {
                    e.printStackTrace();
                    LogUtil.logd(TAG, "KeepLive2: " + e1);
                }
            } catch (Exception e) {
                LogUtil.logd(TAG, "KeepLive3: " + e);
            }
        }
    }

    //断开链接
    public void disconnect() {
        if (mSocket != null && mSocket.isConnected()) {
            try {
                mSocket.close();
                output.close();
                mHandler.sendEmptyMessage(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
