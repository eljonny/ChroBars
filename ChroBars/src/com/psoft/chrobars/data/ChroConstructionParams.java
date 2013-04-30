package com.psoft.chrobars.data;

import java.util.ArrayList;

import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.settings.ChroBarsSettings;

/**
 * Parameter object for use with class ChroConstructionThread.
 * 
 * @author jhyry
 */
public class ChroConstructionParams {
	
	/* These are our "in" parameters. */
	
	/**
	 * Placeholder for a settings object reference.
	 */
	private ChroBarsSettings settings;
	
	/**
	 * Placeholder for the surface to create.
	 */
	private ChroSurface renderSurface;
	
	/**
	 * Main activity reference.
	 */
	private ChroBarsActivity mainActivity;
	
	/*These are our "out" parameters. */
	
	/**
	 *  This contains a mapping of resource strings to texture buffers.
	 */
	private ArrayList<ChroTexture> textures;
	
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

	/**
	 * @return the textures
	 */
	public ArrayList<ChroTexture> getTextures() {
		return textures;
	}

	/**
	 * @param textures the textures to set
	 */
	public void setTextures(ArrayList<ChroTexture> textures) {
		this.textures = textures;
	}
}
