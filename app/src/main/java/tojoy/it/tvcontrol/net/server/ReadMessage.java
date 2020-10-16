package tojoy.it.tvcontrol.net.server;

import java.nio.ByteBuffer;

/**
 * <pre>
 *     author : songmingzhan
 *     e-mail : songmingzhan@tojoy.com
 *     time   : 2020/10/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface ReadMessage {
    void receiveMsg(ByteBuffer byteBuffer);
}
