package com.example.wochat_bmob.tools;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by 邹永鹏 on 2018/5/1.
 * 1、判断是否记住密码
 * 2、如果记住密码就通过SharedPreferences获取用户名和密码
 */

public class LoginTool {

    private static final String TAG="login";

    public static final String FORMER_LOGIN="key_former_login";
    public static final String REMEMBER_PASS="key_remember_pass";
    public static final String AUTO_LOGIN="key_auto_login";
    public static final String PASSWORD="key_password";
    public static final String OBJECT_ID="objectId";
    public static final String AVATAR="avatar";

    /*记住上一个登录用户的账号*/
    public static void setFormerLogin(String userName){
        Log.d(TAG,"setFormerLogin()"+userName);
        SharedPreferencesTool.putString(FORMER_LOGIN,userName);
    }

    /*获取上一个登录用户的账号*/
    public static String getFormerLogin(){
        return SharedPreferencesTool.getString(FORMER_LOGIN);
    }

    /*判断某一个用户是否记住密码*/
    public static boolean isRememberPass(String userName){
        return SharedPreferencesTool.getBoolean(REMEMBER_PASS+userName,false);
    }

    /*为当前用户记住密码*/
    public static void setRememberPass(String userName,boolean isRemember){
        Log.d(TAG,"setRememberPass > "+userName+" - remember? "+isRemember);
        SharedPreferencesTool.putBoolean(REMEMBER_PASS+userName,isRemember);
    }

    /*判断是否自动登录*/
    public static boolean isAutoLogin(String userName){
        return SharedPreferencesTool.getBoolean(AUTO_LOGIN+userName,false);
    }

    /*存储是否自动登录*/
    public static void setAutoLogin(String userName,boolean isAuto){
        Log.d(TAG,"setAutoLogin > "+userName+" - auto? "+isAuto);
        SharedPreferencesTool.putBoolean(AUTO_LOGIN+userName,isAuto);
    }

    public static String getPass(String userName){
        return SharedPreferencesTool.getString(PASSWORD+userName,"");
    }

    public static void setPass(String userName,String userPass){
        Log.d(TAG,"setPass > "+userName+" - auto? "+userPass);
        SharedPreferencesTool.putString(PASSWORD+userName,userPass);
    }

    /*登录时获取该用户的ID,如果能获取，这说明曾登陆过
    * 那么就可以通过本地表BmobIMUserInfo获取头像，
    * 如果不能获取的话头像设为默认*/
    public static String getObjectID(String userName){
        return SharedPreferencesTool.getString(OBJECT_ID+userName,"");
    }

    /*注册时储存该用户的objectId*/
    public static void setObjectID(String userName,String objectID){
        Log.d("register","setID > "+userName+" - objectID= "+objectID);
        SharedPreferencesTool.putString(OBJECT_ID+userName,objectID);
    }

    /*储存用户的头像*/
    public static String getAvatar(String userName){
        return SharedPreferencesTool.getString(OBJECT_ID+userName,"");
    }

    /*注册时储存该用户的objectId*/
    public static void setAvatar(String userName,String avatar){
        Log.d("register","setID > "+userName+" - avatar= "+avatar);
        SharedPreferencesTool.putString(OBJECT_ID+userName,avatar);
    }

    /*储存添加好友状态
    * status:false  还没发送过
    *        true   已发送*/
    public static void setSendAddFriendStatus(String formName,String toName,boolean status){
        SharedPreferencesTool.putBoolean(formName+"add"+toName,status);
    }

    public static boolean getSendAddFriendStatus(String formName,String toName){
        return SharedPreferencesTool.getBoolean(formName+"add"+toName,false);
    }
}
