/**
 * 
 */
package com.psoft.chrobars.service;
import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.util.ChroPrint;
import com.psoft.chrobars.util.ChroUtilities;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
 
 
public class ChroLockOverlayService extends Service {
    
	private BroadcastReceiver mReceiver = null;
     
    @Override
    public void onCreate() {
        
    	super.onCreate();
        
    	// Register receiver that handles screen on and screen off logic
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ChroLockOverlayBroadcastReceiver();
        registerReceiver(mReceiver, filter);
    }
 
    @Override
    public void onStart(Intent intent, int startId) {
         
        boolean screenOn = false;
         
        try{
            // Get ON/OFF values sent from receiver ( AEScreenOnOffReceiver.java )
            screenOn = intent.getBooleanExtra("screen_state", false);
        }
        catch(Exception e) {
        	ChroUtilities.printExDetails(e);
        }
         
        if (!screenOn && ChroData.activityDone) {

        	ChroPrint.println("Detected screen on event, starting ChroBars...", System.out);
            Intent chroBarsLockscreenIntent = new Intent(getBaseContext(), ChroBarsActivity.class);
            chroBarsLockscreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND);
            getBaseContext().startActivity(chroBarsLockscreenIntent);
        }
        else
            Toast.makeText(getBaseContext(), "ChroBars Lockscreen Service started", Toast.LENGTH_LONG).show();
    }
 
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
     
    @Override
    public void onDestroy() {
         
        Log.i("ScreenOnOff", "Service  destroy");
        
        if(mReceiver!=null)
        	unregisterReceiver(mReceiver);
    }
}