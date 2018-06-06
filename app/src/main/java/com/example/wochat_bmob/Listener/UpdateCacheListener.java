package com.example.wochat_bmob.Listener;

import cn.bmob.newim.listener.BmobListener1;
import cn.bmob.v3.exception.BmobException;

/**
 * Created by 邹永鹏 on 2018/5/29.
 */

public abstract class UpdateCacheListener extends BmobListener1 {

    public abstract void done(BmobException e);

    @Override
    protected void postDone(Object o, BmobException e) {
        done(e);
    }

}