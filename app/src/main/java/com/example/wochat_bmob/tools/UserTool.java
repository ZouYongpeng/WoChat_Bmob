package com.example.wochat_bmob.tools;


import android.text.TextUtils;
import android.util.Log;

import com.example.wochat_bmob.Listener.QueryUserListener;
import com.example.wochat_bmob.Listener.UpdateCacheListener;
import com.example.wochat_bmob.activity.RegisterActivity;
import com.example.wochat_bmob.bean.Friend;
import com.example.wochat_bmob.bean.User;


import java.util.List;
import java.util.logging.Logger;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by 邹永鹏 on 2018/5/16.
 * 单例工具类
 */

public class UserTool{

    /*volatile关键字来保证可见性、有序性，但没办法保证对变量的操作的原子性
    * 1、它会强制修改的值会立即被更新到主存，当有其他线程需要读取时，它会去内存中读取新值
    *    即新值对其他线程来说是立即可见的
    * 2、禁止进行指令重排序。*/
    private static volatile UserTool sUserTool;

    /*获取sUserTool实例
    * synchronized修饰一个代码块：
    * 当两个并发线程(thread1和thread2)访问同一个对象(syncThread)中的synchronized代码块时，在同一时刻只能有一个线程得到执行，
    * 另一个线程受阻塞，必须等待当前线程执行完这个代码块以后才能执行该代码块。
    * 参考资料：https://blog.csdn.net/luoweifu/article/details/46613015*/
    public synchronized static UserTool getUserTool(){
        if (sUserTool==null){
            sUserTool=new UserTool();
        }
        return sUserTool;
    }

    private UserTool(){

    }

    /*获取当前用户*/
    public User getCurrentUser(){
        return BmobUser.getCurrentUser(User.class);
    }

    /*登录*/
    public void login(String username, String password, final LogInListener listener) {
        final User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.login(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    listener.done(getCurrentUser(), null);
                } else {
                    listener.done(user, e);
                }
            }
        });
    }

    /*注册*/
//    public void register(String name,String pass,String avatar,final LogInListener listener){
//        if (!name.isEmpty()&& !pass.isEmpty()){
//            User user=new User();
//            user.setUsername(name);
//            user.setPassword(pass);
//            user.setAvatar(avatar);
//            user.signUp(new SaveListener<User>() {
//                @Override
//                public void done(User user, BmobException e) {
//                    if (e==null){
//                        Log.d("register", "done: UserTool register success");
//                        listener.done(null, null);
//                    }else {
//                        Log.d("register", "done: UserTool register failure");
//                        listener.done(null,e);
//                    }
//                }
//            });
//        }
//    }

    /*根据用户名模糊查询用户*/
    public void queryUsers(String username, final int limit, final FindListener<User> listener) {
        BmobQuery<User> query = new BmobQuery<>();
        //去掉当前用户
        try {
            BmobUser user = BmobUser.getCurrentUser();
            query.addWhereNotEqualTo("username", user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        query.addWhereContains("username", username);
        query.setLimit(limit);
        query.order("-createdAt");
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        listener.done(list, e);
                    } else {
                        listener.done(list, new BmobException(1000, "查无此人"));
                    }
                } else {
                    listener.done(list, e);
                }
            }
        });
    }

    /*注册后根据用户名获取该用户ID*/
    public void getObjectID(final String name,final LogInListener listener){
        BmobQuery<User> query=new BmobQuery<User>();
        //查询username叫“name”的数据
        query.addWhereEqualTo("username",name);
        //返回1条数据，如果不加上这条语句，默认最多返回10条数据
        query.setLimit(1);
        //执行查询方法
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e==null){
                    for (User user : object) {
                        //获得数据的objectId信息并返回
                        listener.done(null,new BmobException(user.getObjectId()));
                        Log.d("register","getObjectID-"+name+" is"+user.getObjectId());
                    }
                }else{
                    Log.i("register","getObjectID-"+name+"失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    /*当注册或登录成功并且成功连接到IM服务器后，
        需要更新下当前用户的信息到本地数据库的用户表中，
        这样才能通过getUserInfo方法获取到本地的用户信息。*/
    public void saveLocalUserInfo(String id,final String name, final String avatar){
        BmobIMUserInfo info=new BmobIMUserInfo();
        info.setUserId(id);
        info.setName(name);
        info.setAvatar(avatar);
        BmobIM.getInstance().updateUserInfo(info);
    }

    public void saveLocalUserInfo(final String name) {
        BmobIMUserInfo info = new BmobIMUserInfo();
//        info.setUserId(id);
        info.setName(name);
//        info.setAvatar(avatar);
        BmobIM.getInstance().updateUserInfo(info);
    }


    /*查询指定用户信息*/
    public void queryUserInfo(String objectId, final QueryUserListener listener) {
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId", objectId);
        query.findObjects(
                new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        if (e == null) {
                            if (list != null && list.size() > 0) {
                                listener.done(list.get(0), null);
                            } else {
                                listener.done(null, new BmobException(000, "查无此人"));
                            }
                        } else {
                            listener.done(null, e);
                        }
                    }
                });
    }

    public void updateUserInfo(MessageEvent event, final UpdateCacheListener listener) {
        final BmobIMConversation conversation = event.getConversation();
        final BmobIMUserInfo info = event.getFromUserInfo();
        final BmobIMMessage msg = event.getMessage();
        String username = info.getName();
        String avatar = info.getAvatar();
        String title = conversation.getConversationTitle();
        String icon = conversation.getConversationIcon();
        //SDK内部将新会话的会话标题用objectId表示，因此需要比对用户名和私聊会话标题，后续会根据会话类型进行判断
//        if (!username.equals(title) || !avatar.equals(icon)) {
        UserTool.getUserTool().queryUserInfo(info.getUserId(), new QueryUserListener() {
            @Override
            public void done(User s, BmobException e) {
                if (e == null) {
                    String name = s.getUsername();
                    String avatar = s.getAvatar();
                    conversation.setConversationIcon(avatar);
                    conversation.setConversationTitle(name);
                    info.setName(name);
                    info.setAvatar(avatar);
                    //TODO 用户管理：2.7、更新用户资料，用于在会话页面、聊天页面以及个人信息页面显示
                    BmobIM.getInstance().updateUserInfo(info);
                    //TODO 会话：4.7、更新会话资料-如果消息是暂态消息，则不更新会话资料
                    if (!msg.isTransient()) {
                        BmobIM.getInstance().updateConversation(conversation);
                    }
                } else {
                    Log.d("",e.getMessage());
                }
                listener.done(null);
            }
        });
//        } else {
//            listener.done(null);
//        }
    }

    /*添加好友*/
    public void agreeAddFriend(User friend, SaveListener<String> listener) {
        Friend f = new Friend();
        User user = BmobUser.getCurrentUser(User.class);
        f.setUser(user);
        f.setFriendUser(friend);
        f.save(listener);
    }

    /*查询好友*/
    public void queryFriends(final FindListener<Friend> listener) {
        BmobQuery<Friend> query = new BmobQuery<>();
        User user = BmobUser.getCurrentUser(User.class);
        query.addWhereEqualTo("user", user);
        query.include("friendUser");
        query.order("-updatedAt");
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        listener.done(list, e);
                    } else {
                        listener.done(list, new BmobException(0, "暂无联系人"));
                    }
                } else {
                    listener.done(list, e);
                }
            }
        });
    }
}
