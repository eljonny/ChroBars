/**
 * 
 */
package com.psoft.chrobars.threading;

import java.util.ArrayList;
import java.util.LinkedList;

import com.psoft.chrobars.opengl.BarsRenderer;
import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.util.ChroPrint;
import com.psoft.chrobars.util.ChroUtilities;

/**
 * We run this thread post-start to load the remaining textures.
 * 
 * @author jhyry
 */
public class ChroPostStartLoad extends Thread
								 implements IChroLoadThread{

	private static int progress;
	private static int maxProgress;
	//Cache a map of resids to strings/buffers
	private static ArrayList<ChroTexture> textures;
//	private static BarsRenderer barsRenderer;
	
	public ChroPostStartLoad(ArrayList<ChroTexture> toLoad, BarsRenderer rend) {
		textures = toLoad;
//		barsRenderer = rend;
		progress = 0;
	}
	
	public void run() {
		
		ArrayList<ChroTexture> loadThese = findCacheLaters();
		
		maxProgress = loadThese.size();
		
		LinkedList<Thread> cachingThreads = startCacheJobs(loadThese);

		cacheJobWaitFinish(cachingThreads);
		
		ChroPrint.println("Done caching late-load textures.", System.out);
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
	}

	/**
	 * @param loadThese
	 * @return
	 */
	private LinkedList<Thread> startCacheJobs(ArrayList<ChroTexture> loadThese) {
		LinkedList<Thread> cachingThreads = new LinkedList<Thread>();
		
		for(ChroTexture tex : loadThese) {
//			DEBUG
//			ChroPrint.println(tex, "Chaching:|", loadThese.size(), '-', System.out);
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
