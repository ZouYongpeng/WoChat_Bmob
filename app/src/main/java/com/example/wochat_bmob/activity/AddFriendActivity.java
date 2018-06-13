package com.example.wochat_bmob.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.base.Config;
import com.example.wochat_bmob.base.WoChatApplication;
import com.example.wochat_bmob.bean.Friend;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.bean.UserInfo;
import com.example.wochat_bmob.db.NewFriend;
import com.example.wochat_bmob.db.NewFriendManager;
import com.example.wochat_bmob.messages.AddFriendMessage;
import com.example.wochat_bmob.messages.AgreeAddFriendMessage;
import com.example.wochat_bmob.tools.LoginTool;
import com.example.wochat_bmob.tools.ToastTool;
import com.example.wochat_bmob.tools.UserTool;
import com.example.wochat_bmob.ui.ClearEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

public class AddFriendActivity extends BaseActivity{

    @BindView(R.id.add_friend_toolbar)
    protected Toolbar mAddFriendsToolbar;

    @BindView(R.id.toolbarTitle)
    protected TextView mAddFriendsToolbarTitle;

    @BindView(R.id.search_edit)
    ClearEditText mSearchEdit;

    @BindView(R.id.search_button)
    ImageButton mSearchButton;

    @BindView(R.id.search_recycler_view)
    RecyclerView mSearchRecyclerView;

