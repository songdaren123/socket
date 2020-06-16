package tojoy.it.tvcontrol.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class AppUtil {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getWidth(Context context) {
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getHeight(Context context) {
        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    //判断应用是否在前台
    public static boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void bringCurrentApplicationToFront(Context context) {
        if (!isRunningForeground(context)) {
            //获取ActivityManager
            ActivityManager mAm = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            //获得当前运行的task
            List<ActivityManager.RunningTaskInfo> taskList = mAm.getRunningTasks(100);
            for (ActivityManager.RunningTaskInfo rti : taskList) {
                //找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
                if (rti.topActivity.getPackageName().equals(context.getPackageName())) {
                    mAm.moveTaskToFront(rti.id, 0);
                    return;
                }
            }
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @return px
     */
    public static int getWindowWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    public static boolean checkAppInstalled(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;//true为安装了，false为未安装
        }
    }
}
