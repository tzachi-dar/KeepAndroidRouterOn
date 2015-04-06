package com.example.tzachi.music;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/*
class MyAsyncTask extends android.os.AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
        Log.i("NTP tag","startingrrrrrrrrrrrrrrrrrrrresulr" );
        //do something asynchronously
        SntpClient client = new SntpClient();
        if (client.requestTime("time.nist.gov", 10000)) {
            long now = client.getNtpTime() + SystemClock.elapsedRealtime() -
                    client.getNtpTimeReference();
            Date current = new Date(now);
            Log.i("NTP tag","rrrrrrrrrrrrrrrrrrrresulr" +  current.toString()+" now= "+ now + " sysetm time" +  new Date().getTime());
        }else {
            Log.i("NTP tag","rrrrrrrrrrrrrrrrrrrresulr     errro");
        }
        return null;
    }
}


*/





final class WifiApManager {
      private static final int WIFI_AP_STATE_FAILED = 4;
      private final WifiManager mWifiManager;
      private final String TAG = "Wifi Access Manager";
      private Method wifiControlMethod;
      private Method wifiApConfigurationMethod;
      private Method wifiApState;

      public WifiApManager(Context context) throws SecurityException, NoSuchMethodException {
//???       context = Preconditions.checkNotNull(context);
       mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
       wifiControlMethod = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,boolean.class);
       wifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration",null);
       wifiApState = mWifiManager.getClass().getMethod("getWifiApState");
      }   
      public boolean setWifiApState(WifiConfiguration config, boolean enabled) {
      // config = Preconditions.checkNotNull(config);
       try {
        if (enabled) {
            mWifiManager.setWifiEnabled(!enabled);
        }
        return (Boolean) wifiControlMethod.invoke(mWifiManager, config, enabled);
       } catch (Exception e) {
        Log.e(TAG, "", e);
        return false;
       }
      }
      public WifiConfiguration getWifiApConfiguration()
      {
          try{
              return (WifiConfiguration)wifiApConfigurationMethod.invoke(mWifiManager, null);
          }
          catch(Exception e)
          {
              return null;
          }
      }
      public int getWifiApState() {
       try {
            return (Integer)wifiApState.invoke(mWifiManager);
       } catch (Exception e) {
        Log.e(TAG, "", e);
            return WIFI_AP_STATE_FAILED;
       }
      }
}

public class MainActivity extends ActionBarActivity {

    Button button;


    void EnableRouter() {
    Toast.makeText(getApplicationContext(), "EnableRouter " ,Toast.LENGTH_LONG).show();
	WifiApManager wam = null;
        try {
		wam= new WifiApManager(getApplicationContext());
		Toast.makeText(getApplicationContext(), "wam created " ,Toast.LENGTH_LONG).show();
	} catch (NoSuchMethodException n) {
		//???
	}catch (SecurityException s) {
		//???
	}
	WifiConfiguration wc = wam.getWifiApConfiguration();
Toast.makeText(getApplicationContext(), "calling wam.setWifiApState " ,Toast.LENGTH_LONG).show();
	wam.setWifiApState(wc,true);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
        addListenerOnButton();
    }


    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.button1);

        button.setOnClickListener(new OnClickListener() {

//            @Override
            public void onClick(View arg0) {
                //(new MyAsyncTask()).execute();


		EnableRouter();


            }

        });


        button = (Button) findViewById(R.id.button2);

        button.setOnClickListener(new OnClickListener() {

//            @Override
            public void onClick(View arg0) {
                //r.stop();

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
