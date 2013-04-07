package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.ampsoft.chrobars.data.ChroBarStaticData;
import com.ampsoft.chrobars.opengl.BarsRenderer;
import com.ampsoft.chrobars.opengl.ChroSurface;

/**
 * 
 * @author jhyry
 *
 */
public abstract class ChroBar {

	protected static ChroBarStaticData barsData = null;
	protected static BarsRenderer renderer;
	
	//The bar color is stored as a packed color int
	protected int barColor;
	
	//Whether this bar should be drawn
	protected boolean drawBar, drawNumber;
	
	//Type of data this represents
	protected ChroType barType;
	
	//OpenGL Surface and drawing buffers
	protected GL10 surface = null;
	protected ByteBuffer rawBuffer;
	
	//Screen size for the current device is
	//found using these objects
	protected static DisplayMetrics screen;
	
	//Used in determining bar height
	protected static Calendar currentTime;
	
	/**
	 * 
	 * @param t
	 * @param value
	 * @param color
	 */
	public ChroBar(ChroType t, Integer color, Context activityContext) {
		
		//If the data object is null, make one. Otherwise do nothing.
		barsData = barsData == null ? ChroBarStaticData.getNewDataInstance() : barsData;
		
		synchronized(barsData) {
			if(screen == null) {
				barsData.setObjectReference("renderer", ChroSurface.getRenderer());
				barsData.setObjectReference("wm", (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE));
			}
			barsData.modifyIntegerField("barsCreated", 1);
		}
		
		barType = t;
		
		renderer = ChroSurface.getRenderer();
		screen = new DisplayMetrics();
	}

	/**
	 * 
	 */
	protected abstract void initVertices();
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected abstract void initNormals() throws Exception;
	
	/**
	 * 
	 * @param toDraw
	 */
	public void setDrawBar(boolean toDraw) {
		drawBar = toDraw;
	}
	
