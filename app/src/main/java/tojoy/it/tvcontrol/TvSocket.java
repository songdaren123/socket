package tojoy.it.tvcontrol;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName: ServerSocket
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/6 1:07 PM
 */
public class TvSocket {
    private String TAG = "songmingzhan-tv";
    private ServerSocket serverSocket;
    public static int port = 6038;

    public void init(android.os.Handler handler) {
        try {
            serverSocket = new ServerSocket(port);
            accept(handler);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "init: ");
        }
    }

    private void accept(android.os.Handler handler) throws IOException {
        AudioTrackUtils audioTrackUtils = null;
        if (serverSocket != null) {
            while (true) {
                Socket socket = serverSocket.accept();
                Log.d(TAG, "accept: ");
                handler.sendEmptyMessage(2);
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
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
            }
        }

    }

}
