package com.example.wochat_bmob.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.example.wochat_bmob.Listener.OnRecyclerViewListener;
import com.example.wochat_bmob.base.BaseActivity;

import butterknife.ButterKnife;
import cn.bmob.newim.bean.BmobIMConversation;

/**
 * Created by 邹永鹏 on 2018/6/4.
 */

public abstract class BaseViewHolder<T>
        extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

    OnRecyclerViewListener mOnRecyclerViewListener;
    protected Context mContext;

    public BaseViewHolder(Context context, ViewGroup parent,
                          int layoutResId, OnRecyclerViewListener listener){
        super(LayoutInflater.from(context).inflate(layoutResId,parent,false));
        this.mContext=context;
        ButterKnife.bind(this,itemView);
        this.mOnRecyclerViewListener=listener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public Context getContext(){
//        return mContext;
        return itemView.getContext();
    }

    public abstract void bindData(T t);

    private Toast mToast;
    public void toast(final Object obj){
        try {
            ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mToast==null){
                        mToast=Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
                    }
                    if (obj!=null){
                        mToast.setText(obj.toString());
                    }
                    mToast.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnRecyclerViewListener!=null){
            mOnRecyclerViewListener.onItemClick(getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnRecyclerViewListener!=null){
            mOnRecyclerViewListener.onItemLongClick(getAdapterPosition());
        }
        return true;
    }

    /*启动指定activity*/
    public void startActivity(Class<? extends Activity> target, Bundle bundle){
        Intent intent=new Intent();
        intent.setClass(getContext(),target);
        if (bundle!=null){
            intent.putExtra(getContext().getPackageName(),bundle);
        }
        getContext().startActivity(intent);
    }
}
