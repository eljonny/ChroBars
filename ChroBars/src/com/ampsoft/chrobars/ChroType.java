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
	
	private final static byte MAX_TYPE_VALUE = 7;
	private final static byte MIN_TYPE_VALUE = 0;
	
	private int chroType;
	
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
	public static int types() {
		return MAX_TYPE_VALUE + 1;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean is3D() {
		return chroType > 3;
	}
	
	/**
	 * @return A string decribing the properties of this ChroType enum 
	 * 				value of the form "type-{type}_3D-{is3d?}".
	 */
	@Override
	public String toString() {
		
		return "type-" + chroType + "_3D-" + (chroType > 3);
	}
}
