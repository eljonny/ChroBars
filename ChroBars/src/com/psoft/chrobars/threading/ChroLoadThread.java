package com.psoft.chrobars.threading;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.psoft.chrobars.loading.ChroLoad;
import com.psoft.chrobars.util.ChroUtils;

/**
 * 
 * @author jon
 */
public class ChroLoadThread extends Thread {
	
    private SurfaceHolder loadSurfaceHolder;
    private ChroLoad loadScreenSurface;
    private int progress = 0;
 
    /**
     * 
     * @param surfaceHolder
     * @param panel
     */
    public ChroLoadThread(SurfaceHolder surfaceHolder, ChroLoad panel) {
        loadSurfaceHolder = surfaceHolder;
        loadScreenSurface = panel;
    }
    
    public void setProgress(int prog) {
    	progress = prog;
    }
 
    /**
     * 
     */
    @Override
    public void run() {
        
    	Canvas loadCanvas = null;
        
        do {
        	
            try {
                loadCanvas = loadSurfaceHolder.lockCanvas();
                synchronized (loadSurfaceHolder) {
                    loadScreenSurface.draw(loadCanvas);
                }
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
