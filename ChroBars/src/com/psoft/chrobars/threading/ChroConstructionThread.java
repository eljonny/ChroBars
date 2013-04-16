package com.psoft.chrobars.threading;

import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.data.ChroConstructionParams;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.util.ChroBarsSettings;
import com.psoft.chrobars.util.ChroUtils;

import android.os.AsyncTask;
import android.os.Looper;

public class ChroConstructionThread extends AsyncTask<ChroConstructionParams, Integer, ChroConstructionParams> {
    
	private ChroBarsActivity UIThread;
	
	public ChroConstructionParams doInBackground(ChroConstructionParams... paramObj) {

		publishProgress(5);
		ChroConstructionParams paramsData = paramObj[0];
		publishProgress(10);
		UIThread = paramsData.getMainActivity();
		publishProgress(15);
		ChroBarsSettings settings;
		ChroSurface chronos;
		
		try {
			//Load a settings instance, which in turn loads the settings into ChroBars.
			settings = ChroBarsSettings.getNewSettingsInstance(UIThread);
		}
		catch(Exception unknownEx) {
			ChroUtils.printExDetails(unknownEx);
			System.out.println("Trying to get existing settings instance...");
			settings = ChroBarsSettings.getInstance(UIThread);
			if(settings != null)
				System.out.println("Existing settings instance retrieved.");
			else
				throw new NullPointerException("Cannot continue. The settings object is null.");
		}
		
		publishProgress(50);
		
		//Create the GLSurfaceView and set it as the content view
		Looper.prepare();
		chronos = new ChroSurface(UIThread);
		chronos.setSettingsInstance(settings);
		publishProgress(90);
		
		paramsData.setRenderSurface(chronos);
		publishProgress(95);
		paramsData.setSettings(settings);
		publishProgress(100);
		
		return paramsData;
    }

    protected void onProgressUpdate(Integer... progress) {
        UIThread.setProgressPercent(progress[0]);
    }
}
