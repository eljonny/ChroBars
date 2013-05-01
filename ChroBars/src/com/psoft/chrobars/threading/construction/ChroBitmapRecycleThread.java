package com.psoft.chrobars.threading.construction;

import com.psoft.chrobars.opengl.ChroTextures;

/**
 * Extremely basic worker thread to batch-recycle all
 *  Bitmaps created during program load time.
 * @author jhyry
 */
public class ChroBitmapRecycleThread extends Thread {

	/**
	 * Run the bitmap recycler in the textures class on this thread.
	 */
	public void run() {
		ChroTextures.recycleBitmaps();
		System.gc();
	}
}
