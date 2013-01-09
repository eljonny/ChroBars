package com.ampsoft.chrobars;


import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
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
	 * 
	 * 
	 * @param context The current activity context.
	 */
	public ChroSurface(Context context) {

		super(context);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		//Uncomment if you're having trouble
	    //setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		
		rend = new BarsRenderer();
		rend.setActivityContext(context);
		
		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
		
		if(supportsEs2)
			setEGLContextClientVersion(2);
		else
			setEGLContextClientVersion(0);
		
		setRenderer(rend);
		
		// TODO Need to figure out this type of rendering
		// Listener-based, I think
		//setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	
	/**
	 * 
	 * @return
	 */
	protected BarsRenderer getRenderer() {
		return rend;
	}
}