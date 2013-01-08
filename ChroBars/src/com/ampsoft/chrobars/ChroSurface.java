package com.ampsoft.chrobars;


import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * 
 * @author jhyry
 *
 */
public class ChroSurface extends GLSurfaceView {
	
	/**
	 * Use this GLSurfaceView subclass as it is.
	 * 
	 * @param context The current activity context.
	 */
	public ChroSurface(Context context) {
		super(context);
		//setEGLContextClientVersion(2);
	}
}