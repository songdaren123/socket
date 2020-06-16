package tojoy.it.tvcontrol.net;

import android.os.Handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import tojoy.it.tvcontrol.utils.LogUtil;
import tojoy.it.tvcontrol.utils.RecoderUtils;

/**
 * @ClassName: ClientSocket
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 1:08 PM
 */
public class ClientSocket implements Runnable {
    private String TAG = this.getClass().getSimpleName();
    private Socket mSocket;
    private DataOutputStream output = null;
    private volatile int operator = 0;//操作类型 0 空闲
    private KeepLive mKeepLive;
    private Handler mHandler;
    private String ip;
    private int port;

    public ClientSocket(Handler mHandler, String ip, int port) {
        this.mHandler = mHandler;
        this.ip = ip;
        this.port = port;
    }

    public void connect() {
        try {
            if (mSocket == null || mSocket.isClosed())
                mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(ip, port));
            mSocket.setSoTimeout(50000);
            mSocket.setKeepAlive(true);
            mHandler.removeMessages(NetActivity.MSG_RECONNECTD);
            mHandler.sendEmptyMessage(mSocket.isConnected() ? NetActivity.MSG_CONNECTED : NetActivity.MSG_DISCONNECT);
            output = new DataOutputStream(mSocket.getOutputStream());
            mKeepLive = new KeepLive();
            mKeepLive.start();
        } catch (UnknownHostException e) {
            LogUtil.logd(TAG, "connect:UnknownHostException--> " + e);
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e1) {
                e1.printStackTrace();
                LogUtil.logd(TAG, "connect:IOException--> " + e1);
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
            LogUtil.logd(TAG, "connect:IOException:-->" + e);
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
        LogUtil.logd(TAG, "start write ");
        try {
            if (mSocket != null && mSocket.isConnected()) {
                operator = 1;
                byte cmd = 1;
                sendCmd(cmd);
                byte[] bt = new byte[1024 * 2];
                int len = 0;
                while ((len = RecoderUtils.newInstance().read(bt, 0, bt.length)) > 0) {
                    output.write(bt, 0, len);
                    output.flush();
                }
                operator = 0;
            } else {
                mHandler.sendEmptyMessage(NetActivity.MSG_DISCONNECT);
            }

        } catch (IOException e) {
            LogUtil.logd(TAG, "writeAudio: IOException-->" + e);
            e.printStackTrace();
            mHandler.sendEmptyMessage(NetActivity.MSG_DISCONNECT);
            try {
                mSocket.close();
                output.close();

            } catch (IOException e1) {
                e.printStackTrace();
                LogUtil.logd(TAG, "writeAudio: IOException-->" + e1);
            }
        } catch (Exception e) {
            LogUtil.logd(TAG, "writeAudio: Exception-->" + e);
        }
    }

    /**
     * 发送信令
     *
     * @param cmd 2 心跳 1，实时音频
     */
    private void sendCmd(byte cmd) {
        LogUtil.logd(TAG, "sendCmd:" + cmd);
        try {
            byte[] bt = new byte[2];
            bt[0] = cmd;
            output.write(bt, 0, bt.length);
            output.flush();
        } catch (IOException e) {
            mHandler.sendEmptyMessage(1);
            LogUtil.logd(TAG, "sendCmd: IOException-->" + e);
            e.printStackTrace();
            try {
                mSocket.close();
                output.close();
            } catch (Exception e1) {
                LogUtil.logd(TAG, "sendCmd: Exception-->" + e1);
            }
        } catch (Exception e) {
            LogUtil.logd(TAG, "sendCmd: Exception-->" + e);
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
            byte cmd = 2;
            try {
                while (true) {
                    if (output != null && operator == 0) {
                        sendCmd(cmd);
                    }
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                LogUtil.logd(TAG, "KeepLive: " + e);
            }

        }
    }

    //断开链接
    public void disconnect() {
        if (mSocket != null && mSocket.isConnected()) {
            try {
                mSocket.close();
                output.close();
                mHandler.sendEmptyMessage(NetActivity.MSG_DISCONNECT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
