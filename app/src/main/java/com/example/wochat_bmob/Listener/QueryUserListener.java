package com.example.wochat_bmob.Listener;

import com.example.wochat_bmob.bean.User;

import cn.bmob.newim.listener.BmobListener1;
import cn.bmob.v3.exception.BmobException;

/**
 * Created by 邹永鹏 on 2018/5/29.
 */

public abstract class QueryUserListener extends BmobListener1<User> {

    public abstract void done(User s, BmobException e);

    @Override
    protected void postDone(User o, BmobException e) {
        done(o, e);
    }

}
