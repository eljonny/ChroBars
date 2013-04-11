package com.psoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Color;

import com.psoft.chrobars.data.ChroBarStaticData;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBar2D extends ChroBar {
	
	/**
	 * 
	 * @param t
	 * @param color
	 * @param activityContext
	 */
	public ChroBar2D(ChroType t, Integer color, Context activityContext) {
		//Make sure to pass params to the super constructor, where they are actually used.
		super(t, color, activityContext);
	}

	/**
	 * 
	 */
	@Override
	protected void barGLAllocate(ByteOrder order) {

		//Set 2D vertex arrays
		vertexColors = new float[ChroBarStaticData._2D_VERTICES*ChroBarStaticData._RGBA_COMPONENTS];
		vertices = new float[ChroBarStaticData._2D_VERTEX_COMPONENTS];
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		verticesBuffer = rawBuffer.asFloatBuffer();
		verticesBuffer.put(vertices).position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(ByteOrder.nativeOrder());
		colorBuffer = rawBuffer.asFloatBuffer();
		colorBuffer.put(vertexColors).position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(ChroBarStaticData._vertexDrawSequence_2D.length*ChroBarStaticData._BYTES_IN_SHORT);
		rawBuffer.order(ByteOrder.nativeOrder());
		drawSequence = rawBuffer.asShortBuffer();
		drawSequence.put(ChroBarStaticData._vertexDrawSequence_2D).position(0);
		
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
			vertices[i] = verts_2D[i];
	}

	/**
	 * 
	 */
	@Override
	protected void initNormals() throws Exception {
		throw new Exception("Method not implemented in 2D");
	}
	
	/**
	 * 
	 */
	protected void setBarWidth(float leftX, float rightX) {
		vertices[0] = vertices[3] = leftX;
		vertices[6] = vertices[9] = rightX;
	}

	/**
	 * 
	 */
	protected void setBarHeight(float barTopHeight) {
		vertices[1] = vertices[10] = barTopHeight;
	}
	
	/**
	 * Changes the barColor value and those of the vertices.
	 * 
	 * @param colorInt
	 */
	public void changeChroBarColor(int colorInt) {
		
		barColor = colorInt;
		
		int color2DArrayLength = vertexColors.length;
		
		for(int i = 0; i < color2DArrayLength; i += 4)
			vertexColors[i] = (float)Color.red(barColor)/255.0f;
		for(int i = 1; i < color2DArrayLength; i += 4)
			vertexColors[i] = (float)Color.green(barColor)/255.0f;
		for(int i = 2; i < color2DArrayLength; i += 4)
			vertexColors[i] = (float)Color.blue(barColor)/255.0f;
		for(int i = 3; i < color2DArrayLength; i += 4)
			vertexColors[i] = (float)Color.alpha(barColor)/255.0f;
		
		((FloatBuffer) colorBuffer.clear()).put(vertexColors).position(0);
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
		return drawSequence;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getColorBuffer() {
		return colorBuffer;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getVerticesBuffer() {
		return verticesBuffer;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getNormals() {
		return null;
	}
}
