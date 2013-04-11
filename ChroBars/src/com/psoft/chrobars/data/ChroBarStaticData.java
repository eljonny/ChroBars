package com.psoft.chrobars.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import android.view.WindowManager;

import com.psoft.chrobars.opengl.BarsRenderer;
import com.psoft.chrobars.util.ChroUtils;

/**
 * 
 * @author jhyry
 *
 */
public final class ChroBarStaticData {

	/*
	 * ChroBars static constant application data
	 */
	public static final byte _HOURS_IN_DAY = 24;
	public static final byte _MINUTES_IN_HOUR = 60;
	public static final byte _SECONDS_IN_MINUTE = 60;
	public static final short _MILLIS_IN_SECOND = 1000;
	public static final byte _RGBA_COMPONENTS = 4;
	public static final byte _2D_VERTEX_COMPONENTS = 12;
	public static final byte _3D_VERTEX_COMPONENTS = 24;
	public static final byte _DIMENSIONS = 3;
	public static final byte _BYTES_IN_FLOAT = 4;
	public static final byte _BYTES_IN_SHORT = 2;
	public static final byte _2D_VERTICES = 4;
	public static final byte _3D_VERTICES = 8;
	public static final byte _3D_FACES = 6;
	public static final byte _VERTEX_STRIDE = 0;
	public static final byte _MAX_BARS_TO_DRAW = 4;
	
	//Application default settings, immutable
	public static final byte precision = 0; //No precision. Tick-Tock style.
	public static final byte barEdgeSetting = 2; //Darker edges, since the bars are a bright color.
	public static final byte barMargin = 4; //Medium bar margin. Max is 8
	public static final byte edgeMargin = 4; //Medium edge margin Max is 8
	public static final int backgroundColor = 0x6C6C6C; //Gray background
	public static final int hourBarColor = 0xB7E7FF; //Sky blue hour bar
	public static final int minuteBarColor = 0xFFAF4E; //Tangerine minute bar
	public static final int secondBarColor = 0x9FFF9F; //Bright green second bar
	public static final int millisecondBarColor = 0xFF5757; // Light red millisecond bar
	public static final boolean threeD = true; //Enable 3D
	public static final boolean displayNumbers = true; //Display time numbers
	public static final boolean dynamicLighting = false; //Disable dynamic lighting; very CPU intensive.
	public static final boolean twelveHourTime = true; //Enable 12-hour mode
	public static final boolean[] visibleBars = {true, true, true, false}; //Show h/m/s by default
	public static final boolean[] visibleNumbers = {true, true, true, false}; //Show h/m/s numbers by default
	
	//Base Y-Coordinate from which to draw a ChroBar
	public static final float _baseHeight = -1.8f;
	//Base Z-Coordinate from which to extend a ChroBar into 3D
	public static final float _baseDepth = -0.5f;
	//Other constants
	public static final byte _lighter_edgeColorDifference = 25; //Adds 25 to all components of bar color
	public static final byte _darker_edgeColorDifference = 35; //Subtracts 35 to all components of bar color
	public static final float _max_precision = 3.0f;
	public static final float _left_screen_edge = -1f;
	//Millisecond values
	public static final int _msInDay = ( ChroBarStaticData._HOURS_IN_DAY * ChroBarStaticData._MINUTES_IN_HOUR *
														ChroBarStaticData._SECONDS_IN_MINUTE * ChroBarStaticData._MILLIS_IN_SECOND ),
							   _msInHour = ( ChroBarStaticData._MINUTES_IN_HOUR * ChroBarStaticData.
										 				_SECONDS_IN_MINUTE * ChroBarStaticData._MILLIS_IN_SECOND ),
							   _msInMinute = ( ChroBarStaticData._SECONDS_IN_MINUTE * ChroBarStaticData._MILLIS_IN_SECOND );
	
	//Vertex draw sequences for 2D and 3D, respectively.
	public static final short[] _bar_vertexDrawSequence_2D = {	0, 1, 2,
																	0, 2, 3  };
	
	public static final short[] _bar_vertexDrawSequence_3D = {	0, 4, 5,
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
	
	//Edge draw sequences for 2D and 3D, respectively.
	public static final short[] _edges_vertexDrawSequence_2D = { 0, 1, 1, 2, 2, 3, 3, 0 };	//2D bar
	public static final short[] _edges_vertexDrawSequence_3D = { 0, 1, 1, 2, 2, 3, 3, 0,		//Front side
																	0, 4, 4, 5, 5, 1,			//Left side
																	2, 6, 6, 7,					//Right side
																					  7, 4 };	//Top side
	//GL Light buffer defaults
	public static final float[] //Parameters for light 0
								   _light_0_ambient = {0.05f, 0.05f, 0.05f, 1.0f},
								   _light_0_diffuse = {.1f, .1f, .1f, 1f},
								   _light_0_specular = {1f, 1f, 1f, 1f},
								   _light_0_emission = {0f, 0f, 0f, 1.0f},
								   _light_0_position = {3f, 5.0f, -10.0f, 0.0f},
								   //Parameters for light 1
								   _light_1_ambient = {0.05f, 0.05f, 0.05f, 1.0f},
								   _light_1_diffuse = {.55f, .55f, .55f, 1f},
								   _light_1_specular = {1f, 1f, 1f, 1f},
								   _light_1_emission = {0f, 0f, 0f, 1.0f},
								   _light_1_position = {-2f, -2.0f, 10.0f, 0.0f},
								   //Global light parameters
								   _light_global_ambient = {0.2f, 0.2f, 0.2f, 1.0f},
								   _specular_shininess = {80.0f};
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
	private static float barMarginBase = 5.0f;
	
	/**
	 * Margin, in pixels, from the edge of the screen to the left-most or right-most bar.
	 */
	@SuppressWarnings("unused")
	private static float edgeMarginBase = 5.0f;
	
	/**
	 * Perspective adjustment for rear portion of bar. 
	 * Pushes the rear x-coordinates left (neg value) or right (pos value).
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
	
	/**
	 * Instance object for the data object.
	 */
	private static ChroBarStaticData _inst;
	
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
	public static ChroBarStaticData getNewDataInstance() {
		
		if(_instance) {
			System.err.println("Data class instance already exists. Returning current instance.");
			return _inst;
		}
		else {
			_inst = new ChroBarStaticData();
			return _inst;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static ChroBarStaticData getDataInstance() {
		return _instance ? _inst : getNewDataInstance();
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
