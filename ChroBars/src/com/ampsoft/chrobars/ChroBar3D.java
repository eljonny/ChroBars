package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;

public class ChroBar3D extends ChroBar {
	
	private float[] vertexColors_3D, vertices_3D;
	
	private ShortBuffer drawDirection_3D;
	private FloatBuffer verticesBuffer_3D;
	private FloatBuffer colorBuffer_3D;

	public ChroBar3D(ChroType t, Integer color, Context activityContext) {
		
		super(t, color, activityContext);
		
		//Set 3D vertex arrays
		vertexColors_3D = new float[_3D_VERTICES*_RGBA_COMPONENTS];
		vertices_3D = new float[_3D_VERTEX_COMPONENTS];
		
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
	}

	/**
	 * 
	 */
	protected void initVertices() {
		
		float[] verts_3D = {	 -0.3f,  1.0f,		  0.0f,    		  // Upper Left Front  | 0
					  		  	 -0.3f,  _baseHeight, 0.0f,    		  // Lower Left Front  | 1
					  		  	  0.3f,  _baseHeight, 0.0f,    		  // Lower Right Front | 2
								  0.3f,  1.0f,		  0.0f,    	 	  // Upper Right Front | 3
					  		  	 -0.2f,  1.0f,		  _baseDepth,     // Upper Left Rear   | 4
					  		  	 -0.2f,  _baseHeight, _baseDepth,     // Lower Left Rear   | 5
								  0.4f,  _baseHeight, _baseDepth,     // Lower Right Rear  | 6
								  0.4f,  1.0f,		  _baseDepth  };  // Upper Right Rear  | 7
		
		for(int i = 0; i < _3D_VERTEX_COMPONENTS; i++)
			vertices_3D[i] = verts_3D[i];
	}
	
	/**
	 * 
	 * @param screenMetrics
	 * @param verts2 
	 */
	protected void setBarWidth() {

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
				barTypeCode++;ssssssssssss
			
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

	/**
	 * 
	 * @param type
	 */
	protected void adjustBarHeight() {
		
		wm.getDefaultDisplay().getMetrics(screen);
		
		setBarWidth();
		
		currentTime = Calendar.getInstance(TimeZone.getTimeZone("GMT-0800"), Locale.US);
		float scalingFactor = 3.65f;
		float barTopHeight = ChroBar._baseHeight + 0.01f;

		float timeRatio = getRatio();
		
		//Set the bar height
		vertices_3D[1] = vertices_3D[10] = vertices_3D[13] =
				vertices_3D[22] = barTopHeight + (timeRatio*scalingFactor);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		verticesBuffer_3D.clear();
		verticesBuffer_3D.put(vertices_3D);
		verticesBuffer_3D.position(0);
	}
}
