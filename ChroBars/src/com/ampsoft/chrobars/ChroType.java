package com.ampsoft.chrobars;

/**
 * 
 * @author jhyry
 *
 */
public enum ChroType {

	MILLIS(3),
	SECOND(2),
	MINUTE(1),
	HOUR(0);

	private final byte DIMENSIONS = 3;
	private final byte VERTICES = 4;
	private final byte DIMENSIONS_X_VERTICES = DIMENSIONS*VERTICES;
	private final byte MAX_TYPE_VALUE = 3;
	private final byte MIN_TYPE_VALUE = 0;
	
	private int chroType;
	
	private float[] vertices = new float[DIMENSIONS_X_VERTICES];
	
	/**
	 * 
	 * @param type
	 */
	private ChroType(int type) {
		
		//Build the vertices container.
		//Format<POSITION>(X,Y,Z): UL, UR, LR, LL
		if(type < MIN_TYPE_VALUE || type > MAX_TYPE_VALUE) {
			System.err.print("\nEnumeration failed. " +
							type + " is not a valid type.");
			chroType = -1;
		}
		else {

			float ftype = chroType = type;
			float[] verts = { 0.0f + ftype, 1.0f, 0.0f,
							  1.0f + ftype, 1.0f, 0.0f,
							  1.0f + ftype, 0.0f, 0.0f,
							  0.0f + ftype, 0.0f, 0.0f  };
			
			System.arraycopy(verts, 0, vertices, 0, DIMENSIONS_X_VERTICES);
		}
	}
	
	/*
	 * Getter methods for enum properties.
	 */
	
	/**
	 * 
	 * @return
	 */
	public float[] getTypeVertices() {
		return vertices;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getType() {
		return chroType;
	}
	
	/**
	 * 
	 * @return
	 */
	public byte getDimensions() {
		return DIMENSIONS;
	}
	
	/**
	 * 
	 * @return
	 */
	public byte getVertices() {
		return VERTICES;
	}
}
