package com.ampsoft.chrobars.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.ampsoft.chrobars.ChroType;
import com.ampsoft.chrobars.data.ChroBarStaticData;

/**
 * 
 * @author jhyry
 *
 */
public final class ChroBarsSettings {
	
	//General bars application options
	private int precision;
	private boolean threeD, displayNumbers;
	private ArrayList<Boolean> barsVisibility;
	
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
	private static final String userDef = "user";
	private static final String visList = "barsVisibility";
	private static final int visSize = ChroBarStaticData._MAX_BARS_TO_DRAW*2;
	private static Boolean instance = false;
	private static ArrayList<Field> memberFields = new ArrayList<Field>();
	private static Context instanceActivityContext;
	private static ChroBarsSettings instanceObject;
	
	/**
	 * 
	 * @param activityContext
	 * @return
	 */
	public static final ChroBarsSettings getNewSettingsInstance(Context activityContext) {
		
		if(instance)
			throw new RuntimeException(new IllegalAccessException("There is already an instance of the settings object. Aborting."));
		else
			return new ChroBarsSettings(activityContext);
	}
	
	public static final ChroBarsSettings getInstance(Context aC) {
		if(aC.equals(instanceActivityContext))
			return instanceObject;
		else
			return null;
	}
	
	/**
	 * 
	 * @param activityContext
	 * @return
	 */
	private ChroBarsSettings(Context aC) {
		
		memberFields.clear();
		
		barsVisibility = new ArrayList<Boolean>(visSize);
		barsVisibility.ensureCapacity(visSize);
		
		for(int i = visSize; i > 0; i--)
			barsVisibility.add(false);
		
		for(Field setting : ChroBarsSettings.class.getDeclaredFields())
			if(!Modifier.isFinal(setting.getModifiers()))
				if(!Modifier.isStatic(setting.getModifiers()))
					memberFields.add(setting);
		
		initSettings(aC);
		loadSavedPrefs();
		instance = true;
		instanceObject = this;
	}
	
	/**
	 * 
	 * @param activityContext
	 * @return
	 */
	private void initSettings(Context activityContext) {

		instanceActivityContext = activityContext;
		chroPrefs = activityContext.getSharedPreferences(prefsFile, Context.MODE_PRIVATE);
		chroPrefsEditor = chroPrefs.edit();
	}

	/**
	 * 
	 */
	private void loadSavedPrefs() {
		
		setBarVisibilityDefaults();
		
		Map<String, ?> prefsMap = chroPrefs.getAll();
		Set<String> preferenceKeys = prefsMap.keySet();
		
		Iterator<String> keyz = preferenceKeys.iterator();
		String tempKey;
		
		if(prefsMap.isEmpty()) {
			defaultInit();
			return;
		}
		
		loadUserDefaults(preferenceKeys, prefsMap);
		
		for(Field pref : memberFields) {
			
			System.out.println("Loading preference " + pref.getName());
			
			if(pref.getName().startsWith(userDef))
				continue;
			if(pref.getName().startsWith(visList)) {
				while(keyz.hasNext()) {
					
					tempKey = keyz.next();
					
					//If we have a visibility preference key,
					// parse the key and set the value.
					if(tempKey.startsWith(visList)) {
						System.out.println("Parsing saved item " + tempKey);
						String[] parsed = tempKey.split("_");
						barsVisibility.set(Integer.parseInt(parsed[1]), (Boolean) prefsMap.get(tempKey));
					}
				}
			}
			
			for(String prefKey : preferenceKeys)
				if(prefKey.equals(pref.getName())) {
					try { pref.set(this, prefsMap.get(prefKey)); }
					catch (Exception unknownEx) {
						System.out.println("Exception processing field " + pref.getName());
						ChroUtils.printExDetails(unknownEx);
					}
					break;
				}
		}
	}
	
	/**
	 * 
	 * @param preferenceKeys
	 * @param prefsMap
	 */
	private void loadUserDefaults(Set<String> preferenceKeys, Map<String, ?> prefsMap) {

		for(String key : preferenceKeys)
			if(key.startsWith(userDef)) {
				try {
					for(Field pref : memberFields)
						if(pref.getName().equals(key))
						{
							pref.set(this, prefsMap.get(key));
							break;
						}
				}
				catch (Exception unknownEx) { ChroUtils.printExDetails(unknownEx); }
			}
	}

	/**
	 * Initializes the application with default values.
	 */
	private void defaultInit() {
		
		setGeneralDefaultSettings();
		setBarVisibilityDefaults();
		setColorDefaults();
		putDefaults();
	}

	/**
	 * 
	 */
	private void setBarVisibilityDefaults() {
		
		System.out.println("Setting visibilities back to defaults...");
		
		for(int visIndex = 0; visIndex < visSize; visIndex++)
			barsVisibility.set(visIndex, ChroBarStaticData.visibleBars[visIndex < 4 ? visIndex : visIndex - 4]);
	}

	/**
	 * 
	 */
	private void setGeneralDefaultSettings() {
		
		precision = ChroBarStaticData.precision;
		threeD = ChroBarStaticData.threeD;
		displayNumbers = ChroBarStaticData.displayNumbers;
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
	public final int getPrecision() {
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
	 * Returns blue if you provide an invalid ChroType value.
	 * 
	 * @return The bar color corresponding to the specified ChroType.
	 */
	public final int getBarColor(ChroType t, boolean userDefault) {
		switch(t.getType() < 4 ? t.getType() : t.getType() - 4) {
		case 0:
			return getHourBarColor(userDefault);
		case 1:
			return getMinuteBarColor(userDefault);
		case 2:
			return getSecondBarColor(userDefault);
		case 3:
			return getMillisecondBarColor(userDefault);
		default:
			return Color.BLUE;
		}
	}

	/**
	 * @return the hourBarColor
	 */
	private final int getHourBarColor(boolean userDefault) {
		return userDefault ? userDefault_hourBarColor : hourBarColor;
	}

	/**
	 * @return the minuteBarColor
	 */
	private final int getMinuteBarColor(boolean userDefault) {
		return userDefault ? userDefault_minuteBarColor : minuteBarColor;
	}

	/**
	 * @return the secondBarColor
	 */
	private final int getSecondBarColor(boolean userDefault) {
		return userDefault ? userDefault_secondBarColor : secondBarColor;
	}

	/**
	 * @return the millisecondBarColor
	 */
	private final int getMillisecondBarColor(boolean userDefault) {
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

		System.out.println("Setting Visibility for bar type " + t + " to " + visibility + ".");
		System.out.println("Visibility list def: " + barsVisibility);
		barsVisibility.set(t.getType(), visibility);
		
		putPreference("barsVisibility_" + t.getType(), visibility);
	}
	
	/**
	 * 
	 * @param pref
	 */
	private Field findField(String pref) throws NullPointerException {

		for(Field setting : memberFields)
			if(setting.getName().equals(pref))
				return setting;
		
		throw new NullPointerException("The specified preference does not exist.");
	}
}
