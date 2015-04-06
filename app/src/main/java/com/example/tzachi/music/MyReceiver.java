package com.example.tzachi.music;

/**
 * Created by tzachi on 1/28/15.
 */

import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("tag", "New onRecieve called");

        PlayFile player = PlayFile.get_player();
        player.startMusic(context);

        // assumes WordService is a registered service
        //Intent intent = new Intent(context, WordService.class);
        //context.startService(intent);
    }
}