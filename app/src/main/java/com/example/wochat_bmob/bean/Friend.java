package com.example.wochat_bmob.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 邹永鹏 on 2018/5/29.
 */

public class Friend extends BmobObject {
    private User user;
    private User friendUser;

    private transient String pinyin;

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriendUser() {
        return friendUser;
    }

    public void setFriendUser(User friendUser) {
        this.friendUser = friendUser;
    }
}
