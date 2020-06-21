package tojoy.it.tvcontrol.net;

import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import tojoy.it.tvcontrol.config.SocketCmd;
import tojoy.it.tvcontrol.utils.CloseUtil;
import tojoy.it.tvcontrol.utils.LogUtil;
import tojoy.it.tvcontrol.utils.RecoderUtils;
import tojoy.it.tvcontrol.utils.StringUtil;

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
    private volatile boolean shutdown = false;//结束当前所有的任务
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
            mSocket.setKeepAlive(true);//2消失发送心跳包
            output = new DataOutputStream(mSocket.getOutputStream());
            inputStream = new DataInputStream(mSocket.getInputStream());
            readerThread = new ReaderThread();
            readerThread.start();
            sendCmd(SocketCmd.CMD_CONNECT);
            sendString("老板001");//请求连接

        } catch (UnknownHostException e) {
            LogUtil.logd(TAG, "connect:UnknownHostException--> " + e);
            CloseUtil.close(output);
            CloseUtil.close(mSocket);
            e.printStackTrace();
        } catch (IOException e) {
            CloseUtil.close(output);
            CloseUtil.close(mSocket);
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
                sendCmd(SocketCmd.CMD_AUDIO);
                byte[] bt = new byte[1024 * 2];
                int len = 0;
                while ((len = RecoderUtils.newInstance().read(bt, 0, bt.length)) > 0) {
                    output.write(bt, 0, len);
                    output.flush();
                }
                operator = 0;
            } else {
                mHandler.sendEmptyMessage(NetActivity.MSG_RECONNECTD);
            }

        } catch (IOException e) {
            LogUtil.logd(TAG, "writeAudio: IOException-->" + e);
            e.printStackTrace();
            mHandler.sendEmptyMessage(NetActivity.MSG_RECONNECTD);
            CloseUtil.close(output);
            CloseUtil.close(mSocket);
        } catch (Exception e) {
            LogUtil.logd(TAG, "writeAudio: Exception-->" + e);
        }
    }

    public void sendString(String str) {
        try {
            byte[] bt = str.getBytes(StandardCharsets.UTF_8);
            output.write(bt, 0, bt.length);
            output.flush();
        } catch (IOException e) {
            mHandler.sendEmptyMessage(1);
            LogUtil.logd(TAG, "sendCmd: IOException-->" + e);
            e.printStackTrace();
            CloseUtil.close(output);
            CloseUtil.close(mSocket);
        } catch (Exception e) {
            LogUtil.logd(TAG, "sendCmd: Exception-->" + e);
        }

    }

    /**
     * 发送信令
     */
    public void sendCmd(byte cmd) {
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
            CloseUtil.close(output);
            CloseUtil.close(mSocket);

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
            try {
                while (!shutdown) {
                    if (output != null && operator == 0) {
                        sendCmd(SocketCmd.CMD_HEART_BEAT);
                    }
                    Thread.sleep(3000);
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
                while (!shutdown) {
                    int len = 0;
                    byte[] bt = new byte[2];
                    while (!pauseRead && (len = inputStream.read(bt, 0, bt.length)) != -1) {
                        LogUtil.logd(TAG, "有没有-----");
                        if (bt[0] == SocketCmd.CMD_OCCPUTY && len == 2) {//已有客户端连接
                            pauseRead = true;
                            LogUtil.logd(TAG, "已有用户链接");
                            readerDetail(NetActivity.MSG_OCCPUTY);
                            break;

                        } else if (bt[0] == SocketCmd.CMD_KiCK && len == 2) {//被踢下线，关闭当前链接
                            LogUtil.logd(TAG, "被踢下线");
                            isAccept = false;
                            pauseRead = true;
                            readerDetail(NetActivity.MSG_KiCK);
                            disconnect(false);
                            break;
                        } else if ((bt[0] == SocketCmd.CMD_DISCONNECT) && len == 2) {
                            pauseRead = true;
                            disconnect(true);

                        } else if (bt[0] == SocketCmd.CMD_ACCEPT && len == 2) {
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
            byte[] bt = new byte[1024];
            int lang;
            String str = null;
            while (-1 != (lang = inputStream.read(bt, 0, bt.length))) {
                str = StringUtil.getUTF(bt, lang);
                break;
            }
            LogUtil.logd(TAG, "linkName: name:" + str + "----" + state);
            Message msg = Message.obtain();
            msg.what = state;
            msg.obj = str;
            mHandler.sendMessage(msg);
            pauseRead = false;

        } catch (IOException e) {
            LogUtil.logd(TAG, "linkName: name:" + e);
            e.printStackTrace();
        }
    }

    //断开链接
    public void disconnect(boolean send) {
        if (mSocket != null && mSocket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendCmd(SocketCmd.CMD_CLIENT_DISCONNET);
                }
            }).start();
            shutdown = true;
            if (send)
                mHandler.sendEmptyMessage(NetActivity.MSG_KiCK);

        }
    }

}
