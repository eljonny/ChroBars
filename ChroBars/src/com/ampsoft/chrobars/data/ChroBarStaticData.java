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
	  * Bar pixel margin and visibility array,
	  * Shared between all bars
	  */
	private static float barMargin = 5.0f;
	
	/** 
	  * First column of
	  * visible array is actual bar visibility,
	  * where the second column is whether or not
	  * to draw the bar in 3D; Using the settings
	  * screen, enabling/disabling 3D affects both
	 */
	private static boolean[] visible = new boolean[_MAX_BARS_TO_DRAW];
	
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
	
	/**
	 * Stores the number of bar objects that have been created for this ChroBarsActivity 
	 */
	private static int barsCreated = 0;
	
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
	private void updateNonFinalFields() {
		
		nonFinalStatic.clear();
		
		for(Field f : ChroBarStaticData.class.getFields())
			if(!Modifier.isFinal(f.getModifiers()))
				if(Modifier.isStatic(f.getModifiers()))
					nonFinalStatic.add(f);
	}

	/**
	 * 
	 * @param floatFieldName
	 * @return
	 */
	public float getNonFinalFloat(String floatFieldName) {
		return (Float)getVarFromString(floatFieldName);
	}
	
	/**
	 * 
	 * @param boolArFieldName
	 * @return
	 */
	public boolean[] getNonFinalBooleanArray(String boolArFieldName) {
		return (boolean[])getVarFromString(boolArFieldName);
	}
	
	/**
	 * 
	 * @param intFieldName
	 * @return
	 */
	public int getNonFinalInt(String intFieldName) {
		return (Integer)getVarFromString(intFieldName);
	}
	
	/**
	 * 
	 * @param objFieldName
	 * @return
	 */
	public Object getNonFinalObject(String objFieldName) {
		return getVarFromString(objFieldName);
	}
	
	/**
	 * 
	 */
	public void incIntegerField(String intName) {
		
		for(Field f : nonFinalStatic) {
			if(f.getClass().isPrimitive()) {
				if(f.getName().equals(intName)) {
					
					try {
						f.setInt(null, f.getInt(null) + 1);
					}
					catch (IllegalArgumentException illegalArgEx) {
						
						System.out.println("A IllegalArgumentException occurred:\n" +
											illegalArgEx.getMessage() + "\n\nCause: " + illegalArgEx.getCause());
						
						illegalArgEx.printStackTrace();
					}
					catch (IllegalAccessException illegalAccessEx) {
						
						System.out.println("A IllegalAccessException occurred:\n" +
											illegalAccessEx.getMessage() + "\n\nCause: " + illegalAccessEx.getCause());
						
						illegalAccessEx.printStackTrace();
					}
					catch (Exception unknownEx) {
						
						System.out.println("An unknown exception occurred:\n" +
											unknownEx.getMessage() + "\n\nCause: " +
											unknownEx.getCause());
						
						unknownEx.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param objFieldName
	 * @param ref
	 */
	public <T> void setObjectReference(String objFieldName, T ref) {
		
		for(Field f : nonFinalStatic) {
			if(f.getClass().isPrimitive())
				continue;
			if(f.getName().equals(objFieldName) && f.getType().getClass().
					 getSimpleName().equals(ref.getClass().getSimpleName())) {
				
				try {
					f.set(null, ref);
				}
				catch (IllegalArgumentException illegalArgEx) {
					
					System.out.println("A IllegalArgumentException occurred:\n" +
										illegalArgEx.getMessage() + "\n\nCause: " + illegalArgEx.getCause());
					
					illegalArgEx.printStackTrace();
				}
				catch (IllegalAccessException illegalAccessEx) {
					
					System.out.println("A IllegalAccessException occurred:\n" +
										illegalAccessEx.getMessage() + "\n\nCause: " + illegalAccessEx.getCause());
					
					illegalAccessEx.printStackTrace();
				}
				catch (Exception unknownEx) {

					System.out.println("An unknown exception occurred:\n" +
										unknownEx.getMessage() + "\n\nCause: " +
										unknownEx.getCause());
					
					unknownEx.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param floatFieldName
	 * @return
	 */
	private Object getVarFromString(String floatFieldName) throws NullPointerException {
		
		try {
			if(nonFinalStatic.contains(this.getClass().getField(floatFieldName)))
				return this.getClass().getField(floatFieldName).get(null);
		}
		catch (SecurityException secEx) {
		
			System.out.println("A SecurityException occurred:\n" +
								secEx.getMessage() + "\n\nCause: " + secEx.getCause());
			
			secEx.printStackTrace();
		}
		catch (NoSuchFieldException nsfEx) {
			
			System.out.println("A NoSuchFieldException occurred:\n" +
								nsfEx.getMessage() + "\n\nCause: " + nsfEx.getCause());
			
			nsfEx.printStackTrace();
		}
		catch (IllegalArgumentException illegalArgEx) {
			
			System.out.println("A IllegalArgumentException occurred:\n" +
								illegalArgEx.getMessage() + "\n\nCause: " + illegalArgEx.getCause());
			
			illegalArgEx.printStackTrace();
		}
		catch (IllegalAccessException illegalAccessEx) {
			
			System.out.println("A IllegalAccessException occurred:\n" +
								illegalAccessEx.getMessage() + "\n\nCause: " + illegalAccessEx.getCause());
			
			illegalAccessEx.printStackTrace();
		}
		catch (Exception unknownEx) {
			
			System.out.println("An unknown exception occurred:\n" +
								unknownEx.getMessage() + "\n\nCause: " +
								unknownEx.getCause());
			
			unknownEx.printStackTrace();
		}
		
		throw new NullPointerException("The specified field does not exist.");
	}
}
