package com.ampsoft.chrobars.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.ampsoft.chrobars.ChroType;
import com.ampsoft.chrobars.data.ChroBarStaticData;

public final class ChroBarsSettings {
	
	//General bars application options
	private byte precision;
	private boolean threeD, displayNumbers;
	private ArrayList<Boolean> barsVisibility =
			new ArrayList<Boolean>(ChroBarStaticData._MAX_BARS_TO_DRAW*2);
	
	//Colors
	private int backgroundColor;
	private int hourBarColor, minuteBarColor,
					secondBarColor, millisecondBarColor;
	
	//User Default Colors
	private int userDefault_backgroundColor,
				  userDefault_hourBarColor,
				  userDefault_minuteBarColor,
				  userDefault_secondBarColor,
				  userDefault_millisecondBarColor;
	
	//Preferences objects
	private static SharedPreferences chroPrefs;
	private static SharedPreferences.Editor chroPrefsEditor;
	private static final String prefsFile = "chroprefs";
	private static Boolean instance = false;
	
	/**
	 * 
	 * @param activityContext
	 * @return
	 */
	public static ChroBarsSettings getNewSettingsInstance(Context activityContext) {
		
		if(instance)
			throw new RuntimeException(new IllegalAccessException("There is already an instance of the settings object. Aborting."));
		else
			return new ChroBarsSettings(activityContext);
	}
	
	/**
	 * 
	 * @param activityContext
	 * @return
	 */
	private ChroBarsSettings(Context aC) {
		initSettings(aC);
		loadSavedPrefs();
		instance = true;
	}
	
	/**
	 * 
	 * @param activityContext
	 * @return
	 */
	private void initSettings(Context activityContext) {

		chroPrefs = activityContext.getSharedPreferences(prefsFile, Context.MODE_PRIVATE);
		chroPrefsEditor = chroPrefs.edit();
	}

	/**
	 * 
	 */
	private void loadSavedPrefs() {
		
		Map<String, ?> prefsMap = chroPrefs.getAll();
		Set<String> preferenceKeys = prefsMap.keySet();
		
		if(prefsMap.isEmpty()) {
			defaultInit();
			return;
		}
		
		loadUserDefaults(preferenceKeys, prefsMap);
		
		for(Field pref : ChroBarsSettings.class.getFields()) {
			if(pref.getName().startsWith("user"))
				continue;
			for(String fieldName : preferenceKeys)
				if(fieldName.equals(pref.getName())) {
					try { pref.set(null, prefsMap.get(fieldName)); }
					catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
				}
		}
	}
	
	private void loadUserDefaults(Set<String> preferenceKeys, Map<String, ?> prefsMap) {
		
		Iterator<String> keyItr = preferenceKeys.iterator();
		
		while(keyItr.hasNext())
			if(keyItr.next().startsWith("user")) {
					for(String key : preferenceKeys)
						if(key.startsWith("user")) {
							try { ChroBarsSettings.class.getField(key).set(this, prefsMap.get(key)); }
							catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
						}
				break;
			}
	}

	/**
	 * Initializes the application with default values.
	 */
	private void defaultInit() {
		
		setGeneralDefaultSettings();
		setColorDefaults();
		putDefaults();
	}

	/**
	 * 
	 */
	private void setGeneralDefaultSettings() {
		
		precision = ChroBarStaticData.precision;
		threeD = ChroBarStaticData.threeD;
		displayNumbers = ChroBarStaticData.displayNumbers;
		for(int visIndex = 0; visIndex < ChroBarStaticData.visibleBars.length*2; visIndex++)
			barsVisibility.set(visIndex < ChroBarStaticData.visibleBars.length ? visIndex : visIndex - ChroBarStaticData._MAX_BARS_TO_DRAW,
								ChroBarStaticData.visibleBars[visIndex < ChroBarStaticData.visibleBars.length ? visIndex : visIndex - ChroBarStaticData._MAX_BARS_TO_DRAW]);
		barsVisibility.trimToSize();
	}

