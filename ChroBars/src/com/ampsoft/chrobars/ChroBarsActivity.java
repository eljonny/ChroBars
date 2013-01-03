package com.ampsoft.chrobars;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;

public class ChroBarsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chro_bars);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chro_bars, menu);
		return true;
	}
	
	private class ChroSurface extends GLSurfaceView {

		private BarsRenderer bars;
		
		public ChroSurface(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	}
	
	private class BarsRenderer implements GLSurfaceView.Renderer {

		@Override
		public void onDrawFrame(GL10 gl) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// TODO Auto-generated method stub
			
		}
	}

}
