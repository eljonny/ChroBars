package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;

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
	
	//Sequence of when to draw each vertex
	private static final short[] vertexDrawSequence = {0, 1, 2, 0, 2, 3};
	
	//Constant arrays of width or height adjustment indexes.
	private static final int[] heightAdjustIndices = {1, 10};
	private static final int[] widthAdjustIndices = {0, 3, 6, 9};
	
	//Bar pixel margin, shared between all bars
	private static float barMargin = 5.0f;
	
	//Specifies the color of this bar
	private int barColor;
	
	//Vertex colors
	private float[] vertexColors;
	
	//Whether or not to draw the milliseconds bar
	private boolean usingMillis = false;
	
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
	
	/**
	 * 
	 * @param t
	 * @param value
	 * @param color
	 */
	public ChroBar(ChroType t, Integer color, Context activityContext) {
		
		barType = t;
		
		//Set vertex colors
		vertexColors = new float[t.getVertices()*4];
		
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
		
		wm = (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE);
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(barType.getTypeVertices().length*4);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer = rawBuffer.asFloatBuffer();
		verticesBuffer.put(barType.getTypeVertices());
		verticesBuffer.position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors.length*4);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer = rawBuffer.asFloatBuffer();
		colorBuffer.put(vertexColors);
		colorBuffer.position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexDrawSequence.length*2);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawDirection = rawBuffer.asShortBuffer();
		drawDirection.put(vertexDrawSequence);
		drawDirection.position(0);
	}

	/**
	 * 
	 * @param screenMetrics
	 * @param verts2 
	 */
	private float[] setBarWidth(DisplayMetrics screenMetrics, float[] verts) {

		//Gather required information
		float screenWidth = (float)screenMetrics.widthPixels;
		float barTypeCode = (float)barType.getType();
		
		//Update the bar margin to 5px ratio of screen width
		barMargin /= screenWidth;
		barMargin *= 2.0f;
		
		//Perform bar width calculations
		int numberOfBars = usingMillis  ? 4 : 3;
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth -= barMargin*2.0f;
		barWidth *= 2;
		
		float leftXCoordinate = barMargin +
				(barWidth * barTypeCode) + (barMargin * barTypeCode) +
					(((int)barTypeCode) > 0 ? barMargin : 0.0f) - 1.0f;
		
		float rightXCoordinate = leftXCoordinate + barWidth;
		
		//Set the width of this bar
		verts[0] = verts[3] = leftXCoordinate;
		verts[6] = verts[9] = rightXCoordinate;
		
		//Commit width settings
		barType.setTypeVertices(verts, widthAdjustIndices);
		
		return verts;
	}

	/**
	 * 
	 * @param type
	 */
	private void adjustBarHeight(int type) {
		
		wm.getDefaultDisplay().getMetrics(screen);
		
		float[] verts = setBarWidth(screen, barType.getTypeVertices());
		
		switch(type) {
		
		case 0:
			verts[1] = verts[10] = ((((float)Calendar.HOUR_OF_DAY/(float)HOURS_IN_DAY)*2.0f)-1.0f);
			break;
		case 1:
			verts[1] = verts[10] = ((((float)Calendar.MINUTE/(float)MINUTES_IN_HOUR)*2.0f)-1.0f);
			break;
		case 2:
			verts[1] = verts[10] = ((((float)Calendar.SECOND/(float)SECONDS_IN_MINUTE)*2.0f)-1.0f);
			break;
		case 3:
			verts[1] = verts[10] = ((((float)Calendar.MILLISECOND/(float)MILLIS_IN_SECOND)*2.0f)-1.0f);
			break;
		
		default:
			System.err.print("Invalid type!");
		}
		
		//Commit height adjustment
		barType.setTypeVertices(verts, heightAdjustIndices);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		verticesBuffer.position(0);
		verticesBuffer.put(barType.getTypeVertices());
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
		drawSurface.glVertexPointer(barType.getDimensions(), GL10.GL_FLOAT, 0, verticesBuffer);
        drawSurface.glColorPointer(_RGBA_COMPONENTS, GL10.GL_FLOAT, 0, colorBuffer);
        
		//Draw the bar
		drawSurface.glDrawElements(GL10.GL_TRIANGLES, vertexDrawSequence.length,
									GL10.GL_UNSIGNED_BYTE, drawDirection);
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
		
		int len = vertexColors.length;
		
		for(int i = 0; i < len; i += 4)
			vertexColors[i] = Color.red(barColor);
		for(int i = 1; i < len; i += 4)
			vertexColors[i] = Color.green(barColor);
		for(int i = 2; i < len; i += 4)
			vertexColors[i] = Color.blue(barColor);
		for(int i = 3; i < len; i += 4)
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
