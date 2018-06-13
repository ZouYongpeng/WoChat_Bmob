package com.example.wochat_bmob.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.activity.AddFriendActivity;
import com.example.wochat_bmob.activity.ChatActivity;
import com.example.wochat_bmob.activity.UserInfoActivity;
import com.example.wochat_bmob.base.BaseFragment;
import com.example.wochat_bmob.bean.MainMessage;
import com.example.wochat_bmob.bean.NewFriendMainMessage;
import com.example.wochat_bmob.bean.PrivateMainMessage;
import com.example.wochat_bmob.bean.MainMessage;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.db.NewFriend;
import com.example.wochat_bmob.db.NewFriendManager;
import com.example.wochat_bmob.events.RefreshEvent;
import com.example.wochat_bmob.tools.TimeUtil;
import com.example.wochat_bmob.tools.ToastTool;
import com.example.wochat_bmob.tools.UserTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by 邹永鹏 on 2018/5/13.
 */

public class mainMessageFragment extends BaseFragment{

    private static final String TAG="mainMessageFragment";

    @BindView(R.id.message_swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.main_fragment_message)
    RecyclerView messagesRecyclerView;

    private MessageAdapter messageAdapter;

    private List<MainMessage> mMainMessages;

    private Unbinder mUnbinder;

    /*注意：不能在这里声明控件*/

    /*创建和配置视图*/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_message,container,false);
        ButterKnife.bind(this,view);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent)); // 进度动画颜色
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateUI();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        /*对视图与fragment进行关联*/
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMainMessages=new ArrayList<>();
        /*更新UI*/
        updateUI();
        addItemTouch();
        return view;
    }

    private void updateUI(){
        messageAdapter=new MessageAdapter(getConversations());
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void addItemTouch(){
        //为RecycleView绑定触摸事件
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //首先回调的方法 返回int表示是否监听该方向
                int dragFlags = ItemTouchHelper.UP|ItemTouchHelper.DOWN;//拖拽
//                int swipeFlags = ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;//侧滑删除
                int swipeFlags = ItemTouchHelper.RIGHT;//侧滑删除
                return makeMovementFlags(dragFlags,swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //滑动事件
                Collections.swap(mMainMessages,viewHolder.getAdapterPosition(),target.getAdapterPosition());
                messageAdapter.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //侧滑事件
                /*向右滑动时，direction=8
                * 向右滑动时，direction=4
                * Log.d(TAG, "onSwiped: "+direction);*/
                Log.d(TAG, "onSwiped: "+direction);
                mMainMessages.remove(viewHolder.getAdapterPosition());
                messageAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //是否可拖拽
                return true;
            }
        });
        helper.attachToRecyclerView(messagesRecyclerView);
    }

    /*定义ViewHolder内部类*/
    private class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private MainMessage mMainMessage;

        private ImageView messageFriendHead;
        private TextView messageFriendName;
        private TextView unreadCount;
