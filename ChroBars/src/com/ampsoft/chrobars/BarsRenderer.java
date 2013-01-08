package com.ampsoft.chrobars;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class BarsRenderer implements GLSurfaceView.Renderer {

	private ChroBar seconds, minutes, hours;
	
	private Context activityContext;
	
	/**
	 * Initializes the ChroBar objects.
	 */
	public BarsRenderer(Context aContext) {
		activityContext = aContext;
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
		
		seconds = new ChroBar(ChroType.SECOND, null, activityContext);
		minutes = new ChroBar(ChroType.MINUTE, null, activityContext);
		hours = new ChroBar(ChroType.HOUR, null, activityContext);
		System.out.println("CHROBARS-AMPSOFT<" +
				ChroUtils.getTimeString() +
				">: bar objects created!");
		
		// Set OpenGL Parameters:
		// - Background of the OpenGL surface to black
		// - Smooth GL shader model
		// - Clear the depth buffer for usage
		// - Enable the OpenGL depth testing
		// - Set the OpenGL depth testing function to be used
		// - Use NICEST perspective correction.
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}
	
	/**
	 * Clears the openGL draw buffer and writes the new bars to the screen.
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// Translates 4 units into the screen.
		gl.glTranslatef(0, 0, -4);
		drawChroBars(gl);
		gl.glLoadIdentity();
		
	}

	private void drawChroBars(GL10 gl) {

		hours.draw(gl);
		System.out.println("CHROBARS-AMPSOFT<" +
				ChroUtils.getTimeString() + ">: bar object\n"
				+ hours + "\ndrawn!");
		
		minutes.draw(gl);
		System.out.println("CHROBARS-AMPSOFT<" +
				ChroUtils.getTimeString() + ">: bar object\n"
				+ minutes + "\ndrawn!");
		
		seconds.draw(gl);
		System.out.println("CHROBARS-AMPSOFT<" +
				ChroUtils.getTimeString() + ">: bar object\n"
				+ seconds + "\ndrawn!");
	}

	/**
	 * 
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		//Readjusts the surface to match the current conditions
//		gl.glViewport(0, 0, width, height);
//		gl.glMatrixMode(GL10.GL_PROJECTION);
//		gl.glLoadIdentity();
//		gl.glOrthof(-1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f); 
//		GLU.gluPerspective(gl, 45.0f,
//							((float) width)/((float) height),
//							0.1f, 1.0f);
//		gl.glMatrixMode(GL10.GL_MODELVIEW);
//		gl.glLoadIdentity();
	}
}