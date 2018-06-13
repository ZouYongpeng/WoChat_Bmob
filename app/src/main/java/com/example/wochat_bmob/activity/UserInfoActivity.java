
package com.example.wochat_bmob.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.bean.Friend;
import com.example.wochat_bmob.db.NewFriend;
import com.example.wochat_bmob.messages.AddFriendMessage;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.bean.UserInfo;
import com.example.wochat_bmob.tools.ToastTool;
import com.example.wochat_bmob.tools.UserTool;
import com.example.wochat_bmob.ui.ClearEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoActivity extends BaseActivity {

    @BindView(R.id.userinfo_toolbar)
    Toolbar mUserInfoToolbar;

    @BindView(R.id.toolbarTitle)
    TextView mToolbarTextView;

    @BindView(R.id.data_head)
    CircleImageView head;

    @BindView(R.id.data_name)
    TextView name;

    @BindView(R.id.data_sex)
    TextView sex;

    @BindView(R.id.data_age)
    TextView age;

    @BindView(R.id.data_local)
    TextView local;

    @BindView(R.id.data_phone)
    TextView phone;

    @BindView(R.id.data_mail)
    TextView mail;

    @BindView(R.id.data_about)
    TextView about;

    @BindView(R.id.no_add)
    Button back;

    @BindView(R.id.add)
    Button add;

    @BindView(R.id.delete_friend)
    Button deleteFriend;

    @BindView(R.id.start_chat)
    Button startChat;

    @BindView(R.id.call_btn)
    ImageButton callPhone;

    @BindView(R.id.sendEmail_btn)
    ImageButton sendEmail;

    private UserInfo mUserInfo;
    BmobIMUserInfo info;

    ProgressDialog mDialog;

    protected String way;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        Bundle bundle= getIntent().getExtras();
        String name=bundle.getString("name");
        /*判断way
        * way==chat 即已经是好友，显示对话图标
        *      add, 还不是好友，显示添加好友
        *      agree 还不是好友，显示同意添加*/
        way=bundle.getString("way");
        log("bundle.getString--"+way);

        mDialog=new ProgressDialog(this);
        mDialog.setMessage("正在加载"+name+"的资料");
        mDialog.show();

        BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
        query.addWhereEqualTo("name",name);
        //返回1条数据，如果不加上这条语句，默认返回10条数据
        query.setLimit(1);
        query.findObjects(new FindListener<UserInfo>() {
            @Override
            public void done(List<UserInfo> object, BmobException e) {
                if(e==null){
                    log("查询成功：共"+object.size()+"条数据。");
                    mUserInfo=object.get(0);
                    log(mUserInfo.toString());
                    /*初始化用户资料*/
                    initUserInfo();
                    /*初始化标题栏*/
                    initToolbar();
                    //构造聊天方的用户信息:传入用户id、用户名和用户头像三个参数
                    info = new BmobIMUserInfo(mUserInfo.getId(), mUserInfo.getName(), mUserInfo.getAvatar());
                    mToolbarTextView.setText(info.getName()+"的资料");
                }else{
                    log("失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    private void initToolbar(){
        setSupportActionBar(mUserInfoToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTextView.setText("资料");//mUserInfo.getName()
        mUserInfoToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initUserInfo(){
        /*还要加上头像*/
        name.setText(mUserInfo.getName());
        age.setText("年龄："+mUserInfo.getAge());
        sex.setText("性别："+mUserInfo.getSex());
        local.setText(mUserInfo.getLocal());
        phone.setText("个人电话："+mUserInfo.getPhone());
        mail.setText("个人邮箱："+mUserInfo.getMail());
        about.setText(mUserInfo.getIntroduction());
        /*判断是否为好友显示电话和邮箱按钮*/
        switch (way){
            case "chat":
                back.setVisibility(View.GONE);
                add.setVisibility(View.GONE);
                deleteFriend.setVisibility(View.VISIBLE);
                startChat.setVisibility(View.VISIBLE);
                callPhone.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.VISIBLE);
                break;
            case "add":
                /*还没添加为好友*/
                deleteFriend.setVisibility(View.GONE);
                startChat.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
                add.setVisibility(View.VISIBLE);
                break;
            case "agree":
                /*还没添加为好友*/
                deleteFriend.setVisibility(View.GONE);
                startChat.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
                add.setText("同意添加");
                add.setVisibility(View.VISIBLE);
                break;
            default:
                back.setVisibility(View.GONE);
                add.setVisibility(View.GONE);
                deleteFriend.setVisibility(View.GONE);
                startChat.setVisibility(View.GONE);
                callPhone.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
                break;
        }
        mDialog.dismiss();
    }

    @OnClick(R.id.call_btn)
    public void call(){
        /*电话号码匹配*/
        Pattern phonePattern = Pattern.compile("(\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$");
        if (phonePattern.matcher(mUserInfo.getPhone()).matches()){
            Intent intent=new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+mUserInfo.getPhone()));
            startActivity(intent);
        }else{
            toast("该号码无效");
            return;
        }

    }

    @OnClick(R.id.sendEmail_btn)
    public void sendEmail(){
        toast("邮箱功能尚未完成");
    }

    @OnClick(R.id.delete_friend)
    public void deleteFriend(){
        toast("还不能删除好友");
    }

    @OnClick(R.id.start_chat)
    public void startChat(){
        BmobIMUserInfo info=new BmobIMUserInfo();
        info.setUserId(mUserInfo.getId());
        info.setName(mUserInfo.getName());
        info.setAvatar("");
        Bundle bundle=new Bundle();
        bundle.putSerializable("info",info);
        Intent intent=new Intent(UserInfoActivity.this,ChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.no_add)
    public void back(){
        finish();
    }

    @OnClick(R.id.add)
    public void addFriend(){
        if (way.equals("add")){
            openDialog(info);
        }
        if (way.equals("agree")){
            toast("这里还不可以同意添加，你可以返回上一个界面完成操作");
        }
    }

    private void openDialog(final BmobIMUserInfo info){
        View view=getLayoutInflater().inflate(R.layout.dialog_add_friend,null);
        final ClearEditText editText=(ClearEditText) view.findViewById(R.id.dialog_text);
        AlertDialog dialog=new AlertDialog.Builder(UserInfoActivity.this)
                .setIcon(R.drawable.ic_add_user)
                .setTitle("交友宣言")
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content=editText.getText().toString();
                        if (TextUtils.isEmpty(content)){
                            content="很高兴认识你，可以加个好友吗?";
                        }
                        sendAddFriendMessage(info,content);
                    }
                }).create();
        dialog.show();
    }

    private void sendAddFriendMessage(BmobIMUserInfo info,String context) {//
        User currentUser = BmobUser.getCurrentUser(User.class);
        //创建一个暂态会话入口，发送好友请求
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
        //根据会话入口获取消息管理，发送好友请求
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        AddFriendMessage msg = new AddFriendMessage();
//        msg.setContent(context);//给对方的一个留言信息
        msg.setContent(context);//给对方的一个留言信息
        Map<String, Object> map = new HashMap<>();
        map.put("name", currentUser.getUsername());//发送者姓名
        map.put("avatar", currentUser.getAvatar());//发送者的头像
        map.put("uid", currentUser.getObjectId());//发送者的uid
        msg.setExtraMap(map);
        messageManager.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                if (e == null) {//发送成功
                    ToastTool.show(UserInfoActivity.this,"好友请求发送成功，等待验证");
                    Log.d("sendAddFriendMessage","好友请求发送成功，等待验证");
                } else {//发送失败
                    ToastTool.show(UserInfoActivity.this,"发送失败:" + e.getMessage());
                    Log.d("sendAddFriendMessage","发送失败:" + e.getMessage());
                }
            }
        });
    }

}
