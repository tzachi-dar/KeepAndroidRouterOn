package com.example.tzachi.music;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

 
        Log.e("router", "onCreate called");

        // Start logging to logcat
        String filePath = Environment.getExternalStorageDirectory() + "/tzachilogcat.txt";
        Log.e("router", "filePath = " + filePath );
        filePath = "/mnt/sdcard/Music/tzachilogcat.txt";
        try {
            String[] cmd = { "/system/bin/sh", "-c", "ps | grep logcat  || logcat -f " + filePath + 
                    " -v threadtime router:V *:E " };
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e2) {
            Log.e("router", "running logcat failed, is the device rooted?", e2);
        }

		WifiStarterService wifiStarterService = WifiStarterService.getInstance();
        wifiStarterService.armTimer(getApplicationContext(),10);
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("router", "on resume called");
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
