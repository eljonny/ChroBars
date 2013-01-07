package com.ampsoft.chrobars;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsActivity extends Activity {

	private GLSurfaceView chronos;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.activity_chro_bars);
		chronos = new ChroSurface(this);
		setContentView(chronos);
		
		System.out.println("CHROBARS-AMPSOFT<" + ChroUtils.getTimeString() + ">: ChroSurface created and set as content view!");
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
