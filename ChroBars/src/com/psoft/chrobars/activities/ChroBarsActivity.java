package com.psoft.chrobars.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.psoft.chrobars.R;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.util.ChroBarsSettings;
import com.psoft.chrobars.util.ChroUtils;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsActivity extends Activity {

	private static ChroSurface chronos;
	
	private static ChroBarsSettings settings;
	
	//Intents for starting the other Activities
	private Intent settingsIntent;
	private Intent aboutIntent;
	
	private static ChroBarsActivity instance;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Remove the title bar, get ViewGroup access
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		try {
			settings = ChroBarsSettings.getNewSettingsInstance(this);
		}
		catch(Exception unknownEx) {
			ChroUtils.printExDetails(unknownEx);
			System.out.println("Trying to get existing settings instance...");
			settings = ChroBarsSettings.getInstance(this);
			if(settings != null)
				System.out.println("Existing settings instance retrieved.");
			else
				throw new NullPointerException("Cannot continue. The settings object is null.");
		}
		
		//Create the GLSurfaceView and set it as the content view
		chronos = new ChroSurface(this);
		chronos.setSettingsInstance(settings);
		setContentView(chronos);
		
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
