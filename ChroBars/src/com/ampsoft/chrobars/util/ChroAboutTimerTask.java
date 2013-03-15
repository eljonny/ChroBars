package com.ampsoft.chrobars.util;

import java.util.TimerTask;

import android.widget.TextSwitcher;

/**
 * 
 * @author jhyry
 *
 */
public class ChroAboutTimerTask extends TimerTask {
	
	private TextSwitcher creditsRoller;
	private ChroBarsCredits credits;
	
	/**
	 * 
	 * @param tsw
	 * @param creds
	 */
	public ChroAboutTimerTask(TextSwitcher tsw, ChroBarsCredits creds) {
		creditsRoller = tsw;
		credits = creds;
	}
	
	/**
	 * 
	 */
	@Override
	public void run() {
		creditsRoller.post(new Runnable() {
			public void run() {
				System.out.println("Setting text...");
				creditsRoller.setText(credits.nextCredit());
			}
		});
	}
}