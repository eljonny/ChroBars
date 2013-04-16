package com.psoft.chrobars.loading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.psoft.chrobars.R;
import com.psoft.chrobars.activities.ChroBarsActivity;

/**
 * 
 * @author jhyry
 *
 */
public class ChroLoad extends SurfaceView implements SurfaceHolder.Callback {

    private Canvas loading;
	private RectF logoSizeAndLoc;
    private Bitmap logo;
    
    public ChroLoad(Context context, AttributeSet attrs) {
    	
    	super(context, attrs);
		System.out.println("Loading logo bitmap...");
		logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
		//Find correct place to put the logo
		float left = 0, top = 0, right = 0, bottom = 0;
		
		left 	+= ChroBarsActivity.screen.widthPixels >> 2;
		right 	+= left + left*2;
		top 	+= ChroBarsActivity.screen.heightPixels >> 2;
		bottom	+= top + top*2;
		System.out.println(	"Creating placement RectF from (" +
							left + ", " + top + ", " + right + ", " + bottom + ")...");
		logoSizeAndLoc = new RectF(left, top, right, bottom);
		getHolder().addCallback(this);
    }

	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
 
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        
    	try {
        	System.out.println("Locking drawing canvas...");
            loading = holder.lockCanvas();
            System.out.println("Locking surface container...");
            synchronized (holder) {
//            	DEBUG
            	System.out.println("Drawing logo...");
                loading.drawColor(Color.BLACK);
        		loading.drawBitmap(logo, null, logoSizeAndLoc, null);
            }
            System.out.println("Surface container unlocked.");
        } finally {
            if (loading != null) {
            	System.out.println("Releasing canvas lock...");
                holder.unlockCanvasAndPost(loading);
            	System.out.println("Canvas unlocked.");
            }
        }
    }
 
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { }
}
