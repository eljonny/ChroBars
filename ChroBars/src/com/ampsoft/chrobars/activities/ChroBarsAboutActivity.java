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

	private static final long _switcher_period = 4000;
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
	 * 
	 */
	private void startCreditsRoll() {
		
		System.out.println("Working with " + credits.numberOfEntries() + " credits.");
		textSwitchTimer.schedule(textSwitcher, new Date(), _switcher_period);
	}

	/**
	 * 
	 */
	private void construct() {
		
		System.out.println("Constructing about activity...");
		creditsDisplayView = (TextSwitcher)findViewById(R.id.creditsTextSwitcher);
		credits = new ChroBarsCredits(this);
		textSwitcher = new ChroAboutTimerTask(creditsDisplayView, credits);
	}
}
