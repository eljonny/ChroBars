package com.ampsoft.chrobars;

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
		
		renderer = ChroSurface.getRenderer();
		
		barType = t;
		
		if(barType.getType() == ChroType.MILLIS.getType())
			setDrawBar(false);
		else
			setDrawBar(true);
		
		//Set bar color
		if(color != null)
			barColor = color;
		
		else {
			
			switch(barType.getType()) {
			
			case 0:
				changeChroBarColor(Color.argb(255, 101, 234, 255));
				break;
			case 1:
				changeChroBarColor(Color.argb(255, 255, 193, 70 ));
				break;
			case 2:
				changeChroBarColor(Color.argb(255, 126, 255, 136));
				break;
			case 3:
				changeChroBarColor(Color.argb(255, 255, 133, 233));
				break;
				
			default:
				System.err.print("Invalid type!");
			}
		}
		
		//Initialize the vertex array with default values
		//And get the current window manager
		initVertices();
		wm = (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE);
		
		barsCreated++;
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
		visible[barType.getType()] = drawBar = toDraw;
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
	private void setBarWidth() {

		//Gather required information
		float screenWidth = (float)screen.widthPixels;
		float barTypeCode = (float)barType.getType();
		
		//Update the bar margin to 5px ratio of screen width
		barMargin /= screenWidth;
		barMargin *= 2.0f;
		
		//Perform bar width calculations
		int numberOfBars = renderer.numberOfBarsToDraw();
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth -= barMargin*2.0f;
		barWidth *= 2;
		
		barTypeCode -= (_MAX_BARS_TO_DRAW - numberOfBars);
		
		if(barType.getType() < 3)
			for(int i = barType.getType() + 1; i < _MAX_BARS_TO_DRAW; i++)
				if(!visible[i][0])
					++barTypeCode;
		else if(barType.getType() < 2)
			for(int j = barType.getType() - 1; j >= 0; j--)
				if(!visible[j][0])
					--barTypeCode;
		
		if(barTypeCode < 0)
			while(barTypeCode < 0)
				barTypeCode++;

		//Set the width of this bar
		if(!draw3D) {
			
			float leftXCoordinate_2D = barMargin +
					(barWidth * barTypeCode) + (barMargin * barTypeCode) +
						(((int)barTypeCode) > 0 ? barMargin : 0.0f) - 1.0f;
			float rightXCoordinate_2D = leftXCoordinate_2D + barWidth;
			
			vertices_2D[0] = vertices_2D[3] = leftXCoordinate_2D;
			vertices_2D[6] = vertices_2D[9] = rightXCoordinate_2D;
		}
		else {
			
			float leftXCoordinate_3D_front = barMargin +
					(barWidth * barTypeCode) + (barMargin * barTypeCode) +
						(((int)barTypeCode) > 0 ? barMargin : 0.0f) - 1.0f;
			float leftXCoordinate_3D_rear  = leftXCoordinate_3D_front + bar_3D_offset;
			
			float rightXCoordinate_3D_front = leftXCoordinate_3D_front + barWidth;
			float rightXCoordinate_3D_rear  = rightXCoordinate_3D_front + bar_3D_offset;
			
			vertices_3D[0] = vertices_3D[3]   = leftXCoordinate_3D_front;
			vertices_3D[6] = vertices_3D[9]   = rightXCoordinate_3D_front;
			vertices_3D[12] = vertices_3D[15] = leftXCoordinate_3D_rear;
			vertices_3D[18] = vertices_3D[21] = rightXCoordinate_3D_rear;
		}
	}

	/**
	 * 
	 * @param type
	 */
	private void adjustBarHeight() {
		
		wm.getDefaultDisplay().getMetrics(screen);
		
		setBarWidth();
		
		currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT-0800"), Locale.US);
		float scalingFactor = 3.65f;
		float barTopHeight = ChroBar._baseHeight + 0.01f;

		float timeRatio = getRatio();
		
		if(!draw3D) {
			//Set the bar height
			vertices_2D[1] = vertices_2D[10] = barTopHeight + (timeRatio*scalingFactor);
			
			//Reset the OpenGL vertices buffer with updated coordinates
			verticesBuffer_2D.clear();
			verticesBuffer_2D.put(vertices_2D);
			verticesBuffer_2D.position(0);
		}
		else {
			//Set the bar height
			vertices_3D[1] = vertices_3D[10] = vertices_3D[13] =
					vertices_3D[22] = barTopHeight + (timeRatio*scalingFactor);
			
			//Reset the OpenGL vertices buffer with updated coordinates
			verticesBuffer_3D.clear();
			verticesBuffer_3D.put(vertices_3D);
			verticesBuffer_3D.position(0);
		}
	}

	/**
	 * 
	 * @return
	 */
	private float getRatio() {

		switch(barType.getType()) {
		
		case 0:
			return (float)currentTime.get(Calendar.HOUR_OF_DAY)/(float)_HOURS_IN_DAY;
		case 1:
			return (float)currentTime.get(Calendar.MINUTE)/(float)_MINUTES_IN_HOUR;
		case 2:
			return (float)currentTime.get(Calendar.SECOND)/(float)_SECONDS_IN_MINUTE;
		case 3:
			return (float)currentTime.get(Calendar.MILLISECOND)/(float)_MILLIS_IN_SECOND;
			
		default:
			System.err.print("Invalid type!");
			return 0;
		}
	}

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
			
			if(!draw3D) {
				//Tell openGL where the vertex data is and how to use it
				//System.out.println("Calling glVertexPointer");
				drawSurface.glVertexPointer(_DIMENSIONS, GL10.GL_FLOAT,
												_VERTEX_STRIDE, verticesBuffer_2D);
				
				//System.out.println("Calling glColorPointer");
		        drawSurface.glColorPointer(_RGBA_COMPONENTS, GL10.GL_FLOAT,
		        									_VERTEX_STRIDE, colorBuffer_2D);
		        
				//Draw the bar
		        //System.out.println("Calling glDrawElements");
				drawSurface.glDrawElements(GL10.GL_TRIANGLES, _vertexDrawSequence_2D.length,
													GL10.GL_UNSIGNED_SHORT, drawDirection_2D);
			}
			else {
				//Tell openGL where the vertex data is and how to use it
				//System.out.println("Calling glVertexPointer");
				drawSurface.glVertexPointer(_DIMENSIONS, GL10.GL_FLOAT,
												_VERTEX_STRIDE, verticesBuffer_3D);
				
				//System.out.println("Calling glColorPointer");
		        drawSurface.glColorPointer(_RGBA_COMPONENTS, GL10.GL_FLOAT,
		        									_VERTEX_STRIDE, colorBuffer_3D);
		        
				//Draw the bar
		        //System.out.println("Calling glDrawElements");
				drawSurface.glDrawElements(GL10.GL_TRIANGLES, _vertexDrawSequence_3D.length,
													GL10.GL_UNSIGNED_SHORT, drawDirection_3D);
			}
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
	
	/**
	 * Changes the barColor value and those of the vertices.
	 * 
	 * @param colorInt
	 */
	public void changeChroBarColor(int colorInt) {
		
		barColor = colorInt;
		
		int color2DArrayLength = vertexColors_2D.length;
		int color3DArrayLength = vertexColors_3D.length;
		
		for(int i = 0; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.red(barColor)/255.0f;
		for(int i = 1; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.green(barColor)/255.0f;
		for(int i = 2; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.blue(barColor)/255.0f;
		for(int i = 3; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.alpha(barColor)/255.0f;
		
		for(int i = 0; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.red(barColor)/255.0f;
		for(int i = 1; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.green(barColor)/255.0f;
		for(int i = 2; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.blue(barColor)/255.0f;
		for(int i = 3; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.alpha(barColor)/255.0f;
		
		if(colorBuffer_2D != null) {
			colorBuffer_2D.position(0);
			colorBuffer_2D.put(vertexColors_2D);
			colorBuffer_2D.position(0);
		}
		if(colorBuffer_3D != null) {
			colorBuffer_3D.position(0);
			colorBuffer_3D.put(vertexColors_3D);
			colorBuffer_3D.position(0);
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
		return barsCreated;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		
		return "ChroBar Object " + this.hashCode() +
				"\nType:\n" + barType + "\nColor: " + barColor;
	}
}
