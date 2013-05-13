package com.psoft.chrobars.activities;

import java.util.Date;
import java.util.Timer;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.psoft.chrobars.R;
import com.psoft.chrobars.data.ChroBarsCredits;
import com.psoft.chrobars.threading.about.ChroAboutTimerTask;
import com.psoft.chrobars.util.ChroUtilities;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsAboutActivity extends Activity{

	private static final short _switcher_period = 3500;
	private static final String _chrobarsAppName = "com.psoft.chrobars";
	
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
		
		try {
			PackageInfo chroInfo = getPackageManager().getPackageInfo(_chrobarsAppName, 0x0);
			String versionName = chroInfo.versionName + ":" + chroInfo.versionCode;
			TextView versionTView = (TextView) findViewById(R.id.lblAboutVersion);
			versionTView.setText(versionTView.getText() + " " + versionName);
		}
		catch (Exception unknownEx) { ChroUtilities.printExDetails(unknownEx); }
		
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
		
//		DEBUG
//		System.out.println("Working with " + credits.numberOfEntries() + " credits.");
		
		//Set up temporary date object; set to a .8 seconds past the current time.
		Date nearFuture = new Date();
		nearFuture.setTime(System.currentTimeMillis() + 800);
		
		textSwitchTimer.schedule(textSwitcher, nearFuture, _switcher_period);
	}
}
