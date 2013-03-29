package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.microedition.khronos.opengles.GL10;

import com.ampsoft.chrobars.data.ChroBarStaticData;

import android.content.Context;
import android.graphics.Color;
import android.view.WindowManager;

public class ChroBar3D extends ChroBar {
	
	private float[] vertexColors_3D, vertices_3D;
	
	private ShortBuffer drawDirection_3D;
	private FloatBuffer verticesBuffer_3D;
	private FloatBuffer colorBuffer_3D;

	public ChroBar3D(ChroType t, Integer color, Context activityContext) {
		
		super(t, color, activityContext);
		
		//Set 3D vertex arrays
		vertexColors_3D = new float[ChroBarStaticData._3D_VERTICES*ChroBarStaticData._RGBA_COMPONENTS];
		vertices_3D = new float[ChroBarStaticData._3D_VERTEX_COMPONENTS];
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices_3D.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer_3D = rawBuffer.asFloatBuffer();
		verticesBuffer_3D.put(vertices_3D);
		verticesBuffer_3D.position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors_3D.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer_3D = rawBuffer.asFloatBuffer();
		colorBuffer_3D.put(vertexColors_3D);
		colorBuffer_3D.position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(ChroBarStaticData._vertexDrawSequence_3D.length*ChroBarStaticData._BYTES_IN_SHORT);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawDirection_3D = rawBuffer.asShortBuffer();
		drawDirection_3D.put(ChroBarStaticData._vertexDrawSequence_3D);
		drawDirection_3D.position(0);
		
		//Initialize the vertex array with default values
		//And get the current window manager
		initVertices();
	}

	/**
	 * 
	 */
	protected void initVertices() {
		
		float _baseHeight = ChroBarStaticData._baseHeight,
					_baseDepth = ChroBarStaticData._baseDepth;
		
		float[] verts_3D = {	 -0.3f,  1.0f,		  0.0f,    		  // Upper Left Front  | 0
					  		  	 -0.3f,  _baseHeight, 0.0f,    		  // Lower Left Front  | 1
					  		  	  0.3f,  _baseHeight, 0.0f,    		  // Lower Right Front | 2
								  0.3f,  1.0f,		  0.0f,    	 	  // Upper Right Front | 3
					  		  	 -0.2f,  1.0f,		  _baseDepth,     // Upper Left Rear   | 4
					  		  	 -0.2f,  _baseHeight, _baseDepth,     // Lower Left Rear   | 5
								  0.4f,  _baseHeight, _baseDepth,     // Lower Right Rear  | 6
								  0.4f,  1.0f,		  _baseDepth  };  // Upper Right Rear  | 7
		
		for(int i = 0; i < ChroBarStaticData._3D_VERTEX_COMPONENTS; i++)
			vertices_3D[i] = verts_3D[i];
	}
	
	/**
	 * 
	 */
	protected void setBarWidth() {

		//Gather required information
		float screenWidth = (float)screen.widthPixels;
		System.out.println("Screen width: " + screenWidth);
		float barTypeCode = (float)barType.getType() - 4;
		float barMargin = barsData.getFloat("barMargin");
		
		//Update the bar margin to 5px ratio of screen width
		barMargin /= screenWidth;
		barMargin *= 2.0f;
		
		//Perform bar width calculations
		int numberOfBars = renderer.numberOfBarsToDraw();
		System.out.println("We are drawing " + numberOfBars + " bars.");
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth *= 2;
		
		barTypeCode -= (ChroBarStaticData._MAX_BARS_TO_DRAW - numberOfBars);
		
		if(barType.getType() < 7)
			for(int i = barType.getType() - 3; i < ChroBarStaticData._MAX_BARS_TO_DRAW; i++) {
				if(!renderer.refreshVisibleBars()[i].isDrawn())
					++barTypeCode;
			}
		else if(barType.getType() < 6)
			for(int j = barType.getType() - 5; j >= 0; j--) {
				if(!renderer.refreshVisibleBars()[j].isDrawn())
					--barTypeCode;
			}
		
		if(barTypeCode < 0)
			barTypeCode = 0;
					
		float leftXCoordinate_3D_front = (barWidth * barTypeCode) + (barMargin * barTypeCode) +
					(((int)barTypeCode) > 0 ? barMargin : 0.0f) - (0.99f + barMargin);
		float leftXCoordinate_3D_rear  = leftXCoordinate_3D_front + barsData.getFloat("bar_3D_offset");
		
		float rightXCoordinate_3D_front = leftXCoordinate_3D_front + barWidth;
		float rightXCoordinate_3D_rear  = rightXCoordinate_3D_front + barsData.getFloat("bar_3D_offset");
		
		System.out.println("Current coords: " + leftXCoordinate_3D_front + ", " +
							leftXCoordinate_3D_rear + ", " + rightXCoordinate_3D_front +
							", " + rightXCoordinate_3D_rear);
		
		vertices_3D[0] = vertices_3D[3]   = leftXCoordinate_3D_front;
		vertices_3D[6] = vertices_3D[9]   = rightXCoordinate_3D_front;
		vertices_3D[12] = vertices_3D[15] = leftXCoordinate_3D_rear;
		vertices_3D[18] = vertices_3D[21] = rightXCoordinate_3D_rear;
	}

	/**
	 * 
	 * @param type
	 */
	protected void adjustBarHeight() {
		
		((WindowManager)barsData.getObject("wm")).getDefaultDisplay().getMetrics(screen);
		
		setBarWidth();
		
		currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT-0800"), Locale.US);
		float scalingFactor = 3.65f;
		float barTopHeight = ChroBarStaticData._baseHeight + 0.01f;

		float timeRatio = getRatio();
		
		//Set the bar height
		vertices_3D[1] = vertices_3D[10] = vertices_3D[13] =
				vertices_3D[22] = barTopHeight + (timeRatio*scalingFactor);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		verticesBuffer_3D.clear();
		verticesBuffer_3D.put(vertices_3D);
		verticesBuffer_3D.position(0);
	}
	
	protected void drawBar(GL10 drawSurface) {

		//Tell openGL where the vertex data is and how to use it
		//System.out.println("Calling glVertexPointer");
		drawSurface.glVertexPointer(ChroBarStaticData._DIMENSIONS, GL10.GL_FLOAT,
									ChroBarStaticData._VERTEX_STRIDE, verticesBuffer_3D);
		
		//System.out.println("Calling glColorPointer");
        drawSurface.glColorPointer(ChroBarStaticData._RGBA_COMPONENTS, GL10.GL_FLOAT,
        							ChroBarStaticData._VERTEX_STRIDE, colorBuffer_3D);
        
		//Draw the bar
        //System.out.println("Calling glDrawElements");
		drawSurface.glDrawElements(GL10.GL_TRIANGLES, ChroBarStaticData._vertexDrawSequence_3D.length,
																GL10.GL_UNSIGNED_SHORT, drawDirection_3D);
	}
	
	/**
	 * Changes the barColor value and those of the vertices.
	 * 
	 * @param colorInt
	 */
	public void changeChroBarColor(int colorInt) {
		
		barColor = colorInt;
		
		int color3DArrayLength = vertexColors_3D.length;
		
		for(int i = 0; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.red(barColor)/255.0f;
		for(int i = 1; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.green(barColor)/255.0f;
		for(int i = 2; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.blue(barColor)/255.0f;
		for(int i = 3; i < color3DArrayLength; i += 4)
			vertexColors_3D[i] = (float)Color.alpha(barColor)/255.0f;
		
		colorBuffer_3D.position(0);
		colorBuffer_3D.put(vertexColors_3D);
		colorBuffer_3D.position(0);
	}
}