package com.example.wochat_bmob.messages;

import android.content.Context;
import android.util.Log;

import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.tools.ToastTool;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMExtraMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by 邹永鹏 on 2018/5/24.
 * 自定义消息接收器,处理服务器发来的消息和离线消息
 */

public class MessageHandler extends BmobIMExtraMessage {

    private Context context;
    public MessageHandler(Context context) {
        this.context = context;
    }

//    @Override
//    public void onMessageReceive(final MessageEvent event) {
//        //接收处理在线消息
//        final BmobIMConversation conversation = event.getConversation();
//        final BmobIMUserInfo info = event.getFromUserInfo();
//        final BmobIMMessage msg = event.getMessage();
//        final String username = info.getName();
//        String avatar = info.getAvatar();
//        String title = conversation.getConversationTitle();
//        String icon = conversation.getConversationIcon();
//        //SDK内部将新会话的会话标题用objectId表示，因此需要比对用户名和私聊会话标题，后续会根据会话类型进行判断
//        if (!username.equals(title) || !avatar.equals(icon)) {
//            BmobQuery<User> query = new BmobQuery<>();
//            query.addWhereEqualTo("objectId", info.getUserId());
//            query.findObjects(
//                    new FindListener<User>() {
//                        @Override
//                        public void done(List<User> list, BmobException e) {
//                            if (e == null) {
//                                if (list != null && list.size() > 0) {
//                                    User user=list.get(0);
//                                    conversation.setConversationIcon(user.getAvatar());
//                                    conversation.setConversationTitle(user.getUsername());
//                                    info.setName(user.getUsername());
//                                    info.setAvatar(user.getAvatar());
//                                    //用户管理：2.7、更新用户资料，用于在会话页面、聊天页面以及个人信息页面显示
//                                    BmobIM.getInstance().updateUserInfo(info);
//                                    //会话：4.7、更新会话资料-如果消息是暂态消息，则不更新会话资料
//                                    if (!msg.isTransient()) {
//                                        BmobIM.getInstance().updateConversation(conversation);
//                                    }
//                                    BmobIMMessage msg = event.getMessage();
//                                    if (BmobIMMessageType.getMessageTypeValue(msg.getMsgType()) == 0) {
//                                        //自定义消息类型：0
//                                        processCustomMessage(msg, event.getFromUserInfo());
//                                    } else {
//                                        //SDK内部内部支持的消息类型
//                                        processSDKMessage(msg, event);
//                                    }
//                                } else {
//                                    Log.d("onMessageReceive","找不到用户");
//                                }
//                            } else {
//                                Log.d("onMessageReceive","发生未知错误");
//                            }
//                        }
//                    });
//        } else {
//            Log.d("onMessageReceive","你接收到的是来自自己的消息");
//        }
//    }
//
//    @Override
//    public void onOfflineReceive(final OfflineMessageEvent event) {
//        //接收处理离线消息，每次调用connect方法时会查询一次离线消息，如果有，此方法会被调用
//        Map<String, List<MessageEvent>> map = event.getEventMap();
//        Log.d("onMessageReceive","有" + map.size() + "个用户发来离线消息");
//        //挨个检测下离线消息所属的用户的信息是否需要更新
//        for (Map.Entry<String, List<MessageEvent>> entry : map.entrySet()) {
//            List<MessageEvent> list = entry.getValue();
//            int size = list.size();
//            Log.d("onMessageReceive","用户" + entry.getKey() + "发来" + size + "条消息");
//            for (int i = 0; i < size; i++) {
//                //处理每条消息
//                executeMessage(list.get(i));
//            }
//        }
//    }
//
//    /*处理消息*/
//    private void executeMessage(final MessageEvent event) {
//
//    }
//
//    private void processSDKMessage(BmobIMMessage msg, MessageEvent event) {
//        if (BmobNotificationManager.getInstance(context).isShowNotification()) {
//            //如果需要显示通知栏，SDK提供以下两种显示方式：
//            Intent pendingIntent = new Intent(context, MainActivity.class);
//            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//
//            //TODO 消息接收：8.5、多个用户的多条消息合并成一条通知：有XX个联系人发来了XX条消息
//            //BmobNotificationManager.getInstance(context).showNotification(event, pendingIntent);
//
//            //TODO 消息接收：8.6、自定义通知消息：始终只有一条通知，新消息覆盖旧消息
//            BmobIMUserInfo info = event.getFromUserInfo();
//            //这里可以是应用图标，也可以将聊天头像转成bitmap
//            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
//            BmobNotificationManager.getInstance(context).showNotification(largeIcon,
//                    info.getName(), msg.getContent(), "您有一条新消息", pendingIntent);
//        } else {
//            //直接发送消息事件
//            EventBus.getDefault().post(event);
//        }
//    }
//
//    /**
//     * 处理自定义消息类型
//     *
//     * @param msg
//     */
//    private void processCustomMessage(BmobIMMessage msg, BmobIMUserInfo info) {
//        //消息类型
//        String type = msg.getMsgType();
//        //发送页面刷新的广播
//        EventBus.getDefault().post(new RefreshEvent());
//        //处理消息
//        if (type.equals(AddFriendMessage.ADD)) {//接收到的添加好友的请求
//            NewFriend friend = AddFriendMessage.convert(msg);
//            //本地好友请求表做下校验，本地没有的才允许显示通知栏--有可能离线消息会有些重复
//            long id = NewFriendManager.getInstance(context).insertOrUpdateNewFriend(friend);
//            if (id > 0) {
//                showAddNotify(friend);
//            }
//        } else if (type.equals(AgreeAddFriendMessage.AGREE)) {//接收到的对方同意添加自己为好友,此时需要做的事情：1、添加对方为好友，2、显示通知
//            AgreeAddFriendMessage agree = AgreeAddFriendMessage.convert(msg);
//            addFriend(agree.getFromId());//添加消息的发送方为好友
//            //这里应该也需要做下校验--来检测下是否已经同意过该好友请求，我这里省略了
//            showAgreeNotify(info, agree);
//        } else {
//            Toast.makeText(context, "接收到的自定义消息：" + msg.getMsgType() + "," + msg.getContent() + "," + msg.getExtra(), Toast.LENGTH_SHORT).show();
//        }
//    }
}
