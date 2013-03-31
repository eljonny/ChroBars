package com.ampsoft.chrobars.util;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.view.View;

import com.ampsoft.chrobars.ChroBar;

/**
 * 
 * @author jhyry
 *
 */
public final class ChroUtils {
	
	private static ArrayList<Integer> colorPickerHistory = new ArrayList<Integer>();
	
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

	/**
	 * Adds the color chosen for a ChroBar to the color picker history buffer.
	 * This feature is not fully implemented, but should be in the future to
	 *  make it easier to choose a color someone previously chose before.
	 * 
	 * @param color
	 * @return
	 */
	public static void barColorChosen(int color) {
		colorPickerHistory.add(color);
	}
	
	/**
	 * 
	 * @return
	 */
	public static Integer getLastColorPickerChoice() {
		
		if(colorPickerHistory.isEmpty())
			return null;
		else
			return colorPickerHistory.get(colorPickerHistory.size()-1);
	}
	
	/**
	 * 
	 * @param historyIndex
	 * @return
	 */
	public static int getColorPickerHistoryItem(int historyIndex) {
		return colorPickerHistory.get(historyIndex);
	}
	
	/**
	 * 
	 * @param color
	 * @return
	 */
	public static Integer getColorPickerHistoryColor(int color) {
		
			int historyItemIndex = colorPickerHistory.indexOf(color);
			
			if(historyItemIndex == -1)
				return null;
			else
				return colorPickerHistory.get(historyItemIndex);
	}
	
	
	public static void setViewBackground(Activity activity, int color) {
	    
		View activityView1 = activity.getWindow().getDecorView();
	    activityView1.setBackgroundColor(color);
	}
	
	/**
	 * 
	 * @param ex
	 */
	public static void printExDetails(Exception ex) {
		
		System.out.println(ex.getClass().getCanonicalName() + " occurred:\n" +
				ex.getMessage() + "\n\nCause: " + ex.getCause() + "\n\nTrace:\n");
		
		ex.printStackTrace();
	}

	/**
	 * Builds a semantically-correct variable name from a ChroBar type.
	 * 
	 * @param bar A ChroBar of which to build a color variable string.
	 * @return The string representing the bar's color storage field.
	 */
	public static String getChroBarColorVarString(ChroBar bar) {
		
		final String postFix = "BarColor";
		return bar.getBarType().getTypeString() + postFix;
	}
}
