package com.psoft.chrobars.threading.construction;

import java.util.ArrayList;

import com.psoft.chrobars.ChroBar;
import com.psoft.chrobars.opengl.ChroTexture;
//DEBUG
//import com.psoft.chrobars.util.ChroPrint;

/**
 * @author jhyry
 *
 */
public class ChroBarTexLoadThread extends Thread {

	private ArrayList<ChroTexture> texs;
	private ChroBar bar;

	public ChroBarTexLoadThread(ArrayList<ChroTexture> texs, ChroBar bar) {
		this.texs = texs;
		this.bar = bar;
	}
	
	public void run() {
//		DEBUG
//		ChroPrint.println("Processing textures for bar " + bar, System.out);
		for(ChroTexture tex : texs) {
//			DEBUG
//			ChroPrint.println("Checking if " + tex + " needs to be added to " + bar + "...", System.out);
			bar.putNumberTexture(tex);
		}
//		DEBUG
//		ChroPrint.println("Done.", System.out);
	}
}
