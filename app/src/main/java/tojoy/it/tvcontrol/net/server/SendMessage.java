package tojoy.it.tvcontrol.net.server;

import java.nio.Buffer;

/**
 * <pre>
 *     author : songmingzhan
 *     e-mail : songmingzhan@tojoy.com
 *     time   : 2020/10/16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface SendMessage {
    void sendMsg(Buffer buffer);
}
