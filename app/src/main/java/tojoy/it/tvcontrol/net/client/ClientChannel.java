package tojoy.it.tvcontrol.net.client;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import javax.xml.transform.sax.TemplatesHandler;

import tojoy.it.tvcontrol.task.MsgReceived;
import tojoy.it.tvcontrol.task.SocketMsgTask;

/**
 * <pre>
 *     author : songmingzhan
 *     e-mail : songmingzhan@tojoy.com
 *     time   : 2020/10/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ClientChannel implements Runnable {
    private SocketChannel channel;
    private Selector selector;
    private SocketAddress socketAddress;
    private static final int BUFFER_SIZE = 1024;
    private String TAG = "songmingzhan";
    private MsgReceived msgReceived;

    public ClientChannel(String ip, int port) {
        socketAddress = new InetSocketAddress(ip, port);
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

//    private static class SingleTone {
//        static ClientChannel clientChannel = new ClientChannel();
//    }

//    public static ClientChannel getInstance() {
//        return SingleTone.clientChannel;
//    }

    public void init(String ip, int port) {
        socketAddress = new InetSocketAddress(ip, port);
        Log.d(TAG, "init: " + socketAddress.toString());
    }

    private void connect() throws IOException {
        selector = Selector.open();
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        Log.d(TAG, "connect: ");
        if (channel.connect(socketAddress)) {
            Log.d(TAG, "开始连接");
        } else {
            channel.register(selector, SelectionKey.OP_CONNECT);
            Log.d(TAG, "注册监听");
        }


        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            SelectionKey key;
            while (keyIterator.hasNext()) {
                key = keyIterator.next();
                keyIterator.remove();
                handleInput(key);

            }
        }

    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (socketChannel.finishConnect()) {
                    Log.d(TAG, "handleInput: 已连接");
                    doWrite("你好呀呀呀");
                } else {
                    Log.d(TAG, "handleInput: 退出");
                    System.exit(1);
                }

            }
            if (key.isReadable()) {
                doRead(socketChannel);
            }

        }

    }

    public void doRead(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        long byteRead = socketChannel.read(buffer);
        Log.d(TAG, "doRead:len " + byteRead);
        while (byteRead > 0) {
            buffer.flip();
            byte[] data = buffer.array();
            String msg = new String(data).trim();
            Log.d(TAG, "doRead: " + msg);
            if (msgReceived != null) {
                msgReceived.onReceived(msg);
            }
            buffer.clear();
            byteRead = socketChannel.read(buffer);
        }

    }

    public void doWrite(String str) throws IOException {
        if (channel.isConnected()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            byteBuffer.put(str.getBytes());
            byteBuffer.flip();
            channel.write(byteBuffer);
        }

    }

    public void setMsgReceived(MsgReceived msgReceived) {
        this.msgReceived = msgReceived;
    }

    public void sendMessage(final String msg) {
        SocketMsgTask.SERIAL_EXECUTOR.execute(() -> {
            try {
                doWrite(msg);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: " + e.toString());

            }
        });
    }
}
