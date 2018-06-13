package com.example.wochat_bmob.base;

import android.app.Application;
import android.content.Context;

import com.example.wochat_bmob.tools.WoChatMessageHandler;

import org.litepal.LitePalApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import cn.bmob.newim.BmobIM;

/**
 * Created by 邹永鹏 on 2018/5/14.
 * 自定义Application，还要在AndroidManifest.xml中配置
 */

public class WoChatApplication extends Application {

    private static Context sContext;

    /*1、初始化方法包含了DataSDK的初始化步骤，故无需再初始化DataSDK。
    * 2、最好判断只有主进程运行的时候才进行初始化，避免资源浪费。*/

    @Override
    public void onCreate() {
        super.onCreate();
        sContext=getApplicationContext();
        LitePalApplication.initialize(sContext);
        /*初始化IM SDK，并注册消息接收器*/
        if (getApplicationInfo().packageName.equals(getMyProcessName())){
            BmobIM.init(this);
            BmobIM.registerDefaultMessageHandler(new WoChatMessageHandler(sContext));
        }
    }

    /*获取当前运行的进程名*/
    public static String getMyProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Context getContext(){
        return sContext;
    }
}
