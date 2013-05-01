package com.psoft.chrobars.threading.construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.psoft.chrobars.ChroBar;
import com.psoft.chrobars.ChroType;
import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.util.ChroUtilities;
//DEBUG
//import com.psoft.chrobars.util.ChroPrint;

/**
 * @author jhyry
 *
 */
public class ChroBarsLateTexLoadThread extends Thread {

	private static HashMap<ChroType, ChroBar> bars;
	private static ArrayList<ChroTexture> texs;
	
	public ChroBarsLateTexLoadThread(ArrayList<ChroTexture> texs,
									  HashMap<ChroType, ChroBar> bars) {
		ChroBarsLateTexLoadThread.texs = texs;
		ChroBarsLateTexLoadThread.bars = bars;
	}
	
	public void run() {
		LinkedList<Thread> threadCache = new LinkedList<Thread>();
		
		for(ChroType ct : ChroType.values()) {
			Thread t;
			threadCache.add(t = new ChroBarTexLoadThread(texs, bars.get(ct)));
//			DEBUG
//			ChroPrint.println("Added bar " + ct + " load job with thread " + t, System.out);
			t.start();
		}
		
		threadDeathWait(threadCache);
	}

	private void threadDeathWait(LinkedList<Thread> threadCache) {
//		DEBUG
//		ChroPrint.println("Waiting for late bar tex load threads to die...", System.out);
		//Wait for all the threads to finish their jobs and die.
		while(!threadCache.isEmpty()) {
			Thread cacheThread = threadCache.remove();
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
		}
	}
}
