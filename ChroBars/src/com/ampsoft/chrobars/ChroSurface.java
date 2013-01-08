package com.ampsoft.chrobars;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

/**
 * 
 * @author jhyry
 *
 */
public class ChroSurface extends GLSurfaceView {
	
	private BarsRenderer rend;
	
	/**
	 * Use this GLSurfaceView subclass as it is.
	 * 
	 * @param context The current activity context.
	 */
	public ChroSurface(Context context) {

		super(context);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
	    setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		rend = new BarsRenderer();
		rend.setActivityContext(context);
		setRenderer(rend);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
}