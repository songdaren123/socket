package tojoy.it.tvcontrol.utils;

import java.nio.charset.StandardCharsets;

public class StringUtil {
    /**
     * 获取UTF字符串
     */
    public static String getUTF(byte[] bt, int len) {
        byte[] nbt = new byte[len];
        System.arraycopy(bt, 0, nbt, 0, len);
        return new String(nbt, StandardCharsets.UTF_8);
    }
}
