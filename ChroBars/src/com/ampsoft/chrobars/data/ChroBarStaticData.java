package com.ampsoft.chrobars.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import android.view.WindowManager;

import com.ampsoft.chrobars.opengl.BarsRenderer;

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
	
	//Base Y-Coordinate from which to draw a ChroBar
	public static final float _baseHeight = -1.8f;
	//Base Z-Coordinate from which to extend a ChroBar into 3D
	public static final float _baseDepth = -0.75f;
	
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
	private static int barsCreated = 0;
	
	/** 
	  * Bar pixel margin and visibility array,
	  * Shared between all bars
	  */
	private static float barMargin = 5.0f;
	
	/**
	 * Perspective adjustment for rear portion of bar
	 */
	private static float bar_3D_offset = 0.1f;
	
	/**
	 * An object reference to the current surface renderer
	 */
	private static BarsRenderer renderer;
	
	/**
	 * Object reference to the current window manager
	 */
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
	private ArrayList<Field> nonFinalStatic = new ArrayList<Field>();
	
	/**
	 * 
	 */
	public ChroBarStaticData() {
		updateNonFinalFields();
	}
	
	/**
	 * 
	 */
	private synchronized void updateNonFinalFields() {
		
		for(Field f : ChroBarStaticData.class.getFields())
			if(!Modifier.isFinal(f.getModifiers()))
				if(Modifier.isStatic(f.getModifiers()))
					if(!nonFinalStatic.contains(f))
						synchronized(nonFinalStatic) {
							nonFinalStatic.add(f);
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
			if(nonFinalStatic.contains(this.getClass().getField(floatFieldName)))
				return this.getClass().getField(floatFieldName).get(null);
		}
		catch (Exception unknownEx) { printExDetails(unknownEx); }
		
		return null;
	}
	
	/**
	 * 
	 */
	public synchronized void modifyIntegerField(String intName, int incBy) {
		
		for(Field f : nonFinalStatic) {
			if(f.getClass().isPrimitive()) {
				if(f.getName().equals(intName)) {
					
					try {
						synchronized(f) {
							f.setInt(null, f.getInt(null) + incBy);
						}
					}
					catch (Exception unknownEx) { printExDetails(unknownEx); }
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
		
		for(Field f : nonFinalStatic) {
			if(f.getClass().isPrimitive())
				continue;
			if(f.getName().equals(objFieldName) && f.getType().getClass().
					 getSimpleName().equals(ref.getClass().getSimpleName())) {
				
				try {
					synchronized(f) {
						f.set(null, ref);
					}
				}
				catch (Exception unknownEx) { printExDetails(unknownEx); }
			}
		}
	}
	
	/**
	 * 
	 * @param ex
	 */
	private void printExDetails(Exception ex) {
		
		System.out.println(ex.getClass().getCanonicalName() + " occurred:\n" +
				ex.getMessage() + "\n\nCause: " + ex.getCause() + "\n\nTrace:\n");
		
		ex.printStackTrace();
	}
}
