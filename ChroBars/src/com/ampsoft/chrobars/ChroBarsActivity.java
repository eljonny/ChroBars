package com.ampsoft.chrobars;

import java.util.Calendar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
		setContentView(R.layout.activity_chro_bars);
		
		chronos = new ChroSurface(getBaseContext());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chro_bars, menu);
		return true;
	}
	
	
	
	private class ChroSurface extends GLSurfaceView {
		
		public ChroSurface(Context context) {
			super(context);
			setRenderer(new BarsRenderer());
		}
	}
	
	private class BarsRenderer implements GLSurfaceView.Renderer {

		private ChroBar seconds, minutes, hours;
		
		/**
		 * Initializes the ChroBar objects.
		 */
		private BarsRenderer() {
			seconds = new ChroBar(ChroType.SECOND, (byte) Calendar.SECOND, null);
			minutes = new ChroBar(ChroType.MINUTE, (byte) Calendar.MINUTE, null);
			hours = new ChroBar(ChroType.HOUR, (byte) Calendar.HOUR_OF_DAY, null);
		}
		
		/**
		 * Clears the openGL draw buffer and writes the new bars to the screen.
		 */
		@Override
		public void onDrawFrame(GL10 gl) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			hours.draw(gl);
			minutes.draw(gl);
			seconds.draw(gl);
		}

		/**
		 * 
		 */
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			
		}

		/**
		 * Sets up the surface when it is initially created
		 * or when the EGL context is lost.
		 * 
		 * @param gl The graphics library surface object
		 * @param config Which renderer configuration to use
		 * 
		 * @see android.opengl.GLSurfaceView.Renderer
		 */
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {

			// Set OpenGL Parameters:
			// - Background of the OpenGL surface to white
			// - Smooth GL shader model
			// - Clear the depth buffer for usage
			// - Enable the OpenGL depth testing
			// - Set the OpenGL depth testing function to be used
			// - Use NICEST perspective correction.
			gl.glClearColor(255.0f, 255.0f, 255.0f, 1.0f);
			gl.glShadeModel(GL10.GL_SMOOTH);
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		}
	}

}
