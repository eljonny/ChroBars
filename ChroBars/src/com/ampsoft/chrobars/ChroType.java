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
	
	private final byte MAX_TYPE_VALUE = 3;
	private final byte MIN_TYPE_VALUE = 0;
	
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
}
