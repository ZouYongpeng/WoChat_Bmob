package com.example.wochat_bmob.tools;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by 邹永鹏 on 2018/5/1.
 * 一个Toast的工具类
 */

public class ToastTool {
    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, int resId) {
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show();
    }
}
