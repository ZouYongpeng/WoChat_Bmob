package com.example.wochat_bmob.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.wochat_bmob.R;
import com.example.wochat_bmob.activity.LaunchActivity;
import com.example.wochat_bmob.activity.MainActivity;

import butterknife.BindView;

public class WoChatService extends Service {

    Intent intent;
    PendingIntent pendingIntent;
    Notification notification;
    NotificationCompat.Builder mBuilder;

    public WoChatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent=new Intent(this, MainActivity.class);
        pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        mBuilder=new NotificationCompat.Builder(this);
        mBuilder.setContentText("点击打开WoChat")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.login_user_head)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.login_user_head))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags=START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class showForegroundBinder extends Binder{
        public void show(String name){
            mBuilder.setContentTitle(name);
            startForeground(1,mBuilder.build());
        }
    }

    private showForegroundBinder mBinder =new showForegroundBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
