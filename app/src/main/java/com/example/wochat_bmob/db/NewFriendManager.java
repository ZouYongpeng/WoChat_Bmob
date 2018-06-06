package com.example.wochat_bmob.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.wochat_bmob.base.Config;
import com.example.wochat_bmob.bean.User;
import com.example.wochat_bmob.db.dao.DaoMaster;
import com.example.wochat_bmob.db.dao.DaoSession;
import com.example.wochat_bmob.db.dao.NewFriendDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobUser;

/**
 * Created by 邹永鹏 on 2018/5/26.
 */

public class NewFriendManager {
    private DaoMaster.DevOpenHelper openHelper;
    Context mContext;
    String uid=null;
    private static HashMap<String, NewFriendManager> daoMap = new HashMap<>();

    /*获取DB实例*/
    public static NewFriendManager getInstance(Context context) {
        User user = BmobUser.getCurrentUser(User.class);
        String loginId=user.getObjectId();
        if(TextUtils.isEmpty(loginId)){
            throw new RuntimeException("you must login.");
        }
        NewFriendManager dao = daoMap.get(loginId);
        if (dao == null) {
            dao = new NewFriendManager(context,loginId);
            daoMap.put(loginId, dao);
        }
        return dao;
    }

    private NewFriendManager(Context context, String uId){
        clear();
        this.mContext =context.getApplicationContext();
        this.uid=uId;
        String DBName = uId+".WoChatDB";
        this.openHelper = new DaoMaster.DevOpenHelper(mContext, DBName, null);
    }

    /*清空资源*/
    public void clear() {
        if(openHelper !=null) {
            openHelper.close();
            openHelper = null;
            mContext=null;
            uid =null;
        }
    }

    /*创建可读SQLite*/
    private DaoSession openReadableDb() {
//        checkInit();
        if(openHelper ==null){
            throw new RuntimeException("请初始化db");
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }

    /*创建读写SQLite*/
    private DaoSession openWritableDb(){
//        checkInit();
        if(openHelper ==null){
            throw new RuntimeException("请初始化db");
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession;
    }

    private void checkInit(){
        if(openHelper ==null){
            throw new RuntimeException("请初始化db");
        }
    }

    /*获取本地所有的邀请信息*/
    public List<NewFriend> getAllNewFriend(){
        NewFriendDao dao =openReadableDb().getNewFriendDao();
        return dao.queryBuilder().orderDesc(NewFriendDao.Properties.Time).list();
    }

    /*创建或更新新朋友信息*/
    public long insertOrUpdateNewFriend(NewFriend info){
        NewFriendDao dao = openWritableDb().getNewFriendDao();
        NewFriend local = getNewFriend(info.getUid(), info.getTime());
        if(local==null){
            return dao.insertOrReplace(info);
        }else{
            return -1;
        }
    }

    /*获取本地的好友请求*/
    private NewFriend getNewFriend(String uid,Long time){
        NewFriendDao dao =  openReadableDb().getNewFriendDao();
        return dao.queryBuilder().where(NewFriendDao.Properties.Uid.eq(uid))
                .where(NewFriendDao.Properties.Time.eq(time)).build().unique();
    }

    /*获取所有未读未验证的好友请求*/
    private List<NewFriend> getNoVerifyNewFriend(){
        NewFriendDao dao =  openReadableDb().getNewFriendDao();
        return dao.queryBuilder().where(NewFriendDao.Properties.Status.eq(Config.STATUS_VERIFY_NONE))
                .build().list();
    }

    /*是否有新的好友邀请*/
    public boolean hasNewFriendInvitation(){
        List<NewFriend> infos =getNoVerifyNewFriend();
        if(infos!=null && infos.size()>0){
            return true;
        }else{
            return false;
        }
    }

    /*获取未读的好友邀请*/
    public int getNewInvitationCount(){
        List<NewFriend> infos =getNoVerifyNewFriend();
        if(infos!=null && infos.size()>0){
            return infos.size();
        }else{
            return 0;
        }
    }

    /*批量更新未读未验证的状态为已读*/
    public void updateBatchStatus(){
        List<NewFriend> infos =getNoVerifyNewFriend();
        if(infos!=null && infos.size()>0){
            int size =infos.size();
            List<NewFriend> all =new ArrayList<>();
            for (int i = 0; i < size; i++) {
                NewFriend msg =infos.get(i);
                msg.setStatus(Config.STATUS_VERIFY_READED);//已读-未添加->点击查看了新朋友，则都变成已读状态
                all.add(msg);
            }
            insertBatchMessages(infos);
        }
    }

    /*批量插入消息*/
    public  void insertBatchMessages(List<NewFriend> msgs){
        NewFriendDao dao =openWritableDb().getNewFriendDao();
        dao.insertOrReplaceInTx(msgs);
    }

    /*修改指定好友请求的状态*/
    public long updateNewFriend(NewFriend friend,int status){
        NewFriendDao dao = openWritableDb().getNewFriendDao();
        friend.setStatus(status);
        return dao.insertOrReplace(friend);
    }

    /*删除指定的添加请求*/
    public void deleteNewFriend(NewFriend friend){
        NewFriendDao dao =openWritableDb().getNewFriendDao();
        dao.delete(friend);
    }
}
