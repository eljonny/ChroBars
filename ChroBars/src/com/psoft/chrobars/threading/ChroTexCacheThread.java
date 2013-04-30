package com.psoft.chrobars.threading;

import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.opengl.ChroTextures;
//import com.psoft.chrobars.util.ChroPrint;

/**
 * This Thread subclass runs the texture caching at application startup.
 * 
 * @author Jonathan Hyry
 * 
 * @see java.lang.Thread
 */
public class ChroTexCacheThread extends Thread {

	/**
	 * Stores the reference to a Bitmap object that will be cached by this thread.
	 */
	private ChroTexture toCache;
	
	/**
	 * This is used to update the application loading progress value.
	 * It is also used to map resource ids to buffers.
	 */
	private IChroLoadThread callbackThread;
	
	/**
	 * Sets references to Bitmap to cache and the thread callback.
	 * 
	 * @param numTex The Bitmap texture to cache.
	 * @param callback This allows us to synchronously update the application loading progress.
	 */
	public ChroTexCacheThread(ChroTexture numTex, IChroLoadThread callback) {
		toCache = numTex;
		callbackThread = callback;
	}
	
	/**
	 * The thread runner that caches the textures, then recycles the Bitmap object.
	 * After it is finished it calls back to the parent thread to update the loading progress.
	 */
	public void run() {
//		DEBUG
//		ChroTexture cached;
		(/*cached = */ChroTextures.cacheTexture(toCache)).getBmpTex().recycle();
//		DEBUG
//		ChroPrint.println(this + " cached " + cached, System.out);
		callbackThread.incProgress(1);
	}
}
