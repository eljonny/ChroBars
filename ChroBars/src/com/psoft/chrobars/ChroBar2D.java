package com.psoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;

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
	protected void barGLAllocate(ByteOrder order_native) {

		//Set 2D vertex arrays
		barVertexColors = new float[ChroBarStaticData._2D_VERTICES*ChroBarStaticData._RGBA_COMPONENTS];
		edgeVertexColors = new float[ChroBarStaticData._2D_VERTICES*ChroBarStaticData._RGBA_COMPONENTS];
		vertices = new float[ChroBarStaticData._2D_VERTEX_COMPONENTS];
		
		//Allocate the vertex buffer
		verticesBuffer = (FloatBuffer) ByteBuffer.allocateDirect(vertices.length*ChroBarStaticData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(vertices).position(0);
		//Allocate the bar's color buffer
		barsColorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(barVertexColors.length*ChroBarStaticData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(barVertexColors).position(0);
		//Allocate the vertex bar draw sequence buffer
		barDrawSequenceBuffer = (ShortBuffer) ByteBuffer.allocateDirect(ChroBarStaticData._bar_vertexDrawSequence_2D.length*ChroBarStaticData._BYTES_IN_SHORT).order(order_native).asShortBuffer().put(ChroBarStaticData._bar_vertexDrawSequence_2D).position(0);
		//Allocate the edges color buffer
		edgesColorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(edgeVertexColors.length*ChroBarStaticData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(edgeVertexColors).position(0);
		//Allocate the vertex edge draw sequence buffer
		edgeDrawSequenceBuffer = (ShortBuffer) ByteBuffer.allocateDirect(ChroBarStaticData._edges_vertexDrawSequence_2D.length*ChroBarStaticData._BYTES_IN_SHORT).order(order_native).asShortBuffer().put(ChroBarStaticData._edges_vertexDrawSequence_2D).position(0);

		System.out.println("Intializing vertices in " + this + "...");
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
	 * 
	 */
	@Override
	protected int getBarDrawSequenceBufferLength() {
		return ChroBarStaticData._bar_vertexDrawSequence_2D.length;
	}

	/**
	 * 
	 */
	@Override
	protected int getEdgeDrawSequenceBufferLength() {
		// TODO Auto-generated method stub
		return ChroBarStaticData._edges_vertexDrawSequence_2D.length;
	}
}