//        private ImageView moreMessage;
        private TextView messageContent;
        private TextView messageTime;

        public MessageHolder(LayoutInflater inflater,ViewGroup parent){
            super(inflater.inflate(R.layout.messages_list,parent,false));
            /*实例化相关组件*/
            messageFriendHead=(ImageView) itemView.findViewById(R.id.message_friend_head);
            messageFriendName=(TextView)itemView.findViewById(R.id.message_friend_name);
            unreadCount=(TextView)itemView.findViewById(R.id.message_unread_count);
//            moreMessage=(ImageView) itemView.findViewById(R.id.message_more);
            messageContent=(TextView)itemView.findViewById(R.id.message_content);
            messageTime=(TextView)itemView.findViewById(R.id.message_time);

            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
            messageFriendHead.setOnClickListener(this);
//            moreMessage.setOnClickListener(this);

        }

        /*每次当有一个新的消息要在MessageHolder显示时调用*/
        public void bind(MainMessage message){
            mMainMessage=message;
            Log.d("showHolder",message.getcName()+" "+message.getLastMessageContent()+" "+TimeUtil.getChatTime(false,message.getLastMessageTime()));//+" "+message.getLastMessageTime()
            messageFriendName.setText(message.getcName());
            if (message.getcName().equals("好友请求")){
                messageFriendHead.setImageResource(R.drawable.ic_add_friend_request);
            }
            messageContent.setText(message.getLastMessageContent());
            messageTime.setText(TimeUtil.getChatTime(false,message.getLastMessageTime()));
            long unread= message.getUnReadCount();
            if (unread>0){
                unreadCount.setVisibility(View.VISIBLE);
                unreadCount.setText(String.valueOf(unread));
            }else {
                unreadCount.setVisibility(View.GONE);
            }

        }

        /*点击事件*/
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.message_friend_head:
//                    ToastTool.show(getActivity(),"查看 "+mMainMessage.getcName()+" 的个人资料");
                    Bundle bundle=new Bundle();
                    bundle.putString("name",mMainMessage.getcName());
                    bundle.putString("way","chat");
                    Intent intent=new Intent(getActivity(),UserInfoActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                default:
//                    ToastTool.show(getActivity(),"点击item:"+mMainMessage.getcName());
                    if (messageFriendName.getText().toString().equals("好友请求")){
                        startActivity(AddFriendActivity.class,null);
                    }
                    /*进入聊天界面*/
                    else {
                        /*根据name查询user*/
                        BmobQuery<User> query = new BmobQuery<User>();
                        query.addWhereEqualTo("username", mMainMessage.getcName());
                        query.findObjects(new FindListener<User>() {
                            @Override
                            public void done(List<User> list,BmobException e) {
                                if(e==null){
                                    BmobIMUserInfo info=new BmobIMUserInfo();
                                    info.setUserId(list.get(0).getObjectId());
                                    info.setName(mMainMessage.getcName());
                                    info.setAvatar("");
                                    Bundle bundle=new Bundle();
                                    bundle.putSerializable("info",info);
                                    Intent intent=new Intent(getActivity(),ChatActivity.class);
                                    intent.putExtras(bundle);
                                    getActivity().startActivity(intent);
                                }else{
                                    toast("更新用户信息失败:" + e.getMessage());
                                }
                            }
                        });
                    }
                    break;
            }
        }

        /*长按选择删除功能*/
//        @Override
//        public boolean onLongClick(View v) {
//            ToastTool.show(getActivity(),"删除");
//            return true;
//        }
    }

    /*创建Adaptern内部类*/
    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

        public MessageAdapter(List<MainMessage> messages){
            mMainMessages=messages;
        }

        /*需要新的ViewHolder时候会调用，利用LayoutInflater创建MessageHolder*/
        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
            return new MessageHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(MessageHolder holder, int position) {
            MainMessage message=mMainMessages.get(position);
            holder.bind(message);
        }

        @Override
        public int getItemCount() {
            return mMainMessages.size();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /*获取会话列表的数据：增加新朋友会话*/
    private List<MainMessage> getConversations(){
        //添加会话
        List<MainMessage> conversationList = new ArrayList<>();
        conversationList.clear();
        //查询全部会话
        List<BmobIMConversation> list = BmobIM.getInstance().loadAllConversation();
        if(list!=null && list.size()>0){
            for (BmobIMConversation item:list){
                switch (item.getConversationType()){
                    case 1://私聊
                        conversationList.add(new PrivateMainMessage(item));
                        break;
                    default:
                        break;
                }
            }
        }
        //添加新朋友会话-获取好友请求表中最新一条记录
        List<NewFriend> friends = NewFriendManager.getInstance(getActivity()).getAllNewFriend();
        if(friends!=null && friends.size()>0){
            conversationList.add(new NewFriendMainMessage(friends.get(0)));
        }
        //重新排序
        Collections.sort(conversationList);
        return conversationList;
    }

    /**注册自定义消息接收事件
     * @param event
     */
    @Subscribe
    public void onEventMainThread(RefreshEvent event){
        Log.d("mainMessageFragment","---会话页接收到自定义消息---");
        updateUI();
    }

    /*注册离线消息接收事件*/
    @Subscribe
    public void onEventMainThread(OfflineMessageEvent event){
        //重新刷新列表
        updateUI();
    }

    /**注册消息接收事件
     * @param event
     * 1、与用户相关的由开发者自己维护，SDK内部只存储用户信息
     * 2、开发者获取到信息后，可调用SDK内部提供的方法更新会话
     */
    @Subscribe
    public void onEventMainThread(MessageEvent event){
        //重新获取本地消息并刷新列表
        updateUI();
    }

}
