package com.psoft.chrobars.loading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.psoft.chrobars.R;
import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.threading.ChroLoadThread;

/**
 * This class draws and handles the loading screen 
 *  along with the 
 * 
 * @author jhyry
 */
public class ChroLoad extends SurfaceView implements SurfaceHolder.Callback {

    private ChroLoadThread loadingBarThread;
	private RectF logoLocation, loadingBarFrame;
    private Bitmap logo;
    private Paint textDraw, loadingBarFramePaint,
    					loadingBarPaint, logoPaint;
    
    private ChroBarsActivity mainActivity;
    
    /**
     * 
     * @param context
     * @param attrs
     */
    public ChroLoad(Context context, AttributeSet attrs) {
    	
    	super(context, attrs);
    	
    	mainActivity = (ChroBarsActivity) context;
		System.out.println("Loading logo bitmap...");
		logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
		
		buildLogoPlacementRectF();
		setUpTextDrawing();
		buildLoadingBarRectF();
		setUpLoadingBar();
		
		getHolder().addCallback(this);
		
		loadingBarThread = new ChroLoadThread(getHolder(), this);
    }

	/**
	 * 
	 */
	private void setUpLoadingBar() {
		
		loadingBarFramePaint = new Paint();
		loadingBarFramePaint.setColor(Color.GRAY);
		loadingBarFramePaint.setStyle(Style.STROKE);
		loadingBarFramePaint.setStrokeWidth(3);
		
		loadingBarPaint = new Paint(loadingBarFramePaint);
		loadingBarPaint.setStrokeWidth((float)ChroData._LOADING_BAR_HEIGHT - 10f);
	}

	/**
	 * 
	 */
	private void buildLoadingBarRectF() {
		
		//Find correct place to put the loading bar frame with 30px margin
		float left = logoLocation.left + 30f,
			  top = logoLocation.bottom + textDraw.getTextSize(), //Below the text
			  right = logoLocation.right - 30f,
			  bottom = top + ChroData._LOADING_BAR_HEIGHT;
		
		loadingBarFrame = new RectF(left, top, right, bottom);
	}

	/**
	 * 
	 */
	private void setUpTextDrawing() {
		textDraw = new Paint();
		textDraw.setColor(Color.WHITE);
		textDraw.setTextAlign(Align.CENTER);
		textDraw.setTypeface(Typeface.DEFAULT);
		textDraw.setTextSize(ChroData._LOGO_TEXT_HEIGHT);
	}

	/**
	 * 
	 */
	private void buildLogoPlacementRectF() {
		
		DisplayMetrics screen = ChroBarsActivity.getDisplayMetrics();
//		DEBUG
		System.out.println( "Current screen: h = " +
							screen.heightPixels + " w = " + screen.widthPixels );
		
		//Find correct place to put the logo
		float left = (screen.widthPixels >> 1) - 128f,
			  top = (screen.heightPixels >>  (screen.widthPixels > screen.heightPixels ? 5 : 2)),
			  right = left + 256,
			  bottom = top + 256;
		
//		System.out.println(	"Creating placement RectF from (" +
//							left + ", " + top + ", " + right + ", " + bottom + ")...");
		logoLocation = new RectF(left, top, right, bottom);
		logoPaint = new Paint();
		logoPaint.setAlpha(0);
	}

	/**
	 * 
	 */
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
 
	/**
	 * 
	 */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	loadingBarThread.start();
    }
 
    /**
     * 
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	try {
			loadingBarThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 
     */
    @Override
    public void draw(Canvas loading) {
    	
    	super.draw(loading);
    	
    	int progress = mainActivity.getProgress();
    	float progPercent = (float)progress/100f;
    	int		alpha = (int) (255*progPercent);
    	
//      DEBUG
//      System.out.println("Drawing loading screen...");
        loading.drawColor(Color.BLACK);
        logoPaint.setAlpha(alpha);
        textDraw.setAlpha(alpha);
		loading.drawBitmap(logo, null, logoLocation, logoPaint);
		loading.drawText("ChroBars", (ChroBarsActivity.getDisplayMetrics().widthPixels >> 1),
								logoLocation.bottom + 10f, textDraw);
		loading.drawRect(loadingBarFrame, loadingBarFramePaint);
    	float startX = loadingBarFrame.left + 5f,
    		  startY = loadingBarFrame.top + ((float)ChroData._LOADING_BAR_HEIGHT/2f),
    		  stopX = startX + (loadingBarFrame.right -
    				  	loadingBarFrame.left - 10f) * progPercent;
    	
    	loading.drawLine(startX, startY, stopX, startY, loadingBarPaint);
    	
    	loadingBarThread.setProgress(progress);
    }
}
