package tojoy.it.tvcontrol.utils;

import android.util.Log;

/**
 * @ClassName: LogUtil
 * @Description: java类作用描述
 * @Author: songdaren
 * @CreateDate: 2020/6/8 4:19 PM
 */
public class LogUtil {

    public static void logd(String tag, String msg) {
        String TAG = "songmingzhan";
        Log.d(TAG + tag, msg);
    }
}
