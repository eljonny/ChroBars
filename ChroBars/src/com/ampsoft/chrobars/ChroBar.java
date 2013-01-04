package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBar {
	
	private int timeValue;
	private int barColor;
	
	private ChroType barType;
	
	private GL10 surface = null;
	private ByteBuffer rawVertexBuffer;
	private FloatBuffer verticesBuffer;
	
	/**
	 * 
	 * @param t
	 * @param value
	 * @param color
	 */
	public ChroBar(ChroType t, byte value, Integer color) {
		
		barType = t;
		timeValue = value;
		
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
		
		//Directly allocate the memory necessary to process the vertices
		rawVertexBuffer =
				ByteBuffer.allocateDirect(barType.getTypeVertices().length * 4);
		rawVertexBuffer.order(ByteOrder.nativeOrder());
		
		//Convert the raw buffer to the correct type
		verticesBuffer = rawVertexBuffer.asFloatBuffer();
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
		
		drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glVertexPointer(barType.getDimensions(),
									GL10.GL_FLOAT, 0, verticesBuffer);
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
}
