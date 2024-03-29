/**
 * 
 */
package com.psoft.chrobars.threading.construction;

import java.util.ArrayList;
import java.util.LinkedList;

import com.psoft.chrobars.opengl.BarsRenderer;
import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.threading.IChroLoadThread;
import com.psoft.chrobars.util.ChroPrint;
import com.psoft.chrobars.util.ChroUtilities;

/**
 * We run this thread post-start to load the remaining textures.
 * 
 * @author jhyry
 */
public class ChroPostStartLoadThread extends Thread
								 implements IChroLoadThread{

	private static int progress;
	private static int maxProgress;
	//Texture cache
	private static ArrayList<ChroTexture> textures;
//	NOTE uncomment for late texture loading.
//	private static BarsRenderer callback;
	
	public ChroPostStartLoadThread(ArrayList<ChroTexture> toLoad,
							  BarsRenderer rend) {
		textures = toLoad;
//		NOTE uncomment for late texture loading.
//		callback = rend;
		progress = 0;
	}
	
	public void run() {
		
		bindWait();
		
		ArrayList<ChroTexture> loadThese = findCacheLaters();
//		DEBUG
//		ChroPrint.println("We are now going to load these: " + loadThese, System.out);
		
		maxProgress = loadThese.size()*2;

		cacheJobWaitFinish(startCacheJobs(loadThese));
		
		ChroPrint.println("Done caching late-load textures.", System.out);
		
//		TODO Uncomment this if you need to load textures after ChroBars starts.
//		callback.loadLateCache(loadThese);
	}

	/**
	 * 
	 */
	private void bindWait() {
		//Wait for the main textures to bind to the surface.
		try { Thread.sleep(1000); }
		catch (InterruptedException e) { ChroUtilities.printExDetails(e); }
	}

	/**
	 * @param cachingThreads
	 */
	private void cacheJobWaitFinish(LinkedList<Thread> cachingThreads) {
		ChroPrint.println("Waiting for caching jobs to finish...", System.out);
		//Wait for all the threads to finish their jobs and die.
		while(!cachingThreads.isEmpty()) {
			Thread cacheThread = cachingThreads.remove();
			synchronized(cacheThread) {
				try {
					if(cacheThread.isAlive()) {
//						DEBUG
//						ChroPrint.println("Trying to join thread " + cacheThread, System.out);
						cacheThread.join();
					}
				}
				catch(Exception ex) { ChroUtilities.printExDetails(ex); }
			}
			incProgress(1);
		}
		//When we are done with all caching, recycle the loaded bitmaps.
		recycleBitmaps();
	}

	/**
	 * 
	 */
	private void recycleBitmaps() {
		(new ChroBitmapRecycleThread()).start();
	}

	/**
	 * @param loadThese
	 * @return
	 */
	private LinkedList<Thread> startCacheJobs(ArrayList<ChroTexture> loadThese) {
		LinkedList<Thread> cachingThreads = new LinkedList<Thread>();
		
		for(ChroTexture tex : loadThese) {
//			DEBUG
//			ChroPrint.println(tex.toString(), "Chaching:|", loadThese.size()+"", '-', System.out);
			ChroTexCacheThread cachingThread = new ChroTexCacheThread(tex, this);
			cachingThread.start();
			cachingThreads.add(cachingThread);
		}
		return cachingThreads;
	}

	/**
	 * @return
	 */
	private ArrayList<ChroTexture> findCacheLaters() {
		ArrayList<ChroTexture> loadThese = new ArrayList<ChroTexture>();
//		DEBUG
//		ChroPrint.println("Searching for textures to cache later in " + textures, System.out);
		for(ChroTexture tex : textures)
			if(tex.isCacheLater())
				loadThese.add(tex);
		return loadThese;
	}

	/**
	 * Prints the status of the texture loading process.
	 *  
	 *  Synchronized, because we will absolutely for sure have more 
	 *   than one thread trying to access this method at once.
	 * 
	 * @param incBy The amount to increment progress by.
	 */
	public synchronized void incProgress(int currentProgress) {
		progress += currentProgress;
		ChroPrint.println("Progress: " + progress + "/" + maxProgress + " items processed.", System.out);
	}
}
