package com.ampsoft.chrobars.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.ampsoft.chrobars.R;
import com.ampsoft.chrobars.opengl.ChroSurface;
import com.ampsoft.chrobars.util.ChroBarsSettings;

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
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		
		//Remove the title bar, get ViewGroup access
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//Create the GLSurfaceView and set it as the content view
		chronos = new ChroSurface(this);
		setContentView(chronos);
		
		settigs = new ChroBarsSettings();
		settingsIntent = new Intent(this, ChroBarsSettingsActivity.class);
		aboutIntent = new Intent(this, ChroBarsAboutActivity.class);
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
}
