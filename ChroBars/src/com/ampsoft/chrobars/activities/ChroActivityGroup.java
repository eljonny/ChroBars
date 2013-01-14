package com.ampsoft.chrobars.activities;

import java.util.ArrayList;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class ChroActivityGroup extends ActivityGroup {
	
	//Intents for starting the other Activities
	private Intent settingsIntent;
	private Intent aboutIntent;
	
	//For managing the child activities
	private static LocalActivityManager activityManager;
	private static ArrayList<Window> activityWindows;
	private static ArrayList<String> activityIds;
	private static Window childWindow;
	private static int _SETTINGS_WINDOW = 0;
	private static int _ABOUT_WINDOW = 1;

	public void onCreate(Bundle savedInstanceState) {
		
		//Tools to manage activities
		activityManager = new LocalActivityManager(this, true);
		activityManager.dispatchCreate(savedInstanceState);
		activityWindows = new ArrayList<Window>(2);
		activityIds = new ArrayList<String>(2);
		
		//For managing the settings and about activities
		setUpChildActivities();
	}

	/**
	 * 
	 */
	private void setUpChildActivities() {
		
		settingsIntent = new Intent(this, ChroBarsSettingsActivity.class);
		aboutIntent = new Intent(this, ChroBarsAboutActivity.class);
		settingsIntent.addCategory(Intent.CATEGORY_PREFERENCE);
		aboutIntent.addCategory(Intent.CATEGORY_DEFAULT);
		activityIds.add(settingsIntent.toString());
		activityIds.add(aboutIntent.toString());
		
		activityWindows.add(activityManager.startActivity(activityIds.get(_SETTINGS_WINDOW), settingsIntent));
		activityWindows.add(activityManager.startActivity(activityIds.get(_ABOUT_WINDOW), aboutIntent));
	}
}
