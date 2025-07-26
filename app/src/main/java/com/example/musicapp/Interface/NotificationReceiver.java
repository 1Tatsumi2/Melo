package com.example.musicapp.Interface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    public static  final String ACTION_NEXTS="NEXT";
    public static  final String ACTION_PREV="PREVIOUS";
    public static  final String ACTION_PLAY="PLAY";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1=new Intent(context,MusicService.class);
        if(intent.getAction()!=null)
        {
            switch (intent.getAction()){
                case ACTION_PLAY:
                    intent1.putExtra("myActionName",intent.getAction());
                    context.startService(intent1);
                    break;
                case ACTION_NEXTS:
                    intent1.putExtra("myActionName",intent.getAction());
                    context.startService(intent1);
                    break;
                case ACTION_PREV:
                    intent1.putExtra("myActionName",intent.getAction());
                    context.startService(intent1);
                    break;
            }
        }
    }
}