package com.ampsoft.chrobars.activities;

import java.util.Date;
import java.util.Timer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextSwitcher;

import com.ampsoft.chrobars.R;
import com.ampsoft.chrobars.util.ChroAboutTimerTask;
import com.ampsoft.chrobars.util.ChroBarsCredits;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsAboutActivity extends Activity {

	private static final long _switcher_period = 3500;
	
	private static ChroBarsCredits credits;
	private static ChroAboutTimerTask textSwitcher;

	private TextSwitcher creditsDisplayView;
	private Timer textSwitchTimer = new Timer();
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.menu_about_chro_bars);
		
		construct();
		startCreditsRoll();
	}
	
	/**
	 * Make sure the Timer gets cancelled before we finish.
	 */
	@Override
	public void onBackPressed() {
		
		textSwitchTimer.cancel();
		textSwitchTimer.purge();
		finish();
	}

	/**
	 * 
	 */
	private void construct() {
		
		System.out.println("Constructing about activity...");
		
		creditsDisplayView = (TextSwitcher)findViewById(R.id.txtswitchCredits);
		credits = new ChroBarsCredits(this);
		textSwitcher = new ChroAboutTimerTask(creditsDisplayView, credits);
	}

	/**
	 * @throws InterruptedException 
	 * 
	 */
	private void startCreditsRoll() {
		
		System.out.println("Working with " + credits.numberOfEntries() + " credits.");
		
		//Set up temporary date object set to a half second past the current time.
		Date nearFuture = new Date();
		nearFuture.setTime(System.currentTimeMillis() + 800);
		
		textSwitchTimer.schedule(textSwitcher, nearFuture, _switcher_period);
	}
}
