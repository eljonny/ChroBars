package com.ampsoft.chrobars;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsActivity extends Activity {

	private ChroSurface chronos;
	private FrameLayout settings, about;
	private View frontView;
	
	private Animation fadeIn, fadeOut;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Set up transition animations
		fadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
		
		frontView = chronos = new ChroSurface(this);

		//setContentView(R.layout.activity_chro_bars);
		setContentView(chronos);

		//Get this Context's LayoutInflater and inflate the settings/about layouts
		LayoutInflater inflater = getLayoutInflater();
		
		settings = (FrameLayout) inflater.inflate(R.layout.menu_settings_chro_bars, null);
		settings.setVisibility(View.INVISIBLE);
		about = (FrameLayout) inflater.inflate(R.layout.menu_about_chro_bars, null);
		about.setVisibility(View.INVISIBLE);
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
		frontView.startAnimation(fadeOut);
		about.setVisibility(View.VISIBLE);
		about.startAnimation(fadeIn);
		frontView.setVisibility(View.INVISIBLE);
		frontView = about;
	}

	private void chroSettings() {
		frontView.startAnimation(fadeOut);
		settings.setVisibility(View.VISIBLE);
		settings.startAnimation(fadeIn);
		frontView.setVisibility(View.INVISIBLE);
		frontView = settings;
	}

	/**
	 * 
	 */
	@Override
	public void onBackPressed() {
		
		if(frontView instanceof ChroSurface)
			finish();
		else {
			frontView.startAnimation(fadeOut);
			chronos.setVisibility(View.VISIBLE);
			chronos.startAnimation(fadeIn);
			frontView.setVisibility(View.INVISIBLE);
			frontView = chronos;
		}
	}
}
