package com.example.wochat_bmob.tools;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.wochat_bmob.base.WoChatContext;

/**
 * Created by 邹永鹏 on 2018/5/1.
 * 1、整合SharedPreferences数据持久化操作的工具类
 */

public class SharedPreferencesTool {

    private  static final String FileName="login_user";

    private static SharedPreferences sp;

    /*static{}(即static块)，会在类被加载的时候执行且仅会被执行一次，一般用来初始化静态变量和调用静态方法
    * 参考：https://www.cnblogs.com/caolaoshi/p/7824748.html*/
    static {
        sp = PreferenceManager.getDefaultSharedPreferences(WoChatContext.getWoIMInstance());
    }

    /*使用SharedPreferences键值对存储String*/
    public static void putString(String key,String value){
        Log.d("login","sp>key-- "+key+" , value-- "+value);
        sp.edit().putString(key, value).commit();
    }

    public static String getString(String key){
        return getString(key,"");
    }

    public static String getString(String key,String defaultValue){
        return sp.getString(key,defaultValue);
    }

    /*int*/
    public static void putInt(String key,int value){
        sp.edit().putInt(key, value).commit();
    }

    public static int getInt(String key){
        return getInt(key,0);
    }

    public static int getInt(String key,int defaultValue){
        return sp.getInt(key,defaultValue);
    }

    /*boolean*/
    public static void putBoolean(String key,boolean value){
        sp.edit().putBoolean(key, value).commit();
    }

    public static Boolean getBoolean(String key){
        return sp.getBoolean(key,false);
    }

    public static Boolean getBoolean(String key,boolean value){
        return sp.getBoolean(key,value);
    }

}
