package tojoy.it.tvcontrol.net;

import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
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
    private DataInputStream inputStream = null;
    private volatile int operator = 0;//操作类型 0 空闲
    private KeepLive mKeepLive;
    private ReaderThread readerThread;
    private Handler mHandler;
    private String ip;
    private int port;
    private volatile boolean isAccept = false;
    private volatile boolean pauseRead = false;
//    private int len;

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
//            mHandler.sendEmptyMessage(mSocket.isConnected() ? NetActivity.MSG_CONNECTED : NetActivity.MSG_DISCONNECT);
            output = new DataOutputStream(mSocket.getOutputStream());
            inputStream = new DataInputStream(mSocket.getInputStream());
            byte state = 3;
            readerThread = new ReaderThread();
            readerThread.start();
            sendCmd(state);
            sendString("老板001");//请求连接

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
        if (isAccept) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    writeAudio();
                }
            }).start();
        }

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

    public void sendString(String str) {
        try {
            output.writeUTF(str);
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

    /**
     * 发送信令
     *
     * @param cmd 2 心跳 1，实时音频 3 设备名称
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

    private class ReaderThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (true) {
                    LogUtil.logd(TAG, "有没有");
                    int len = 0;
                    byte[] bt = new byte[1024 * 2];
                    while (!pauseRead && (len = inputStream.read(bt, 0, bt.length)) != -1) {
                        LogUtil.logd(TAG, "有没有-----");
                        if (bt[0] == 7 && len == 2) {//已有用户链接，是否下线
                            pauseRead = true;
                            LogUtil.logd(TAG, "已有用户链接");
                            readerDetail(7);
                            break;

                        } else if (bt[0] == 8 && len == 2) {//被踢下线，关闭当前链接
                            LogUtil.logd(TAG, "被踢下线");
                            isAccept = false;
                            pauseRead = true;
                            readerDetail(8);
                            break;
                        } else if (bt[0] == 9 && len == 2) {
                            isAccept = true;
                            LogUtil.logd(TAG, "可以连接");
                            mHandler.sendEmptyMessage(NetActivity.MSG_CONNECTED);
                            mKeepLive = new KeepLive();
                            mKeepLive.start();
                        }

                    }

                }
            } catch (Exception e) {
                LogUtil.logd(TAG, "Exception: " + e);
            }

        }
    }

    private void readerDetail(int state) {
        try {
            String name = inputStream.readUTF();
            LogUtil.logd(TAG, "linkName: name:" + name + "----" + state);
            Message msg = Message.obtain();
            msg.what = state;
            msg.obj = name;
            mHandler.sendMessage(msg);
            pauseRead = false;

        } catch (IOException e) {
            LogUtil.logd(TAG, "linkName: name:" + e);
            e.printStackTrace();
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
