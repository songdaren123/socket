package tojoy.it.tvcontrol;

import android.util.Log;

/**
 * @ClassName: LogUtil
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/8 4:19 PM
 */
class LogUtil {
    private static String TAG = "songmingzhan";

    public static void logd(String tag, String msg) {
        Log.d(TAG + tag, msg);
    }
}
