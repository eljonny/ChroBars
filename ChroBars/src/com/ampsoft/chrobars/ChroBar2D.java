package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;

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

}
