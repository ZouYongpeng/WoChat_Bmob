package com.example.wochat_bmob.Holder_Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wochat_bmob.Listener.OnRecyclerViewListener;
import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseViewHolder;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMSendStatus;
import cn.bmob.newim.bean.BmobIMUserInfo;

/**
 * Created by 邹永鹏 on 2018/6/4.
 */

public class SendTextHolder extends BaseViewHolder implements View.OnClickListener,View.OnLongClickListener{

    @BindView(R.id.chat_time)
    TextView chatTime;

    @BindView(R.id.my_head)
    ImageView myHead;

    @BindView(R.id.text_send)
    TextView textSend;

    @BindView(R.id.text_send_fail)
    ImageView textSendFail;

    @BindView(R.id.text_send_status)
    TextView textSendStatus;

    @BindView(R.id.text_send_progress)
    ProgressBar textSendProgress;

    BmobIMConversation mConversation;

    public SendTextHolder(Context context, ViewGroup parent,
                          BmobIMConversation conversation, OnRecyclerViewListener listener){
        super(context,parent,R.layout.chat_send_text,listener);
        mConversation=conversation;
    }

    public void bindData(Object obj){
        final BmobIMMessage message=(BmobIMMessage)obj;
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final BmobIMUserInfo info=message.getBmobIMUserInfo();
        /*获取头像省略*/

        final String time=dateFormat.format(message.getCreateTime());
        chatTime.setText(time);
        textSend.setText(message.getContent());

        /*判断消息状态，根据消息状态显示控件*/
        int status=message.getSendStatus();
        if (status== BmobIMSendStatus.SEND_FAILED.getStatus()){
            /*发送失败*/
            textSendFail.setVisibility(View.VISIBLE);
            textSendProgress.setVisibility(View.GONE);
        }else if (status== BmobIMSendStatus.SENDING.getStatus()){
            /*正在发送*/
            textSendFail.setVisibility(View.GONE);
            textSendProgress.setVisibility(View.VISIBLE);
        }else {
            /*发送成功*/
            textSendFail.setVisibility(View.GONE);
            textSendProgress.setVisibility(View.GONE);
        }

        /*点击事件*/
        textSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("当前信息："+message.getContent());
            }
        });

        textSend.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast("长按");
                return true;
            }
        });

        myHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("这是你的头像");
            }
        });

        textSendFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("该信息将重发");
            }
        });

    }

    /*判断是否显示时间*/
    public void showTime(boolean canShow){
        if (canShow){
            chatTime.setVisibility(View.VISIBLE);
        }else {
            chatTime.setVisibility(View.GONE);
        }
    }

}
