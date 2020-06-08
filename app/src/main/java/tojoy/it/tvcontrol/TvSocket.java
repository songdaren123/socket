package tojoy.it.tvcontrol;


import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: ServerSocket
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 1:07 PM
 */
public class TvSocket {
    private String TAG = "TvSocket";
    private ServerSocket serverSocket;
    public static int port = 6038;
    private static ThreadPoolExecutor sBackupExecutor;
    private static final int CORE_POOL_SIZE = 5;//核心线程
    private static final int MAX_POOL_SIZE = 20;//最大线程数
    private static final int KEEP_ALIVE_SECONDS = 3;

    public void init(android.os.Handler handler) {
        try {
            serverSocket = new ServerSocket(port);
            initThreadPool();
            accept(handler);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.logd(TAG, "init: ");
        }
    }

    private void accept(android.os.Handler handler) throws IOException {
        if (serverSocket != null) {
            while (true) {
                Socket socket = serverSocket.accept();
                LogUtil.logd(TAG, "accept: ");
                handler.sendEmptyMessage(2);
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

    private class ClientThread implements Runnable {
        private Socket socket;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            AudioTrackUtils audioTrackUtils = null;
            try {
                while (true) {

                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    byte[] bt = new byte[1024 * 2];
                    int len = 0;
                    while ((len = inputStream.read(bt, 0, bt.length)) != -1) {
                        LogUtil.logd(TAG, "accept--bt: " + bt[0]);
                        LogUtil.logd(TAG, "accept--len: " + len);
                        if (bt[0] == 1) {
                            if (audioTrackUtils == null) {
                                audioTrackUtils = new AudioTrackUtils();
                                audioTrackUtils.play();
                            } else {
                                audioTrackUtils.write(bt, 1, len);
                            }
                        } else if (bt[0] == 2) {
                            LogUtil.logd(TAG, "run: 心跳包");
                            if (audioTrackUtils != null) {
                                audioTrackUtils.stop();
                                audioTrackUtils = null;
                            }

                        }

                    }

                }
            } catch (Exception e) {
                LogUtil.logd(TAG, "run: " + e);
            }

        }
    }

}
