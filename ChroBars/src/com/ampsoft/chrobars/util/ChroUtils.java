package com.ampsoft.chrobars.util;

import java.util.Calendar;

import com.ampsoft.chrobars.ChroBar;

/**
 * 
 * @author jhyry
 *
 */
public class ChroUtils {

	/**
	 * 
	 * @return
	 */
	public static String getTimeString() {
		
		return (Calendar.YEAR + "-" + Calendar.MONTH + "-" +
				Calendar.DAY_OF_MONTH + "{" + Calendar.HOUR +
				":" + Calendar.MINUTE + ":" + Calendar.SECOND +
				"." + Calendar.MILLISECOND + "}");
	}
	
	/**
	 * 
	 * @param toChange
	 * @param colorInt
	 */
	public static void changeChroBarColor(ChroBar toChange, Integer colorInt) {
		toChange.changeChroBarColor(colorInt);
	}
	
	/**
	 * 
	 * @param toChange
	 * @param colorString
	 */
	public static void changeChroBarColor(ChroBar toChange, String colorString) {
		toChange.changeChroBarColor(colorString);
	}
	
	/**
	 * 
	 * @param toChange
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 */
	public static void changeChroBarColor(ChroBar toChange,
											int alpha, int red,
											int green, int blue) {
		toChange.changeChroBarColor(alpha, red, green, blue);
	}
}
