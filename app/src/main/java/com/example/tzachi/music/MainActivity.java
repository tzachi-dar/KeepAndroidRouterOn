package com.example.tzachi.music;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.media.Ringtone;
import android.media.RingtoneManager;


import java.util.Date;

class SntpClient
{
    private static final String TAG = "SntpClient";
    private static final int REFERENCE_TIME_OFFSET = 16;
    private static final int ORIGINATE_TIME_OFFSET = 24;
    private static final int RECEIVE_TIME_OFFSET = 32;
    private static final int TRANSMIT_TIME_OFFSET = 40;
    private static final int NTP_PACKET_SIZE = 48;
    private static final int NTP_PORT = 123;
    private static final int NTP_MODE_CLIENT = 3;
    private static final int NTP_VERSION = 3;
    // Number of seconds between Jan 1, 1900 and Jan 1, 1970
    // 70 years plus 17 leap days
    private static final long OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L;
    // system time computed from NTP server response
    private long mNtpTime;
    // value of SystemClock.elapsedRealtime() corresponding to mNtpTime
    private long mNtpTimeReference;
    // round trip time in milliseconds
    private long mRoundTripTime;
    /**
     * Sends an SNTP request to the given host and processes the response.
     *
     * @param host host name of the server.
     * @param timeout network timeout in milliseconds.
     * @return true if the transaction was successful.
     */
    public boolean requestTime(String host, int timeout) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            InetAddress address = InetAddress.getByName(host);
            byte[] buffer = new byte[NTP_PACKET_SIZE];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, NTP_PORT);
            // set mode = 3 (client) and version = 3
            // mode is in low 3 bits of first byte
            // version is in bits 3-5 of first byte
            buffer[0] = NTP_MODE_CLIENT | (NTP_VERSION << 3);
            // get current time and write it to the request packet
            long requestTime = System.currentTimeMillis();
            long requestTicks = SystemClock.elapsedRealtime();
            writeTimeStamp(buffer, TRANSMIT_TIME_OFFSET, requestTime);
            socket.send(request);
            // read the response
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            long responseTicks = SystemClock.elapsedRealtime();
            long responseTime = requestTime + (responseTicks - requestTicks);
            // extract the results
            long originateTime = readTimeStamp(buffer, ORIGINATE_TIME_OFFSET);
            long receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET);
            long transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET);
            long roundTripTime = responseTicks - requestTicks - (transmitTime - receiveTime);
            // receiveTime = originateTime + transit + skew
            // responseTime = transmitTime + transit - skew
            // clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime))/2
            //             = ((originateTime + transit + skew - originateTime) +
            //                (transmitTime - (transmitTime + transit - skew)))/2
            //             = ((transit + skew) + (transmitTime - transmitTime - transit + skew))/2
            //             = (transit + skew - transit + skew)/2
            //             = (2 * skew)/2 = skew
            long clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime))/2;
            // if (false) Log.d(TAG, "round trip: " + roundTripTime + " ms");
            // if (false) Log.d(TAG, "clock offset: " + clockOffset + " ms");
            // save our results - use the times on this side of the network latency
            // (response rather than request time)
            mNtpTime = responseTime + clockOffset;
            mNtpTimeReference = responseTicks;
            mRoundTripTime = roundTripTime;
        } catch (Exception e) {
             Log.d(TAG, "request time failed: " + e);
            return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return true;
    }
    /**
     * Returns the time computed from the NTP transaction.
     *
     * @return time value computed from NTP server response.
     */
    public long getNtpTime() {
        return mNtpTime;
    }
    /**
     * Returns the reference clock value (value of SystemClock.elapsedRealtime())
     * corresponding to the NTP time.
     *
     * @return reference clock corresponding to the NTP time.
     */
    public long getNtpTimeReference() {
        return mNtpTimeReference;
    }
    /**
     * Returns the round trip time of the NTP transaction
     *
     * @return round trip time in milliseconds.
     */
    public long getRoundTripTime() {
        return mRoundTripTime;
    }
    /**
     * Reads an unsigned 32 bit big endian number from the given offset in the buffer.
     */
    private long read32(byte[] buffer, int offset) {
        byte b0 = buffer[offset];
        byte b1 = buffer[offset+1];
        byte b2 = buffer[offset+2];
        byte b3 = buffer[offset+3];
        // convert signed bytes to unsigned values
        int i0 = ((b0 & 0x80) == 0x80 ? (b0 & 0x7F) + 0x80 : b0);
        int i1 = ((b1 & 0x80) == 0x80 ? (b1 & 0x7F) + 0x80 : b1);
        int i2 = ((b2 & 0x80) == 0x80 ? (b2 & 0x7F) + 0x80 : b2);
        int i3 = ((b3 & 0x80) == 0x80 ? (b3 & 0x7F) + 0x80 : b3);
        return ((long)i0 << 24) + ((long)i1 << 16) + ((long)i2 << 8) + (long)i3;
    }
    /**
     * Reads the NTP time stamp at the given offset in the buffer and returns
     * it as a system time (milliseconds since January 1, 1970).
     */
    private long readTimeStamp(byte[] buffer, int offset) {
        long seconds = read32(buffer, offset);
        long fraction = read32(buffer, offset + 4);
        return ((seconds - OFFSET_1900_TO_1970) * 1000) + ((fraction * 1000L) / 0x100000000L);
    }
    /**
     * Writes system time (milliseconds since January 1, 1970) as an NTP time stamp
     * at the given offset in the buffer.
     */
    private void writeTimeStamp(byte[] buffer, int offset, long time) {
        long seconds = time / 1000L;
        long milliseconds = time - seconds * 1000L;
        seconds += OFFSET_1900_TO_1970;
        // write seconds in big endian format
        buffer[offset++] = (byte)(seconds >> 24);
        buffer[offset++] = (byte)(seconds >> 16);
        buffer[offset++] = (byte)(seconds >> 8);
        buffer[offset++] = (byte)(seconds >> 0);
        long fraction = milliseconds * 0x100000000L / 1000L;
        // write fraction in big endian format
        buffer[offset++] = (byte)(fraction >> 24);
        buffer[offset++] = (byte)(fraction >> 16);
        buffer[offset++] = (byte)(fraction >> 8);
        // low order bits should be random data
        buffer[offset++] = (byte)(Math.random() * 255.0);
    }
}



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


