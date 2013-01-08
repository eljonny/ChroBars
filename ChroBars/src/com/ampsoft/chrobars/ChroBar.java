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
	
	//For drawing the bars on the screen
	private static final int HOURS_IN_DAY = 24;
	private static final int MINUTES_IN_HOUR = 60;
	private static final int SECONDS_IN_MINUTE = 60;
	private static final int MILLIS_IN_SECOND = 1000;
	private static final int _RGBA_COMPONENTS = 4;
	private static final int VERTEX_COMPONENTS = 12;
	private static final int DIMENSIONS = 3;
	private static final int BYTES_IN_FLOAT = 4;
	private static final int BYTES_IN_SHORT = 2;
	private static final int VERTICES = 4;
	private static final int VERTEX_STRIDE = 0;
	
	//Sequence of when to draw each vertex
	private static final short[] vertexDrawSequence = {0, 1, 2, 0, 2, 3};
	
	//Bar pixel margin, shared between all bars
	private static float barMargin = 5.0f;
	
	//Specifies the color of this bar
	private int barColor;
	
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
	private Calendar currentTime;
	private static WindowManager wm;
	
	/**
	 * 
	 * @param t
	 * @param value
	 * @param color
	 */
	public ChroBar(ChroType t, Integer color, Context activityContext) {
		
		barType = t;
		
		//Set vertex arrays
		vertexColors = new float[VERTICES*_RGBA_COMPONENTS];
		vertices = new float[VERTEX_COMPONENTS];
		
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
		
		System.out.println("CHROBARS-AMPSOFT<" +
				ChroUtils.getTimeString() +
				">: Bar color set to " + barColor);
		
		//Initialize the vertex array with default values
		//And get the current window manager
		initVertices();
		wm = (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE);
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices.length*BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer = rawBuffer.asFloatBuffer();
		verticesBuffer.put(vertices);
		verticesBuffer.position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors.length*BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer = rawBuffer.asFloatBuffer();
		colorBuffer.put(vertexColors);
		colorBuffer.position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexDrawSequence.length*BYTES_IN_SHORT);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawDirection = rawBuffer.asShortBuffer();
		drawDirection.put(vertexDrawSequence);
		drawDirection.position(0);
	}

	/**
	 * 
	 */
	private void initVertices() {
		
		float[] verts = { -0.5f, 1.0f, 0.0f,    // Upper Left  | 0
				  		  -0.5f, -0.9f, 0.0f,   // Lower Left  | 1
						   0.5f, -0.9f, 0.0f,   // Lower Right | 2
						   0.5f, 1.0f, 0.0f   };// Upper Right | 3

		for(int i = 0; i < VERTEX_COMPONENTS; i++)
			vertices[i] = verts[i];
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
		int numberOfBars = BarsRenderer.usingMilliseconds() ? 4 : 3;
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth -= barMargin*2.0f;
		barWidth *= 2;
		
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
	private void adjustBarHeight(int type) {
		
		wm.getDefaultDisplay().getMetrics(screen);
		currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT-0800"), Locale.US);
		
		setBarWidth();
		
		switch(type) {
		
		case 0:
			vertices[1] = vertices[10] = ((((float)currentTime.get(Calendar.HOUR_OF_DAY)/(float)HOURS_IN_DAY)*2.0f)-0.5f);
			break;
		case 1:
			vertices[1] = vertices[10] = ((((float)currentTime.get(Calendar.MINUTE)/(float)MINUTES_IN_HOUR)*2.0f)-0.5f);
			break;
		case 2:
			vertices[1] = vertices[10] = ((((float)currentTime.get(Calendar.SECOND)/(float)SECONDS_IN_MINUTE)*2.0f)-0.5f);
			break;
		case 3:
			vertices[1] = vertices[10] = ((((float)currentTime.get(Calendar.MILLISECOND)/(float)MILLIS_IN_SECOND)*2.0f)-0.5f);
			break;
		
		default:
			System.err.print("Invalid type!");
		}
		
		//Reset the OpenGL vertices buffer with updated coordinates
		verticesBuffer.clear();
		verticesBuffer.put(vertices);
		verticesBuffer.position(0);
	}

	/**
	 * 
	 * @param drawSurface
	 */
	public void draw(GL10 drawSurface) {

		//Set up face culling
	    drawSurface.glFrontFace(GL10.GL_CCW);
	    drawSurface.glEnable(GL10.GL_CULL_FACE);
	    drawSurface.glCullFace(GL10.GL_BACK);
		
	    //Enable the OpenGL vertex array buffer space
		drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		//Tell openGL where the vertex data is and how to use it
		drawSurface.glVertexPointer(DIMENSIONS, GL10.GL_FLOAT,
										VERTEX_STRIDE, verticesBuffer);
        drawSurface.glColorPointer(_RGBA_COMPONENTS, GL10.GL_FLOAT,
        									VERTEX_STRIDE, colorBuffer);
        
		//Draw the bar
		drawSurface.glDrawElements(GL10.GL_TRIANGLES, vertexDrawSequence.length,
											GL10.GL_UNSIGNED_SHORT, drawDirection);
		//Clear the buffer space
		drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
		//Disable face culling.
		drawSurface.glDisable(GL10.GL_CULL_FACE);
	    
		//Cache the surface
	    if(surface == null)
			surface = drawSurface;
	    
		//Recalculate the bar dimensions in preparation for a redraw
		adjustBarHeight(barType.getType());
	}
	
	/**
	 * Changes the barColor value and those of the vertices.
	 * 
	 * @param colorInt
	 */
	private void changeChroBarColor(int colorInt) {
		
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
		
		return "ChroBar Object " + this.hashCode() + "\nType:\n" + barType
				+ "\nColor: " + barColor;
	}
}
