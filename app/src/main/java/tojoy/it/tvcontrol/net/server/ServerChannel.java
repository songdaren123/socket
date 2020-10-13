package tojoy.it.tvcontrol.net.server;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import tojoy.it.tvcontrol.utils.LogUtil;

public class ServerChannel {
    private String TAG = this.getClass().getSimpleName();
    private ServerSocketChannel socketChannel;
    private SelectableChannel selectableChannel;
    private Selector mSelect;
    private static final int BUFFER_SIZE = 1024;

    public void initServer() {
        try {
            mSelect = Selector.open();
            socketChannel = ServerSocketChannel.open();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                socketChannel.bind(new InetSocketAddress(6083));
                socketChannel.accept();
                selectableChannel.configureBlocking(false);
            }
            socketChannel.register(mSelect, SelectionKey.OP_ACCEPT);
            while (true) {
                mSelect.select();
                Set keys = mSelect.selectedKeys();
                Iterator iterable = keys.iterator();
                while (iterable.hasNext()) {
                    SelectionKey key = (SelectionKey) iterable.next();
                    iterable.remove();
                    if (key.isAcceptable()) {

                    } else if (key.isConnectable()) {

                    } else if (key.isReadable()) {

                    } else if (key.isWritable()) {

                    }
                }

            }
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
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        long byteRead = socketChannel.read(buffer);
        while (byteRead > 0) {
            buffer.flip();
            byte[] data = buffer.array();
            String info = new String(data).trim();
            Log.d(TAG, "客户端发来的消息：" + info);
            buffer.clear();
            byteRead = socketChannel.read(buffer);
        }

    }

}
