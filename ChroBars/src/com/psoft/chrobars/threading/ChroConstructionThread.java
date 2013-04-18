package com.psoft.chrobars.threading;

import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.data.ChroConstructionParams;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.util.ChroBarsSettings;
import com.psoft.chrobars.util.ChroUtils;

import android.os.AsyncTask;
import android.os.Looper;

/**
 * This defines a background task for constructing the 
 *  necessary data and objects required to run the application.
 *  
 * @author jon
 */
public class ChroConstructionThread extends
				AsyncTask< ChroConstructionParams,
						   Integer,
						   ChroConstructionParams > {

	//For holding the parameter/data return object.
	private static ChroConstructionParams paramsData;
	//Parameter object contents temp storage for processing.
	private static ChroBarsActivity UIThread;
	private static ChroBarsSettings settings;
	private static ChroSurface time;
	//Progress cache of the startup operation.
	private static Integer progress;
	
	/**
	 * The background task that allows the application to run.
	 * All necessary surface objects and settings are instantiated/
	 *  loaded through this worker thread.
	 * 
	 * @param paramObjs The array of parameter objects.
	 */
	public ChroConstructionParams doInBackground(ChroConstructionParams... paramObjs) {
		
		//Wait for the main view to attach to the window manager.
		synchronized(this) {
			try {
				Thread.sleep(650);
			}
			catch(InterruptedException intEx) {
				ChroUtils.printExDetails(intEx);
			}
		}
		
		progress = 0; //Initial progress is 0%
		paramsData = paramObjs[0]; //Set the parameter object.
		UIThread = paramsData.getMainActivity(); //Set the main Activity reference.
		getSettingsInstance();
		//Create the GLSurfaceView and set it as the content view
		buildSurface(); //Load settings into the OpenGL renderer.
		setReturnData();
		
		return paramsData;
    }

	/**
	 * When we are finished with our construction tasks,
	 *  we need to reset the data in the parameter object for 
	 *  transfer back to the main thread.
	 */
	private void setReturnData() {
		paramsData.setRenderSurface(time); //Set surface reference to send back to main Activity.
		incProgress(5); //progress is now 95%
		paramsData.setSettings(settings); //Set settings reference to send back to main Activity.
		incProgress(5); //progress is now 100%
	}

	/**
	 * Here, we put together the surface and renderer then load the settings
	 *  into the renderer.
	 */
	private void buildSurface() {
		//This is necessary to give this thread control of the GLSurfaceView creation process.
		Looper.prepare();
		incProgress(17);
		time = new ChroSurface(UIThread); //Construct the surface and bars.
		incProgress(14);
		time.setSettingsInstance(settings); //Load the application settings into the renderer.
		incProgress(44);
	}

	/**
	 * Trys moderately hard to get either a new instance of 
	 * 	ChroBarsSettings or get an existing instance.
	 */
	private void getSettingsInstance() {

		incProgress(5); //progress is now 5%
		
		try {
			//Load a settings instance, which in turn loads the settings into ChroBars.
//			DEBUG
//			System.out.println("Getting new settings instance...");
			settings = ChroBarsSettings.getNewSettingsInstance(UIThread);
			incProgress(10); //progress is now 15%
		}
		catch(Exception unknownEx) {
			//Something went wrong that can possibly be corrected by attempting
			// to get the current settings instance.
			ChroUtils.printExDetails(unknownEx);
			incProgress(2); //progress is now 7%
			System.out.println("Trying to get existing settings instance...");
			settings = ChroBarsSettings.getInstance(UIThread);
			incProgress(3); //progress is now 10%
			if(settings != null) {
				System.out.println("Existing settings instance retrieved.");
				incProgress(5); //progress is now 15%
			}
			//If this happens, we have no access to the program settings,
			// thus we cannot continue past this point.
			// TODO in the future make this handle this case without closing the application.
			else
				throw new NullPointerException("Cannot continue. The settings object is null.");
		}
	}

	/**
	 * Increments the progress then publishes it.
	 *  Publishing the progress will fire a progress 
	 *  update event that will be caught by an onProgressUpdate listener.
	 * 
	 * @param incBy The amount to increment progress by.
	 */
	private void incProgress(int incBy) {
		progress += incBy;
//		System.out.println("Publishing progress: " + progress);
		publishProgress(progress);
	}

	/**
	 * Updates the progress parameter in the main application Activity.
	 * 
	 * @param progress The current background task progress.
	 */
    protected void onProgressUpdate(Integer... progress) {
        UIThread.setProgressPercent(progress[0]);
    }
}
