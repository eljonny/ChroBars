package com.ampsoft.chrobars.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import com.ampsoft.chrobars.ChroType;

import android.content.Context;
import android.content.SharedPreferences;

public final class ChroBarsSettings {
	
	//General bars application options
	private byte precision;
	private boolean threeD, displayNumbers;
	private boolean[] barsVisibility;
	
	//Colors
	private int backgroundColor;
	private int hourBarColor, minuteBarColor,
					secondBarColor, millisecondBarColor;
	
	//Preferences objects
	private static SharedPreferences chroPrefs;
	private static SharedPreferences.Editor chroPrefsEditor;
	private static final String prefsFile = "chroprefs";
	
	/**
	 * 
	 * @param activityContext
	 * @return
	 */
	public static boolean setPrefsInstance(Context activityContext) {
		
		chroPrefs = activityContext.getSharedPreferences(prefsFile, activityContext.MODE_PRIVATE);
		chroPrefsEditor = chroPrefs.edit();
		
		if(chroPrefs == null)
			return false;
		else
			return true;
	}
	
	/**
	 * 
	 */
	public static void loadSavedPrefs() {
		
		Map<String, ?> prefsMap = chroPrefs.getAll();
		Set<String> preferenceKeys = prefsMap.keySet();
		
		if(prefsMap.isEmpty()) {
			defaultInit();
			return;
		}
		
		for(Field pref : ChroBarsSettings.class.getFields()) {
			if(pref.getClass().isPrimitive()) {
				if(prefsMap.containsKey(pref.getName()))
					pref.set(this, chroPrefs.)
			}
			else
				pref.set(null, chroPrefs.))
		}
	}
	
	/**
	 * Initializes the application with default values.
	 */
	private static void defaultInit() {
		
	}

	public static void setPrefValue(String pref, boolean value) {
		
	}
	
	public static void setPrefValue(String pref, byte value) {
		
	}
	
	public static void setPrefValue(String pref, int value) {
		
	}
	
	public static void setVisibilityPrefValue(ChroType t, boolean visibility) {
		
	}
}
