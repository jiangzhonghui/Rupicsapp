package com.yi4all.rupics.service;

import com.yi4all.rupics.MainActivity;

import cn.jpush.android.api.JPushInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class JPushReceiver extends BroadcastReceiver {
	
	public static final String TAG = "JPushReceiver";
	public static final String HEAD_CHAT = "[CHAT]"; //聊天消息
	public static final String HEAD_FRIEND_PARTNER = "[FRIEND_PARTNER]"; //朋友，拍档关系处理
	
	public interface ChatListener{
		public void onGotChatMessage(String msg);
	}
	
	private ChatListener chatListener;
	
	public void setChatListener(ChatListener chatListener) {
		this.chatListener = chatListener;
	}



	@Override
public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "onReceive - " + intent.getAction());
         
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
             
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            System.out.println("收到了自定义消息。消息内容是：" + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            // 自定义消息不会展示在通知栏，完全要开发者写代码去处理
            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
                    if(message.startsWith(HEAD_CHAT)){
                    	if(chatListener != null){
                    		message = message.substring(HEAD_CHAT.length());
                    		chatListener.onGotChatMessage(message);
                    	}
                    }
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            System.out.println("收到了通知");
            // 在这里可以做些统计，或者做些其他工作
            
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            System.out.println("用户点击打开了通知");
            // 在这里可以自己写代码去定义用户点击后的行为
            Intent i = new Intent(context, MainActivity.class);  //自定义打开的界面
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
   
        } else {
            Log.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }

}
