/**
 * 
 */
package com.psoft.chrobars.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author jhyry
 *
 */
public class ChroLockOverlayBroadcastReceiver extends BroadcastReceiver {
	 
    private boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
         
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
        }
         
        // Send Current screen ON/OFF value to service
        Intent i = new Intent(context, ChroLockOverlayService.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);
    }
}