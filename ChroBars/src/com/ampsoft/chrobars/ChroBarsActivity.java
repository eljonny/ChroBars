package com.ampsoft.chrobars;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsActivity extends Activity {

	private ChroSurface chronos;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		chronos = new ChroSurface(this);

		//setContentView(R.layout.activity_chro_bars);
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
}
