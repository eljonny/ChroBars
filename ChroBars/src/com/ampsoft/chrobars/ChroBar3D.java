package com.ampsoft.chrobars;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Color;
import android.view.WindowManager;

import com.ampsoft.chrobars.data.ChroBarStaticData;
import com.ampsoft.chrobars.opengl.Vec3D;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBar3D extends ChroBar {
	
	private float[] vertexColors_3D, vertices_3D;
	private static float[] normals = null;
	
	private ShortBuffer drawDirection_3D;
	private FloatBuffer verticesBuffer_3D;
	private FloatBuffer colorBuffer_3D;
	private static FloatBuffer normalsBuffer;

	/**
	 * 
	 * @param t
	 * @param color
	 * @param activityContext
	 */
	public ChroBar3D(ChroType t, Integer color, Context activityContext) {
		
		super(t, color, activityContext);
		
		ByteOrder order_native = ByteOrder.nativeOrder();
		
		//Set 3D vertex arrays
		vertexColors_3D = new float[ChroBarStaticData._3D_VERTICES*ChroBarStaticData._RGBA_COMPONENTS];
		vertices_3D = new float[ChroBarStaticData._3D_VERTEX_COMPONENTS];
		normals = new float[ChroBarStaticData._3D_VERTEX_COMPONENTS];
		
		//Allocate the raw vertex buffer
		rawBuffer = ByteBuffer.allocateDirect(vertices_3D.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(order_native);
		verticesBuffer_3D = rawBuffer.asFloatBuffer();
		verticesBuffer_3D.put(vertices_3D).position(0);
		
		//Allocate the raw color buffer
		rawBuffer = ByteBuffer.allocateDirect(vertexColors_3D.length*ChroBarStaticData._BYTES_IN_FLOAT);
		rawBuffer.order(order_native);
		colorBuffer_3D = rawBuffer.asFloatBuffer();
		colorBuffer_3D.put(vertexColors_3D).position(0);
		
		//Allocate the vertex draw sequence buffer
		rawBuffer = ByteBuffer.allocateDirect(ChroBarStaticData._vertexDrawSequence_3D.length*ChroBarStaticData._BYTES_IN_SHORT);
		rawBuffer.order(order_native);
		drawDirection_3D = rawBuffer.asShortBuffer();
		drawDirection_3D.put(ChroBarStaticData._vertexDrawSequence_3D).position(0);
		
		System.out.println("Intializing vertices...");
		//Initialize the vertex array with default values
		initVertices();
		//Initialize the vertex normals for lighting.
//		if(normalsBuffer == null) {
//			
//			System.out.println("Initializing static vertex normals...");
//			initNormals();
//		
//			//Set up the buffer of normal vectors
//			rawBuffer = ByteBuffer.allocateDirect(normals.length*ChroBarStaticData._BYTES_IN_FLOAT);
//			rawBuffer.order(order_native);
//			normalsBuffer = rawBuffer.asFloatBuffer();
//			normalsBuffer.put(normals).position(0);
//		}
	}

	/**
	 * 
	 */
	protected void initVertices() {
		
		float _baseHeight = ChroBarStaticData._baseHeight,
					_baseDepth = ChroBarStaticData._baseDepth;
		
		float[] verts_3D = {	 -0.3f,  1.0f,		  0.0f,    		  // Upper Left Front  | 0
					  		  	 -0.3f,  _baseHeight, 0.0f,    		  // Lower Left Front  | 1 | Base height is -1.8
					  		  	  0.3f,  _baseHeight, 0.0f,    		  // Lower Right Front | 2
								  0.3f,  1.0f,		  0.0f,    	 	  // Upper Right Front | 3
					  		  	 -0.2f,  1.0f,		  _baseDepth,     // Upper Left Rear   | 4 | Base depth is -.75
					  		  	 -0.2f,  _baseHeight, _baseDepth,     // Lower Left Rear   | 5
								  0.4f,  _baseHeight, _baseDepth,     // Lower Right Rear  | 6
								  0.4f,  1.0f,		  _baseDepth  };  // Upper Right Rear  | 7
		
		for(int i = 0; i < ChroBarStaticData._3D_VERTEX_COMPONENTS; i++)
			vertices_3D[i] = verts_3D[i];
	}

	/**
	 * Initializes the vertex normals for the bars.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void initNormals() {
		
//		DEBUG
//		System.out.println("Current memory usage:\nTotal allocated heap: " + Runtime.getRuntime().totalMemory() + " Total free: " + Runtime.getRuntime().freeMemory());
		
		System.out.println("Separating vertices...");
		
		//Instantiate arrays of LinkedLists
		LinkedList<Short> alClassShort = new LinkedList<Short>(); LinkedList<Float> alClassFloat = new LinkedList<Float>(); LinkedList<Vec3D> f = new LinkedList<Vec3D>();
		LinkedList<LinkedList<Short>> bucket = new LinkedList<LinkedList<Short>>(); LinkedList<LinkedList<Vec3D>> alClassVec3D = new LinkedList<LinkedList<Vec3D>>();
		
		LinkedList<Short>[] drawSequences = (LinkedList<Short>[]) Array.newInstance(alClassShort.getClass(), ChroBarStaticData._vertexDrawSequence_3D.length/3);
		LinkedList<Float>[] vertexTriples = (LinkedList<Float>[]) Array.newInstance(alClassFloat.getClass(), vertices_3D.length/3);
		
		int i, j;
		
		//break the vertices up into their respective triples
		//At the same time we can take care of breaking up some draw sequence triples as well.
		for(i = 0; i < vertices_3D.length/3; i++) {
			LinkedList<Float> vertex = new LinkedList<Float>();
			LinkedList<Short> drawSequence = new LinkedList<Short>();
			for(j = 0; j < 3; j++) {
				vertex.add(vertices_3D[i*3 + j]);
				drawSequence.add(ChroBarStaticData._vertexDrawSequence_3D[i*3 + j]);
			}
			drawSequences[i] = drawSequence;
			vertexTriples[i] = vertex;
		}
		
		System.out.println("Separating draw sequences...");
		
		//break the remaining vertex draw sequences up into their respective triples
		for(; i < drawSequences.length; i++) {
			LinkedList<Short> drawSequence = new LinkedList<Short>();
			for(j = 0; j < 3; j++) {
				drawSequence.add(ChroBarStaticData._vertexDrawSequence_3D[i*3 + j]);
			}
			drawSequences[i] = drawSequence;
		}
		
//		DEBUG
//		System.out.println("Draw sequences:\n");
//		for(LinkedList<Short> sequence : drawSequences)
//			System.out.println(sequence + "\n");
//		System.out.println("Vertices:\n");
//		for(LinkedList<Float> vertex : vertexTriples)
//			System.out.println(vertex + "\n");
		
		System.out.println("Done.\nGrouping sequences...");
		
		//Sort the vertex draw sequences into buckets
		LinkedList<LinkedList<Short>>[] buckets = (LinkedList<LinkedList<Short>>[]) Array.newInstance(bucket.getClass(), ChroBarStaticData._3D_VERTICES);
		//Initialize the buckets
		for(i = 0; i < ChroBarStaticData._3D_VERTICES; i++)
			buckets[i] = new LinkedList<LinkedList<Short>>();
		//Sort the sequences
		for(i = 0; i < drawSequences.length; i++) {
			for(LinkedList<Short> sequence : drawSequences) {
				for(Short sequencePiece : sequence) {
					if(i == sequencePiece.shortValue()) {
						buckets[i].add(sequence);
						break;
					}
				}
			}
		}

//		DEBUG
//		System.out.println("Buckets:\n");
//		for(LinkedList<LinkedList<Short>> bckt : buckets)
//			System.out.println(bckt + "\n");
		
		System.out.println("Done. Building vector objects...");
		
		//For every bucket, examine the draw sequence and drop the vertices into a Vec3D object
		// even though they are not necessarily vectors yet.
		LinkedList<LinkedList<Vec3D>>[] faceVertices = (LinkedList<LinkedList<Vec3D>>[]) Array.newInstance(alClassVec3D.getClass(), ChroBarStaticData._3D_VERTICES);
		//Initialize the list containers
		for(i = 0; i < faceVertices.length; i++)
			faceVertices[i] = new LinkedList<LinkedList<Vec3D>>();
		for(i = 0; i < buckets.length; i++) {
			System.out.println(buckets[i]);
			for(LinkedList<Short> faceSeq : buckets[i]) {
				Iterator<Short> faceItr = faceSeq.iterator();
				LinkedList<Vec3D> faceVerts = new LinkedList<Vec3D>();
				while(faceItr.hasNext()) {
					faceVerts.add(new Vec3D(vertexTriples[faceItr.next()]));
				}
				faceVertices[i].add(faceVerts);
			}
		}
		
//		DEBUG
//		System.out.println("Detected faces: ");
//		for(LinkedList<LinkedList<Vec3D>> face : faceVertices)
//			System.out.println(face);
		
		System.out.println("Done. Finding face normals...");
		
		//Calculate normal vectors for all faces connected to each vertex, then average to get a vertex normal.
		LinkedList<Vec3D>[] faceNormals = (LinkedList<Vec3D>[]) Array.newInstance(f.getClass(), ChroBarStaticData._3D_VERTICES);
		//Initialize the vector lists for holding all face normals
		for(i = 0; i < faceNormals.length; i++)
			faceNormals[i] = new LinkedList<Vec3D>();
		for(i = 0; i < faceVertices.length; i++) {
			//This calculates the face normal for each face.
			for(LinkedList<Vec3D> face : faceVertices[i]) {
				//Essentially this is equivalent to (B-A) x (C-B)
				faceNormals[i].add(face.get(1).sub(face.getFirst()).cross(face.getLast().sub(face.get(1))));
			}
		}
		
//		DEBUG
//		System.out.println("Calculated face normals:");
//		for(LinkedList<Vec3D> norms : faceNormals)
//			System.out.println(norms);
		
		//Now, calculate the average of all face normals for each vertex.
		System.out.println("Done. Calculating vertex normals...");
		LinkedList<Vec3D> vertexNormals = new LinkedList<Vec3D>();
		for(LinkedList<Vec3D> faceNorms : faceNormals) {
			vertexNormals.add(Vec3D.average(faceNorms));
		}
		
//		DEBUG
//		System.out.println("Calculated vertex normals:");
//		for(Vec3D vertexNormal : vertexNormals)
//			System.out.println(vertexNormal);
		
		//Finally! Populate the normals array for use with the OpenGL lighting engine.
		i = 0;
		for(Vec3D vertexNormal : vertexNormals) {
			float[] normal = vertexNormal.asArray();
			for(j = 0; j < normal.length; j++) {
				normals[i*3 + j] = normal[j];
			}
			i++;
		}
		
//		DEBUG
//		System.out.println("Current memory usage:\nTotal allocated heap: " + Runtime.getRuntime().totalMemory() + " Total free: " + Runtime.getRuntime().freeMemory());
	}
	
	/**
	 * 
	 */
	protected void setBarWidth() {

		//Gather required information
		float screenWidth = (float)screen.widthPixels;
		//System.out.println("Screen width: " + screenWidth);
		float barTypeCode = (float)barType.getType() - 4;
		float barMargin = barsData.getFloat("barMargin");
		
		//Update the bar margin to 5px ratio of screen width
		barMargin /= screenWidth;
		barMargin *= 2.0f;
		
		//Perform bar width calculations
		int numberOfBars = renderer.numberOfBarsToDraw();
		//System.out.println("We are drawing " + numberOfBars + " bars.");
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth -= barMargin;
		barWidth *= 2f;
		
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
					
		float leftXCoordinate_3D_front = (barWidth * barTypeCode) + (barMargin * barTypeCode) - (0.95f + barMargin);
		float leftXCoordinate_3D_rear  = leftXCoordinate_3D_front + barsData.getFloat("bar_3D_offset");
		
		float rightXCoordinate_3D_front = leftXCoordinate_3D_front + barWidth;
		float rightXCoordinate_3D_rear  = rightXCoordinate_3D_front + barsData.getFloat("bar_3D_offset");
		
		//System.out.println("Current coords: " + leftXCoordinate_3D_front + ", " +
		//					leftXCoordinate_3D_rear + ", " + rightXCoordinate_3D_front +
		//					", " + rightXCoordinate_3D_rear);
		
		vertices_3D[0]  = vertices_3D[3]  = leftXCoordinate_3D_front;
		vertices_3D[6]  = vertices_3D[9]  = rightXCoordinate_3D_front;
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
		((FloatBuffer) verticesBuffer_3D.clear()).put(vertices_3D).position(0);
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
		
		((FloatBuffer) colorBuffer_3D.clear()).put(vertexColors_3D).position(0);
	}

	/**
	 * 
	 */
	@Override
	protected int getDrawSequenceBufferLength() {
		return ChroBarStaticData._vertexDrawSequence_3D.length;
	}

	/**
	 * 
	 */
	@Override
	protected ShortBuffer getDrawDirectionBuffer() {
		return drawDirection_3D;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getColorBuffer() {
		return colorBuffer_3D;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getVerticesBuffer() {
		return verticesBuffer_3D;
	}

	/**
	 * 
	 */
	@Override
	protected FloatBuffer getNormals() {
		return normalsBuffer;
	}
}