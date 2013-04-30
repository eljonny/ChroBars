package com.psoft.chrobars.threading;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;

import com.psoft.chrobars.ChroType;
import com.psoft.chrobars.R;
import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.data.ChroConstructionParams;
import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.opengl.ChroTextures;
import com.psoft.chrobars.settings.ChroBarsSettings;
import com.psoft.chrobars.util.ChroPrint;
import com.psoft.chrobars.util.ChroUtilities;

/**
 * This defines a background task for constructing the 
 *  necessary data and objects required to run the application.
 *  
 * @author Jonathan Hyry
 * 
 * @see android.graphics.Bitmap
 * @see android.graphics.BitmapFactory
 * @see android.os.AsyncTask
 * @see android.os.Looper
 * @see java.lang.Thread
 */
public class ChroConstructionThread extends AsyncTask<ChroConstructionParams,
														 Integer,
														 ChroConstructionParams>
									  implements IChroLoadThread {

	/**
	 * For holding the parameter/data return object.
	 */
	private static ChroConstructionParams paramsData;
	/**
	 * Parameter object contents temp storage for processing.
	 */
	private static ChroBarsActivity UIThread;
	/**
	 * Parameter object contents temp storage for processing.
	 */
	private static ChroBarsSettings settings;
	/**
	 * Parameter object contents temp storage for processing.
	 */
	private static ChroSurface time;
	/**
	 * Progress cache of the startup operation.
	 */
	private static Integer progress;
	/**
	 * Texture cache for decoded/loaded textures.
	 */
	private static ArrayList<ChroTexture> textures;
	
	/**
	 * The background task that allows the application to run.
	 * All necessary surface objects and settings are instantiated/
	 *  loaded through this worker thread.
	 * 
	 * @param paramObjs The array of parameter objects.
	 * 
	 * @see com.psoft.ChroConstructionParams
	 */
	public ChroConstructionParams doInBackground(ChroConstructionParams... paramObjs) {
		
		wmAttachWait();
		prepareConstruction(paramObjs);
		construct();
		postConstruction();
		
		return paramsData;
    }

	/**
	 * Wait for the main view to attach to the window manager.
	 */
	private void wmAttachWait() {
		synchronized(this) {
			try { Thread.sleep(650); }
			catch(InterruptedException intEx) { ChroUtilities.printExDetails(intEx); }
		}
	}

	/**
	 * Post-construction phase
	 */
	private void postConstruction() {
		setReturnData();
		incProgress(2);
		Looper.myLooper().quit();
		incProgress(2);
	}

	/**
	 * Construction phase
	 */
	private void construct() {
		incProgress(2);
		getSettingsInstance();
		incProgress(2);
		cacheTextures();
		incProgress(5);
		//Create the GLSurfaceView and set it as the content view
		buildSurface(); //Load settings into the OpenGL renderer.
		incProgress(4);
	}

	/**
	 * Pre-construction phase
	 * 
	 * @param paramObjs The ChroConstructionParams object, which will hold values to return to the main thread.
	 */
	private void prepareConstruction(ChroConstructionParams... paramObjs) {
		progress = 0; //Initial progress is 0%
		paramsData = paramObjs[0]; //Set the parameter object.
		UIThread = paramsData.getMainActivity(); //Set the main Activity reference.
		//This is necessary to give this thread control of the GLSurfaceView creation process.
		if(Looper.myLooper() == null) {
			ChroPrint.println("Currently no Looper. Preparing looper...", System.out);
			Looper.prepare();
			ChroPrint.println("Done.", System.out);
		}
		else
			ChroPrint.println("Will use current looper for " + Thread.currentThread() + "...", System.out);
	}

	/**
	 * Caches all the required texture resources so they are available when we build the GLSurfaceView.
	 *  The magic numbers here aren't really magic. They increment the loading progress.
	 *  If I become aware of a better way to do this, I will change it.
	 */
	private void cacheTextures() {
		
		ArrayList<Field> imgResIds = findImgResIds();
		ArrayList<ChroTexture> cache = ChroTextures.cache();
		
		if(checkMemoryForCache(imgResIds, cache))
			return;
		
		LinkedList<Thread> cachingThreads = initCache(imgResIds, cache);
		
		final BitmapFactory.Options bmpLoadOptions = setBitmapOptions();
		
//		DEBUG
//		ChroPrint.println("New max progress: " + ChroData._max_prog, System.out);
//		ChroPrint.println("Textures to cache: " + imgResIds.size(), System.out);
		
		cacheGraphics(imgResIds, cachingThreads, bmpLoadOptions);
		
		ChroPrint.println("Done caching needed textures.", System.out);
	}

	/**
	 * @param imgResIds
	 * @param cachingThreads
	 * @param bmpLoadOptions
	 */
	private void cacheGraphics(ArrayList<Field> imgResIds,
								LinkedList<Thread> cachingThreads,
								final BitmapFactory.Options bmpLoadOptions) {
		
		//Get the application's resources object reference.
		Resources chroRes = UIThread.getResources();
		//Get the initial numbersVisibility from the settings object.
		ArrayList<Boolean> numbersVis = settings.getNumbersVisibility();
		ArrayList<Field> resids = new ArrayList<Field>();
		
		ChroPrint.println("Caching graphics...", System.out);
		
		//Pore through the fields, load the corresponding images.
		for(Field image : imgResIds) {
			ChroTexture tex = decodeAndProcessResource(bmpLoadOptions, chroRes, numbersVis, image);
			cacheTextureOrContinue(cachingThreads, image, tex, resids);
		}
		
		updateProgressMaximum((ChroData._max_prog - ((imgResIds.size() - resids.size()) * 2)));
		cacheJobsWait(cachingThreads);
	}

	/**
	 * @param changeTo The 
	 */
	private void updateProgressMaximum(int changeTo) {
		
		//Change the progress maximum based on the
		// number of textures we are loading.
		synchronized(ChroData._max_prog) {
			ChroData._max_prog = (short) changeTo;
		}
		
//		DEBUG
//		ChroPrint.println("New maximum: " + ChroData._max_prog, System.out);
	}

	/**
	 * @param cachingThreads
	 * @param image
	 * @param tex
	 */
	private void cacheTextureOrContinue(LinkedList<Thread> cachingThreads,
										  Field image, ChroTexture tex,
										  ArrayList<Field> resids) {
		
		//If the bitmap reference is still null, something went wrong.
		if(tex.getBmpTex() == null)
			ChroPrint.println("Error decoding resid field " + image, System.err);

		//Then check if it is set for loading post-start
		if(tex.isCacheLater())
			return;
		//Otherwise, we will go ahead and cache the bitmap object.
		else {
//				DEBUG
//				ChroPrint.println(texture.toString(), "Chaching:|", image.getName(), '-', System.out);
			ChroTexCacheThread texCacheThread = new ChroTexCacheThread(tex, this);
			texCacheThread.start();
			cachingThreads.add(texCacheThread);
			resids.add(image);
		}
	}

	/**
	 * @param cachingThreads
	 */
	private void cacheJobsWait(LinkedList<Thread> cachingThreads) {
		ChroPrint.println("Waiting for caching jobs to finish...", System.out);
		//Wait for all the threads to finish their jobs and die.
		while(!cachingThreads.isEmpty()) {
			Thread cacheThread = cachingThreads.remove();
			synchronized(cacheThread) {
				try {
					if(cacheThread.isAlive()) {
//						DEBUG
//						ChroPrint.println("Trying to join thread " + cacheThread, System.out);
						cacheThread.join();
					}
				}
				catch(Exception ex) { ChroUtilities.printExDetails(ex); }
			}
			incProgress(1);
		}
	}

	/**
	 * In this method, we first decode the resource.
	 * 
	 * We then figure out which bar type the texture is applicable to:
	 * The names of the textures are semantically structured to 
	 *	include a value at the end of the name that indicates which 
	 *	bar, or bars, the texture applies to.
	 *
	 * For example, one texture name: threed_num1_w0_small_0
	 * We can see by analyzing the name that there are specific pieces of 
	 *  information describing the content of the texture,
	 *  which is broken down as follows:
	 *  	
	 *  	[number depth]_[number graphical text content]_[with leading zero (optional)]_[texture resolution]_[bar type applicable]
	 * 
	 * @param bmpLoadOptions The options with which to decode a bitmap, specifically to not pre-scale the image. 
	 * 							We want a raw bitmap.
	 * @param chroRes The application resources object.
	 * @param numbersVis The visibility of the numbers for each bar.
	 * @param image The Field object that contains the resource name and ID.
	 * 
	 * @return A ChroTexture object based on the in-parameters.
	 * 
	 * @see java.lang.reflect.Field
	 * @see java.lang.reflect.Field#getInt(Object obj)
	 */
	private ChroTexture decodeAndProcessResource(final BitmapFactory.Options bmpLoadOptions,
												  Resources chroRes,
												  ArrayList<Boolean> numbersVis,
												  Field image) {
		
		//Temporary texture variables for this resource ID
		ChroTexture tex = null;
		//Storage for the resource ID and name
		int resId = 0;
		String resName = null;
		//Modify the bar type based on the visibility
		// of either 3D or 2D initial bars.
		int visMod = settings.isThreeD() ? 4 : 0;
		
		//Try to decode the png via its drawable resid
		try {
			
			//Here we get the resource ID and resource name from the Field object.
			resId = image.getInt(null);
			resName = image.getName();
//			DEBUG
//			ChroPrint.println("Will try to decode  resource " + resName + " with resource ID " + resId + " from resources object " + chroRes, System.out);
			//We then attempt to decode the PNG texture.
			Bitmap texture = BitmapFactory.decodeResource(chroRes, resId, bmpLoadOptions);
//			DEBUG
//			ChroPrint.println("Decoded png into bitmap object " + texture, System.out);
			//Figure out which bar this texture applies to and create the texture object.
			String[] texName = resName.split("_");
			String bar = texName[texName.length - 1];
//			DEBUG
//			ChroPrint.println("Found bar " + bar + " in texture " + resName, System.out);
			boolean loadNow = false;
			
			if(bar.length() > 1) {
				char[] types = bar.toCharArray();
				//Check if we need to load this texture now.
				for(int i = 0; i < types.length; i++)
					loadNow |= numbersVis.get(Integer.parseInt("" + types[i]) + visMod) |
							   numbersVis.get(Integer.parseInt("" + types[i]) + visMod + (visMod == 0 ? 4 : -4));
//				DEBUG
				ChroPrint.println("Loading " + texture + " with resid " + resId + " now: " + loadNow, System.out);
				tex = new ChroTexture(resId, resName, !loadNow, texture);
				
				for(char b : types)
					tex.addBarTypes(ChroType.valueByNumber(Integer.parseInt("" + b) + visMod),
									ChroType.valueByNumber(Integer.parseInt("" + b) + visMod + (visMod == 0 ? 4 : -4)));
				
				textures.add(tex);
			}
			else {
				//The type of bar applicable to the
				// resource we are dealing with.
				int barType = Integer.parseInt(bar) + visMod;
				//Check if we need to load texture now.
				loadNow |= numbersVis.get(barType) | numbersVis.get(barType + (visMod == 0 ? 4 : -4));
//				DEBUG
				ChroPrint.println("Loading " + texture + " with resid " + resId + " now: " + loadNow, System.out);
				tex = new ChroTexture(resId, resName, !loadNow, texture);
				tex.addBarTypes(ChroType.valueByNumber(barType),
								ChroType.valueByNumber(barType + (visMod == 0 ? 4 : -4)));
//				DEBUG
//				ChroPrint.println("Current bar types in texture object " + tex + ": " + tex.getBarTypes().length, System.out);
				textures.add(tex);
			}
		}
		catch(Exception ex) { ChroUtilities.printExDetails(ex); }
		
		return tex;
	}

	/**
	 * @return
	 */
	private BitmapFactory.Options setBitmapOptions() {
		//Set the bitmap creation options.
		final BitmapFactory.Options bmpLoadOptions = new BitmapFactory.Options();
		bmpLoadOptions.inScaled = false;
		incProgress(2);
		return bmpLoadOptions;
	}

	/**
	 * @param imgResIds
	 * @return
	 */
	private LinkedList<Thread> initCache(ArrayList<Field> imgResIds,
										  ArrayList<ChroTexture> cache) {
		
		ChroPrint.println("Initializing caching mechanisms...", System.out);
		LinkedList<Thread> cachingThreads = new LinkedList<Thread>();
		
		//Initialize the settings cache
		if(cache == null)
			new ChroTextures(imgResIds.size());
		else if(!cache.isEmpty())
			ChroTextures.clean();
			
		incProgress(3);
		return cachingThreads;
	}

	/**
	 * @return
	 */
	private ArrayList<Field> findImgResIds() {
		
		//Get the class fields and set up a list to store the 
		// Fields we will use to cache the textures.
		Field[] drawables = R.drawable.class.getFields();
		ArrayList<Field> imgResIds = new ArrayList<Field>();
		incProgress(2);
		
		ChroPrint.println("Finding drawables...", System.out);
		//Search through the res/drawables class for the correct entries.
		for(Field d : drawables)
			if(d.getName().startsWith("threed")) {
				imgResIds.add(d); incProgress(1);
			}
		
		//Allocate space for texture object refs
		textures = new ArrayList<ChroTexture>(imgResIds.size());
		
		updateProgressMaximum((ChroData._max_prog + (imgResIds.size() * 3)));
		
		return imgResIds;
	}

	/**
	 * @param imgResIds
	 * @return
	 */
	private boolean checkMemoryForCache(ArrayList<Field> imgResIds, ArrayList<ChroTexture> cache) {
		
		ChroPrint.println("Checking for cache in memory...", System.out);
		
		if(cache != null) {
			if(!cache.isEmpty()) {
				//If the cache matches the correct size, it's already in memory.
				//Therefore, we can greatly increment the progress and exit this method.
				if(cache.size() == imgResIds.size()) {
					ChroPrint.println("Cache already exists! woohoo!", System.out);
					incProgress(5+(imgResIds.size()*2));
					return true;
				}
				else
					ChroPrint.println("Cache is not synchronized. Will rebuild.", System.out);
			}
			else
				ChroPrint.println("Cache object exists but is empty. Will generate graphics cache.", System.out);
		}
		else
			ChroPrint.println("Cache is null, will build new graphics cache.", System.out);
		
		return false;
	}

	/**
	 * When we are finished with our construction tasks,
	 *  we need to reset the data in the parameter object for 
	 *  transfer back to the main thread.
	 */
	private void setReturnData() {
		paramsData.setRenderSurface(time); //Set surface reference to send back to main Activity.
		incProgress(1);
		paramsData.setSettings(settings); //Set settings reference to send back to main Activity.
		incProgress(1);
		paramsData.setTextures(textures);
		incProgress(1);
	}

	/**
	 * Here, we put together the surface and renderer then load the settings
	 *  into the renderer.
	 */
	private void buildSurface() {
//		ChroPrint looperp = new ChroPrint();
		incProgress(6);
//		Looper.myLooper().setMessageLogging(looperp);
		incProgress(11);
//		Looper.myLooper().dump(looperp, "ChroBars-constr : ");
		incProgress(4);
		time = new ChroSurface(UIThread); //Construct the surface and bars.
		incProgress(17);
		time.setSettingsInstance(settings); //Load the application settings into the renderer.
		incProgress(21);
//		Looper.myLooper().dump(looperp, "ChroBars-constr : ");
		incProgress(4);
	}

	/**
	 * Trys moderately hard to get either a new instance of 
	 * 	ChroBarsSettings or get an existing instance.
	 */
	private void getSettingsInstance() {
		
		try {
			//Load a settings instance, which in turn loads the settings into the ChroBars.
//			DEBUG
//			System.out.println("Getting new settings instance...");
			settings = ChroBarsSettings.getNewSettingsInstance(UIThread);
			incProgress(10);
		}
		catch(Exception unknownEx) {
			//Something went wrong that can possibly be corrected by attempting
			// to get the current settings instance.
			ChroUtilities.printExDetails(unknownEx);
			incProgress(3);
			ChroPrint.println("Trying to get existing settings instance...", System.err);
			settings = ChroBarsSettings.getInstance(UIThread);
			incProgress(3);
			if(settings != null) {
//				DEBUG
//				ChroPrint.println("Existing settings instance retrieved.", System.out);
				incProgress(5);
			}
			//If this happens, we have no access to the program settings,
			// thus we cannot continue past this point.
			// TODO in the future make this handle this case without closing the application.
			else {
				ChroPrint.println("Failed to get settings object.", System.err);
				throw new NullPointerException("Cannot continue. The settings object is null.");
			}
		}
	}

	/**
	 * Increments the progress then publishes it.
	 *  Publishing the progress will fire a progress 
	 *  update event that will be caught by an onProgressUpdate listener.
	 *  
	 *  Synchronized, because we will absolutely for sure have more 
	 *   than one thread trying to access this method at once.
	 * 
	 * @param incBy The amount to increment progress by.
	 */
	@Override
	public synchronized void incProgress(int incBy) {
		progress += incBy;
//		DEBUG
//		ChroPrint.println("Publishing progress: " + progress, System.out);
		publishProgress(progress);
	}

	/**
	 * Updates the progress parameter in the main application Activity.
	 * 
	 * @param progress The current background task progress.
	 * 
	 * @see com.psoft.ChroBarsActivity#setProgressPercent(int)
	 */
    protected void onProgressUpdate(Integer... progress) {
        UIThread.setProgressPercent(progress[0]);
    }
}
