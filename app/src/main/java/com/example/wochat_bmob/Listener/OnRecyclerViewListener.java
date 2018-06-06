package com.example.wochat_bmob.Listener;

/**
 * Created by 邹永鹏 on 2018/6/4.
 * RecycleView添加点击事件的回调接口
 */

public interface OnRecyclerViewListener {

    void onItemClick(int position);

    boolean onItemLongClick(int position);
}
