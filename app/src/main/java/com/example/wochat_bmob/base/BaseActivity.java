package com.example.wochat_bmob.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wochat_bmob.activity.UserInfoActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 邹永鹏 on 2018/5/24.
 */

public class BaseActivity extends AppCompatActivity {

    /*SDK内部使用EventBus来进行应用内消息的分发，故在应用内需要接收消息的地方注册和解注册EventBus即可*/
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ButterKnife.bind(this);
        initView();
    }

    @Subscribe
    public void onEvent(Boolean empty){

    }

    protected void initView(){

    }

    private Toast mToast;
    public void toast(final Object object){
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mToast==null){
                        mToast=Toast.makeText(BaseActivity.this,"",Toast.LENGTH_SHORT);
                    }
                    mToast.setText(object.toString());
                    mToast.show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void startActivity(Class<? extends Activity> target, Bundle bundle,boolean finished){
        Intent intent=new Intent(this,target);
        if (bundle!=null){
            intent.putExtra(getPackageName(),bundle);
        }
        startActivity(intent);
        if (finished){
            finish();
        }
    }

    protected Bundle getBundle(){
        if (getIntent()!=null && getIntent().hasExtra(getPackageName())){
            return getIntent().getBundleExtra(getPackageName());
        }else {
            return null;
        }
    }

    public void log(String msg){
        Log.d(getLocalClassName()+"_zyp",msg);
    }

    @Override
    public void onBackPressed() {
        if(doubleExitAppEnable()) {//判断是否需要双击退出
            exitAppDoubleClick();
        } else {
            super.onBackPressed();
        }
    }

    public boolean doubleExitAppEnable() {
        return false;
    }

    /*双击退出函数变量*/
    private long exitTime = 0;
    /*双击退出APP*/
    private void exitAppDoubleClick() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            toast("再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            exitApp();
        }
    }

    /**
     * 退出APP
     */
    private void exitApp() {
        onDestroy();
        super.onBackPressed();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
