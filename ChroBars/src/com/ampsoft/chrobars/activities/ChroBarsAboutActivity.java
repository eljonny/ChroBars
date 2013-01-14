package com.ampsoft.chrobars.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.ampsoft.chrobars.R;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsAboutActivity extends Activity {

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		System.out.println("Constructing about activity...");

		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu_about_chro_bars);
	}
}
