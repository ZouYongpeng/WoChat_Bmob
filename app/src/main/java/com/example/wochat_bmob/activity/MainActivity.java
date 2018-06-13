package com.example.wochat_bmob.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wochat_bmob.R;
import com.example.wochat_bmob.base.BaseActivity;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.db.NewFriendManager;
import com.example.wochat_bmob.events.RefreshEvent;
import com.example.wochat_bmob.fragment.mainFriendsFragment;
import com.example.wochat_bmob.fragment.mainMessageFragment;
import com.example.wochat_bmob.service.WoChatService;
import com.example.wochat_bmob.tools.FragmentAdapter;
import com.example.wochat_bmob.tools.LoginTool;
import com.example.wochat_bmob.tools.ToastTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.newim.listener.ConnectStatusChangeListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener
        ,ViewPager.OnPageChangeListener, TabHost.OnTabChangeListener {

    @BindView(R.id.main_layout)
    protected DrawerLayout mainLayout;

    @BindView(R.id.main_toolbar)
    protected Toolbar mainToolbar;

    @BindView(R.id.toolbarTitle)
    protected TextView mainToolbarTitle;

    @BindView(R.id.nav_view)
    protected NavigationView mainNavView;

    @BindView(R.id.main_viewPage)
    protected ViewPager mainViewPage;

    @BindView(R.id.main_fragmentTabHost)
    protected FragmentTabHost tabHost;

    /*NavigationView滑动菜单内控件*/
    private View headerLayout;
    private CircleImageView navUserHead;
    private TextView navUserName;

    /*底部菜单栏——Fragment+FragmentTabHost++ViewPager
    * 参考资料：https://www.jianshu.com/p/a663803b2a44*/
    private LayoutInflater mLayoutInflater;
    private Class fragmentArray[]={mainMessageFragment.class, mainFriendsFragment.class};
    private int tabImageViewArray[]={R.drawable.ic_main_message,R.drawable.ic_main_friends};
    private int tabSelectedImageViewArray[]={R.drawable.ic_main_message_selected,R.drawable.ic_main_friends_selected};
    private int tabTextViewArray[]={R.string.fragment_message_tab,R.string.fragment_friends_tab};

    private List<Fragment> mFragmentList=new ArrayList<Fragment>();

    private User user;
    private BmobIMUserInfo info;

    private Dialog mAboutDialog;

    private IntentFilter mIntentFilter;
    private NetworkChangeReceiver mNetworkChangeReceiver;

    private WoChatService.showForegroundBinder mForegroundBinder;

    private ServiceConnection foregroundCon=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mForegroundBinder=(WoChatService.showForegroundBinder) service;
            mForegroundBinder.show(user.getUsername());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = BmobUser.getCurrentUser(User.class);
        log("当前用户："+user.getObjectId()+" "+user.getUsername()+" "+user.getAvatar());
        info=new BmobIMUserInfo(user.getObjectId(),user.getUsername(),user.getAvatar());
        initNav();//初始化NavigationView滑动菜单
        initToolBar();//初始化标题栏
        initViewPage();//初始化ViewPage
        initFragment();//初始化碎片
        initConnection();//初始化连接
        openWoChatService();
        openNetworkChangeReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //每次进来应用都检查会话和好友请求的情况
        checkUnRead();
        //进入应用后，通知栏应取消
        BmobNotificationManager.getInstance(this).cancelNotification();
    }

    /*登录成功、注册成功或处于登录状态重新打开应用后执行连接IM服务器的操作*/
    private void initConnection(){
        if (!TextUtils.isEmpty(user.getObjectId())) {
            BmobIM.connect(user.getObjectId(), new ConnectListener() {
                @Override
                public void done(String uid, BmobException e) {
                    if (e == null) {
                        //连接成功
                        toast("连接成功");
                        /*连接成功后再进行修改本地用户信息的操作，并查询本地用户信息
                        * 服务器连接成功就发送一个更新事件，同步更新会话及主页的小红点*/
                        EventBus.getDefault().post(new RefreshEvent());
                        /*更新用户资料，用于在会话页面、聊天页面以及个人信息页面显示*/
                        BmobIM.getInstance().updateUserInfo(info);

                    } else {
                        //连接失败
                        toast(e.getMessage());
                        log(e.getMessage());
                    }
                }
            });
            /*监听连接状态，可通过BmobIM.getInstance().getCurrentStatus()来获取当前的长连接状态*/
            BmobIM.getInstance().setOnConnectStatusChangeListener(new ConnectStatusChangeListener() {
                @Override
                public void onChange(ConnectionStatus status) {
                    toast(status.getMsg());
                    log(BmobIM.getInstance().getCurrentStatus().getMsg());
                }
            });
        }
    }

    /*聊天消息接收事件
    * 通知有在线消息接收*/
    @Subscribe
    public void onEventMainThread(MessageEvent event){
        //处理聊天消息
        log("处理聊天消息");
        checkUnRead();
    }

    /*离线消息接收事件
    * 通知有离线消息接收*/
    @Subscribe
    public void onEventMainThread(OfflineMessageEvent event){
        //处理离线消息
        log("处理离线消息");
        checkUnRead();
    }

    /*自定义消息接收事件
    * 通知有自定义消息接受*/
    @Subscribe
    public void onEventMainThread(RefreshEvent event) {
        //处理自定义消息
        log("处理自定义消息");
        checkUnRead();
    }

    /*处理消息*/
    private void checkUnRead() {
        //获取全部会话的未读消息数量
        int unReadCount = (int) BmobIM.getInstance().getAllUnReadCount();
        if (unReadCount > 0) {
//            toast("你有"+unReadCount+"条未读消息");
            log("你有"+unReadCount+"条未读消息");
        } else {
//            toast("你没有未读消息");
            log("你没有未读消息");
        }
        //TODO 好友管理：是否有好友添加的请求
        if (NewFriendManager.getInstance(this).hasNewFriendInvitation()) {
//            toast("有用户向你发起请求");
            log("有用户向你发起请求");
        } else {
//            toast("没有用户向你发起请求");
            log("没有用户向你发起请求");
        }
    }

    /*初始化NavigationView滑动菜单*/
    private void initNav(){
        /*NavigationView中获取headerLayout的方法:
        * 1、获取headerLayout
        * 2、获取其中的组件
        * https://blog.csdn.net/qq_15907463/article/details/52352561*/
        //获取headerLayout
        headerLayout = mainNavView.inflateHeaderView(R.layout.nav_header);
        //获取其中的组件
        navUserHead=(CircleImageView)headerLayout.findViewById(R.id.user_head);
        navUserName=(TextView)headerLayout.findViewById(R.id.user_name);
        navUserName.setText(user.getUsername());

        /*加载当前用户头像
        * 1、获取url*/
//        currentUserHeadUrl="http://192.168.155.1:8080/new_head.jpg";
        Glide.with(this)
                .load(R.drawable.login_user_head)
//                .error(R.drawable.login_user_head)
                .into(navUserHead);

        mainNavView.setNavigationItemSelectedListener(this);

        mAboutDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.edition,null);
        Window window = mAboutDialog.getWindow();
        window.setContentView(view);

        WindowManager manager = getWindowManager();
        Display display = manager.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = window.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (display.getHeight() * 0.7);
        p.width = (int) (display.getWidth() * 0.9);
        window.setAttributes(p);
        ImageView cancel = view.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAboutDialog.dismiss();
            }
        });
    }

    /*初始化标题栏*/
    private void initToolBar(){
        /*设置标题栏*/
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示ToolBar的标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//显示图标
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_menu);//设置图标
        mainToolbarTitle.setText(R.string.toolbar_title);//设置标题
    }

    /*初始化ViewPage*/
    private void initViewPage(){

        /*实现OnPageChangeListener接口,目的是监听Tab选项卡的变化，然后通知ViewPager适配器切换界面
        * 简单来说,是为了让ViewPager滑动的时候能够带着底部菜单联动*/
        mainViewPage.addOnPageChangeListener(this);//设置页面切换时的监听器
        mLayoutInflater=LayoutInflater.from(this);//加载布局管理器

        /*绑定viewpager*/
        tabHost.setup(this,getSupportFragmentManager(),R.id.main_viewPage);
        /*设置列表框和底部菜单栏的分隔条*/
        tabHost.getTabWidget().setDividerDrawable(new ColorDrawable(Color.TRANSPARENT));
        /*实现setOnTabChangedListener接口,目的是为监听界面切换），然后实现TabHost里面图片文字的选中状态切换
        * 简单来说,是为了当点击下面菜单时,上面的ViewPager能滑动到对应的Fragment*/
        tabHost.setOnTabChangedListener(this);

        /*新建Tabspec选项卡并设置Tab菜单栏的内容和绑定对应的Fragment*/
        int tabCount=tabTextViewArray.length;
        for (int i=0;i<tabCount;i++){
            //给每个Tab按钮设置标签、图标和文字
            TabHost.TabSpec tabSpec=tabHost.newTabSpec(getString(tabTextViewArray[i]))
                    .setIndicator(getTabItemView(i));
            /*将Tab按钮添加进Tab选项卡中，并绑定Fragment*/
            tabHost.addTab(tabSpec,fragmentArray[i],null);
            tabHost.setTag(i);
        }
    }

    /*将xml布局转换为View对象*/
    private View getTabItemView(int i){
        View view =mLayoutInflater.inflate(R.layout.main_tab_content,null);
        /*利用view对象，找到布局中的组件,并设置内容，然后返回视图*/
        ImageView tabImageView=(ImageView)view.findViewById(R.id.main_tab_image);
        /*第一次打开页面时，默认打开第一个底部标签，所以设计点击图标*/
        if (i==0)
            tabImageView.setBackgroundResource(tabSelectedImageViewArray[i]);
        else
            tabImageView.setBackgroundResource(tabImageViewArray[i]);
        TextView tabTextView=(TextView)view.findViewById(R.id.main_tab_textView);
        tabTextView.setText(tabTextViewArray[i]);
        return view;
    }

    private void initFragment(){
        Fragment messageFragment=new mainMessageFragment();
        Fragment friendsFragment=new mainFriendsFragment();

        mFragmentList.add(messageFragment);
        mFragmentList.add(friendsFragment);

        /*绑定适配器*/
        mainViewPage.setAdapter(new FragmentAdapter(getSupportFragmentManager(),mFragmentList));
        tabHost.getTabWidget().setDividerDrawable(null);
    }

    /*arg0 ==1的时候表示正在滑动
    * arg0==2的时候表示滑动完毕了
    * arg0==0的时候表示什么都没做，就是停在那。*/
    @Override
    public void onPageScrollStateChanged(int arg0) {
        switch (arg0){
            case 0:
                log("onPageScrollStateChanged: arg==0");
                break;
            case 1:
                log("onPageScrollStateChanged: arg==1");
                break;
            case 2:
                log("onPageScrollStateChanged: arg==2");
                break;
        }
    }

    /*表示在前一个页面滑动到后一个页面的时候，在前一个页面滑动前调用的方法*/
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    /*arg0是表示你当前选中的页面位置Postion，页面跳转完毕的时候调用。*/
    @Override
    public void onPageSelected(int arg0) {
        TabWidget widget = tabHost.getTabWidget();
        int oldFocusability = widget.getDescendantFocusability();
        widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);//设置View覆盖子类控件而直接获得焦点
        tabHost.setCurrentTab(arg0);//根据位置Postion设置当前的Tab
        widget.setDescendantFocusability(oldFocusability);//设置取消分割线
        /*改变底部标签图片*/
        ImageView nowView=(ImageView) widget.getChildAt(arg0).findViewById(R.id.main_tab_image);
        ImageView oldView=(ImageView) widget.getChildAt(1-arg0).findViewById(R.id.main_tab_image);
        nowView.setBackgroundResource(tabSelectedImageViewArray[arg0]);
        oldView.setBackgroundResource(tabImageViewArray[1-arg0]);
    }

    /*Tab改变的时候调用*/
    @Override
    public void onTabChanged(String tabId) {
        int position = tabHost.getCurrentTab();
        mainToolbarTitle.setText(getString(tabTextViewArray[position]));
        mainViewPage.setCurrentItem(position);//把选中的Tab的位置赋给适配器，让它控制页面切换
    }

    /*创建主界面右上角的菜单*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu,menu);
        return true;
    }

    /*菜单点击事件*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            /*toolbar菜单项*/
            case android.R.id.home:
                mainLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.add_user:
                startActivity(AddFriendActivity.class,null,false);
                break;
            case R.id.create_group:
                toast("createGroup()");
                break;
        }
        return true;
    }

    /*滑动菜单点击事件*/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.personal_data:
                log("mainNavView");
                Bundle bundle=new Bundle();
                bundle.putSerializable("info",info);
                Intent intent=new Intent(MainActivity.this,MyDataActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.setting:
                log("mainNavView");
                toast("设置");
                break;
            case R.id.about:
                mAboutDialog.show();
                break;
            case R.id.help:
                log("mainNavView");
                toast("帮助");
                break;
            case R.id.exit:
                log("mainNavView");
                toast("退出");
                /*退出时，返回登录界面，并将自己的自动登录状态记为false*/
                LoginTool.setAutoLogin(user.getUsername(),false);
                LoginTool.setFormerLogin(user.getUsername());
                BmobUser.logOut();//清除缓存用户对象
                startActivity(LoginActivity.class,null,true);
                break;
        }
        return false;
    }

    private void openNetworkChangeReceiver(){
        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mNetworkChangeReceiver=new NetworkChangeReceiver();
        registerReceiver(mNetworkChangeReceiver,mIntentFilter);
    }

    class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if (networkInfo!=null && networkInfo.isAvailable()){
                toast("网络已连接");
                log("网络已连接");

            }else {
                toast("网络已断开");
                log("网络已断开");
            }
        }
    }

    private void openWoChatService(){
        Intent openWoChatService=new Intent(this, WoChatService.class);
//        startService(openWoChatService);
        bindService(openWoChatService,foregroundCon,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        log("onDestroy()");
        Intent stopWoChatService=new Intent(this, WoChatService.class);
        unbindService(foregroundCon);
        super.onDestroy();
        //清理导致内存泄露的资源
        BmobIM.getInstance().clear();
        unregisterReceiver(mNetworkChangeReceiver);
    }

    @Override
    public boolean doubleExitAppEnable() {
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mainLayout.isDrawerOpen(GravityCompat.START)) {
            mainLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

