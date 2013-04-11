package com.psoft.chrobars;

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
	
	//The bar and edge color is stored as a packed color int
	protected int barColor, edgeColor;
	//Whether this bar should be drawn
	protected boolean drawBar, drawNumber;
	protected float[] barVertexColors, vertices;
	protected float[] edgeVertexColors, normals;
	//OpenGL Surface and drawing buffers
	protected ShortBuffer barDrawSequenceBuffer;
	protected ShortBuffer edgeDrawSequenceBuffer;
	protected FloatBuffer verticesBuffer;
	protected FloatBuffer barsColorBuffer;
	protected FloatBuffer edgesColorBuffer;
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
		
		//Do the actual buffer allocation.
		barGLAllocate(ByteOrder.nativeOrder());
	}
	
	/* Begin subclass Interface. */
	
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
	 * Accessor for the length of the
	 *  bar-specific draw vertices sequence buffer to be used.
	 * @return An integer representation of the length of the draw sequence buffer.
	 */
	protected abstract int getBarDrawSequenceBufferLength();
	
	/**
	 * 
	 * @return
	 */
	protected abstract int getEdgeDrawSequenceBufferLength();
	
	/* End subclass Interface, Begin partial implementation. */
	
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
		
		for(int i = barType.getType() - (barType.is3D() ? 3 : (-1));
							i < ChroBarStaticData._MAX_BARS_TO_DRAW; i++) {
//			DEBUG
//			System.out.println("Current bar check index: " + i);
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
				drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, barsColorBuffer);
				drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, barsColorBuffer);
				
				//load the buffer of normals into the OpenGL draw object.
				drawSurface.glNormalPointer(GL10.GL_FLOAT, ChroBarStaticData._VERTEX_STRIDE, normalsBuffer);
			}
			
			//Tell openGL where the vertex data is and how to use it
			//System.out.println("Calling glVertexPointer");
			drawSurface.glVertexPointer(ChroBarStaticData._DIMENSIONS, GL10.GL_FLOAT,
										ChroBarStaticData._VERTEX_STRIDE, verticesBuffer);
			
			//Color buffer for the bars.
			//System.out.println("Calling glColorPointer for bars");
	        drawSurface.glColorPointer(ChroBarStaticData._RGBA_COMPONENTS, GL10.GL_FLOAT,
	        							ChroBarStaticData._VERTEX_STRIDE, barsColorBuffer);
	        
			//Draw the bar
	        //System.out.println("Calling glDrawElements for bars");
			drawSurface.glDrawElements(GL10.GL_TRIANGLES, getBarDrawSequenceBufferLength(),
										GL10.GL_UNSIGNED_SHORT, barDrawSequenceBuffer);
			
			if(renderer.getBarEdgeSetting() != 0) {
					
				//Color buffer for the edges.
				//System.out.println("Calling glColorPointer for edges");
		        drawSurface.glColorPointer(ChroBarStaticData._RGBA_COMPONENTS, GL10.GL_FLOAT,
		        							ChroBarStaticData._VERTEX_STRIDE, edgesColorBuffer);
				
				//Draw the accented bar edges
				//System.out.println("Calling glDrawElements for edges");
				drawSurface.glDrawElements(GL10.GL_LINES, getEdgeDrawSequenceBufferLength(),
											GL10.GL_UNSIGNED_SHORT, edgeDrawSequenceBuffer);
			}
			
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
	 * Changes the barColor/edgeColor values and those of the vertices.
	 * 
	 * @param colorInt
	 */
	public void changeChroBarColor(int colorInt) {
		
		byte edgeColorDiff;
		int colorArrayLength = barVertexColors.length;
		barColor = colorInt;
		
		//If we change the color of the bar,
		// we also need to change the edge color accordingly.
		switch(renderer.getBarEdgeSetting()) {
		//If the edges are supposed to be the same color
		case 0:
			edgeColor = barColor;
			break;
		//If the edges are supposed to be lighter than the bar
		case 1:
			edgeColorDiff = ChroBarStaticData._lighter_edgeColorDifference;
			edgeColor = Color.argb( Color.alpha(barColor),
								    Color.red(barColor) 	+ edgeColorDiff,
								    Color.green(barColor) 	+ edgeColorDiff,
								    Color.blue(barColor) 	+ edgeColorDiff   );
			break;
		//If the edges are supposed to be darker than the bar
		case 2:
			edgeColorDiff = ChroBarStaticData._darker_edgeColorDifference;
			edgeColor = Color.argb( Color.alpha(barColor),
								    Color.red(barColor) 	- edgeColorDiff,
								    Color.green(barColor) 	- edgeColorDiff,
								    Color.blue(barColor) 	- edgeColorDiff   );
			break;
		//We'll just set it to the bar color if it's an unknown edge color option to be safe.
		default:
			edgeColor = barColor;
		}
		
		
		for(int i = 0; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)Color.red(barColor)/255.0f;
			edgeVertexColors[i] = (float)Color.red(edgeColor)/255.0f;
		}
		for(int i = 1; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)Color.green(barColor)/255.0f;
			edgeVertexColors[i] = (float)Color.green(edgeColor)/255.0f;
		}
		for(int i = 2; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)Color.blue(barColor)/255.0f;
			edgeVertexColors[i] = (float)Color.blue(edgeColor)/255.0f;
		}
		for(int i = 3; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)Color.alpha(barColor)/255.0f;
			edgeVertexColors[i] = (float)Color.alpha(edgeColor)/255.0f;
		}
		
		((FloatBuffer) barsColorBuffer.clear()).put(barVertexColors).position(0);
		((FloatBuffer) edgesColorBuffer.clear()).put(edgeVertexColors).position(0);
	}

	/**
	 * If a user changes the edge style setting, we only want to update the edges.
	 */
	public void updateEdgeColor(int edgeType) {
		
		byte edgeColorDiff;
		int colorArrayLength = edgeVertexColors.length;
		
		switch(edgeType) {
		//If the edges are supposed to be the same color
		case 0:
			edgeColor = barColor;
			break;
		//If the edges are supposed to be lighter than the bar
		case 1:
			edgeColorDiff = ChroBarStaticData._lighter_edgeColorDifference;
			edgeColor = Color.argb( Color.alpha(barColor),
								    Color.red(barColor) 	+ edgeColorDiff,
								    Color.green(barColor) 	+ edgeColorDiff,
								    Color.blue(barColor) 	+ edgeColorDiff   );
			break;
		//If the edges are supposed to be darker than the bar
		case 2:
			edgeColorDiff = ChroBarStaticData._darker_edgeColorDifference;
			edgeColor = Color.argb( Color.alpha(barColor),
								    Color.red(barColor) 	- edgeColorDiff,
								    Color.green(barColor) 	- edgeColorDiff,
								    Color.blue(barColor) 	- edgeColorDiff   );
			break;
		//We'll just set it to the bar color if it's an unknown edge color option to be safe.
		default:
			edgeColor = barColor;
		}
		
		for(int i = 0; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)Color.red(edgeColor)/255.0f;
		for(int i = 1; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)Color.green(edgeColor)/255.0f;
		for(int i = 2; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)Color.blue(edgeColor)/255.0f;
		for(int i = 3; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)Color.alpha(edgeColor)/255.0f;
		
		((FloatBuffer) edgesColorBuffer.clear()).put(edgeVertexColors).position(0);
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