    @BindView(R.id.add_friend_wsipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    ProgressDialog mDialog;

    private SearchAdapter mSearchAdapter;
    private NewFriendAdapter mNewFriendAdapter;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        user=BmobUser.getCurrentUser(User.class);

        mDialog=new ProgressDialog(this);
        mDialog.setMessage("搜索中......");

        /*下拉刷新*/
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent)); // 进度动画颜色
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSearchEdit.setText("");
                mNewFriendAdapter=new NewFriendAdapter(getAllNewFriend());
                mSearchRecyclerView.setAdapter(mNewFriendAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        /*初始化标题栏*/
        initToolbar();
        mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNewFriendAdapter=new NewFriendAdapter(getAllNewFriend());
        mSearchRecyclerView.setAdapter(mNewFriendAdapter);
    }

    private void initToolbar(){
        setSupportActionBar(mAddFriendsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAddFriendsToolbarTitle.setText("添加好友");
        mAddFriendsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @OnClick(R.id.search_button)
    public void startSearch(){
        final String username = mSearchEdit.getText().toString();
        if (username.isEmpty()) {
            ToastTool.show(this,getString(R.string.search_edit_error));
            return;
        }
        mDialog.show();
        search(username);
    }

    public void search(final String name){
        final List<UserInfo> userInfos=new ArrayList<>();
        log("search "+name);
        BmobQuery<UserInfo> query=new BmobQuery<UserInfo>();
        query.addQueryKeys("name");
        query.findObjects(new FindListener<UserInfo>() {
            @Override
            public void done(List<UserInfo> list, BmobException e) {
                if (e==null){
                    log("list.size()="+list.size());
                    for (int i=0;i<list.size();i++){
                        if (list.get(i).getName().toLowerCase().contains(name.toLowerCase())
                                &&!list.get(i).getName().equals(BmobUser.getCurrentUser().getUsername())){
                            log(list.get(i).getObjectId()+list.get(i).getName()+list.get(i).getId());
                            UserInfo userInfo=new UserInfo();
                            userInfo.setId(list.get(i).getObjectId());
                            userInfo.setName(list.get(i).getName());
                            userInfos.add(userInfo);
                        }
                    }
                    mSearchAdapter=new SearchAdapter(userInfos);
                    mSearchRecyclerView.setAdapter(mSearchAdapter);
                    mDialog.dismiss();
                }else {
                    log("error in search!");
                }
            }
        });
    }

    private class SearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private UserInfo mUserInfo;
        /*mUserInfo里面只含有objectId和name，所以还要在UserInfoActivity查询数据*/
        private BmobIMUserInfo info;

        private ImageView friendHead;
        private TextView friendName;
        private Button addButton;

        public SearchHolder(LayoutInflater inflater,ViewGroup parent){
            super(inflater.inflate(R.layout.search_friends_list,parent,false));
            friendHead=(ImageView)itemView.findViewById(R.id.search_friend_head);
            friendName=(TextView)itemView.findViewById(R.id.search_friend_name);
            addButton=(Button)itemView.findViewById(R.id.search_add);

            itemView.setOnClickListener(this);
            friendHead.setOnClickListener(this);
            addButton.setOnClickListener(this);
        }

        /*每次当有一个新的消息要在MessageHolder显示时调用*/
        public void bind(final UserInfo userInfo){
            mUserInfo=userInfo;
            friendName.setText(mUserInfo.getName());
            /*判断是否已经添加过*/
            UserTool.getUserTool().queryFriends(new FindListener<Friend>() {
                @Override
                public void done(List<Friend> list, BmobException e) {
                    if (e==null){
                        boolean hasAdd=false;
                        List<Friend> friends=new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            Friend friend = list.get(i);
                            String friendID=friend.getFriendUser().getObjectId();
                            String friendName=friend.getFriendUser().getUsername();
                            if (mUserInfo.getName().equals(friendName)) {
                                hasAdd=true;
                                break;
                            }
                        }
                        if (hasAdd){
                            addButton.setText("已添加");
                            addButton.setEnabled(false);
                        }
                        else {
                            if (LoginTool.getSendAddFriendStatus(user.getUsername(),mUserInfo.getName())){
                                addButton.setText("等待对方同意");
                                addButton.setEnabled(false);
                            }else {
                                addButton.setText("添加");
                                addButton.setEnabled(true);
                            }
                        }
                    }
                }
            });

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.search_add:
                    searchUserInfo(mUserInfo.getId());
                    break;
                default:
                    Log.d("sendAddFriendMessage",mUserInfo.getName()+" "+mUserInfo.getId());
                    Bundle bundle=new Bundle();
                    bundle.putString("name",mUserInfo.getName());
                    bundle.putString("way","add");
                    Intent intent=new Intent(AddFriendActivity.this,UserInfoActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
            }
        }

        /*根据userInfo的objectID查询用户消息*/
        public void searchUserInfo(String objectID){
            BmobQuery<UserInfo> query = new BmobQuery<UserInfo>();
            query.getObject(objectID, new QueryListener<UserInfo>() {
                @Override
                public void done(UserInfo object, BmobException e) {
                    if(e==null){
                        info = new BmobIMUserInfo(object.getId(), object.getName(), object.getAvatar());
                        log(object.getObjectId()+" "+object.getName()+" "+object.getId()+object.getIntroduction());
                        log(info.getUserId()+" "+info.getName()+" "+info.getAvatar());
                        openDialog(info);
                    }else{
                        log("失败："+e.getMessage()+","+e.getErrorCode());
                    }
                }
            });
        }

        /*打开对话框*/
        private void openDialog(final BmobIMUserInfo info){
            View view=getLayoutInflater().inflate(R.layout.dialog_add_friend,null);
            final ClearEditText editText=(ClearEditText) view.findViewById(R.id.dialog_text);
            AlertDialog dialog=new AlertDialog.Builder(AddFriendActivity.this)
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
                            addButton.setText("等待对方同意");
                            addButton.setEnabled(false);
                            String content=editText.getText().toString();
                            if (TextUtils.isEmpty(content)){
                                content="很高兴认识你，可以加个好友吗?";
                            }
                            sendAddFriendMessage(info,content);
                        }
                    }).create();
            dialog.show();
        }
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchHolder>{
        private List<UserInfo> searchFriendsList;

        public SearchAdapter(List<UserInfo> userInfos){
            searchFriendsList=userInfos;
        }

        /*需要新的ViewHolder时候会调用，利用LayoutInflater创建MSearchHolder*/
        @Override
        public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater=LayoutInflater.from(AddFriendActivity.this);
            return new SearchHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(SearchHolder holder, int position) {
            UserInfo userInfo=searchFriendsList.get(position);
            log(userInfo.getId()+" "+userInfo.getName()+" "+userInfo.getAvatar());
            holder.bind(userInfo);
        }

        @Override
        public int getItemCount() {
            return searchFriendsList.size();
        }
    }

    public class NewFriendHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private NewFriend newFriend;

        private ImageView requestFriendHead;
        private TextView requestFriendName;
        private TextView requestContent;
        private Button agreeButton;

        public NewFriendHolder(LayoutInflater inflater,ViewGroup parent){
            super(inflater.inflate(R.layout.friends_request_list,parent,false));
            requestFriendHead=(ImageView)itemView.findViewById(R.id.friend_request_head);
            requestFriendName=(TextView)itemView.findViewById(R.id.friend_request_name);
            requestContent=(TextView)itemView.findViewById(R.id.friend_request_content);
            agreeButton=(Button)itemView.findViewById(R.id.friend_request_agree);

            itemView.setOnClickListener(this);
            agreeButton.setOnClickListener(this);
        }

        public void bind(NewFriend friend){
            newFriend=friend;
            requestFriendName.setText(newFriend.getName());
            requestContent.setText(newFriend.getMsg());
            agreeButton.setText("同意");
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.friend_request_agree:
                    log("同意添加");
                    agreeAdd(newFriend, new SaveListener<Object>() {
                        @Override
                        public void done(Object o, BmobException e) {
                            if (e == null) {
                                agreeButton.setText("已添加");
                                agreeButton.setEnabled(false);
                            } else {
                                log("添加好友失败:" + e.getMessage());
                                toast("添加好友失败:" + e.getMessage());
                            }
                        }
                    });
                    break;
                default:
                    log("查看信息");
                    Bundle bundle=new Bundle();
                    bundle.putString("name",newFriend.getName());
                    bundle.putString("way","agree");
                    Intent intent=new Intent(AddFriendActivity.this,UserInfoActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
            }
        }

    }

    public class NewFriendAdapter extends RecyclerView.Adapter<NewFriendHolder>{
        private List<NewFriend> newFriends=new ArrayList<>();

        public NewFriendAdapter(List<NewFriend> friends){
            for (int i=0;i<friends.size();i++){
                if (friends.get(i).getStatus()!=Config.STATUS_VERIFIED){
                    newFriends.add(friends.get(i));
                    log(friends.get(i).getName()+" "+friends.get(i).getMsg());
                }
            }
        }
        @Override
        public NewFriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater=LayoutInflater.from(AddFriendActivity.this);
            return new NewFriendHolder(layoutInflater,parent);
        }
        @Override
        public void onBindViewHolder(NewFriendHolder holder, int position) {
            NewFriend newFriend=newFriends.get(position);
            log("onBindViewHolder:"+newFriend.getName()+" "+newFriend.getMsg());
            //当状态是未添加或者是已读未添加
            Integer status = newFriend.getStatus();
            if (status == null || status == Config.STATUS_VERIFY_NONE || status == Config.STATUS_VERIFY_READED) {
                holder.bind(newFriend);
            }
        }

        @Override
        public int getItemCount() {
            return newFriends.size();
        }
    }



    private void sendAddFriendMessage(final BmobIMUserInfo info, String context) {
        User currentUser = BmobUser.getCurrentUser(User.class);
        //创建一个暂态会话入口，发送好友请求
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
        //根据会话入口获取消息管理，发送好友请求
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        AddFriendMessage msg = new AddFriendMessage();
        msg.setContent(context);//给对方的一个留言信息
        //这里只是举个例子，其实可以不需要传发送者的信息过去
        Map<String, Object> map = new HashMap<>();
        map.put("name", currentUser.getUsername());//发送者姓名
        map.put("avatar", currentUser.getAvatar());//发送者的头像
        map.put("uid", currentUser.getObjectId());//发送者的uid
        msg.setExtraMap(map);
        messageManager.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                if (e == null) {//发送成功
                    toast("好友请求发送成功，等待验证");
                    log("好友请求发送成功，等待验证");
                    /*将添加好友状态改为true*/
                    LoginTool.setSendAddFriendStatus(user.getUsername(),info.getName(),true);

                } else {//发送失败
                    toast("发送失败:" + e.getMessage());
                    log("发送失败:" + e.getMessage());
                }
            }
        });
    }

    private List<NewFriend> getAllNewFriend(){
        List<NewFriend> newFriends=NewFriendManager.getInstance(this).getAllNewFriend();
        return newFriends;
    }

    /*添加到好友表中再发送同意添加好友的消息*/
    private void agreeAdd(final NewFriend add, final SaveListener<Object> listener) {
        User user = new User();
        user.setObjectId(add.getUid());
        UserTool.getUserTool()
                .agreeAddFriend(user, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            //TODO 2、发送同意添加好友的消息
                            sendAgreeAddFriendMessage(add, listener);
                        } else {
                            log(e.getMessage());
                            listener.done(null, e);
                        }
                    }
                });
    }

    /*发送同意添加好友的消息*/
    //TODO 好友管理：9.8、发送同意添加好友
    private void sendAgreeAddFriendMessage(final NewFriend add, final SaveListener<Object> listener) {
        BmobIMUserInfo info = new BmobIMUserInfo(add.getUid(), add.getName(), add.getAvatar());
        //TODO 会话：4.1、创建一个暂态会话入口，发送同意好友请求
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, true, null);
        //TODO 消息：5.1、根据会话入口获取消息管理，发送同意好友请求
        BmobIMConversation messageManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        //而AgreeAddFriendMessage的isTransient设置为false，表明我希望在对方的会话数据库中保存该类型的消息
        AgreeAddFriendMessage msg = new AgreeAddFriendMessage();
        final User currentUser = BmobUser.getCurrentUser(User.class);
        msg.setContent("我通过了你的好友验证请求，我们可以开始聊天了!");//这句话是直接存储到对方的消息表中的
        Map<String, Object> map = new HashMap<>();
        map.put("msg", currentUser.getUsername() + "同意添加你为好友");//显示在通知栏上面的内容
        map.put("uid", add.getUid());//发送者的uid-方便请求添加的发送方找到该条添加好友的请求
        map.put("time", add.getTime());//添加好友的请求时间
        msg.setExtraMap(map);
        messageManager.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage msg, BmobException e) {
                if (e == null) {//发送成功
                    //TODO 3、修改本地的好友请求记录
                    NewFriendManager.getInstance(WoChatApplication.getContext()).updateNewFriend(add, Config.STATUS_VERIFIED);
                    listener.done(msg, e);
                } else {//发送失败
                    log(e.getMessage());
                    listener.done(msg, e);
                }
            }
        });
    }
}
