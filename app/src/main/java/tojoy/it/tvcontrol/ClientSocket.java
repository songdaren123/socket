package tojoy.it.tvcontrol;

import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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

    public void createSocket(String ip, Handler handler) {
        try {
            if (mSocket == null || mSocket.isClosed())
                mSocket = new Socket();
            Log.i(TAG, "isClosed:" + mSocket.isClosed());
            mSocket.connect(new InetSocketAddress(ip, TvSocket.port));
            mSocket.setSoTimeout(50000);
            mSocket.setKeepAlive(true);
            handler.sendEmptyMessage(mSocket.isConnected() ? 2 : 0);
            writeAudio();
        } catch (UnknownHostException e) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.d(TAG, "createSocket: " + e1);
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
            Log.i(TAG, "IOException1:" + e);
        }
    }

    private void writeAudio() {
        try {
            if (mSocket != null && mSocket.isConnected()) {
                output = new DataOutputStream(mSocket.getOutputStream());
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
            try {
                mSocket.close();
                output.close();
                isConnected = false;

            } catch (IOException e1) {
                e.printStackTrace();
                Log.d(TAG, "writeAudio: "+e1);
            }
        }
        catch (Exception e){
            Log.d(TAG, "writeAudio: "+e);
        }
    }

}
