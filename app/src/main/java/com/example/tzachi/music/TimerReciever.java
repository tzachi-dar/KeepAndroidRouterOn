package com.example.tzachi.music;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TimerReciever extends BroadcastReceiver {

    private final static String TAG = "router";
    
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "New onRecieve called Threadid " + Thread.currentThread().getId());

        WifiStarterService wifiStarterService = WifiStarterService.getInstance();
        wifiStarterService.periodicTimer(context);

    }
}
