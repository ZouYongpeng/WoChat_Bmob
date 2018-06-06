package com.example.wochat_bmob.activity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.base.WoChatContext;
import com.example.wochat_bmob.bean.Person;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.bean.UserInfo;
import com.example.wochat_bmob.events.FinishEvent;
import com.example.wochat_bmob.tools.LoginTool;
import com.example.wochat_bmob.tools.ToastTool;
import com.example.wochat_bmob.tools.UserTool;
import com.example.wochat_bmob.ui.ClearEditText;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.register_toolbar)
    protected Toolbar mRegisterToolbar;

    @BindView(R.id.toolbarTitle)
    protected TextView mRegisterToolbarTitle;

    @BindView(R.id.register_user_name)
    protected ClearEditText mRegisterName;

    @BindView(R.id.register_user_password)
    protected ClearEditText mRegisterPass;

    @BindView(R.id.register_user_password_again)
    protected ClearEditText mRegisterPassAgain;

    @BindView(R.id.register_button)
    protected Button mRegisterButton;

    @BindView(R.id.goto_login)
    protected TextView mGotoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setSupportActionBar(mRegisterToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRegisterToolbarTitle.setText(R.string.register);
        mRegisterToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /*注册按钮点击事件
    * 1、检测用户名是否符合规范：3-16的字母+数字
    * 2、检查密码是否符合规范：6-18的字母和数字
    * 3、检查两次输入的密码是否一致
    * 4、用户名及密码符合规范时执行注册事件*/
    @OnClick(R.id.register_button)
    public void ClickRegister(){
        closeInput();
        String registerName=mRegisterName.getText().toString();
        String registerPass=mRegisterPass.getText().toString();
        String registerPassAgain=mRegisterPassAgain.getText().toString();
        String registerAvatar=null;

        if (registerName.equals("")||registerPass.equals("")||registerPassAgain.equals("")){
            ToastTool.show(this,getString(R.string.error_register_input));
            openInput();
            return;
        }

        /*使用Pattern类，编译正则表达式后创建一个匹配模式.
        * name:^[a-zA-Z0-9]{3,16}$   长度为3-16的字母+数字
        * pass:^[a-zA-Z0-9]{6,18}$   长度为6-16的字母+数字*/
        Pattern namePattern = Pattern.compile(getString(R.string.pattern_register_name));
        Pattern passPattern = Pattern.compile(getString(R.string.pattern_register_password));

        /*matches() 最常用方法:尝试对整个目标字符展开匹配检测,也就是只有整个目标字符串完全匹配时才返回真值*/
        if(!namePattern.matcher(registerName).matches()){
            ToastTool.show(this,getString(R.string.error_register_name));
            mRegisterName.setText("");
            openInput();
            return;
        }
        if(!passPattern.matcher(registerPass).matches()){
            ToastTool.show(this,getString(R.string.error_register_password));
            mRegisterPass.setText("");
            mRegisterPassAgain.setText("");
            openInput();
            return;
        }
        if (!registerPass.equals(registerPassAgain)){
            ToastTool.show(this,getString(R.string.error_register_password_again));
            mRegisterPassAgain.setText("");
            openInput();
            return;
        }

        register(registerName,registerPass,"");
    }

    public void register(final String name, final String pass, final String avatar){
        log("register: start!");
        if (!name.isEmpty()&& !pass.isEmpty()){
            BmobUser user=new BmobUser();
            user.setUsername(name);
            user.setPassword(pass);
            user.signUp(new SaveListener<User>() {
                @Override
                public void done(User user, BmobException e) {
                    if (e==null){
                        log("done: UserTool register success");
//                        toast(getString(R.string.register_success));
                        registerSuccess(user.getObjectId(),name,pass,avatar);
                    }else {//}else if (e.getErrorCode()!=9015){
                        log("done: UserTool register failure"+e.toString());
                        toast("该用户已存在或服务器连接失败");
                        openInput();
                    }
                }
            });
        }
    }

    /*注册成功后
    * 将用户id、用户名、和用户头像储存到UserInfo表
    * 跳转至登录界面*/
    public void registerSuccess(final String id,final String name,String pass, final String avatar){
        Log.d("register", "registerSuccess");
        UserInfo userInfo=new UserInfo();
        userInfo.setId(id);
        userInfo.setName(name);
        userInfo.setAvatar(avatar);
        userInfo.setAge(0);
        userInfo.setSex("保密");
        userInfo.setPhone("未填写电话号码");
        userInfo.setMail("未填写邮箱");
        userInfo.setLocal("未知");
        userInfo.setIntroduction("这个家伙很懒，什么都不写");
        userInfo.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    log("创建数据成功");
                    e=null;
                }else{
                    log("失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });


        /*注册时储存该用户的objectId和头像*/
        LoginTool.setObjectID(name,id);
        LoginTool.setAvatar(name,avatar);

//        Intent intent=new Intent();
//        intent.putExtra("registerName",mRegisterName.getText().toString());
//        setResult(RESULT_OK,intent);
//        finish();
        UserTool.getUserTool().login(name, pass, new LogInListener() {
            @Override
            public void done(Object o, BmobException e) {
                if (e==null){
                    //登录成功
                    startActivity(MainActivity.class,null,true);
                }else {
                    startActivity(LoginActivity.class,null,true);
                }
            }
        });
    }

    /*关闭输入*/
    public void closeInput(){
        mRegisterName.setEnabled(false);
        mRegisterPass.setEnabled(false);
        mRegisterPassAgain.setEnabled(false);
        mRegisterButton.setEnabled(false);
        mRegisterButton.setText(getString(R.string.registering));
    }

    /*开启输入*/
    public void openInput(){
        mRegisterName.setEnabled(true);
        mRegisterPass.setEnabled(true);
        mRegisterPassAgain.setEnabled(true);
        mRegisterButton.setEnabled(true);
        mRegisterButton.setText(getString(R.string.register));
    }
}
