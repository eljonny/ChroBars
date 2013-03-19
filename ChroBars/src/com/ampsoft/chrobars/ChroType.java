package com.ampsoft.chrobars;

/**
 * 
 * @author jhyry
 *
 */
public enum ChroType {

	MILLIS3D(7),
	SECOND3D(6),
	MINUTE3D(5),
	HOUR3D(4),
	MILLIS(3),
	SECOND(2),
	MINUTE(1),
	HOUR(0);
	
	private final byte MAX_TYPE_VALUE = 7;
	private final byte MIN_TYPE_VALUE = 0;
	
	private int chroType;
	private boolean barIs3D = false;
	
	/**
	 * 
	 * @param type
	 */
	private ChroType(int type) {
		
		if(type < MIN_TYPE_VALUE || type > MAX_TYPE_VALUE) {
			System.err.print("\nEnumeration failed. " +
							type + " is not a valid type.");
			chroType = -1;
		}
		else
			chroType = type;
		if(type > 3)
			barIs3D = true;
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
	public boolean is3D() {
		return barIs3D;
	}
}
