package com.psoft.chrobars.threading;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.psoft.chrobars.loading.ChroLoad;
import com.psoft.chrobars.util.ChroUtilities;

/**
 * This class is derived from code at:
 * 
 * http://www.helloandroid.com/tutorials/how-use-canvas-your-android-apps-part-1
 * 
 * This is the thread that controls the loading screen drawing.
 * 
 * @author Jonathan Hyry
 */
public class ChroLoadThread extends Thread {
	
    private SurfaceHolder loadSurfaceHolder;
    private ChroLoad loadScreenSurface;
    private int progress = 0;
 
    /**
     * Here we only need to initialize the appropriate instance variables.
     * 
     * @param surfaceHolder The SurfaceView's underlying Surface container.
     * @param loadScreen The SurfaceView for the loading screen.
     */
    public ChroLoadThread(SurfaceHolder surfaceHolder, ChroLoad loadScreen) {
        loadSurfaceHolder = surfaceHolder;
        loadScreenSurface = loadScreen;
    }
    
    /**
     * Helper method that sets the progress from the 
     *  loading screen SurfaceView.
     *  
     * @param prog The progress being reported by the SurfaceView.
     */
    public void setProgress(int prog) {
    	progress = prog;
    }
 
    /**
     * This is the thread task to run that draws the loading screen.
     */
    @Override
    public void run() {
        
    	Canvas loadCanvas = null;
        
    	//Draw the loading screen until the progress
    	// is 100%. I have tried to put the progress == 100 
    	// check as the while condition, but for some 
    	// reason it does not function properly even though it 
    	// is doing the exact same thing??? Hmmmmm.
        do {
            try {
                loadCanvas = loadSurfaceHolder.lockCanvas();
                synchronized (loadSurfaceHolder) {
                    loadScreenSurface.draw(loadCanvas);
                }
            }
            //If we run into an Exception, print a report.
            catch(Exception ex) {
            	ChroUtilities.printExDetails(ex);
            }
            //Make sure we do not leave the canvas locked.
            finally {
                if (loadCanvas != null)
                    loadSurfaceHolder.unlockCanvasAndPost(loadCanvas);
            }
            
            if(progress == 100)
            	break;
            
        } while (true);
    }
}