// should allow 2 functions play, stop. the caller sholdnot care for it's state...
class PlayFile {

    static PlayFile singletone;
    static PlayFile get_player() {
        if(singletone == null) {
            Log.e("tag","Creating a new PlayFile");
            singletone = new PlayFile();
        } else {
            Log.e("tag","Using existing PlayFile");
        }
        return singletone;
    }

    private MediaPlayer mediaPlayer;



    // not playing, playing, finished
    int state;


    void startMusic(Context ctx) {
        Log.e("tag", "start called");
      mediaPlayer = MediaPlayer.create(ctx, R.raw.a_neel_kamal_5702);    //(this, R.raw.a_neel_kamal_5702);
      mediaPlayer.start();
    }



    void stopMusic() {
        Log.e("tag", "stop called");
        if (mediaPlayer != null) {
            mediaPlayer.stop();

            mediaPlayer.release();
            mediaPlayer = null;
        }

    }



}



public class MainActivity extends ActionBarActivity {

    Button button;

    public void SetAlarm()
    {
//        final Button button = buttons[2]; // replace with a button from your own UI
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive( Context context, Intent I_ )
            {
                Log.e("tag", "On recieve called");
                button.setBackgroundColor( Color.RED );
                context.unregisterReceiver( this ); // this == BroadcastReceiver, not Activity
            }
        };


        Intent intent = new Intent();
        intent.setAction("de.vogella.android.mybroadcast");
//        sendBroadcast(intent);


        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this /* ??? context */, 0, intent, 0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        15 * 1000, alarmIntent);








/* old code did not work, when app died

        this.registerReceiver( receiver, new IntentFilter("com.blah.blah.somemessage") );

        PendingIntent pintent = PendingIntent.getBroadcast(this, 0, new Intent("com.blah.blah.somemessage"), 0);
        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000*15, pintent );

        */
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

            @Override
            public void onClick(View arg0) {
                //(new MyAsyncTask()).execute();
/*
                Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

                if(alert == null){
                    // alert is null, using backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    // I can't see this ever being null (as always have a default notification)
                    // but just incase
                    if(alert == null) {
                        // alert backup is null, using 2nd backup
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }
                }
                r = RingtoneManager.getRingtone(getApplicationContext(), alert);
                r.play();
*/

                SetAlarm();
//                PlayFile player = PlayFile.get_player();
//                player.startMusic(getApplicationContext());
            }

        });


        button = (Button) findViewById(R.id.button2);

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //r.stop();
                PlayFile player = PlayFile.get_player();
                player.stopMusic();

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
