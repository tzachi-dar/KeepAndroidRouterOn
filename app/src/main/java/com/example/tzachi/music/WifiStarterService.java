package com.example.tzachi.music;

import java.lang.reflect.Method;

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

public class WifiStarterService extends IntentService {
	  /**
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public WifiStarterService() {
	      super("WifiStarterService");
	  }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
    */
	@Override
	protected void onHandleIntent(Intent intent) {
		  
		WifiConfiguration wc;
		WifiApManager wam = null;
		 
		try {
			wam= new WifiApManager(getApplicationContext());
			Toast.makeText(getApplicationContext(), "wam created " ,Toast.LENGTH_LONG).show();
		} catch (NoSuchMethodException n) {
			//???
		}catch (SecurityException s) {
			//???
		}
		wc = wam.getWifiApConfiguration();
		Toast.makeText(getApplicationContext(), "calling wam.setWifiApState " ,Toast.LENGTH_LONG).show();
		while (true) {
			wam.setWifiApState(wc,true);
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// If we were interrupted, let's get out, hopefully, there is a good reason for that (the interuption)
				break;
			}
		}
	}
}