	/**
	 * 
	 */
	private void setColorDefaults() {
		
		backgroundColor 	= userDefault_backgroundColor 		= ChroBarStaticData.backgroundColor;
		hourBarColor 		= userDefault_hourBarColor 			= ChroBarStaticData.hourBarColor;
		minuteBarColor 		= userDefault_minuteBarColor 		= ChroBarStaticData.minuteBarColor;
		secondBarColor 		= userDefault_secondBarColor		= ChroBarStaticData.secondBarColor;
		millisecondBarColor = userDefault_millisecondBarColor	= ChroBarStaticData.millisecondBarColor;
	}

	/**
	 * 
	 */
	private void putDefaults() {
		
		putPreference("precision", precision);
		putPreference("threeD", threeD);
		putPreference("displayNumbers", displayNumbers);
		
		for(int barVis = 0; barVis < barsVisibility.size(); barVis++)
			putPreference("barsVisibility_" + barVis, barsVisibility.get(barVis));
		
		HashMap<String, Integer> defaultColors = ChroBarStaticData.getColorDefaults();
		
		for(String key : defaultColors.keySet()) {
			putPreference(key, defaultColors.get(key));
			putPreference("userDefault_" + key, defaultColors.get(key));
		}
	}

	private void putPreference(String prefName, boolean val) {
		
		chroPrefsEditor.putBoolean(prefName, val);
		chroPrefsEditor.commit();
	}

	private void putPreference(String prefName, int val) {
		
		chroPrefsEditor.putInt(prefName, val);
		chroPrefsEditor.commit();
	}

	/**
	 * @return the precision
	 */
	public final byte getPrecision() {
		return precision;
	}

	/**
	 * @return the threeD
	 */
	public final boolean isThreeD() {
		return threeD;
	}

	/**
	 * @return the displayNumbers
	 */
	public final boolean isDisplayNumbers() {
		return displayNumbers;
	}

	/**
	 * @return the barsVisibility
	 */
	public final ArrayList<Boolean> getBarsVisibility() {
		System.gc();
		return new ArrayList<Boolean>(barsVisibility);
	}

	/**
	 * @return the backgroundColor
	 */
	public final int getBackgroundColor(boolean userDefault) {
		return userDefault ? userDefault_backgroundColor : backgroundColor;
	}

	/**
	 * @return the hourBarColor
	 */
	public final int getHourBarColor(boolean userDefault) {
		return userDefault ? userDefault_hourBarColor : hourBarColor;
	}

	/**
	 * @return the minuteBarColor
	 */
	public final int getMinuteBarColor(boolean userDefault) {
		return userDefault ? userDefault_minuteBarColor : minuteBarColor;
	}

	/**
	 * @return the secondBarColor
	 */
	public final int getSecondBarColor(boolean userDefault) {
		return userDefault ? userDefault_secondBarColor : secondBarColor;
	}

	/**
	 * @return the millisecondBarColor
	 */
	public final int getMillisecondBarColor(boolean userDefault) {
		return userDefault ? userDefault_millisecondBarColor : millisecondBarColor;
	}

	/**
	 * 
	 * @param pref
	 * @param value
	 */
	public final void setPrefValue(String pref, boolean value) {
		
		if(pref.startsWith("instance"))
			throw new RuntimeException(new IllegalArgumentException("Illegal modification request."));
		else {
			try { findField(pref).setBoolean(this, value); }
			catch(Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
		}
		
		putPreference(pref, value);
	}

	/**
	 * 
	 * @param pref
	 * @param value
	 */
	public final void setPrefValue(String pref, byte value) {

		try { findField(pref).setByte(this, value); }
		catch(Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
		
		putPreference(pref, value);
	}
	
	/**
	 * 
	 * @param pref
	 * @param value
	 */
	public final void setPrefValue(String pref, int value) {

		try { findField(pref).setInt(this, value); }
		catch(Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
		
		putPreference(pref, value);
	}
	
	/**
	 * 
	 * @param t
	 * @param visibility
	 */
	public final void setVisibilityPrefValue(ChroType t, boolean visibility) {

		barsVisibility.set(t.getType(), visibility);
		
		putPreference("barsVisibility_" + t.getType(), visibility);
	}
	
	/**
	 * 
	 * @param pref
	 */
	private Field findField(String pref) throws NullPointerException {

		for(Field setting : this.getClass().getFields())
			if(setting.getName().equals(pref))
				return setting;
		
		throw new NullPointerException("The specified preference does not exist.");
	}
}
