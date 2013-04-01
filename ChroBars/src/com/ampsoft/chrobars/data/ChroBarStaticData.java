package com.ampsoft.chrobars.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import android.view.WindowManager;

import com.ampsoft.chrobars.opengl.BarsRenderer;
import com.ampsoft.chrobars.util.ChroUtils;

/**
 * 
 * @author jhyry
 *
 */
public final class ChroBarStaticData {

	/*
	 * ChroBars static constant application data
	 */
	public static final int _HOURS_IN_DAY = 24;
	public static final int _MINUTES_IN_HOUR = 60;
	public static final int _SECONDS_IN_MINUTE = 60;
	public static final int _MILLIS_IN_SECOND = 1000;
	public static final int _RGBA_COMPONENTS = 4;
	public static final int _2D_VERTEX_COMPONENTS = 12;
	public static final int _3D_VERTEX_COMPONENTS = 24;
	public static final int _DIMENSIONS = 3;
	public static final int _BYTES_IN_FLOAT = 4;
	public static final int _BYTES_IN_SHORT = 2;
	public static final int _2D_VERTICES = 4;
	public static final int _3D_VERTICES = 8;
	public static final int _VERTEX_STRIDE = 0;
	public static final int _MAX_BARS_TO_DRAW = 4;
	
	//Application default settings, immutable
	public static final byte precision = 0;
	public static final int backgroundColor = 0x6C6C6C;
	public static final int hourBarColor = 0xB7E7FF;
	public static final int minuteBarColor = 0xFFAF4E;
	public static final int secondBarColor = 0x9FFF9F;
	public static final int millisecondBarColor = 0xFF5757;
	public static final boolean threeD = true, displayNumbers = true;
	public static final boolean[] visibleBars = {true, true, true, false};
	
	//Base Y-Coordinate from which to draw a ChroBar
	public static final float _baseHeight = -1.8f;
	//Base Z-Coordinate from which to extend a ChroBar into 3D
	public static final float _baseDepth = -0.75f;
	public static final float _max_precision = 3.0f;
	
	public static final float _msInDay = (float) ( ChroBarStaticData._HOURS_IN_DAY * ChroBarStaticData._MINUTES_IN_HOUR *
														ChroBarStaticData._SECONDS_IN_MINUTE * ChroBarStaticData._MILLIS_IN_SECOND ),
								 _msInHour = (float) ( ChroBarStaticData._MINUTES_IN_HOUR * ChroBarStaticData.
										 				_SECONDS_IN_MINUTE * ChroBarStaticData._MILLIS_IN_SECOND ),
								 _msInMinute = (float) ( ChroBarStaticData._SECONDS_IN_MINUTE * ChroBarStaticData._MILLIS_IN_SECOND );
	
	//Vertex draw sequences for 3D and 2D
	public static final short[] _vertexDrawSequence_2D = {	0, 1, 2,
																0, 2, 3  };
	
	public static final short[] _vertexDrawSequence_3D = {	0, 4, 5,
													           	0, 5, 1,
													           	1, 5, 6,
													           	1, 6, 2,
														        2, 6, 7,
														        2, 7, 3,
														        3, 7, 4,
														        3, 4, 0,
														        4, 7, 6,
														        4, 6, 5,
														        3, 0, 1,
														        3, 1, 2  };
	/*
	 * Chrobars non-final static application data
	 */
	
	/**
	 * Stores the number of bar objects that have been created for this ChroBarsActivity 
	 */
	@SuppressWarnings("unused")
	private static int barsCreated = 0;
	
	/** 
	  * Bar pixel margin and visibility array,
	  * Shared between all bars
	  */
	@SuppressWarnings("unused")
	private static float barMargin = 5.0f;
	
	/**
	 * Perspective adjustment for rear portion of bar
	 */
	@SuppressWarnings("unused")
	private static float bar_3D_offset = 0.1f;
	
	/**
	 * An object reference to the current surface renderer
	 */
	@SuppressWarnings("unused")
	private static BarsRenderer renderer;
	
	/**
	 * Object reference to the current window manager
	 */
	@SuppressWarnings("unused")
	private static WindowManager wm;
	
	/*
	 * End ChroBars application data
	 */
	
	/*
	 * Begin static data Class methods/members
	 */
	
	/**
	 * Where the non-final fields are stored.
	 */
	private static ArrayList<Field> _nonFinalStatic = new ArrayList<Field>();
	
	/**
	 * Default colors are stored here.
	 */
	private static HashMap<String, Integer> _colors;
	
	/**
	 * Singleton control
	 */
	private static Boolean _instance = false;
	
	/**
	 * 
	 * @return
	 */
	public static ChroBarStaticData getDataInstance() {
		
		if(_instance)
			throw new RuntimeException(new IllegalAccessException("There is already an instance of the settings object. Aborting."));
		else
			return new ChroBarStaticData();
	}
	
	/**
	 * 
	 */
	private ChroBarStaticData() {
		updateNonFinalFields();
		_instance = true;
		
		System.out.println("Non-Final Static fields consist of: " + _nonFinalStatic);
	}
	
	/**
	 * 
	 */
	private synchronized void updateNonFinalFields() {
		
		for(Field f : ChroBarStaticData.class.getDeclaredFields())
			if(!Modifier.isFinal(f.getModifiers()))
				if(Modifier.isStatic(f.getModifiers())) {
					try {
						if(!f.getName().startsWith("_"))
							synchronized(f) {
								synchronized(_nonFinalStatic) {
									if(!_nonFinalStatic.contains(f))
										_nonFinalStatic.add(f);
								}
							}
					}
					catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
				}
	}

	/**
	 * 
	 * @param floatFieldName
	 * @return
	 */
	public float getFloat(String floatFieldName) {
		return (Float)getVarFromString(floatFieldName);
	}
	
	/**
	 * 
	 * @param intFieldName
	 * @return
	 */
	public int getInt(String intFieldName) {
		return (Integer)getVarFromString(intFieldName);
	}
	
	/**
	 * 
	 * @param objFieldName
	 * @return
	 */
	public Object getObject(String objFieldName) {
		return getVarFromString(objFieldName);
	}

	/**
	 * 
	 * @param floatFieldName
	 * @return
	 */
	private Object getVarFromString(String floatFieldName) {
		
		try {
			for(Field f : _nonFinalStatic)
				if(f.getName().equals(floatFieldName))
					return f.get(null);
		}
		catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
		
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public static HashMap<String, Integer> getColorDefaults() {
		
		_colors = new HashMap<String, Integer>();
		
		for(Field data : ChroBarStaticData.class.getFields())
			if(data.getName().endsWith("Color")) {
				try { _colors.put(data.getName(), data.getInt(null)); }
				catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
			}
		
		return _colors;
	}
	
	/**
	 * 
	 */
	public synchronized void modifyIntegerField(String intName, int modBy) {
		
		for(Field f : _nonFinalStatic) {
			if(f.getClass().isPrimitive()) {
				if(f.getName().equals(intName)) {
					
					try {
						synchronized(f) {
							f.setInt(null, f.getInt(null) + modBy);
						}
					}
					catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
				}
			}
		}
	}
	
	/**
	 * 
	 * @param objFieldName
	 * @param ref
	 */
	public synchronized <T> void setObjectReference(String objFieldName, T ref) {
		
		for(Field f : _nonFinalStatic) {
			if(f.getName().equals(objFieldName)) {
				try {
					synchronized(f) {
						System.out.println("Setting field " + objFieldName + " to " + ref);
						f.set(null, ref);
					}
				}
				catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
			}
		}
	}
}
