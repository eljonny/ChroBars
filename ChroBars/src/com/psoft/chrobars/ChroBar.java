package com.psoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.psoft.chrobars.data.ChroBarStaticData;
import com.psoft.chrobars.opengl.BarsRenderer;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.util.ChroUtils;

/**
 * 
 * @author jhyry
 *
 */
public abstract class ChroBar {

	/* Static fields */
	
	protected static ChroBarStaticData barsData = null;
	protected static BarsRenderer renderer;
	protected static GL10 surface = null;
	//Screen size for the current device is
	//found using these objects
	protected static DisplayMetrics screen;
	//Used in determining bar height
	protected static Calendar currentTime;
	
	/* Instance Variables */
	
	//The bar color is stored as a packed color int
	protected int barColor;
	//Whether this bar should be drawn
	protected boolean drawBar, drawNumber;
	protected float[] vertexColors, vertices;
	protected float[] normals;
	//OpenGL Surface and drawing buffers
	protected ByteBuffer rawBuffer;
	protected ShortBuffer drawSequence;
	protected ShortBuffer lineSequence;
	protected FloatBuffer verticesBuffer;
	protected FloatBuffer colorBuffer;
	protected FloatBuffer normalsBuffer;
	//Type of data this represents
	protected ChroType barType;
	
	/**
	 * 
	 * @param t
	 * @param color
	 * @param activityContext
	 */
	public ChroBar(ChroType t, Integer color, Context activityContext) {
		
		//If the data object is null, make one. Otherwise do nothing.
		if(barsData == null)
			barsData = ChroBarStaticData.getNewDataInstance();
		
		renderer = ChroSurface.getRenderer();
		screen = new DisplayMetrics();
		
		//Set the data class object refs.
		synchronized(barsData) {
			if(barsData.getInt("barsCreated") < 1) {
				
				barsData.setObjectReference("renderer", ChroSurface.getRenderer());
				barsData.setObjectReference("wm", (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE));

				((WindowManager)barsData.getObject("wm")).getDefaultDisplay().getMetrics(screen);
			}
			barsData.modifyIntegerField("barsCreated", 1);
		}
		
		barType = t;
		
		//Use the native machine byte order for raw buffers.
		ByteOrder order_native = ByteOrder.nativeOrder();
		
		//Do the actual buffer allocation.
		barGLAllocate(order_native);
	}
	
	/**
	 * 
	 * @param order
	 */
	protected abstract void barGLAllocate(ByteOrder order);

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
	
	/**
	 * 
	 * @return
	 */
	public boolean isNumberDrawn() {
		return drawNumber;
	}
	
	/**
	 * 
	 */
	protected void calculateBarWidth() {

		//Gather required information
		float screenWidth = screen.widthPixels;
		//System.out.println("Screen width: " + screenWidth);
		float barTypeCode = (float)barType.getType() - 4;
		float barMargin = barsData.getFloat("barMarginBase");
		float edgeMargin = barsData.getFloat("edgeMarginBase");
		
		//Update the bar margin to current pixel width ratio of screen.
		barMargin /= screenWidth;
		barMargin *= 2f;
		barMargin *= renderer.getBarMarginScalar();
		//Update the edge margin to current screen pixels
		edgeMargin /= screenWidth; //Get the edge margin as a ratio to the entire screen width
		edgeMargin *= 2f; //Normalize the edge margin to cartesian coordinates.
		edgeMargin *= renderer.getEdgeMarginScalar(); //Scale the margin to the correct size.
		
		//Perform bar width calculations
		int numberOfBars = renderer.numberOfBarsToDraw();
		//System.out.println("We are drawing " + numberOfBars + " bars.");
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth *= 2f;
		barWidth -= ((edgeMargin*2f)/(float)numberOfBars);
		barWidth -= ((barMargin*((float)numberOfBars-1f))/(float)numberOfBars);
		
		barTypeCode -= (ChroBarStaticData._MAX_BARS_TO_DRAW - numberOfBars);
		
		for(int i = barType.getType() - 3; i < ChroBarStaticData._MAX_BARS_TO_DRAW; i++) {
			if(!renderer.refreshVisibleBars()[i].isDrawn())
				++barTypeCode;
		}
		
		if(barTypeCode < 0)
			barTypeCode = 0;
		
//		DEBUG
//		System.out.println("Bar type code: " + barTypeCode + "\nBar type: " + barType);
					
		float leftX = ChroBarStaticData._left_screen_edge + edgeMargin + (barWidth * barTypeCode) + (barMargin * barTypeCode);
		float rightX = leftX + barWidth;
		
		//Set the width of this bar object
		setBarWidth(leftX, rightX);
	}
	
	/**
	 * 
	 */
	protected void calculateBarHeight() {
		
		calculateBarWidth();
		
		currentTime = Calendar.getInstance(TimeZone.getDefault(), Locale.US);
		
		//This seems to do the trick. Fun with magic numbers!
		float scalingFactor = 3.65f;
		float barTopHeight = ChroBarStaticData._baseHeight + 0.01f;
		
		barTopHeight += (getRatio()*scalingFactor);
		
		setBarHeight(barTopHeight);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		((FloatBuffer) verticesBuffer.clear()).put(vertices).position(0);
		
		//If we're using dynamic lighting with a 3D bar, rebuild the vertex normals for the new bar height.
		if(barType.is3D() && renderer.usesDynamicLighting()) {
			try { initNormals(); }
			catch(Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
			((FloatBuffer) normalsBuffer.clear()).put(normals).position(0);
		}
	}
	
	/**
	 * 
	 * @param leftXCoord
	 * @param rightXCoord
	 */
	protected abstract void setBarWidth(float leftXCoord, float rightXCoord);

	/**
	 * 
	 * @param height
	 */
	protected abstract void setBarHeight(float height);

	/**
	 * Returns the current time ratios for hours, minutes, seconds, and milliseconds.
	 * 
	 * This takes into account the selected motion precision.
	 * 
	 * @return
	 */
	protected float getRatio() {
		
		int t = barType.getType();
		
		float currentHour = (float)currentTime.get(renderer.usesTwelveHourTime() ? Calendar.HOUR : Calendar.HOUR_OF_DAY),
				currentMinute = (float)currentTime.get(Calendar.MINUTE),
				currentSecond = (float)currentTime.get(Calendar.SECOND),
				currentMillisecond = (float)currentTime.get(Calendar.MILLISECOND);
		
		float currentMSInDay = 0, currentMSInHour = 0, currentMSInMinute = 0;
		
		int precision = (int) renderer.getPrecision();
		
		float hoursInDay = renderer.usesTwelveHourTime() ? ChroBarStaticData._HOURS_IN_DAY >> 1 : ChroBarStaticData._HOURS_IN_DAY;
		float msInDay = renderer.usesTwelveHourTime() ? ChroBarStaticData._msInDay >> 1 : ChroBarStaticData._msInDay;
		
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
			return ( currentHour + (precisionRatio * currentMSInDay) ) / ( hoursInDay + (precisionRatio * msInDay)	);
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
			calculateBarHeight();
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
