package com.example.wochat_bmob.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.wochat_bmob.activity.LaunchActivity;
import com.example.wochat_bmob.activity.MainActivity;
import com.example.wochat_bmob.service.WoChatService;

public class AutoOpenWoChatReceiver extends BroadcastReceiver {

    static final String action_boot ="android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Toast.makeText(context,"WoChat 将会开机启动",Toast.LENGTH_SHORT).show();
//        Log.d("AutoOpenWoChatReceiver","WoChat 将会开机启动");
        if (intent.getAction().equals(action_boot)){
            Log.d("AutoOpenWoChatReceiver","WoChat自动启动");
            Intent autoOpenIntent=new Intent(context.getApplicationContext(), LaunchActivity.class);
            autoOpenIntent.setAction("android.intent.action.MAIN");
            autoOpenIntent.addCategory("android.intent.category.LAUNCHER");
            autoOpenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(autoOpenIntent);
//
//            Log.d("AutoOpenWoChatReceiver","WoChatService自动启动");
            /*开启 WoChatService 服务*/
//            Intent startWoChatService=new Intent(context, WoChatService.class);
//            startWoChatService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(startWoChatService);
        }
    }
}
