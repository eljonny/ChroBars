package com.ampsoft.chrobars;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.SlidingDrawer;

public class ChroBarsSettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.menu_settings_chro_bars);
		
		((SlidingDrawer)findViewById(R.id.chrobars_settings_slidingDrawer)).animateOpen();
	}
}
