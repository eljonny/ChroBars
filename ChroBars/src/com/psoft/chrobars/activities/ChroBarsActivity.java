package com.psoft.chrobars.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.psoft.chrobars.R;
import com.psoft.chrobars.data.ChroConstructionParams;
import com.psoft.chrobars.loading.ChroLoad;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.threading.ChroConstructionThread;
import com.psoft.chrobars.util.ChroBarsSettings;
import com.psoft.chrobars.util.ChroUtils;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsActivity extends Activity {
	
	public static DisplayMetrics screen;

	private static ViewSwitcher loadToGL;
	private static ViewFlipper chroModeFlipper;
	private static ChroConstructionParams params;
	private static ChroSurface chronos;
	private static ChroBarsActivity instance;
	private static ChroBarsSettings settings;
	
	//Intents for starting the other Activities
	private Intent settingsIntent;
	private Intent aboutIntent;
	
	//For maintaining app startup progress
	private Integer startupProgress = 0;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		loadToGL = new ViewSwitcher(this);
		chroModeFlipper = new ViewFlipper(this);
		
		screen = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(screen);
		
		//Remove the title bar if SDK version is below Honeycomb.
		//If it is 0xB or higher, we need the title for access to the ActionBar.
		if(Build.VERSION.SDK_INT < 0xB)
			requestWindowFeature(Window.FEATURE_NO_TITLE);

		ChroLoad  loadingCanvas = new ChroLoad(this, null);
		
		loadToGL.addView(loadingCanvas,0);
		loadToGL.addView(chroModeFlipper,1);
		
		setContentView(loadToGL);
		loadToGL.showNext();
		
		params = new ChroConstructionParams(this, chronos, settings);
		new ChroConstructionThread().execute(params);
		
		settings = params.getSettings();
		chronos = params.getRenderSurface();
		
		chroModeFlipper.addView(chronos);
		
		settingsIntent = new Intent(this, ChroBarsSettingsActivity.class);
		aboutIntent = new Intent(this, ChroBarsAboutActivity.class);
		
		instance = this;
	}
	
	/**
	 * Ensures that the settings singleton can be regenerated.
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ChroBarsSettings.clean();
		finish();
	}
	
	/**
	 * Makes sure that when the activity is destroyed, the settings instance is cleansed.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		ChroBarsSettings.clean();
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
	        	startActivity(settingsIntent);
	            break;
	        case R.id.menu_about:
	        	startActivity(aboutIntent);
	            break;
	            
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		
		return true;
	}
	
	/**
	 * The loading background task sets this during app loading.
	 * @param progress
	 */
	public void setProgressPercent(int progress) {
		startupProgress = progress;
		if(progress == 100)
			loadToGL.showNext();
	}
	
	/**
	 * Accessor for the field holding the current progress of the loading thread.
	 * @return
	 */
	public int getProgress() {
		return startupProgress;
	}
	
	/**
	 * This allows other activities in the ChroBars activity package to obtain the settings instance object reference, if needed.
	 * 
	 * @param requester
	 * @return
	 */
	protected static ChroBarsSettings requestSettingsObjectReference(Object requester) throws IllegalAccessException, NullPointerException {
		
		System.out.println(requester + " is requesting the settings object reference.");
		if(settings == null) {
			System.out.println("Settings object is null. Will try to get a new settings instance...");
			try {
				settings = ChroBarsSettings.getNewSettingsInstance(instance);
				System.out.println("New settings instance retrieved: " + settings);
			}
			catch(Exception unknownEx) {
				ChroUtils.printExDetails(unknownEx);
				System.out.println("Trying to get existing settings instance...");
				settings = ChroBarsSettings.getInstance(instance);
				if(settings != null)
					System.out.println("Existing settings instance retrieved.");
				else
					throw new NullPointerException("Cannot continue. The settings object is null.");
			}
		}
		System.out.println("Checking that requester is allowed to access settings object...");
		System.out.println("Requester info: " + requester.getClass().getPackage());
		if(requester.getClass().getPackage().equals(ChroBarsActivity.class.getPackage())) {
			System.out.println("Requester authenticated!");
			return settings;
		}
		else
			throw new IllegalAccessException("Class not authorized to receive settings object reference.");
	}
}
