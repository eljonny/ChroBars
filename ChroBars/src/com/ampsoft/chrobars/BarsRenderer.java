package com.ampsoft.chrobars;

import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class BarsRenderer implements GLSurfaceView.Renderer {

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
		
		chroBars.put(ChroType.HOUR, new ChroBar(ChroType.HOUR, null, activityContext));
		chroBars.put(ChroType.MINUTE, new ChroBar(ChroType.MINUTE, null, activityContext));
		chroBars.put(ChroType.SECOND, new ChroBar(ChroType.SECOND, null, activityContext));
		chroBars.put(ChroType.MILLIS, new ChroBar(ChroType.MILLIS, null, activityContext));
		
		// Set OpenGL Parameters:
		// - Background of the OpenGL surface to white
		// - Smooth GL shader model
		// - Clear the depth buffer for usage
		// - Enable the OpenGL depth testing
		// - Set the OpenGL depth testing function to be used
		// - Use NICEST perspective correction.
		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.5f);
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

		gl.glTranslatef(0, 0, -5);

		(chroBars.get(ChroType.HOUR)).draw(gl);
		(chroBars.get(ChroType.MINUTE)).draw(gl);
		(chroBars.get(ChroType.SECOND)).draw(gl);
		(chroBars.get(ChroType.MILLIS)).draw(gl);
		
		gl.glLoadIdentity();
	}

	/**
	 * 
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		//Readjusts the surface to match the current conditions
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, 40.0f, ((float) width)/((float) height), 1.0f, 50.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	/**
	 * 
	 * @param context
	 */
	protected void setActivityContext(Context context) {
		
		activityContext = context;
	}
	
	/**
	 * 
	 * @return
	 */
	protected static int numberOfBarsToDraw() {
		
		return  ( ((chroBars.get(ChroType.HOUR)).isDrawn() ? 1 : 0) +
				  ((chroBars.get(ChroType.MINUTE)).isDrawn() ? 1 : 0) +
				  ((chroBars.get(ChroType.SECOND)).isDrawn() ? 1 : 0) +
				  ((chroBars.get(ChroType.MILLIS)).isDrawn() ? 1 : 0) );
	}
	
	protected static ChroBar getChroBar(ChroType type) {
		return chroBars.get(type);
	}

	private static HashMap<ChroType, ChroBar> chroBars = new HashMap<ChroType, ChroBar>(4);
	
	private Context activityContext;
}