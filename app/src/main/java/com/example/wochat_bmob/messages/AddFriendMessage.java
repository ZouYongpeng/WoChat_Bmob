package com.example.wochat_bmob.messages;

import android.text.TextUtils;
import android.util.Log;

import com.example.wochat_bmob.base.Config;
import com.example.wochat_bmob.db.NewFriend;

import org.json.JSONObject;

import cn.bmob.newim.bean.BmobIMExtraMessage;
import cn.bmob.newim.bean.BmobIMMessage;

/**
 * Created by 邹永鹏 on 2018/5/21.
 * 自定义消息类型，用于发送添加好友请求
 * 自定义添加好友的消息类型
 */

public class AddFriendMessage extends BmobIMExtraMessage {

    public AddFriendMessage() {
    }

    /*将BmobIMMessage转成NewFriend*/
    public static NewFriend convert(BmobIMMessage msg){
        NewFriend add=new NewFriend();
        String content=msg.getContent();
        add.setMsg(content);
        add.setTime(msg.getCreateTime());
        add.setStatus(Config.STATUS_VERIFY_NONE);//未读-未添加->接收到别人发给我的好友添加请求，初始状态
        try {
            String extra=msg.getExtra();
            if (!TextUtils.isEmpty(extra)){
                JSONObject json = new JSONObject(extra);
                String name = json.getString("name");
                add.setName(name);
                String avatar = json.getString("avatar");
                add.setAvatar(avatar);
                add.setUid(json.getString("uid"));
            }else {
                Log.d("AddFriendMessage", "extra为空");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return add;
    }

    /*重写getMsgType方法，填写自定义的消息类型；*/
    @Override
    public String getMsgType() {
        return "add";
    }

    /*重写isTransient方法，定义是否是暂态消息。*/
    @Override
    public boolean isTransient() {
        //设置为true,表明为暂态消息，那么这条消息并不会保存到本地db中，SDK只负责发送出去
        //设置为false,则会保存到指定会话的数据库中
        return true;
    }
}
