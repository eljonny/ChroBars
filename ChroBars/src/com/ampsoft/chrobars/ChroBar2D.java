package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Color;
import android.view.WindowManager;

import com.ampsoft.chrobars.data.ChroBarStaticData;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBar2D extends ChroBar {
	
	private float[] vertexColors_2D, vertices_2D;
	
	private ShortBuffer drawDirection_2D;
	private FloatBuffer verticesBuffer_2D;
	private FloatBuffer colorBuffer_2D;
	
	/**
	 * 
	 * @param t
	 * @param color
	 * @param activityContext
	 */
	public ChroBar2D(ChroType t, Integer color, Context activityContext) {
		
		super(t, color, activityContext);

		//Set 2D vertex arrays
		vertexColors_2D = new float[ChroBarStaticData._2D_VERTICES*ChroBarStaticData._RGBA_COMPONENTS];
		vertices_2D = new float[ChroBarStaticData._2D_VERTEX_COMPONENTS];
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices_2D.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer_2D = rawBuffer.asFloatBuffer();
		verticesBuffer_2D.put(vertices_2D).position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors_2D.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer_2D = rawBuffer.asFloatBuffer();
		colorBuffer_2D.put(vertexColors_2D).position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(ChroBarStaticData._vertexDrawSequence_2D.length*ChroBarStaticData._BYTES_IN_SHORT);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawDirection_2D = rawBuffer.asShortBuffer();
		drawDirection_2D.put(ChroBarStaticData._vertexDrawSequence_2D).position(0);
		
		//Initialize the vertex array with default values
		initVertices();
	}
	
	/**
	 * 
	 */
	@Override
	protected void initVertices() {
		
		float _baseHeight = ChroBarStaticData._baseHeight;
		
		float[] verts_2D = { 	 -0.5f, 1.0f, 		 0.0f,    	// Upper Left  | 0
						  		 -0.5f, _baseHeight, 0.0f,    	// Lower Left  | 1
								  0.5f, _baseHeight, 0.0f,    	// Lower Right | 2
								  0.5f, 1.0f, 		 0.0f  };	// Upper Right | 3
		
		for(int i = 0; i < ChroBarStaticData._2D_VERTEX_COMPONENTS; i++)
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
		float barMargin = barsData.getFloat("barMarginBase");
		float edgeMargin = barsData.getFloat("edgeMarginBase");
		
		//Update the bar margin to default ratio of screen width
		barMargin /= screenWidth;
		barMargin *= 2.0f;
		barMargin *= renderer.getBarMarginScalar();
		//Update the edge margin to current screen pixels
		edgeMargin /= screenWidth; //Get the edge margin as a ratio to the entire screen width
		edgeMargin *= 2f; //Normalize the edge margin to cartesian coordinates.
		edgeMargin *= renderer.getEdgeMarginScalar(); //Scale the margin to the correct size.
		
		//Perform bar width calculations
		int numberOfBars = renderer.numberOfBarsToDraw();
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth *= 2f;
		barWidth -= ((edgeMargin*2f)/(float)numberOfBars);
		barWidth -= ((barMargin*((float)numberOfBars-1f))/(float)numberOfBars);
		
		barTypeCode -= (ChroBarStaticData._MAX_BARS_TO_DRAW - numberOfBars);
		
		for(int i = barType.getType() + 1; i < ChroBarStaticData._MAX_BARS_TO_DRAW; i++)
			if(!renderer.refreshVisibleBars()[i].isDrawn())
				++barTypeCode;
		
		if(barTypeCode < 0)
			barTypeCode = 0;

		float leftXCoordinate_2D =  ChroBarStaticData._left_screen_edge + edgeMargin + (barWidth * barTypeCode) + (barMargin * barTypeCode);
		float rightXCoordinate_2D = leftXCoordinate_2D + barWidth;
		
		vertices_2D[0] = vertices_2D[3] = leftXCoordinate_2D;
		vertices_2D[6] = vertices_2D[9] = rightXCoordinate_2D;
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
		vertices_2D[1] = vertices_2D[10] = barTopHeight + (timeRatio*scalingFactor);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		((FloatBuffer) verticesBuffer_2D.clear()).put(vertices_2D).position(0);
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
		
		((FloatBuffer) colorBuffer_2D.clear()).put(vertexColors_2D).position(0);
	}

	/**
	 * 
	 */
	@Override
	protected int getDrawSequenceBufferLength() {
		return ChroBarStaticData._vertexDrawSequence_2D.length;
	}

	/**
	 * 
	 */
	@Override
	protected ShortBuffer getDrawDirectionBuffer() {
		return drawDirection_2D;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getColorBuffer() {
		return colorBuffer_2D;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getVerticesBuffer() {
		return verticesBuffer_2D;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getNormals() {
		return null;
	}

	@Override
	protected void initNormals() throws Exception {
		throw new Exception("Method not implemented in 2D");
	}
}
