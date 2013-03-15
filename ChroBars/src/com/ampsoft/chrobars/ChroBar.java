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

import com.ampsoft.chrobars.opengl.BarsRenderer;
import com.ampsoft.chrobars.opengl.ChroSurface;

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
	private static final int _2D_VERTEX_COMPONENTS = 12;
	private static final int _3D_VERTEX_COMPONENTS = 24;
	private static final int _DIMENSIONS = 3;
	private static final int _BYTES_IN_FLOAT = 4;
	private static final int _BYTES_IN_SHORT = 2;
	private static final int _2D_VERTICES = 4;
	private static final int _3D_VERTICES = 8;
	private static final int _VERTEX_STRIDE = 0;
	private static final int _MAX_BARS_TO_DRAW = 4;
	
	//Vertex draw sequences for 3D and 2D
	private static final short[] _vertexDrawSequence_2D = {	0, 1, 2,
																0, 2, 3  };
	
	private static final short[] _vertexDrawSequence_3D = {	0, 4, 5,
													           	0, 5, 1,
													           	1, 5, 6,
													           	1, 6, 2,
														        2, 6, 7,
														        2, 7, 3,
														        3, 7, 4,
														        3, 4, 0,
														        4, 7, 6,
														        4, 6, 5,
														        3, 0, 1,
														        3, 1, 2  };
	
	//Bar pixel margin and visibility array,
	//Shared between all bars. First column of
	// visible array is actual bar visibility,
	// where the second column is whether or not
	// to draw the bar in 3D. Using the settings
	// screen, enabling/disabling 3D affects each one.
	private static float barMargin = 5.0f;
	private static boolean[][] visible = new boolean[_MAX_BARS_TO_DRAW][2];
	//Perspective adjustment for rear portion of bar
	private static float bar_3D_offset = 0.1f;
	
	//Base Y-Coordinate from which to draw a ChroBar
	private static final float _baseHeight = -1.8f;
	//Base Z-Coordinate from which to extend a ChroBar into 3D
	private static final float _baseDepth = -0.75f;
	
	//The bar color is stored as a color int
	private int barColor;
	//How many bars have been created
	private static int barsCreated = 0;
	
	//Whether this bar should be drawn
	private boolean drawBar = true, draw3D = true;
	
	//Vertex arrays
	private float[] vertexColors_2D, vertexColors_3D;
	private float[] vertices_2D, vertices_3D;
	
	//Type of data this represents
	private ChroType barType;
	private static BarsRenderer renderer;
	
	//OpenGL Surface and drawing buffers
	private GL10 surface = null;
	private ByteBuffer rawBuffer;
	//2D buffers
	private ShortBuffer drawDirection_2D;
	private FloatBuffer verticesBuffer_2D;
	private FloatBuffer colorBuffer_2D;
	//3D buffers
	private ShortBuffer drawDirection_3D;
	private FloatBuffer verticesBuffer_3D;
	private FloatBuffer colorBuffer_3D;
	
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
		
		renderer = ChroSurface.getRenderer();
		
		barType = t;
		
		if(barType.getType() == 3)
			setDrawBar(false);
		else
			setDrawBar(true);
		
		setDraw3D(true);
		
		//Set 2D vertex arrays
		vertexColors_2D = new float[_2D_VERTICES*_RGBA_COMPONENTS];
		vertices_2D = new float[_2D_VERTEX_COMPONENTS];
		
		//Set 3D vertex arrays
		vertexColors_3D = new float[_3D_VERTICES*_RGBA_COMPONENTS];
		vertices_3D = new float[_3D_VERTEX_COMPONENTS];
		
		System.out.println("New 3D vertex array length: " + vertices_3D.length);
		
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
		
		/* Init 2D structures */
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices_2D.length*_BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer_2D = rawBuffer.asFloatBuffer();
		verticesBuffer_2D.put(vertices_2D);
		verticesBuffer_2D.position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors_2D.length*_BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer_2D = rawBuffer.asFloatBuffer();
		colorBuffer_2D.put(vertexColors_2D);
		colorBuffer_2D.position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(_vertexDrawSequence_2D.length*_BYTES_IN_SHORT);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawDirection_2D = rawBuffer.asShortBuffer();
		drawDirection_2D.put(_vertexDrawSequence_2D);
		drawDirection_2D.position(0);
		
		/* Init 3D structures */
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices_3D.length*_BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer_3D = rawBuffer.asFloatBuffer();
		verticesBuffer_3D.put(vertices_3D);
		verticesBuffer_3D.position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors_3D.length*_BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer_3D = rawBuffer.asFloatBuffer();
		colorBuffer_3D.put(vertexColors_3D);
		colorBuffer_3D.position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(_vertexDrawSequence_3D.length*_BYTES_IN_SHORT);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawDirection_3D = rawBuffer.asShortBuffer();
		drawDirection_3D.put(_vertexDrawSequence_3D);
		drawDirection_3D.position(0);
		
		barsCreated++;
	}

	/**
	 * 
	 */
	private void initVertices() {
		
		float[] verts_2D = { 	 -0.5f, 1.0f, 		 0.0f,    	// Upper Left  | 0
						  		 -0.5f, _baseHeight, 0.0f,    	// Lower Left  | 1
								  0.5f, _baseHeight, 0.0f,    	// Lower Right | 2
								  0.5f, 1.0f, 		 0.0f  };	// Upper Right | 3
		
		float[] verts_3D = {	 -0.3f,  1.0f,		  0.0f,    		  // Upper Left Front  | 0
					  		  	 -0.3f,  _baseHeight, 0.0f,    		  // Lower Left Front  | 1
					  		  	  0.3f,  _baseHeight, 0.0f,    		  // Lower Right Front | 2
								  0.3f,  1.0f,		  0.0f,    	 	  // Upper Right Front | 3
					  		  	 -0.2f,  1.0f,		  _baseDepth,     // Upper Left Rear   | 4
					  		  	 -0.2f,  _baseHeight, _baseDepth,     // Lower Left Rear   | 5
								  0.4f,  _baseHeight, _baseDepth,     // Lower Right Rear  | 6
								  0.4f,  1.0f,		  _baseDepth  };  // Upper Right Rear  | 7

		System.out.println("New 3D vertex array length: " + verts_3D.length);
		
		for(int i = 0; i < _2D_VERTEX_COMPONENTS; i++)
			vertices_2D[i] = verts_2D[i];
		for(int i = 0; i < _3D_VERTEX_COMPONENTS; i++)
			vertices_3D[i] = verts_3D[i];
	}
	
	/**
	 * 
	 * @param toDraw
	 */
	public void setDrawBar(boolean toDraw) {
		visible[barType.getType()][0] = drawBar = toDraw;
	}
	
	/**
	 * 
	 * @param toDraw3D
	 */
	public void setDraw3D(boolean toDraw3D) {
		visible[barType.getType()][1] = draw3D = toDraw3D;
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
	public boolean isDrawnIn3D() {
		return draw3D;
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
