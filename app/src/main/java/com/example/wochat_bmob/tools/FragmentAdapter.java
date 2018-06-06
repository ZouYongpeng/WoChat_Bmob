package com.example.wochat_bmob.tools;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by 邹永鹏 on 2018/5/13.
 * fragment适配器，管理主界面的消息和好友fragment
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    List<Fragment> mFragmentList;

    public FragmentAdapter(FragmentManager fm, List<Fragment> list){
        super(fm);
        mFragmentList=list;
    }

    /*根据Item的位置返回对应位置的Fragment，绑定item和Fragment*/
    @Override
    public Fragment getItem(int i){
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
