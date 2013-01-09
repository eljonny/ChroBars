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

/**
 * 
 * @author jhyry
 *
 */
public class ChroBar {
	
	private static final int _HOURS_IN_DAY = 24;
	private static final int _MINUTES_IN_HOUR = 60;
	private static final int _SECONDS_IN_MINUTE = 60;
	private static final int _MILLIS_IN_SECOND = 1000;
	private static final int _RGBA_COMPONENTS = 4;
	private static final int _VERTEX_COMPONENTS = 12;
	private static final int _DIMENSIONS = 3;
	private static final int _BYTES_IN_FLOAT = 4;
	private static final int _BYTES_IN_SHORT = 2;
	private static final int _VERTICES = 4;
	private static final int _VERTEX_STRIDE = 0;
	private static final int _MAX_BARS_TO_DRAW = 4;
	
	private static final short[] _vertexDrawSequence = {0, 1, 2, 0, 2, 3};
	
	//Bar pixel margin and visibility array,
	//Shared between all bars
	private static float barMargin = 5.0f;
	private static boolean[] visible = new boolean[_MAX_BARS_TO_DRAW];
	
	//Base Y-Coordinate from which to draw a ChroBar
	private static final float _baseHeight = -1.8f;
	
	//The bar color is stored as a color int
	private int barColor;
	
	//Whether this bar should be drawn
	private boolean drawBar = true;
	
	//Vertex arrays
	private float[] vertexColors;
	private float[] vertices;
	
	//Type of data this represents
	private ChroType barType;
	
	//OpenGL Surface and drawing buffers
	private GL10 surface = null;
	private ByteBuffer rawBuffer;
	private ShortBuffer drawDirection;
	private FloatBuffer verticesBuffer;
	private FloatBuffer colorBuffer;
	
	//Screen size for the current device is
	//found using these objects
	private DisplayMetrics screen = new DisplayMetrics();
	private static WindowManager wm;
	
	//Used in determining bar height
	private Calendar currentTime;
	
	/**
	 * 
	 * @param t
	 * @param value
	 * @param color
	 */
	public ChroBar(ChroType t, Integer color, Context activityContext) {
		
		barType = t;
		
		if(barType.getType() == 0)
			for(int i = 0; i < visible.length; i++)
				visible[i] = true;
		
		//Set vertex arrays
		vertexColors = new float[_VERTICES*_RGBA_COMPONENTS];
		vertices = new float[_VERTEX_COMPONENTS];
		
		//Set bar color
		if(color != null)
			barColor = color;
		else {
			
			switch(barType.getType()) {
			
			case 0:
				changeChroBarColor(Color.BLACK);
				break;
			case 1:
				changeChroBarColor(Color.CYAN);
				break;
			case 2:
				changeChroBarColor(Color.GREEN);
				break;
			case 3:
				changeChroBarColor(Color.MAGENTA);
				break;
				
			default:
				System.err.print("Invalid type!");
			}
		}
		
		//Initialize the vertex array with default values
		//And get the current window manager
		initVertices();
		wm = (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE);
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices.length*_BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer = rawBuffer.asFloatBuffer();
		verticesBuffer.put(vertices);
		verticesBuffer.position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors.length*_BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer = rawBuffer.asFloatBuffer();
		colorBuffer.put(vertexColors);
		colorBuffer.position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(_vertexDrawSequence.length*_BYTES_IN_SHORT);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawDirection = rawBuffer.asShortBuffer();
		drawDirection.put(_vertexDrawSequence);
		drawDirection.position(0);
	}

	/**
	 * 
	 */
	private void initVertices() {
		
		float[] verts = { -0.5f, 1.0f, 0.0f,    // Upper Left  | 0
				  		  -0.5f, _baseHeight, 0.0f,   // Lower Left  | 1
						   0.5f, _baseHeight, 0.0f,   // Lower Right | 2
						   0.5f, 1.0f, 0.0f   };// Upper Right | 3

		for(int i = 0; i < _VERTEX_COMPONENTS; i++)
			vertices[i] = verts[i];
	}
	
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
		int numberOfBars = BarsRenderer.numberOfBarsToDraw();
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth -= barMargin*2.0f;
		barWidth *= 2;
		
		barTypeCode -= (_MAX_BARS_TO_DRAW - numberOfBars);
		
		if(barType.getType() < 3)
			for(int i = barType.getType() + 1; i < _MAX_BARS_TO_DRAW; i++)
				if(!visible[i])
					++barTypeCode;
		else if(barType.getType() < 2)
			for(int j = barType.getType() - 1; j >= 0; j--)
				if(!visible[j])
					--barTypeCode;
		
		if(barTypeCode < 0)
			while(barTypeCode < 0)
				barTypeCode++;
		
		float leftXCoordinate = barMargin +
				(barWidth * barTypeCode) + (barMargin * barTypeCode) +
					(((int)barTypeCode) > 0 ? barMargin : 0.0f) - 1.0f;
		
		float rightXCoordinate = leftXCoordinate + barWidth;
		
		//Set the width of this bar
		vertices[0] = vertices[3] = leftXCoordinate;
		vertices[6] = vertices[9] = rightXCoordinate;
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
		
		//Set the bar height
		float timeRatio = getRatio();
		vertices[1] = vertices[10] = barTopHeight + (timeRatio*scalingFactor);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		verticesBuffer.clear();
		verticesBuffer.put(vertices);
		verticesBuffer.position(0);
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
			
			//Tell openGL where the vertex data is and how to use it
			//System.out.println("Calling glVertexPointer");
			drawSurface.glVertexPointer(_DIMENSIONS, GL10.GL_FLOAT,
											_VERTEX_STRIDE, verticesBuffer);
			
			//System.out.println("Calling glColorPointer");
	        drawSurface.glColorPointer(_RGBA_COMPONENTS, GL10.GL_FLOAT,
	        									_VERTEX_STRIDE, colorBuffer);
	        
			//Draw the bar
	        //System.out.println("Calling glDrawElements");
			drawSurface.glDrawElements(GL10.GL_TRIANGLES, _vertexDrawSequence.length,
												GL10.GL_UNSIGNED_SHORT, drawDirection);
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
		
		int colorArrayLength = vertexColors.length;
		
		for(int i = 0; i < colorArrayLength; i += 4)
			vertexColors[i] = Color.red(barColor);
		for(int i = 1; i < colorArrayLength; i += 4)
			vertexColors[i] = Color.green(barColor);
		for(int i = 2; i < colorArrayLength; i += 4)
			vertexColors[i] = Color.blue(barColor);
		for(int i = 3; i < colorArrayLength; i += 4)
			vertexColors[i] = Color.alpha(barColor);
		
		if(colorBuffer != null) {
			colorBuffer.position(0);
			colorBuffer.put(vertexColors);
			colorBuffer.position(0);
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
	 */
	@Override
	public String toString() {
		
		return "ChroBar Object " + this.hashCode() +
				"\nType:\n" + barType + "\nColor: " + barColor;
	}
}
