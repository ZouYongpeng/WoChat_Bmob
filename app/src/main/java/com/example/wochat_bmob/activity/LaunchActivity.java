package com.example.wochat_bmob.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.tools.UserTool;

import butterknife.BindView;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;



public class LaunchActivity extends BaseActivity {

    @BindView(R.id.launch_bg)
    ImageView launchImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
//        Bmob.initialize(this, "257f546c5a44cb4c1878fad6fb621a30");
        Glide.with(this)
                .load(R.drawable.launch_bg)
                .into(launchImage);
        Handler handler=new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                User user=UserTool.getUserTool().getCurrentUser();
                if (user==null){
                    /*不存在正在使用的用户，则跳至登录界面*/
                    startActivity(LoginActivity.class,null,true);
                }else {
                    startActivity(MainActivity.class,null,true);
                }
            }
        },1000);
    }
}
