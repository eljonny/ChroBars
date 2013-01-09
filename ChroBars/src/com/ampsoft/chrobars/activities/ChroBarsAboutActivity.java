package com.ampsoft.chrobars.activities;

import com.ampsoft.chrobars.R;
import com.ampsoft.chrobars.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class ChroBarsAboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.menu_about_chro_bars);
	}
}
