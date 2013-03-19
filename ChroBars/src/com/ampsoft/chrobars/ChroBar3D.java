package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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
}
