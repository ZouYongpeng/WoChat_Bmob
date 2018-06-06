package com.example.wochat_bmob.bean;

import android.content.Context;
import android.text.TextUtils;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.Config;
import com.example.wochat_bmob.base.WoChatApplication;
import com.example.wochat_bmob.db.NewFriend;
import com.example.wochat_bmob.db.NewFriendManager;

/**
 * Created by 邹永鹏 on 2018/5/26.
 */

public class NewFriendMainMessage extends MainMessage {

    NewFriend lastFriend;

    public NewFriendMainMessage(NewFriend friend){
        this.lastFriend=friend;
        this.cName="好友请求";
    }

    @Override
    public String getLastMessageContent() {
        if(lastFriend!=null){
            Integer status =lastFriend.getStatus();
            String name = lastFriend.getName();
            if(TextUtils.isEmpty(name)){
                name = lastFriend.getUid();
            }
            //目前的好友请求都是别人发给我的
            if(status==null || status== Config.STATUS_VERIFY_NONE||status ==Config.STATUS_VERIFY_READED){
                return name+"请求添加好友";
            }else{
                return "我已添加"+name;
            }
        }else{
            return "";
        }
    }

    @Override
    public long getLastMessageTime() {
        if(lastFriend!=null){
            return lastFriend.getTime();
        }else{
            return 0;
        }
    }

    @Override
    public Object getAvatar() {
        return R.drawable.login_user_head;
    }

    @Override
    public int getUnReadCount() {
        return NewFriendManager.getInstance(WoChatApplication.getContext()).getNewInvitationCount();
    }

    @Override
    public void readAllMessages() {
        //批量更新未读未认证的消息为已读状态
        NewFriendManager.getInstance(WoChatApplication.getContext()).updateBatchStatus();
    }

    @Override
    public void onClick(Context context) {
//        Intent intent = new Intent();
//        intent.setClass(context, NewFriendActivity.class);
//        context.startActivity(intent);
    }

    @Override
    public void onLongClick(Context context) {
//        NewFriendManager.getInstance(context).deleteNewFriend(lastFriend);
    }
}
