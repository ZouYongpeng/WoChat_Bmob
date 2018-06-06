package com.example.wochat_bmob.Holder_Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wochat_bmob.Listener.OnRecyclerViewListener;
import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseViewHolder;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import cn.bmob.newim.bean.BmobIMMessage;

/**
 * Created by 邹永鹏 on 2018/6/4.
 */

public class AgreeHolder extends BaseViewHolder implements View.OnClickListener,View.OnLongClickListener{

    @BindView(R.id.chat_time)
    TextView chatTime;

    @BindView(R.id.agree_message)
    TextView agreeMessage;

    public AgreeHolder(Context context, ViewGroup parent, OnRecyclerViewListener listener){
        super(context,parent,R.layout.chat_agree,listener);
    }

    @Override
    public void bindData(Object o) {
        final BmobIMMessage message = (BmobIMMessage)o;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = dateFormat.format(message.getCreateTime());
        chatTime.setText(time);
        agreeMessage.setText(message.getContent());
    }

    public void showTime(boolean isShow) {
        chatTime.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}
