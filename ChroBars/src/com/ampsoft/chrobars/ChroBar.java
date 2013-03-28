package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.util.Calendar;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ampsoft.chrobars.data.ChroBarStaticData;
import com.ampsoft.chrobars.opengl.ChroSurface;

/**
 * 
 * @author jhyry
 *
 */
public abstract class ChroBar {

	protected static ChroBarStaticData barsData = null;
	
	//The bar color is stored as a color int
	protected int barColor;
	
	//Whether this bar should be drawn
	protected boolean drawBar = true;
	
	//Type of data this represents
	protected ChroType barType;
	
	//OpenGL Surface and drawing buffers
	protected GL10 surface = null;
	protected ByteBuffer rawBuffer;
	
	//Screen size for the current device is
	//found using these objects
	protected DisplayMetrics screen = new DisplayMetrics();
	
	//Used in determining bar height
	protected Calendar currentTime;
	
	/**
	 * 
	 * @param t
	 * @param value
	 * @param color
	 */
	public ChroBar(ChroType t, Integer color, Context activityContext) {
		
		//If the data object is null, make one. Otherwise do nothing.
		barsData = barsData == null ? new ChroBarStaticData() : barsData;
		
		barsData.setObjectReference("renderer", ChroSurface.getRenderer());
		barsData.setObjectReference("wm", (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE));
		barsData.incIntegerField("barsCreated");
		
		barType = t;
		
		//Initialize the vertex array with default values
		//And get the current window manager
		initVertices();
	}

	/**
	 * 
	 */
	protected abstract void initVertices();
	
	/**
	 * 
	 * @param toDraw
	 */
	public void setDrawBar(boolean toDraw) {
		barsData.getNonFinalBooleanArray("visible")[barType.getType()] = drawBar = toDraw;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isDrawn() {
		return drawBar;
	}

	/**
	 * 
	 * @param screenMetrics
	 * @param verts2 
	 */
	protected abstract void setBarWidth();

	/**
	 * 
	 * @param type
	 */
	protected abstract void adjustBarHeight();

	/**
	 * 
	 * @return
	 */
	protected abstract float getRatio(int barType);

	/**
	 * 
	 * @param drawSurface
	 */
	public void draw(GL10 drawSurface) {

		//If this bar should not be drawn, exit
		//The method
		if(drawBar) {
		
			//Set up face culling
			//System.out.println("Calling glFrontFace");
		    drawSurface.glFrontFace(GL10.GL_CCW);
		    
		    //System.out.println("Calling glEnable");
		    drawSurface.glEnable(GL10.GL_CULL_FACE);
		    
		    //System.out.println("Calling glCullFace");
		    drawSurface.glCullFace(GL10.GL_BACK);
			
		    //Enable the OpenGL vertex array buffer space
		    //System.out.println("Calling glEnableClientState");
			drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			
			//System.out.println("Calling glEnableClientState");
			drawSurface.glEnableClientState(GL10.GL_COLOR_ARRAY);
			
			drawBar(drawSurface);
			
			//Clear the buffer space
			//System.out.println("Calling glDisableClientState");
			drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			
			//System.out.println("Calling glDisableClientState");
			drawSurface.glDisableClientState(GL10.GL_COLOR_ARRAY);
			
			//Disable face culling.
			//System.out.println("Calling glDisable");
			drawSurface.glDisable(GL10.GL_CULL_FACE);
		    
			//Cache the surface
		    if(surface == null)
				surface = drawSurface;
		    
			//Recalculate the bar dimensions in preparation for a redraw
			adjustBarHeight();
		}
	}
	
	protected abstract void drawBar(GL10 drawSurface);
	
	/**
	 * Changes the barColor value and those of the vertices.
	 * 
	 * @param colorInt
	 */
	public abstract void changeChroBarColor(int colorInt);
	
	/**
	 * Changes the color of this bar using ARGB parameters.
	 * <br><br>
	 * If the surface has not yet been drawn, the color will 
	 * be changed on the next redraw.
	 * 
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void changeChroBarColor(int alpha, int red, int green, int blue) {
		
		changeChroBarColor(Color.argb(alpha, red, green, blue));
		
		if(surface != null)
			draw(surface);
	}
	
	/**
	 * Changes the color of this bar using a formatted string.
	 * <br><br>
	 * If the surface has not yet been drawn, the color will 
	 * be changed on the next redraw.
	 * 
	 * @param colorstring format of #RRGGBB or #AARRGGBB
	 * 
	 * @see android.graphics.Color#parseColor(String)
	 */
	public void changeChroBarColor(String colorstring) {
		
		changeChroBarColor(Color.parseColor(colorstring));

		if(surface != null)
			draw(surface);
	}

	/**
	 * 
	 * @return
	 */
	public int getBarColor() {
		return barColor;
	}
	
	/**
	 * 
	 * @return
	 */
	public static int barsCreated() {
		return barsData.getInt("barsCreated");
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		
		return "ChroBar Object " + this.hashCode() +
				"\nType:\n" + barType + "\nColor: " + barColor;
	}

	/**
	 * 
	 * @param ct
	 * @param object
	 * @param activityContext
	 * @return
	 */
	public static ChroBar getInstance(ChroType ct, Context activityContext) {
		
		if(ct.is3D())
			return new ChroBar3D(ct, null, activityContext);
		else
			return new ChroBar2D(ct, null, activityContext);
	}
}
