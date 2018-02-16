package com.example.tzachi.music;

import java.lang.reflect.Method;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

import android.os.Build;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings;
import android.net.Uri;


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
        Calendar rightNow = Calendar.getInstance();
        int hours = rightNow.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = rightNow.get(Calendar.DAY_OF_WEEK);

    	Log.e(TAG, "PeriodicTimer called hours="+ hours);
        
    	if((dayOfWeek != Calendar.SATURDAY) && (hours >= 7 &&  hours <= 14)) {
    		StartRouter(context);
    	}
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

    StartRouter(Context context) {

        WifiConfiguration wc;
        WifiApManager wam = null;

                Log.e(TAG, "Before if");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.System.canWrite(context)) {
                        // Do stuff here
                        Log.e(TAG, "having permission");
                    }
                    else {
                        Log.e(TAG, "Asking for permission");
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }

		try {
			wam= new WifiApManager(context);
			Toast.makeText(context, "wam created " ,Toast.LENGTH_LONG).show();
			Log.e(TAG, "wam created");
		} catch (NoSuchMethodException n) {
			Log.e(TAG, "ERROR: NoSuchMethodException", n);
		}catch (SecurityException s) {
			Log.e(TAG, "ERROR: SecurityException", s);
		}
		wc = wam.getWifiApConfiguration();
		Toast.makeText(context, "calling wam.setWifiApState " ,Toast.LENGTH_LONG).show();
		wam.setWifiApState(wc,true);
        Log.e(TAG, "wam.setWifiApState has returned");
	}
}



