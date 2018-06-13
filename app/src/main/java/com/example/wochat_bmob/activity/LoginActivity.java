package com.example.wochat_bmob.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.tools.LoginTool;
import com.example.wochat_bmob.tools.ToastTool;
import com.example.wochat_bmob.tools.UserTool;
import com.example.wochat_bmob.ui.ClearEditText;

import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends BaseActivity {

    /*用户名*/
    @BindView(R.id.login_user_name)
    protected ClearEditText mEditUserName;

    /*用户密码*/
    @BindView(R.id.login_user_password)
    protected ClearEditText mEditUserPass;

    /*是否记住密码*/
    @BindView(R.id.remember_password)
    protected AppCompatCheckBox mRememberPass;

    /*是否自动登录*/
    @BindView(R.id.auto_login)
    protected AppCompatCheckBox mAutoLogin;

    /*登录按钮*/
    @BindView(R.id.login_button)
    protected Button mLoginButton;

    /*注册按钮*/
    @BindView(R.id.goto_register)
    protected TextView mToRegister;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*获取上次登录的用户名*/
        String formerLogin= LoginTool.getFormerLogin();
        mEditUserName.setText(formerLogin);

        dialog = new ProgressDialog(this);
        dialog.setMessage("登录中......");
        initUserInfo(formerLogin);
        changeEditText();//输入框监听事件
    }

    /*初始化界面时
    * 1、先在用户名输入栏显示上次登录成功的用户名
    *   如果用户名不为空，则通过SharedPreferencesTool查看上次登录是否记住密码
    *   如果记住密码，那么就通过tool获取上次登录的用户（包括name和password）
    *   将“记住密码”状态设置为上次记住密码状态
    * 2、通过LoginTool的getObjectID查看该用户是否登录过或者注册过
    *   如果有就说明能获取头像
    *   紧接着在内部表BmobIMUserInfo获取头像*/
    private void initUserInfo(String userName){
        if (!userName.isEmpty()){//formerLogin!=null && !formerLogin.equals("")
            Boolean isRember=LoginTool.isRememberPass(userName);
            if (isRember){
//                ToastTool.show(this,"上个用户记住密码啦");
                mRememberPass.setChecked(true);
                mAutoLogin.setChecked(LoginTool.isAutoLogin(userName));
                //获取数据库中的密码
                mEditUserPass.setText(LoginTool.getPass(userName));
                if (LoginTool.isAutoLogin(userName) && !mEditUserPass.getText().toString().isEmpty()){
//                    ToastTool.show(this,"自动登录");
                    mLoginButton.performClick();
                }
            }
            /*获取头像*/
            String avatar=LoginTool.getAvatar(userName);
            if (avatar.isEmpty()||avatar.equals("")){
                    /*该用户采用默认头像*/
//                ToastTool.show(this,"该用户采用默认头像");
            }else {
//                ToastTool.show(this,"该用户采用自己的头像");
            }

        }
    }

    /*用户名输入框监听器，当用户名改变时，密码框内密码清空*/
    private void changeEditText(){
        mEditUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mEditUserPass.setText("");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                initUserInfo(mEditUserName.getText().toString());
            }
        });
    }

    /**
     * 登陆响应
     * 1、判断用户名和密码是否为空
     * 2、改变登录按钮外观
     * 3、使用RxJava进行异步操作
     *      1、创建Observable时，用just原样发射用户名和密码
     *      2、设置在io线程激活或产生该提交登录事件
     *      3、设置在主线程运行事件
     *      4、使用flatMap进行变换，在内部调用login()实现登录操作，并返回loginResult登录结果
     *
     */
    @OnClick(R.id.login_button)
    public void userLogin(View view){
        //判断用户和密码是否为空
        final String userName=mEditUserName.getText().toString();
        final String userPass=mEditUserPass.getText().toString();
        if (userName.isEmpty() || userName.length()==0){
            ToastTool.show(this,getString(R.string.login_null_name));
            return;
        }
        if (userPass.isEmpty() || userPass.length()==0){
            ToastTool.show(this,getString(R.string.login_null_pass));
            return;
        }
        //改变登录按钮外观
        mLoginButton.setEnabled(false);//禁用登录按钮
        mLoginButton.setText(getString(R.string.logining));
        dialog.show();

        UserTool.getUserTool().login(userName, userPass, new LogInListener() {
            @Override
            public void done(Object o, BmobException e) {
                if (e==null){
                    //登录成功
                    log("登录成功");
                    loginSuccess(userName,userPass);
                }else {
                    log(e.getMessage() + "(" + e.getErrorCode() + ")");
                    toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                    mLoginButton.setEnabled(true);//启用登录按钮
                    mLoginButton.setText(getString(R.string.login));
                    mEditUserPass.setText("");
                }
            }
        });
    }

    /*登录成功
    * 一、如果选择记住密码
    *     1、那么将利用litepal存储用户名和密码
    *     2、利用sharedpreferences的boolean存储记住密码状态
    *     3、利用sharedpreferences的int存储自动登录状态
    * 二、更新下当前用户的信息到本地数据库的用户表中，这样才能通过getUserInfo方法获取到本地的用户信息。*/
    private void loginSuccess(String userName,String userPass){
        LoginTool.setFormerLogin(userName);
        LoginTool.setRememberPass(userName,mRememberPass.isChecked());//保存记住密码状态
        if (mRememberPass.isChecked()){
            /*储存该用户是否自动注册*/
            LoginTool.setAutoLogin(userName,mAutoLogin.isChecked());
            LoginTool.setPass(userName,userPass);
        }
        dialog.dismiss();
        //跳转至主界面
        startActivity(MainActivity.class, null, true);
    }

    @OnClick(R.id.goto_register)
    public void gotoRegister(){
        startActivity(RegisterActivity.class, null, false);
    }

}
