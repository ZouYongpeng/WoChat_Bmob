package com.example.wochat_bmob.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.activity.AddFriendActivity;
import com.example.wochat_bmob.activity.MainActivity;
import com.example.wochat_bmob.activity.UserInfoActivity;
import com.example.wochat_bmob.base.BaseFragment;
import com.example.wochat_bmob.bean.Friend;
import com.example.wochat_bmob.bean.MainMessage;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.bean.UserInfo;
import com.example.wochat_bmob.db.NewFriend;
import com.example.wochat_bmob.events.RefreshEvent;
import com.example.wochat_bmob.tools.UserTool;
import com.github.promeg.pinyinhelper.Pinyin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by 邹永鹏 on 2018/5/13.
 */

public class mainFriendsFragment extends BaseFragment {

    private static final String TAG="mainFriendsFragment";

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView friendsRecyclerView;

    private FriendAdapter friendsAdapter;

    private List<Friend> mFriends=new ArrayList<>();

    /*创建和配置视图*/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_friends,null);
        mSwipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.friend_swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent)); // 进度动画颜色
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateFriends();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        friendsRecyclerView=(RecyclerView) view.findViewById(R.id.main_fragment_friend);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        friendsRecyclerView.setAdapter(friendsAdapter);
        updateFriends();
        return view;
    }

    /*定义ViewHolder内部类*/
    private class FriendsHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Friend mFriend;
        private User mUser;

        private ImageView friendHead;
        private TextView friendName;

        public FriendsHolder(LayoutInflater inflater,ViewGroup parent){
            super(inflater.inflate(R.layout.friends_list,parent,false));
            friendHead=(ImageView)itemView.findViewById(R.id.friend_head);
            friendName=(TextView)itemView.findViewById(R.id.friend_name);

            itemView.setOnClickListener(this);
        }

        private void bind(Friend friend){
            mFriend=friend;
            mUser=mFriend.getFriendUser();
            log("bind--"+friend.getFriendUser().getUsername());
            friendName.setText(friend.getFriendUser().getUsername());
        }

        /*点击事件*/
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.friend_head:

                    break;
                default:
                    Bundle bundle=new Bundle();
                    bundle.putString("name",mUser.getUsername());
                    bundle.putString("way","chat");
                    Intent intent=new Intent(getActivity(),UserInfoActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
            }
        }
    }

    private class FriendAdapter extends RecyclerView.Adapter<FriendsHolder>{

        public FriendAdapter(List<Friend> friends){
            mFriends=friends;
            for (int i=0;i<mFriends.size();i++){
                Friend friend=mFriends.get(i);
            }
        }

        public void setFriends(List<Friend> friends){
            mFriends.clear();
            mFriends=friends;
        }

        public FriendsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
            return new FriendsHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(FriendsHolder holder, int position) {
            Friend friend=mFriends.get(position);
            holder.bind(friend);

        }

        @Override
        public int getItemCount() {
            return mFriends.size();
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
        updateFriends();
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

    /*注册自定义消息接收事件*/
    @Subscribe
    public void onEventMainThread(RefreshEvent event){
        updateFriends();
    }

    private void updateFriends(){
        /*获取好友列表*/
        UserTool.getUserTool().queryFriends(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if (e==null){
                    List<Friend> friends=new ArrayList<>();
                    /*添加首字母*/
                    for (int i = 0; i < list.size(); i++) {
                        Friend friend = list.get(i);
                        String username = friend.getFriendUser().getUsername();

                        if (username != null) {
                            String pinyin = Pinyin.toPinyin(username.charAt(0));
                            friend.setPinyin(pinyin.substring(0, 1).toUpperCase());
                            friends.add(friend);
                        }
                    }
                    if (friends!=null){
                        friendsAdapter=new FriendAdapter(friends);
//                    friendsAdapter.setFriends(friends);
//                    friendsAdapter.notifyDataSetChanged();
                        friendsRecyclerView.setAdapter(friendsAdapter);
                    }

                }
            }
        });
    }
}
