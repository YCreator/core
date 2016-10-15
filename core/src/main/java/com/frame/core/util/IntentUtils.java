package com.frame.core.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

/**
 * 跳转工具
 * Created by yzd on 2016/5/23.
 */
public class IntentUtils {

    /**
     * 跳转到手机相册
     * @param activity
     * @param type1
     * @param type2
     */
    public static void intentImgLib(Activity activity, int type1, int type2) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(intent, type1);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            activity.startActivityForResult(intent, type2);
        }
    }

    /**
     * 跳转到手机浏览器
     * @param context       上下文环境
     * @param url           链接地址
     */
    public static void intentSystemWeb(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(content_url);
        context.startActivity(intent);
    }

    /**
     * 跳转到其他应用
     * @param context
     * @param packageName
     */
    public static void intentOtherApp(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
        } else {
            ToastUtil.showShort(context, "未安装该应用");
        }
    }

    /**
     * 重新启动app
     * @param context
     */
    public static void restartApplication(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}
