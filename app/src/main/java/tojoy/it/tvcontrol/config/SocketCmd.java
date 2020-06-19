package tojoy.it.tvcontrol.config;

public class SocketCmd {
    /**
     * 音频
     */
    public static final byte CMD_AUDIO = 1;
    /**
     * 心跳
     */
    public static final byte CMD_HEART_BEAT = 2;
    /**
     * 请求连接
     */
    public static final byte CMD_CONNECT = 3;
    /**
     * 强踢下线
     */
    public static final byte CMD_KiCK = 4;
    /**
     * 大屏断开当前的连接
     */
    public static final byte CMD_DISCONNECT = 5;
    /**
     * 设置占用
     */
    public static final byte CMD_OCCPUTY = 6;
    /**
     * 接受设备连接
     */
    public static final byte CMD_ACCEPT = 7;
}
