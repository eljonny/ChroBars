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
	
	private int chroType;
	
	/**
	 * 
	 * @param type
	 */
	private ChroType(int type) {
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
	public boolean is3D() {
		return chroType > 3;
	}
	
	public String getTypeString() {
		switch(is3D() ? chroType - 4 : chroType) {
		case 0:
			return "hour";
		case 1:
			return "minute";
		case 2:
			return "second";
		case 3:
			return "millisecond";
		default:
			return "unknown";
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static ChroType[] types() {
		return values();
	}
	
	public static ChroType valueByNumber(int typeCode)
							throws IllegalArgumentException {
		
		for(ChroType t : values())
			if(t.getType() == typeCode)
				return t;
		
		throw new IllegalArgumentException("The type code " + typeCode + " is not a valid enum constant in " + ChroType.class.getCanonicalName());
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
