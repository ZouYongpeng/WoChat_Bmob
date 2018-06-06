package com.example.wochat_bmob.bean;

import com.example.wochat_bmob.db.NewFriend;

import cn.bmob.v3.BmobUser;

/**
 * Created by 邹永鹏 on 2018/5/4.
 * 专门的用户类——BmobUser来自动处理用户账户管理所需的功能
 * BmobUser除了从BmobObject继承的属性外，还有几个特定的属性：
 * username: 用户的用户名（必需）。
 * password: 用户的密码（必需）。
 * email: 用户的电子邮件地址（可选）。
 * emailVerified:邮箱认证状态（可选）。
 * mobilePhoneNumber：手机号码（可选）。
 * mobilePhoneNumberVerified：手机号码的认证状态（可选）。
 */

public class User extends BmobUser {

    private String avatar;

    public User(){}

    public User(NewFriend friend){
        setObjectId(friend.getUid());
        setUsername(friend.getName());
        setAvatar(friend.getAvatar());
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
