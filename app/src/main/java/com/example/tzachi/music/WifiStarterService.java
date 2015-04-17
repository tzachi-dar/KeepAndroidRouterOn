package com.example.tzachi.music;

import java.lang.reflect.Method;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

final class WifiApManager {
	private static final int WIFI_AP_STATE_FAILED = 4;
	private final WifiManager mWifiManager;
	private final String TAG = "Wifi Access Manager";
	private Method wifiControlMethod;
	private Method wifiApConfigurationMethod;
	private Method wifiApState;

	public WifiApManager(Context context) throws SecurityException,
			NoSuchMethodException {
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiControlMethod = mWifiManager.getClass().getMethod(
				"setWifiApEnabled", WifiConfiguration.class, boolean.class);
		wifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration", null);
		wifiApState = mWifiManager.getClass().getMethod("getWifiApState");
	}

	public boolean setWifiApState(WifiConfiguration config, boolean enabled) {
		try {
			if (enabled) {
				mWifiManager.setWifiEnabled(!enabled);
			}
			return (Boolean) wifiControlMethod.invoke(mWifiManager, config,	enabled);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return false;
		}
	}

	public WifiConfiguration getWifiApConfiguration() {
		try {
			return (WifiConfiguration) wifiApConfigurationMethod.invoke(mWifiManager, null);
		} catch (Exception e) {
			return null;
		}
	}

	public int getWifiApState() {
		try {
			return (Integer) wifiApState.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			return WIFI_AP_STATE_FAILED;
		}
	}
}

public class WifiStarterService {

    private static WifiStarterService instance = null;
    private final static String TAG = "router";
    final static int callbackPeriod = 300000;




    public static WifiStarterService getInstance() {
       if(instance == null) {
          instance = new WifiStarterService();
       }
       return instance;
    }

    public void setContext() {

    }

    public void periodicTimer(Context context) {
        // This is the timer function that will be called every minute. It is used in order to replay alerts,
        // execute snoozes and alert if we are not recieving data for a long time.
        Log.e(TAG, "PeriodicTimer called");
        StartRouter(context);
        armTimer(context, callbackPeriod);
    }

    public void  armTimer(Context context, int time) {
        Log.e(TAG, "ArmTimer called");
        Intent intent = new Intent();
        intent.setAction("com.example.tzachi.music.WifiStarterService");

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        time , alarmIntent);
    }

	void StartRouter(Context context) {
		  
		WifiConfiguration wc;
		WifiApManager wam = null;
		 
		try {
			wam= new WifiApManager(context);
			Toast.makeText(context, "wam created " ,Toast.LENGTH_LONG).show();
			Log.e(TAG, "wam created");
		} catch (NoSuchMethodException n) {
			Log.e(TAG, "ERROR: NoSuchMethodException", e);
		}catch (SecurityException s) {
			Log.e(TAG, "ERROR: SecurityException", e);
		}
		wc = wam.getWifiApConfiguration();
		Toast.makeText(context, "calling wam.setWifiApState " ,Toast.LENGTH_LONG).show();
		wam.setWifiApState(wc,true);
        Log.e(TAG, "wam.setWifiApState has returned");
	}
}



