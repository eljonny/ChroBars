package com.psoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.content.Context;

import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.opengl.ChroTexture;

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
	public ChroBar2D(ChroType t, ArrayList<ChroTexture> texs, Context activityContext) {
		//Make sure to pass params to the super constructor, where they are actually used.
		super(t, texs, activityContext);
	}

	/**
	 * 
	 */
	@Override
	public void barGLAllocate(ByteOrder order_native) {

		//Set 2D vertex arrays
		barVertexColors = new float[ChroData._2D_VERTICES*ChroData._RGBA_COMPONENTS];
		edgeVertexColors = new float[ChroData._2D_VERTICES*ChroData._RGBA_COMPONENTS];
		barVertices = new float[ChroData._2D_VERTEX_COMPONENTS];
		
		//Allocate the vertex buffer
		barVerticesBuffer = (FloatBuffer) ByteBuffer.allocateDirect(barVertices.length*ChroData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(barVertices).position(0);
		//Allocate the bar's color buffer
		barsColorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(barVertexColors.length*ChroData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(barVertexColors).position(0);
		//Allocate the vertex bar draw sequence buffer
		barDrawSequenceBuffer = (ShortBuffer) ByteBuffer.allocateDirect(ChroData._bar_vertexDrawSequence_2D.length*ChroData._BYTES_IN_SHORT).order(order_native).asShortBuffer().put(ChroData._bar_vertexDrawSequence_2D).position(0);
		//Allocate the edges color buffer
		edgesColorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(edgeVertexColors.length*ChroData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(edgeVertexColors).position(0);
		//Allocate the vertex edge draw sequence buffer
		edgeDrawSequenceBuffer = (ShortBuffer) ByteBuffer.allocateDirect(ChroData._edges_vertexDrawSequence_2D.length*ChroData._BYTES_IN_SHORT).order(order_native).asShortBuffer().put(ChroData._edges_vertexDrawSequence_2D).position(0);

		System.out.println("Intializing vertices in " + this + "...");
		//Initialize the vertex array with default values
		initVertices();
	}
	
	/**
	 * 
	 */
	@Override
	public void initVertices() {
		
		float _baseHeight = ChroData._baseHeight;
		
		float[] verts_2D = { 	 -0.5f, 1.0f, 		 0.0f,    	// Upper Left  | 0
						  		 -0.5f, _baseHeight, 0.0f,    	// Lower Left  | 1
								  0.5f, _baseHeight, 0.0f,    	// Lower Right | 2
								  0.5f, 1.0f, 		 0.0f  };	// Upper Right | 3
		
		for(int i = 0; i < ChroData._2D_VERTEX_COMPONENTS; i++)
			barVertices[i] = verts_2D[i];
	}

	/**
	 * 
	 */
	@Override
	public void initNormals() throws Exception {
		throw new Exception("Method not implemented in 2D");
	}
	
	/**
	 * 
	 */
	public void setBarWidth(float leftX, float rightX) {
		barVertices[0] = barVertices[3] = leftX;
		barVertices[6] = barVertices[9] = rightX;
	}

	/**
	 * 
	 */
	public void setBarHeight(float barTopHeight) {
		barVertices[1] = barVertices[10] = barTopHeight;
	}

	/**
	 * 
	 */
	@Override
	public int getBarDrawSequenceBufferLength() {
		return ChroData._bar_vertexDrawSequence_2D.length;
	}

	/**
	 * 
	 */
	@Override
	public int getEdgeDrawSequenceBufferLength() {
		// TODO Auto-generated method stub
		return ChroData._edges_vertexDrawSequence_2D.length;
	}
}
