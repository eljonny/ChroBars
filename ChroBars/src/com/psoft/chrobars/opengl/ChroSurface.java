package com.psoft.chrobars.opengl;


import java.util.Locale;

import com.psoft.chrobars.settings.ChroBarsSettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;

/**
 * 
 * @author jhyry
 */
@SuppressLint("DefaultLocale")
public class ChroSurface extends GLSurfaceView {
	
	private static BarsRenderer rend;
	
	/**
	 * 
	 * 
	 * @param context The current activity context.
	 */
	public ChroSurface(Context context) {

		super(context);
		
		//Set the pixel format
		if((Build.MANUFACTURER.toLowerCase(Locale.US)).contains("samsung") ||
		   (Build.MANUFACTURER.toLowerCase(Locale.US)).contains("moto"))
			getHolder().setFormat(PixelFormat.RGB_565);
		else
			getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		//Uncomment if you're having trouble
	    //setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		
		rend = new BarsRenderer();
		rend.setActivityContext(context);
		
		setEGLContextClientVersion(1);
		
		setRenderer(rend);
		
		// TODO Need to figure out this type of rendering
		// Listener-based, I think
		//setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	
	/**
	 * Method to pass the settings object reference on to the renderer.
	 * @param s The settings object instance, of type ChroBarsSettings.
	 */
	public void setSettingsInstance(ChroBarsSettings s) {
		rend.setSettingsObjectReference(s);
	}
	
	/**
	 * 
	 * @return
	 */
	public static BarsRenderer getRenderer() {
		return rend;
	}
}