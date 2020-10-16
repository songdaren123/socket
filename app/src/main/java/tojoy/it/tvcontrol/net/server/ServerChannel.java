package tojoy.it.tvcontrol.net.server;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServerChannel implements Runnable {
    private String TAG = "songmingzhan";
    private ServerSocketChannel socketChannel;
    private Selector mSelect;
    private static final int BUFFER_SIZE = 1024;

    public void initServer() {
        try {
            mSelect = Selector.open();
            socketChannel = ServerSocketChannel.open();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                socketChannel.bind(new InetSocketAddress(6083));
                socketChannel.configureBlocking(false);
            }
            socketChannel.register(mSelect, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doAccept(SelectionKey key) {

        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(key.selector(), SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doRead(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        SocketChannel socketChannel = (SocketChannel) key.channel();
        long byteRead = socketChannel.read(buffer);
        Log.d(TAG, "run: 可读" + byteRead);
        String message = "";
        while (byteRead > 0) {
            buffer.flip();
            byte[] data = buffer.array();
            message = new String(data).trim();
            Log.d(TAG, "客户端发来的消息：" + message);
            buffer.clear();
            byteRead = socketChannel.read(buffer);
        }
        doWrite(socketChannel, "我已收到：" + message);
        if (byteRead < 0) {
            key.cancel();
            socketChannel.close();
        }
        Log.d(TAG, "客户端发来的消息：读完");
    }

    public void doWrite(SocketChannel channel, String str) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(str.getBytes());
        byteBuffer.flip();
        channel.write(byteBuffer);
    }

    @Override
    public void run() {
        while (true) {
            try {
                mSelect.select();
                Set keys = mSelect.selectedKeys();
                Iterator iterable = keys.iterator();
                while (iterable.hasNext()) {
                    SelectionKey key = (SelectionKey) iterable.next();
                    iterable.remove();
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            doAccept(key);
                            Log.d(TAG, "run: 收到连接请求");
                        }
                        if (key.isConnectable()) {
                            Log.d(TAG, "run: 可以连接");

                        }
                        if (key.isReadable()) {
                            Log.d(TAG, "run: 可读");
//                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            doRead(key);
//
                        } else if (key.isWritable()) {
                            Log.d(TAG, "run: 可写");
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
