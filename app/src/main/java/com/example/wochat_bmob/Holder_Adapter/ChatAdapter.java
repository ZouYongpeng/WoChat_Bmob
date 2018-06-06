package com.example.wochat_bmob.Holder_Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.example.wochat_bmob.Listener.OnRecyclerViewListener;
import com.example.wochat_bmob.base.BaseViewHolder;
import com.example.wochat_bmob.bean.User;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.v3.BmobUser;

/**
 * Created by 邹永鹏 on 2018/6/4.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    /*消息类型*/
    /*同意添加好友成功后的样式*/
    private final int TYPE_AGREE = 0;
    /*文本*/
    private final int TYPE_TEXT_RECEIVED=1;
    private final int TYPE_TEXT_SEND=2;

    /*事件显示间隔*/
    private final long TIME_INTERVAL = 10 * 60 * 1000;

    private List<BmobIMMessage> msgs = new ArrayList<>();

    private User mUser;

    BmobIMConversation mConversation;

    private OnRecyclerViewListener mOnRecyclerViewListener;

    public ChatAdapter(Context context,BmobIMConversation conversation){
        mUser=BmobUser.getCurrentUser(User.class);
        mConversation=conversation;
    }

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener){
        mOnRecyclerViewListener=onRecyclerViewListener;
    }

    public void addMessage(BmobIMMessage msg){
        this.msgs.add(msg);
        notifyDataSetChanged();
    }

    public void addMessages(List<BmobIMMessage> msgs){
        this.msgs.addAll(msgs);
        notifyDataSetChanged();
    }

    /*查找msgs中message的位置*/
    public int findPosition(BmobIMMessage message){
        int index=getCount();
        int position=-1;
        while (index--> 0) {
            if (message.equals(getItem(index))){
                position=index;
                break;
            }
//            index--;
        }
        return position;
    }

    /*根据id查找位置*/
    public int findPosition(long id){
        int index=getCount();
        int position=-1;
        while (index--> 0) {
            if (this.getItemId(index)==id){
                position=index;
                break;
            }
//            index--;
        }
        return position;
    }

    /*获取msgs的长度*/
    public int getCount() {
        if (msgs==null){
            return 0;
        }else{
            return msgs.size();
        }
    }

    /*获取第index位置的message*/
    public BmobIMMessage getItem(int index){
        if (msgs==null || msgs.size()<=index){
            return null;
        }else {
            return msgs.get(index);
        }
    }

    public void remove(int position){
        msgs.remove(position);
        notifyDataSetChanged();
    }

    /*获取最上面的message*/
    public BmobIMMessage getFirstMessage(){
        if (msgs==null){
            return null;
        }else {
            return msgs.get(0);
        }
    }

    /*根据类型获取viewHoder*/
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        switch (viewType){
            /*发送文本消息*/
            case TYPE_TEXT_SEND:
                return new SendTextHolder(parent.getContext(),parent,mConversation,mOnRecyclerViewListener);
            /*接收文本消息*/
            case TYPE_TEXT_RECEIVED:
                return new ReceiveTextHolder(parent.getContext(),parent,mOnRecyclerViewListener);
            case TYPE_AGREE:
                return new AgreeHolder(parent.getContext(),parent,mOnRecyclerViewListener);
            default:
                return null;
        }
//        return null;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder,int position){
        ((BaseViewHolder)holder).bindData(msgs.get(position));
        Boolean shouldShowTime=shouldShowTime(position);
        if (holder instanceof SendTextHolder){
            ((SendTextHolder)holder).showTime(shouldShowTime);
        }else if (holder instanceof ReceiveTextHolder){
            ((ReceiveTextHolder)holder).showTime(shouldShowTime);
        }else {
            ((AgreeHolder)holder).showTime(shouldShowTime);
        }
    }

    @Override
    public int getItemViewType(int position) {
        BmobIMMessage message=msgs.get(position);

        /*判断是发送还是接收*/
        boolean isSend;
        String messageFormId=message.getFromId();
        String currentUID=mUser.getObjectId();
        if (messageFormId.equals(currentUID)){
            isSend=true;
        }else {
            isSend=false;
        }

        /*判断消息类型*/
        String messageType=message.getMsgType();
        if (messageType.equals(BmobIMMessageType.TEXT.getType())){
            return isSend ? TYPE_TEXT_SEND:TYPE_TEXT_RECEIVED;
        }
        else if (messageType.equals("agree")){
            return TYPE_AGREE;
        }else{
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }

    private boolean shouldShowTime(int position){
        /*第一条消息显示时间*/
        if (position == 0) {
            return true;
        }
        /*如果当前消息与上条消息的时间间隔大于一小时，则显示时间*/
        long lastTime=msgs.get(position-1).getCreateTime();
        long curTime=msgs.get(position).getCreateTime();
        if (curTime-lastTime>TIME_INTERVAL){
            return true;
        }else {
            return false;
        }
    }

}
