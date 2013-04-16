package com.psoft.chrobars.data;

import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.util.ChroBarsSettings;

/**
 * Parameter object for use with class ChroConstructionThread.
 * 
 * @author jhyry
 */
public class ChroConstructionParams {
	
	private ChroBarsSettings settings;
	private ChroSurface renderSurface;
	private ChroBarsActivity mainActivity;
	
	/**
	 * 
	 * @param a
	 * @param c
	 * @param s
	 */
	public ChroConstructionParams( ChroBarsActivity a,
									ChroSurface c,
									ChroBarsSettings s ) {
		settings = s;
		renderSurface = c;
		mainActivity = a;
	}
	
	/**
	 * @return the mainActivity
	 */
	public ChroBarsActivity getMainActivity() {
		return mainActivity;
	}
	/**
	 * @param mainActivity the mainActivity to set
	 */
	public void setMainActivity(ChroBarsActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	/**
	 * @return the settings
	 */
	public ChroBarsSettings getSettings() {
		return settings;
	}
	/**
	 * @param settings the settings to set
	 */
	public void setSettings(ChroBarsSettings settings) {
		this.settings = settings;
	}
	/**
	 * @return the renderSurface
	 */
	public ChroSurface getRenderSurface() {
		return renderSurface;
	}
	/**
	 * @param renderSurface the renderSurface to set
	 */
	public void setRenderSurface(ChroSurface renderSurface) {
		this.renderSurface = renderSurface;
	}
}
