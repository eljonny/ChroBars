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
	
	//Bar pixel margin, shared between all bars
	private static float barMargin = 5;
	
	//Sequence of when to draw each vertex
	private static final short[] vertexDrawSequence = {0, 3, 2, 0, 2, 1};
	
	//Constant arrays of width or height adjustment indexes.
	private static final int[] heightAdjustIndices = {1, 4};
	private static final int[] widthAdjustIndices = {0, 3, 6, 9};
	
	//Specifies the color of this bar
	private int barColor;
	private boolean usingMillis = false;
	
	//Type of data this represents
	private ChroType barType;
	
	//OpenGL Surface and drawing buffers
	private GL10 surface = null;
	private ByteBuffer rawVertexBuffer, rawDrawDirection;
	private ShortBuffer drawDirection;
	private FloatBuffer verticesBuffer;
	
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
	    
		if(color != null)
			barColor = color;
		else {
			switch(barType.getType()) {
			
			case 0:
				barColor = Color.BLACK;
				break;
			case 1:
				barColor = Color.CYAN;
				break;
			case 2:
				barColor = Color.GREEN;
				break;
			case 3:
				barColor = Color.MAGENTA;
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
		rawVertexBuffer = ByteBuffer.allocateDirect(barType.getTypeVertices().length*4);
		rawVertexBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer = rawVertexBuffer.asFloatBuffer();

		//Allocate the vertex draw sequence buffer
		rawDrawDirection = ByteBuffer.allocateDirect(vertexDrawSequence.length*2);
		rawDrawDirection.order(ByteOrder.nativeOrder());
		drawDirection = rawDrawDirection.asShortBuffer();
		drawDirection.put(vertexDrawSequence);
		drawDirection.position(0);
		
		//According to the time, set the bar height
		adjustBarHeight(barType.getType());
	}

	/**
	 * 
	 * @param screenMetrics
	 * @param verts2 
	 */
	private void setBarWidth(DisplayMetrics screenMetrics, float[] verts) {
		
		//Perform bar width calculations
		int numberOfBars = usingMillis  ? 4 : 3;
		float barWidth = (float)screen.widthPixels/(float)numberOfBars;
		barWidth -= barMargin*2;
		
		//Set the width of this bar
		verts[0] = verts[9] = (barMargin +
				(barWidth * (float)barType.getType())) +
				(barMargin * barType.getType());
		verts[3] = verts[6] = verts[0] + barWidth;
		
		//Commit width settings
		barType.setTypeVertices(verts, widthAdjustIndices);
	}

	/**
	 * 
	 * @param type
	 */
	private void adjustBarHeight(int type) {
		
		wm.getDefaultDisplay().getMetrics(screen);
		
		float[] verts = barType.getTypeVertices();
		
		setBarWidth(screen, verts);
		
		switch(type) {
		
		case 0:
			verts[4] = verts[1] = ((float)Calendar.HOUR_OF_DAY/(float)HOURS_IN_DAY) * screen.heightPixels;
			break;
		case 1:
			verts[4] = verts[1] = ((float)Calendar.MINUTE/(float)MINUTES_IN_HOUR);
			break;
		case 2:
			verts[4] = verts[1] = ((float)Calendar.SECOND/(float)SECONDS_IN_MINUTE);
			break;
		case 3:
			verts[4] = verts[1] = ((float)Calendar.MILLISECOND/(float)MILLIS_IN_SECOND);
			break;
		
		default:
			System.err.print("Invalid type!");
		}
		
		verts[4] = verts[1] *= screen.heightPixels;
		
		barType.setTypeVertices(verts, heightAdjustIndices);
		verticesBuffer.clear();
		verticesBuffer.put(barType.getTypeVertices());
		verticesBuffer.position(0);
	}

	/**
	 * 
	 * @param drawSurface
	 */
	public void draw(GL10 drawSurface) {
	    
	    if(surface == null)
			surface = drawSurface;
		
        drawSurface.glColor4f((float)Color.red(barColor)/255.0f,
        					  (float)Color.green(barColor)/255.0f,
        					  (float)Color.blue(barColor)/255.0f,
        					  (float)Color.alpha(barColor)/255.0f);
		
		adjustBarHeight(barType.getType());

		//Set up face culling
	    drawSurface.glFrontFace(GL10.GL_CCW);
	    drawSurface.glEnable(GL10.GL_CULL_FACE);
	    drawSurface.glCullFace(GL10.GL_BACK);
		
	    //Enable the OpenGL vertex array buffer space
		drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glVertexPointer(barType.getDimensions(),
									GL10.GL_FLOAT, 0, verticesBuffer);
		//Draw the bar
		drawSurface.glDrawElements(GL10.GL_TRIANGLES, vertexDrawSequence.length,
									GL10.GL_UNSIGNED_BYTE, drawDirection);
		//Clear the buffer space
		drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
		// Disable face culling.
		drawSurface.glDisable(GL10.GL_CULL_FACE);
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
		
		barColor = Color.argb(alpha, red, green, blue);
		
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
		
		barColor = Color.parseColor(colorstring);

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
