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

public class ChroBar2D extends ChroBar {
	
	private float[] vertexColors_2D, vertices_2D;
	
	private ShortBuffer drawDirection_2D;
	private FloatBuffer verticesBuffer_2D;
	private FloatBuffer colorBuffer_2D;

	public ChroBar2D(ChroType t, Integer color, Context activityContext) {
		
		super(t, color, activityContext);

		//Set 2D vertex arrays
		vertexColors_2D = new float[_2D_VERTICES*_RGBA_COMPONENTS];
		vertices_2D = new float[_2D_VERTEX_COMPONENTS];
		
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
	}
	
	/**
	 * 
	 */
	protected void initVertices() {
		
		float[] verts_2D = { 	 -0.5f, 1.0f, 		 0.0f,    	// Upper Left  | 0
						  		 -0.5f, _baseHeight, 0.0f,    	// Lower Left  | 1
								  0.5f, _baseHeight, 0.0f,    	// Lower Right | 2
								  0.5f, 1.0f, 		 0.0f  };	// Upper Right | 3
		
		for(int i = 0; i < _2D_VERTEX_COMPONENTS; i++)
			vertices_2D[i] = verts_2D[i];
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
				barTypeCode++;

		float leftXCoordinate_2D = barMargin +
				(barWidth * barTypeCode) + (barMargin * barTypeCode) +
					(((int)barTypeCode) > 0 ? barMargin : 0.0f) - 1.0f;
		float rightXCoordinate_2D = leftXCoordinate_2D + barWidth;
		
		vertices_2D[0] = vertices_2D[3] = leftXCoordinate_2D;
		vertices_2D[6] = vertices_2D[9] = rightXCoordinate_2D;
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
		vertices_2D[1] = vertices_2D[10] = barTopHeight + (timeRatio*scalingFactor);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		verticesBuffer_2D.clear();
		verticesBuffer_2D.put(vertices_2D);
		verticesBuffer_2D.position(0);
	}

	/**
	 * 
	 * @return
	 */
	protected float getRatio(int barType) {
		
		switch(barType) {
		
		case 0:
			return (float)currentTime.get(Calendar.HOUR_OF_DAY)/(float)ChroBarStaticData._HOURS_IN_DAY;
		case 1:
			return (float)currentTime.get(Calendar.MINUTE)/(float)ChroBarStaticData._MINUTES_IN_HOUR;
		case 2:
			return (float)currentTime.get(Calendar.SECOND)/(float)ChroBarStaticData._SECONDS_IN_MINUTE;
		case 3:
			return (float)currentTime.get(Calendar.MILLISECOND)/(float)ChroBarStaticData._MILLIS_IN_SECOND;
			
		default:
			System.err.print("Invalid type!");
			return 0;
		}
	}
	
	protected void drawBar(GL10 drawSurface) {

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
	
	/**
	 * Changes the barColor value and those of the vertices.
	 * 
	 * @param colorInt
	 */
	public void changeChroBarColor(int colorInt) {
		
		barColor = colorInt;
		
		int color2DArrayLength = vertexColors_2D.length;
		
		for(int i = 0; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.red(barColor)/255.0f;
		for(int i = 1; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.green(barColor)/255.0f;
		for(int i = 2; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.blue(barColor)/255.0f;
		for(int i = 3; i < color2DArrayLength; i += 4)
			vertexColors_2D[i] = (float)Color.alpha(barColor)/255.0f;
		
		colorBuffer_2D.position(0);
		colorBuffer_2D.put(vertexColors_2D);
		colorBuffer_2D.position(0);
	}
}
