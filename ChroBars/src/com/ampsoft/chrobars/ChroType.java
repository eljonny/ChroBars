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
	private final byte DIMENSIONS_X_VERTICES = 12;
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
		//Format<POSITION>(X,Y,Z): UL, LL, LR, UR
		if(type < MIN_TYPE_VALUE || type > MAX_TYPE_VALUE) {
			System.err.print("\nEnumeration failed. " +
							type + " is not a valid type.");
			chroType = -1;
		}
		else {

			chroType = type;
			float[] verts = { -0.5f, 1.0f, 0.0f,  // Upper Left  | 0
					  		  -0.5f, -0.9f, 0.0f, // Lower Left  | 1
							  0.5f, -0.9f, 0.0f,  // Lower Right | 2
							  0.5f, 1.0f, 0.0f  };// Upper Right | 3
			
			for(int i = 0; i < DIMENSIONS_X_VERTICES; i++)
				vertices[i] = verts[i];
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
	
	public void setTypeVertices(float[] verts, int[] indices) {
		
		for(int index : indices)
			vertices[index] = verts[index];
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
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		
		String description;
		int i = 0, limit = 3;
		
		description = "\nType: " + chroType + "\nVertices:"
				+ "\nUpper Left:\t( ";
		
		for(;i < limit;i++) {
			description += vertices[i];
			if(i + 1 < limit)
				description += ", ";
		}
		
		limit += 3;
		description += ")\nLower Left:\t( ";
		
		for(;i < limit;i++){
			description += vertices[i];
			if(i + 1 < limit)
				description += ", ";
		}

		limit += 3;
		description += ")\nLower Right:\t( ";
		
		for(;i < limit;i++) {
			description += vertices[i];
			if(i + 1 < limit)
				description += ", ";
		}

		limit += 3;
		description += ")\nUpper Right:\t( ";
		
		for(;i < limit;i++) {
			description += vertices[i];
			if(i + 1 < limit)
				description += ", ";
		}
		
		description += ")\n";
		
		return description;
	}
}
