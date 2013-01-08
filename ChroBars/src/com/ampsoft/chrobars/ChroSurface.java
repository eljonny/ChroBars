package com.ampsoft.chrobars;


import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * 
 * @author jhyry
 *
 */
public class ChroSurface extends GLSurfaceView {
	
	/*
	 * Might need this later.
	 * HACK
	 * private boolean wrapper = false;
	
	private GLSurfaceView surface;
	 */
	
	/**
	 * Use this GLSurfaceView subclass as it is.
	 * 
	 * @param context The current activity context.
	 */
	public ChroSurface(Context context) {
		super(context);
		//setEGLContextClientVersion(2);
		//Might need this later
		//HACK
		//surface = null;
	}
	
	/**
	 * Wrap an underlying GLSurfaceView already defined in the layout.
	 * 
	 * @param context The current activity context.
	 * @param surfaceView The surface this ChroSurface will wrap.
	 */
	/*
	 * I don't need this yet. Might need it later.
	public ChroSurface(Context context, GLSurfaceView surfaceView) {
		super(context);
		surface = surfaceView;
		surface.setRenderer(new BarsRenderer(context));
		wrapper = true;
	}
	
	/**
	 * 
	 * @return
	public boolean isWrapper() {
		return wrapper;
	}
	*/
}