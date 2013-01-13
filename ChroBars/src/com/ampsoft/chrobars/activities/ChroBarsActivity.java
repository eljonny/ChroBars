package com.ampsoft.chrobars.activities;

import com.ampsoft.chrobars.ChroSurface;
import com.ampsoft.chrobars.R;
import com.ampsoft.chrobars.R.id;
import com.ampsoft.chrobars.R.menu;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsActivity extends Activity {

	private static ChroSurface chronos;
	
	//Intents for starting the other Activities
	private Intent settingsIntent;
	private Intent aboutIntent;
	
	//For managing the child activities
	private static LocalActivityManager activityManager;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		activityManager = new LocalActivityManager(this, true);
		
		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		settingsIntent = new Intent(this, ChroBarsSettingsActivity.class);
		aboutIntent = new Intent(this, ChroBarsAboutActivity.class);
		
		settingsIntent.addCategory(Intent.CATEGORY_PREFERENCE);
		aboutIntent.addCategory(Intent.CATEGORY_DEFAULT);
		
		chronos = new ChroSurface(this);
		
		setContentView(chronos);
	}

	/**
	 * 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chro_bars, menu);
		return true;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	            chroSettings();
	            return true;
	        case R.id.menu_about:
	            aboutChroBars();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void aboutChroBars() {
		startActivity(aboutIntent);
	}

	private void chroSettings() {
		startActivity(settingsIntent);
	}
}
