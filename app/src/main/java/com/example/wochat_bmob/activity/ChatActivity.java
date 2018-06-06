package com.example.wochat_bmob.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wochat_bmob.Holder_Adapter.ChatAdapter;
import com.example.wochat_bmob.Listener.OnRecyclerViewListener;
import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.bean.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.BmobRecordManager;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageListHandler;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;

public class ChatActivity extends BaseActivity implements MessageListHandler{

    @BindView(R.id.chat_linearLayout)
    LinearLayout mChatLinear;

    @BindView(R.id.chat_toolbar)
    Toolbar mChatToolbar;

    @BindView(R.id.toolbarTitle)
    TextView mChatToolbarTitle;

    @BindView(R.id.chat_swipe_refresh)
    SwipeRefreshLayout mChatSwipeRefresh;

    @BindView(R.id.chat_recyclerView)
    RecyclerView mChatRecyclerView;

    @BindView(R.id.chat_message)
    EditText mMessageEdit;

    @BindView(R.id.chat_send)
    Button mChatButton;

    private User user;

    BmobIMUserInfo info=new BmobIMUserInfo();

    String FriendName;

    ChatAdapter mChatAdapter;
    LinearLayoutManager mLayoutManager;

    BmobRecordManager mBmobRecordManager;
    BmobIMConversation mBmobIMConversation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /*当前用户*/
        user= BmobUser.getCurrentUser(User.class);
        /*当前对话对象*/
        info=(BmobIMUserInfo)getIntent().getSerializableExtra("info");
        BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
        //TODO 消息：5.1、根据会话入口获取消息管理，聊天页面
        mBmobIMConversation = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        /*初始化标题栏*/
        initToolbar();
        /*初始化下拉刷新*/
        initSwipeReflash();
        /*初始化下方输入框控件*/
        initButtom();
    }

    private void initToolbar(){
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mChatToolbarTitle.setText(info.getName());
        mChatToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initSwipeReflash(){
        /*下拉加载*/
        mChatSwipeRefresh.setEnabled(true);
        mChatSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BmobIMMessage message=mChatAdapter.getFirstMessage();

            }
        });
        mLayoutManager=new LinearLayoutManager(this);
        mChatRecyclerView.setLayoutManager(mLayoutManager);

        mChatAdapter=new ChatAdapter(this,mBmobIMConversation);
        mChatRecyclerView.setAdapter(mChatAdapter);

        mChatLinear.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mChatLinear.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mChatSwipeRefresh.setRefreshing(true);
                //自动刷新
                queryMessages(null);
            }
        });
        /*下拉加载*/
        mChatSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BmobIMMessage msg = mChatAdapter.getFirstMessage();
                queryMessages(msg);
            }
        });

        mChatAdapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {

            }

            @Override
            public boolean onItemLongClick(int position) {
                /*长按要删除*/
                log("长按删除功能没写");
                return true;
            }
        });
    }

    private void initButtom(){
        /*写输入框监听事件,开始输入时，将信息滚动至底部*/
        mMessageEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                    scrollToBottom();
                }
                return false;
            }
        });

        /*当开始输入时,*/
        mMessageEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick(R.id.chat_send)
    public void onSendClick(View view){
        String content=mMessageEdit.getText().toString();
        if (TextUtils.isEmpty(content)){
            toast("不能发送空白消息");
            return;
        }
        sendMessage(content);
    }

    private void sendMessage(String content){
        //TODO 发送消息：6.1、发送文本消息
        BmobIMTextMessage msg = new BmobIMTextMessage();
        msg.setContent(content);
        //可随意设置额外信息
        Map<String, Object> map = new HashMap<>();
        map.put("level", "1");
        msg.setExtraMap(map);
        mBmobIMConversation.sendMessage(msg, listener);
    }

    /*消息发送监听器，收到消息时，将滚动至底部*/
    public MessageSendListener listener=new MessageSendListener() {
        @Override
        public void onStart(BmobIMMessage message) {
            super.onStart(message);
            mChatAdapter.addMessage(message);
            mMessageEdit.setText("");
            scrollToBottom();
        }

        @Override
        public void done(BmobIMMessage bmobIMMessage, BmobException e) {
            mChatAdapter.notifyDataSetChanged();
            mMessageEdit.setText("");
            scrollToBottom();
            if (e!=null){
                toast(e.getMessage());
            }
        }
    };

    //TODO 消息接收：8.2、单个页面的自定义接收器
    @Override
    public void onMessageReceive(List<MessageEvent> list) {
        log("聊天页面接收到消息：" + list.size());
        //当注册页面消息监听时候，有消息（包含离线消息）到来时会回调该方法
        for (int i = 0; i < list.size(); i++) {
            addMessageIntoChat(list.get(i));
        }
    }

    /*接收到消息是，将消息添加至界面*/
    private void addMessageIntoChat(MessageEvent event){
        BmobIMMessage message=event.getMessage();
        if (mBmobIMConversation!=null
                && event!=null
                && !message.isTransient() //消息不为暂存状态
                && mBmobIMConversation.getConversationId()
                .equals(event.getConversation().getConversationId())){//如果是当前对话
            if (mChatAdapter.findPosition(message)<0){//未添加至界面
                mChatAdapter.addMessage(message);
                //更新该会话下面的已读状态
                mBmobIMConversation.updateReceiveStatus(message);
            }
            scrollToBottom();
        }
    }

    /* 首次加载，可设置msg为null
     * 下拉刷新的时候，默认取消息表的第一个msg作为刷新的起始时间点，默认按照消息时间的降序排列
     */
    public void queryMessages(BmobIMMessage msg) {
        //TODO 消息：5.2、查询指定会话的消息记录
        mBmobIMConversation.queryMessages(msg, 10, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> list, BmobException e) {
                mChatSwipeRefresh.setRefreshing(false);
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        mChatAdapter.addMessages(list);
                        mLayoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                    }
                } else {
                    toast(e.getMessage() + "(" + e.getErrorCode() + ")");
                }
            }
        });
    }

    private void scrollToBottom() {
        mLayoutManager.scrollToPositionWithOffset(mChatAdapter.getItemCount() - 1, 0);
    }

    @Override
    protected void onResume() {
        //锁屏期间的收到的未读消息需要添加到聊天界面中
        addUnReadMessage();
        //添加页面消息监听器
        BmobIM.getInstance().addMessageListHandler(this);
        // 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知
        BmobNotificationManager.getInstance(this).cancelNotification();
        super.onResume();
    }

    /**
     * 添加未读的通知栏消息到聊天界面
     */
    private void addUnReadMessage() {
        List<MessageEvent> cache = BmobNotificationManager.getInstance(this).getNotificationCacheList();
        if (cache.size() > 0) {
            int size = cache.size();
            for (int i = 0; i < size; i++) {
                MessageEvent event = cache.get(i);
                addMessageIntoChat(event);
            }
        }
        scrollToBottom();
    }

    @Override
    protected void onPause() {
        //移除页面消息监听器
        BmobIM.getInstance().removeMessageListHandler(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //清理资源
//        if (recordManager != null) {
//            recordManager.clear();
//        }
//        //TODO 消息：5.4、更新此会话的所有消息为已读状态
        if (mBmobIMConversation != null) {
            mBmobIMConversation.updateLocalCache();
        }
        super.onDestroy();
    }

}