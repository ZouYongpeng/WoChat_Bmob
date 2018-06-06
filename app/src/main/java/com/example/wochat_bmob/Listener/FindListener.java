package com.example.wochat_bmob.Listener;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BmobCallback2;

/**
 * Created by 邹永鹏 on 2018/6/3.
 */

public abstract class FindListener<T> extends BmobCallback2<List<T>, BmobException> {

    public FindListener() {
    }

    public abstract void done(List<T> var1, BmobException var2);
}