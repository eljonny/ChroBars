package com.psoft.chrobars;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;

import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.opengl.Vec3D;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBar3D extends ChroBar {
	
	//ArrayLists so we can get their class objects when we calculate the vertex normals.
	//For use when instantiating the generic arrays.
	private ArrayList<Short> alClassShort;
	private ArrayList<Float> alClassFloat;
	private ArrayList<Vec3D> vectorClass;
	private ArrayList<ArrayList<Short>> bucketClass;
	private ArrayList<ArrayList<Vec3D>> alClassVec3D;

	/**
	 * 
	 * @param t
	 * @param color
	 * @param activityContext
	 */
	public ChroBar3D(ChroType t, ArrayList<ChroTexture> texs, Context activityContext) {
		//Make sure to pass params to the super constructor, where they are actually used.
		super(t, texs, activityContext);
	}

	/**
	 * 
	 */
	public void barGLAllocate(ByteOrder order_native) {
		
		//Instantiate empty arraylists to reduce the number of allocations when
		// users turn on dynamic lighting. Will use slightly more memory throughout
		// the application run, but will decrease processor demand.
		alClassShort = new ArrayList<Short>();
		alClassFloat = new ArrayList<Float>();
		vectorClass = new ArrayList<Vec3D>();
		bucketClass = new ArrayList<ArrayList<Short>>();
		alClassVec3D = new ArrayList<ArrayList<Vec3D>>();
		
		//Set 3D vertex arrays
		barVertexColors = new float[ChroData._3D_VERTICES*ChroData._RGBA_COMPONENTS];
		edgeVertexColors = new float[ChroData._3D_VERTICES*ChroData._RGBA_COMPONENTS];
		barVertices = new float[ChroData._3D_VERTEX_COMPONENTS];
		normals = new float[ChroData._3D_VERTEX_COMPONENTS];
		
		//Allocate the vertex buffer
		barVerticesBuffer = (FloatBuffer) ByteBuffer.allocateDirect(barVertices.length*ChroData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(barVertices).position(0);
		//Allocate the color buffer
		barsColorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(barVertexColors.length*ChroData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(barVertexColors).position(0);
		//Allocate the vertex draw sequence buffer
		barDrawSequenceBuffer = (ShortBuffer) ByteBuffer.allocateDirect(ChroData._bar_vertexDrawSequence_3D.length*ChroData._BYTES_IN_SHORT).order(order_native).asShortBuffer().put(ChroData._bar_vertexDrawSequence_3D).position(0);
		//Allocate the edges color buffer
		edgesColorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(edgeVertexColors.length*ChroData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(edgeVertexColors).position(0);
		//Allocate the vertex edge draw sequence buffer
		edgeDrawSequenceBuffer = (ShortBuffer) ByteBuffer.allocateDirect(ChroData._edges_vertexDrawSequence_3D.length*ChroData._BYTES_IN_SHORT).order(order_native).asShortBuffer().put(ChroData._edges_vertexDrawSequence_3D).position(0);
		
		System.out.println("Intializing vertices in " + this + "...");
		//Initialize the vertex array with default values
		initVertices();
		
		System.out.println("Initializing vertex normals in " + this + "...");
		//Initialize the vertex normals.
		initNormals();
		
		//Collect garbage from vertex normals calculation and initialization.
		System.gc();
	
		//Set up the buffer of vertex normal vectors
		normalsBuffer = (FloatBuffer) ByteBuffer.allocateDirect(normals.length*ChroData._BYTES_IN_FLOAT).order(order_native).asFloatBuffer().put(normals).position(0);
	}

	/**
	 * 
	 */
	public void initVertices() {
		
		float _baseHeight = ChroData._baseHeight,
					_baseDepth = ChroData._baseDepth;
		
		float[] verts_3D = {	 -0.3f,  1.0f,		  0.0f,    		  // Upper Left Front  | 0
					  		  	 -0.3f,  _baseHeight, 0.0f,    		  // Lower Left Front  | 1 | Base height is -1.8
					  		  	  0.3f,  _baseHeight, 0.0f,    		  // Lower Right Front | 2
								  0.3f,  1.0f,		  0.0f,    	 	  // Upper Right Front | 3
					  		  	 -0.2f,  1.0f,		  _baseDepth,     // Upper Left Rear   | 4 | Base depth is -.75
					  		  	 -0.2f,  _baseHeight, _baseDepth,     // Lower Left Rear   | 5
								  0.4f,  _baseHeight, _baseDepth,     // Lower Right Rear  | 6
								  0.4f,  1.0f,		  _baseDepth  };  // Upper Right Rear  | 7
		
		for(int i = 0; i < ChroData._3D_VERTEX_COMPONENTS; i++)
			barVertices[i] = verts_3D[i];
	}

	/**
	 * Initializes the vertex normals for the bars.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initNormals() {
		
//		DEBUG
//		System.out.println("Current memory usage:\nTotal allocated heap: " + Runtime.getRuntime().totalMemory() + " Total free: " + Runtime.getRuntime().freeMemory());
		
//		System.out.println("Separating vertices...");
		
		ArrayList<Short>[] drawSequences = (ArrayList<Short>[]) Array.newInstance(alClassShort.getClass(), ChroData._bar_vertexDrawSequence_3D.length/3);
		ArrayList<Float>[] vertexTriples = (ArrayList<Float>[]) Array.newInstance(alClassFloat.getClass(), barVertices.length/3);
		
		int i, j;
		
		//break the vertices up into their respective triples
		//At the same time we can take care of breaking up some draw sequence triples as well.
		for(i = 0; i < barVertices.length/3; i++) {
			ArrayList<Float> vertex = new ArrayList<Float>();
			ArrayList<Short> drawSequence = new ArrayList<Short>();
			for(j = 0; j < 3; j++) {
				vertex.add(barVertices[i*3 + j]);
				drawSequence.add(ChroData._bar_vertexDrawSequence_3D[i*3 + j]);
			}
			drawSequences[i] = drawSequence;
			vertexTriples[i] = vertex;
		}
		
//		System.out.println("Separating draw sequences...");
		
		//break the remaining vertex draw sequences up into their respective triples
		for(; i < drawSequences.length; i++) {
			ArrayList<Short> drawSequence = new ArrayList<Short>();
			for(j = 0; j < 3; j++) {
				drawSequence.add(ChroData._bar_vertexDrawSequence_3D[i*3 + j]);
			}
			drawSequences[i] = drawSequence;
		}
		
//		DEBUG
//		System.out.println("Draw sequences:\n");
//		for(ArrayList<Short> sequence : drawSequences)
//			System.out.println(sequence + "\n");
//		System.out.println("Vertices:\n");
//		for(ArrayList<Float> vertex : vertexTriples)
//			System.out.println(vertex + "\n");
		
//		System.out.println("Done.\nGrouping sequences...");
		
		//Sort the vertex draw sequences into buckets
		ArrayList<ArrayList<Short>>[] buckets = (ArrayList<ArrayList<Short>>[]) Array.newInstance(bucketClass.getClass(), ChroData._3D_VERTICES);
		//Initialize the buckets
		for(i = 0; i < ChroData._3D_VERTICES; i++)
			buckets[i] = new ArrayList<ArrayList<Short>>();
		//Sort the sequences
		for(i = 0; i < drawSequences.length; i++) {
			for(ArrayList<Short> sequence : drawSequences) {
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
//		for(ArrayList<ArrayList<Short>> bckt : buckets)
//			System.out.println(bckt + "\n");
		
//		System.out.println("Done. Building vector objects...");
		
		//For every bucket, examine the draw sequence and drop the vertices into a Vec3D object
		// even though they are not necessarily vectors yet.
		ArrayList<ArrayList<Vec3D>>[] faceVertices = (ArrayList<ArrayList<Vec3D>>[]) Array.newInstance(alClassVec3D.getClass(), ChroData._3D_VERTICES);
		//Initialize the list containers
		for(i = 0; i < faceVertices.length; i++)
			faceVertices[i] = new ArrayList<ArrayList<Vec3D>>();
		for(i = 0; i < buckets.length; i++) {
//			DEBUG
//			System.out.println(buckets[i]);
			for(ArrayList<Short> faceSeq : buckets[i]) {
				Iterator<Short> faceItr = faceSeq.iterator();
				ArrayList<Vec3D> faceVerts = new ArrayList<Vec3D>();
				while(faceItr.hasNext()) {
					faceVerts.add(new Vec3D(vertexTriples[faceItr.next()]));
				}
				faceVertices[i].add(faceVerts);
			}
		}
		
//		DEBUG
//		System.out.println("Detected faces: ");
//		for(ArrayList<ArrayList<Vec3D>> face : faceVertices)
//			System.out.println(face);
		
//		System.out.println("Done. Finding face normals...");
		
		//Calculate normal vectors for all faces connected to each vertex, then average to get a vertex normal.
		ArrayList<Vec3D>[] faceNormals = (ArrayList<Vec3D>[]) Array.newInstance(vectorClass.getClass(), ChroData._3D_VERTICES);
		//Initialize the vector lists for holding all face normals
		for(i = 0; i < faceNormals.length; i++)
			faceNormals[i] = new ArrayList<Vec3D>();
		for(i = 0; i < faceVertices.length; i++) {
			//This calculates the face normal for each face.
			for(ArrayList<Vec3D> face : faceVertices[i]) {
				//Essentially this is equivalent to (B-A) x (C-B)
				faceNormals[i].add(face.get(1).sub(face.get(0)).cross(face.get(2).sub(face.get(1))));
			}
		}
		
//		DEBUG
//		System.out.println("Calculated face normals:");
//		for(ArrayList<Vec3D> norms : faceNormals)
//			System.out.println(norms);
		
		//Now, calculate the average of all face normals for each vertex.
//		System.out.println("Done. Calculating vertex normals...");
		ArrayList<Vec3D> vertexNormals = new ArrayList<Vec3D>();
		for(ArrayList<Vec3D> faceNorms : faceNormals) {
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
	public void setBarWidth(float leftXCoord, float rightXCoord) {
		
		float leftXCoordinate_3D_rear  = leftXCoord + barsData.getFloat("bar_3D_offset");
		float rightXCoordinate_3D_rear  = rightXCoord + barsData.getFloat("bar_3D_offset");
		
//		DEBUG
//		System.out.println("Current coords: " + leftXCoord + ", " +
//							leftXCoordinate_3D_rear + ", " + rightXCoord +
//							", " + rightXCoordinate_3D_rear);
		
		barVertices[0]  = barVertices[3]  = leftXCoord;
		barVertices[6]  = barVertices[9]  = rightXCoord;
		barVertices[12] = barVertices[15] = leftXCoordinate_3D_rear;
		barVertices[18] = barVertices[21] = rightXCoordinate_3D_rear;
	}
	
	/**
	 * 
	 */
	public void setBarHeight(float barTopHeight) {
		barVertices[1] = barVertices[10] = barVertices[13] = barVertices[22] = barTopHeight;
	}

	/**
	 * 
	 */
	@Override
	public int getBarDrawSequenceBufferLength() {
		return ChroData._bar_vertexDrawSequence_3D.length;
	}
	
	/**
	 * 
	 */
	@Override
	public int getEdgeDrawSequenceBufferLength() {
		return ChroData._edges_vertexDrawSequence_3D.length;
	}
}