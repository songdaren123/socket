package tojoy.it.tvcontrol.net;


import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import tojoy.it.tvcontrol.utils.AudioTrackUtils;
import tojoy.it.tvcontrol.utils.LogUtil;

/**
 * @ClassName: ServerSocket
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 1:07 PM
 */
public class TvSocket {
    private String TAG = "TvSocket";
    private ServerSocket serverSocket;
    public static int port = 6083;
    private static ThreadPoolExecutor sBackupExecutor;
    private static final int CORE_POOL_SIZE = 5;//核心线程
    private static final int MAX_POOL_SIZE = 20;//最大线程数
    private static final int KEEP_ALIVE_SECONDS = 3;
    /**
     * 空闲 0， 正在处理数据 1
     */
    private volatile int readSate = 0;


    public void init(android.os.Handler handler) {
        try {
            serverSocket = new ServerSocket(port);
            initThreadPool();
            accept(handler);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.logd(TAG, "init: "+e);
        }
    }

    private void accept(android.os.Handler handler) throws IOException {
        if (serverSocket != null) {
            while (true) {
                Socket socket = serverSocket.accept();
                LogUtil.logd(TAG, "accept: ");
                handler.sendEmptyMessage(NetActivity.MSG_CONNECTED);
                sBackupExecutor.execute(new ClientThread(socket));
            }
        }

    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    private void initThreadPool() {
        if (sBackupExecutor == null) {
            sBackupExecutor = new ThreadPoolExecutor(
                    CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS,
                    TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), sThreadFactory);
            sBackupExecutor.allowCoreThreadTimeOut(true);
        }
    }

    private class ClientThread implements Runnable, AudioTrackUtils.AudioTrackInterface {
        private Socket socket;
        DataInputStream inputStream;
        AudioTrackUtils audioTrackUtils = null;
        int len = 0;
        private boolean isHeartbeat = true;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                inputStream = new DataInputStream(socket.getInputStream());
                accpetHeartbeat();
            } catch (Exception e) {
                LogUtil.logd(TAG, "Exception: " + e);
            }

        }

        //读取心跳数据
        private void accpetHeartbeat() {
            isHeartbeat = true;
            try {
                while (true && isHeartbeat) {
                    byte[] bt = new byte[1024 * 2];
                    while ((len = inputStream.read(bt, 0, bt.length)) != -1) {
                        if (bt[0] == 1 && len == 2) {
                            LogUtil.logd(TAG, "run: 开始接受音频包");
                            if (readSate == 0) {
                                readSate = 1;
                                isHeartbeat = false;
                                readerAudio();
                                break;
                            }
                        } else if (bt[0] == 2 && len == 2) {
                            LogUtil.logd(TAG, "run: 心跳包");
                            if (audioTrackUtils != null) {
                                audioTrackUtils.stop();
                                audioTrackUtils = null;
                            }

                        }

                    }

                }
            } catch (Exception e) {
                LogUtil.logd(TAG, "Exception: " + e);
            }

        }

        private void readerAudio() {
            try {
                len = 0;
                while (len != -1) {
                    if (audioTrackUtils == null) {
                        audioTrackUtils = new AudioTrackUtils();
                        audioTrackUtils.setAudioTrackInterface(this);
                        audioTrackUtils.play();
                    }
                    len = audioTrackUtils.write();

                }
                audioTrackUtils.stop();
                accpetHeartbeat();
            } catch (Exception e) {
                LogUtil.logd(TAG, "Exception: " + e);
            }
        }


        @Override
        public int getAudioData(@NonNull byte[] audioData, int offsetInBytes, int sizeInBytes) {
            try {
                len = inputStream.read(audioData, 0, sizeInBytes);
                LogUtil.logd(TAG, "getAudioData: len-->" + len);
            } catch (Exception e) {
                LogUtil.logd(TAG, "getAudioData:Exception :" + e);
            }
            if (len == 2 || len == -1) {
                len = -1;
                readSate = 0;
            }
            return len;
        }
    }

}
