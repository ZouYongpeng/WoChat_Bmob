package com.example.wochat_bmob.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 邹永鹏 on 2018/5/16.
 */

public class Person extends BmobObject {
    private String name;
    private String address;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
