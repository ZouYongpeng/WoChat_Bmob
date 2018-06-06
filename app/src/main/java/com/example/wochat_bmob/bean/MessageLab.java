package com.example.wochat_bmob.bean;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 邹永鹏 on 2018/5/15.
 * 测试消息列表的单例类
 */

public class MessageLab {

//    private static MessageLab sMessageLab;
//
//    private List<MainMessage> mMainMessages;
//
//    public static MessageLab getMessageLab(Context context){
//        if (sMessageLab==null)
//            sMessageLab=new MessageLab(context);
//        return sMessageLab;
//    }
//
//    private MessageLab(Context context){
//        mMainMessages=new ArrayList<>();
//        for (int i=0;i<100;i++){
//            MainMessage message=new MainMessage();
//            message.setFriendName("friend # "+i);
//            message.setMessage("来自friend # "+i+" 的信息");
//            message.setTime("09:"+i%100);
//            mMainMessages.add(message);
//        }
//    }
//
//    public List<MainMessage> getMainMessages(){
//        return mMainMessages;
//    }
//
//    public MainMessage getCrime(String name){
//        for (MainMessage message:mMainMessages){
//            if (message.getFriendName().equals(name)){
//                return message;
//            }
//        }
//        return null;
//    }
}
