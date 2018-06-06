package com.example.wochat_bmob.Holder_Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wochat_bmob.Listener.OnRecyclerViewListener;
import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseViewHolder;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;

/**
 * Created by 邹永鹏 on 2018/6/4.
 */

public class ReceiveTextHolder extends BaseViewHolder {

    @BindView(R.id.chat_time)
    TextView chatTime;

    @BindView(R.id.friend_head)
    ImageView friendHead;

    @BindView(R.id.text_received)
    TextView textReceived;

    public ReceiveTextHolder(Context context, ViewGroup parent, OnRecyclerViewListener listener){
        super(context,parent,R.layout.chat_received_text,listener);
    }

    @Override
    public void bindData(Object o) {
        final BmobIMMessage message=(BmobIMMessage)o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        String time = dateFormat.format(message.getCreateTime());
        chatTime.setText(time);

        final BmobIMUserInfo info = message.getBmobIMUserInfo();
        /*头像忽略*/
        textReceived.setText(message.getContent());

        friendHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("你点的是对方的头像");
            }
        });

        textReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("内容为"+message.getContent());
            }
        });

        textReceived.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toast("长按也没用，还没想好什么功能呢");
                return true;
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