	/**
	 * 
	 * @param drawNum
	 */
	public void setDrawNumber(boolean drawNum) {
		drawNumber = drawNum;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isDrawn() {
		return drawBar;
	}
	
	public boolean isNumberDrawn() {
		return drawNumber;
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
	 * Returns the current time ratios for hours, minutes, seconds, and milliseconds.
	 * 
	 * This takes into account the selected motion precision.
	 * @return
	 */
	protected float getRatio() {
		
		int t = barType.getType();
		
		float currentHour = (float)currentTime.get(Calendar.HOUR_OF_DAY),
				currentMinute = (float)currentTime.get(Calendar.MINUTE),
				currentSecond = (float)currentTime.get(Calendar.SECOND),
				currentMillisecond = (float)currentTime.get(Calendar.MILLISECOND);
		
		float currentMSInDay = 0, currentMSInHour = 0, currentMSInMinute = 0;
		
		int precision = (int) renderer.getPrecision();
		
		if(precision > 0) {
			
			currentMSInMinute = (currentSecond*ChroBarStaticData._MILLIS_IN_SECOND) + currentMillisecond;
			currentMSInHour = (currentMinute*ChroBarStaticData._msInMinute) +
					 (currentSecond*ChroBarStaticData._MILLIS_IN_SECOND) + currentMillisecond;
			currentMSInDay = (currentHour*ChroBarStaticData._msInHour) +
					(currentMinute*ChroBarStaticData._msInMinute) +
					(currentSecond*ChroBarStaticData._MILLIS_IN_SECOND) + currentMillisecond;
		}
		
//		DEBUG
//		System.out.println("Current time:\n" + currentHour + "/" + currentMSInDay + "\n" + currentMinute + "/" + currentMSInHour + "\n" + currentSecond + "/" + currentMSInMinute + "\n" + currentMillisecond);
		
		float precisionRatio = renderer.getPrecision()/ChroBarStaticData._max_precision;
		
//		DEBUG
//		System.out.println("Precision ratio: " + precisionRatio);
		
		switch(t > 3 ? t - 4 : t) {
		
		case 0:
			return ( currentHour + (precisionRatio * currentMSInDay) ) / ( (float)ChroBarStaticData._HOURS_IN_DAY + (precisionRatio * ChroBarStaticData._msInDay)	);
		case 1:
			return ( currentMinute + (precisionRatio * currentMSInHour) ) / ( (float)ChroBarStaticData._MINUTES_IN_HOUR + (precisionRatio * ChroBarStaticData._msInHour) );
		case 2:
			return ( currentSecond + (precisionRatio * currentMSInMinute) ) / ( (float)ChroBarStaticData._SECONDS_IN_MINUTE + (precisionRatio * ChroBarStaticData._msInMinute));
		case 3:
			return currentMillisecond / (float)ChroBarStaticData._MILLIS_IN_SECOND;
			
		default:
			System.err.print("Invalid type!");
			return 0;
		}
	}

	/**
	 * General drawing preparation that needs to happen no matter what is being drawn.
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
		    //System.out.println("Calling glEnableClientState for vertex array");
			drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			//System.out.println("Calling glEnableClientState for color array");
			drawSurface.glEnableClientState(GL10.GL_COLOR_ARRAY);
			
			if(barType.is3D()) {
				
				//System.out.println("Calling glEnableClientState for normals array");
				drawSurface.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				
				//Set general lighting buffers
				drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, renderer.getSpecularBuffer());
				drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, renderer.getEmissionLightBuffer());
				drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, renderer.getShininessBuffer());
				
				//Set the color material to the appropriate colors.
				drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, getColorBuffer());
				drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, getColorBuffer());
				
				//load the buffer of normals into the OpenGL draw object.
				drawSurface.glNormalPointer(GL10.GL_FLOAT, ChroBarStaticData._VERTEX_STRIDE, getNormals());
			}
			
			//Tell openGL where the vertex data is and how to use it
			//System.out.println("Calling glVertexPointer");
			drawSurface.glVertexPointer(ChroBarStaticData._DIMENSIONS, GL10.GL_FLOAT,
										ChroBarStaticData._VERTEX_STRIDE, getVerticesBuffer());
			
			//System.out.println("Calling glColorPointer");
	        drawSurface.glColorPointer(ChroBarStaticData._RGBA_COMPONENTS, GL10.GL_FLOAT,
	        							ChroBarStaticData._VERTEX_STRIDE, getColorBuffer());
	        
			//Draw the bar
	        //System.out.println("Calling glDrawElements");
			drawSurface.glDrawElements(GL10.GL_TRIANGLES, getDrawSequenceBufferLength(),
										GL10.GL_UNSIGNED_SHORT, getDrawDirectionBuffer());
			
			//Clear the buffer space
			//System.out.println("Calling glDisableClientState for vertex array");
			drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			//System.out.println("Calling glDisableClientState for color array");
			drawSurface.glDisableClientState(GL10.GL_COLOR_ARRAY);
			
			if(barType.is3D()) {
				//System.out.println("Calling glDisableClientState for normals array");
				drawSurface.glDisableClientState(GL10.GL_NORMAL_ARRAY);
			}
			
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

	/**
	 * Accessor for the float array of normal vectors for the 3D bars.
	 * @return
	 */
	protected abstract FloatBuffer getNormals();
	
	/**
	 * Accessor for the length of the
	 *  bar-specific draw vertices sequence buffer to be used.
	 * @return An integer representation of the length of the draw sequence buffer.
	 */
	protected abstract int getDrawSequenceBufferLength();

	/**
	 * Accessor for the draw direction buffer
	 *  for the current bar.
	 * @return A FloatBuffer containing the draw sequence buffer.
	 */
	protected abstract ShortBuffer getDrawDirectionBuffer();

	/**
	 * Accessor for the vertices color buffer 
	 * for the current bar.
	 * @return A FloatBuffer that represents the colors of vertices.
	 */
	protected abstract FloatBuffer getColorBuffer();

	/**
	 * Accessor for the buffer defining the actual vertices
	 *  that make up the current bar.
	 * @return A FloatBuffer containing the vertex definitions.
	 */
	protected abstract FloatBuffer getVerticesBuffer();
	
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
	public ChroType getBarType() {
		return barType;
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
				"\nType: " + barType + "\nColor: " + barColor;
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
