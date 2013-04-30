package com.psoft.chrobars.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.psoft.chrobars.ChroBar;
import com.psoft.chrobars.ChroType;
import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.settings.ChroBarsSettings;
import com.psoft.chrobars.util.ChroPrint;
import com.psoft.chrobars.util.ChroUtilities;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

/**
 * This class is an implementation of GLSurfaceView.Renderer.
 *  It is used for the rendering of the ChroBars.
 *  
 *  Other class objects may interact with this through the 
 *   ChroSurface object.
 * 
 * @author jhyry
 */
public class BarsRenderer implements GLSurfaceView.Renderer {

	/**
	 * Sets up the surface when it is initially created
	 * or when the EGL context is lost.
	 * 
	 * @param gl The graphics library surface object
	 * @param config Which renderer configuration to use
	 * 
	 * @see android.opengl.GLSurfaceView.Renderer
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		ChroTextures.loadTextures(gl, ChroData._TEX_SIZE);
		refreshVisibleBars();
		
		ByteOrder order_native = ByteOrder.nativeOrder();
		
		glRendererAllocate(order_native);
		setUpLights(gl);
		
		//Set the default global ambient light
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, globalAmbientLightBuffer);
		
		// Set OpenGL Parameters:
		// - Background color of the OpenGL surface to white
		// - Smooth GL shader model
		// - Clear the depth buffer for usage
		// - Enable texture mapping
		// - Enable the OpenGL depth testing
		// - Set the OpenGL depth testing function to be used
		// - Use NICEST perspective correction.
		gl.glClearColor(backgroundColor[0],
						backgroundColor[1],
						backgroundColor[2],
						backgroundColor[3] );
		
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearDepthf(1.0f);
//		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	/**
	 * Sets the parameters for the various lights.
	 * 
	 * @param gl The OpenGL environment object
	 */
	private void setUpLights(GL10 gl) {
		//Apply the buffered light parameters to light 0
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientLight0Buffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuseLight0Buffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, specularLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0PositionBuffer);
		
		//Apply the buffered light parameters to light 1
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, ambientLight1Buffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, diffuseLight1Buffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, specularLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, light1PositionBuffer);
	}

	/**
	 * This method allocates space for light and related buffers.
	 * 
	 * @param order_native The architectural native byte order.
	 */
	private void glRendererAllocate(ByteOrder order_native) {
		
		byte bytesInFloat = ChroData._BYTES_IN_FLOAT;
		
		allocateLight0Buffers(order_native, bytesInFloat);
		allocateLight1Buffers(order_native, bytesInFloat);
		allocateGlobalLightBuffers(order_native, bytesInFloat);
	}

	/**
	 * Allocates memory for light buffers used by either both 
	 *  lights or the global environment.
	 * 
	 * @param order_native The architectural native byte order.
	 * @param bytesInFloat The number of bytes in a float.
	 */
	private void allocateGlobalLightBuffers(ByteOrder order_native, byte bytesInFloat) {
		//Set up the emission buffer
		emissionLightBuffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_0_emission.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_0_emission).position(0);
		//Set up specular light parameters
		specularLightBuffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_0_specular.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_0_specular).position(0);
		//Set up material shininess buffer
		specularShininessBuffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._specular_shininess.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._specular_shininess).position(0);
		//Set up global ambient light parameters
		globalAmbientLightBuffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_global_ambient.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_global_ambient).position(0);
	}

	/**
	 * Allocates memory for light buffers used by light 1.
	 * 
	 * @param order_native The architectural native byte order.
	 * @param bytesInFloat The number of bytes in a float.
	 */
	private void allocateLight1Buffers(ByteOrder order_native, byte bytesInFloat) {
		//Set up local ambient light parameters
		ambientLight1Buffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_1_ambient.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_1_ambient).position(0);
		//Set up diffuse light parameters
		diffuseLight1Buffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_1_diffuse.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_1_diffuse).position(0);
		//Set up the position of the light in the scene.
		light1PositionBuffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_1_position.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_1_position).position(0);
	}

	/**
	 * Allocates memory for light buffers used by light 0.
	 * 
	 * @param order_native The architectural native byte order.
	 * @param bytesInFloat The number of bytes in a float.
	 */
	private void allocateLight0Buffers(ByteOrder order_native, byte bytesInFloat) {
		//Set up local ambient light parameters
		ambientLight0Buffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_0_ambient.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_0_ambient).position(0);
		//Set up diffuse light parameters
		diffuseLight0Buffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_0_diffuse.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_0_diffuse).position(0);
		//Set up the position of the light in the scene.
		light0PositionBuffer =
			(FloatBuffer) ByteBuffer.allocateDirect(ChroData._light_0_position.length*bytesInFloat)
									.order(order_native).asFloatBuffer()
									.put(ChroData._light_0_position).position(0);
	}
	
	/**
	 * Clears the openGL draw buffer and writes the new bars to the screen.
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		
		if(loadLateTextures) {
			loadLateTextures = false;
			ChroTextures.loadTextures(gl, lates, ChroData._TEX_SIZE);
		}
		
		gl.glPushMatrix(); {
		
			gl.glLoadIdentity();
			
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			gl.glClearColor(backgroundColor[0],
							backgroundColor[1],
							backgroundColor[2],
							backgroundColor[3] );
			
			gl.glTranslatef(0, 0, -5);
			
			for(ChroBar cb : visibleBars) {
				
//				DEBUG
//				System.out.println("Drawing " + cb);
				
				if(cb.getBarType().is3D())
					enableGLSceneLighting(gl);
				
				//Draw the bar
				cb.draw(gl);
				
				if(cb.getBarType().is3D())
					disableGLSceneLighting(gl);
			}
			
			gl.glLoadIdentity();
		}
		
		gl.glPopMatrix();
	}

	/**
	 * This method calls the correct OpenGL helpers 
	 *  to disable scene lighting.
	 * 
	 * @param gl The OpenGL environment object.
	 */
	private void disableGLSceneLighting(GL10 gl) {
		//Disable OpenGL normalization.
		gl.glDisable(GL10.GL_NORMALIZE);
		
		//Disable OpenGL lighting facilities
		gl.glDisable(GL10.GL_COLOR_MATERIAL);
		gl.glDisable(GL10.GL_LIGHT1);
		gl.glDisable(GL10.GL_LIGHT0);
		gl.glDisable(GL10.GL_LIGHTING);
	}

	/**
	 * This method calls the correct OpenGL helpers 
	 *  to enable scene lighting.
	 * 
	 * @param gl The OpenGL environment object.
	 */
	private void enableGLSceneLighting(GL10 gl) {
		//Enable OpenGL light facilities
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHT1);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);

		//Enable OpenGL normalization
		gl.glEnable(GL10.GL_NORMALIZE);
	}

	/**
	 * This necessarily overridden method correctly changes the 
	 * 	surface and viewport parameters according to the change of the surface.
	 * 
	 * @param gl The OpenGL environment object.
	 * @param width The new Viewport width.
	 * @param height the new Viewport height.
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		//Readjusts the surface to match the current conditions
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, 40.0f, ((float) width)/((float) height), 1.0f, 50.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	/**
	 * Sets the current application context.
	 * 
	 * @param context An object reference to the current application context.
	 */
	protected void setActivityContext(Context context) {
		activityContext = context;
	}
	
	/**
	 * Sets the settings object reference then proceeds to populate 
	 *  the ChroBars map with newly constructed bars. The method that 
	 *  loads the settings into the renderer is then called.
	 * 
	 * @param settings The current ChroBarsSettings instance reference.
	 */
	protected void setSettingsObjectReference(ChroBarsSettings settings) {
		
		//Set settings object reference
		BarsRenderer.settings = settings;
		
		ChroPrint.println("Constructing bars...", System.out);
		//Then populate the HashMap so we can load settings
		for(ChroType ct : ChroType.values())
			chroBars.put(ct, ChroBar.getInstance(ct, activityContext));
		
		ChroPrint.println("Loading settings...", System.out);
		//Then load all the relevant settings into the renderer.
		loadSettings();
		ChroPrint.println("Done.", System.out);
	}
	
	/**
	 * Loads the stored settings into the OpenGL renderer.
	 */
	private void loadSettings() {
//		DEBUG
//		ChroPrint.println("Setting visibility lists...", System.out);
		ArrayList<Boolean> barVis = settings.getBarsVisibility();
		ArrayList<Boolean> numVis = settings.getNumbersVisibility();
//		DEBUG
//		ChroPrint.println("Setting background color...", System.out);
		setBackgroundColor(settings.getBackgroundColor(false));
//		DEBUG
//		ChroPrint.println("Load bar settings...", System.out);
		//Loads the appropriate settings for each bar.
		for(ChroType t : ChroType.values()) {
			
			ChroBar current = chroBars.get(t);
//			DEBUG
//			ChroPrint.println("Current bar: " + current, System.out);
//			ChroPrint.println("Setting bar visibility...", System.out);
			current.setDrawBar(barVis.get(t.getType()));
//			DEBUG
//			ChroPrint.println("Setting number visibility...", System.out);
			current.setDrawNumber(numVis.get(t.getType()));
//			DEBUG
//			ChroPrint.println("Setting bar color...", System.out);
			ChroUtilities.changeChroBarColor(current, settings.getBarColor(t, false));
		}
	}
	
	/**
	 * 
	 * @param loadThese
	 */
	public void loadLateCache(ArrayList<ChroTexture> loadThese) {
		lates = loadThese;
		loadLateTextures = true;
	}
	
	/**
	 * This should only be called if colors are indirectly changed,
	 *  aka not through the bars or renderer. This is the case when 
	 *  settings are reset to defaults in the settings menu, so we
	 *   need this to reload the settings from the settings object.
	 */
	public void reloadSettings() {
//		DEBUG
//		ChroPrint.println("Reloading settings...", System.out);
		loadSettings();
	}

	/**
	 * Dynamically calculates the number of bars 
	 *  in the array of currently visible bars which 
	 *  have their drawBar field set to true.
	 * 
	 * @return The number of bars in which the drawBar field is set to true.
	 */
	public int numberOfBarsToDraw() {
		
		int sum = 0;
		
		for(ChroBar cb : visibleBars)
			if(cb.isDrawn())
				sum++;
		
		return  sum;
	}
	
	/**
	 * Retrieves a ChroBar for the caller.
	 * 
	 * @param type Specifies the type of of ChroBar the caller wants with a ChroType enum value.
	 * @return The requested ChroBar from the static bars map.
	 */
	public ChroBar getChroBar(ChroType type) {
		return chroBars.get(type);
	}
	
	/**
	 * Refreshes the array of the currently visible bars.
	 * 
	 * @return The newly cached bars that are currently visible on the screen.
	 */
	public ChroBar[] refreshVisibleBars() {
		
		if(settings.isThreeD()) {
			for(ChroType t : chroBars.keySet()) {
				if(t.is3D()) {
					visibleBars[t.getType() - 4] = chroBars.get(t);
				}
			}
		}
		else {
			for(ChroType t : chroBars.keySet()) {
				if(!t.is3D()) {
					visibleBars[t.getType()] = chroBars.get(t);
				}
			}
		}
		
		return visibleBars;
	}
	
	/**
	 * Returns the current level of bar drawing precision.
	 * 
	 * @return The level of precision as an integer.
	 */
	public float getPrecision() {
		return (float)settings.getPrecision();
	}
	
	/**
	 * Retrieves the current barEdgeSetting from the 
	 *  settings object.
	 *  
	 * @return The current bar edges setting.
	 */
	public int getBarEdgeSetting() {
		return settings.getBarEdgeSetting();
	}
	
	/**
	 * Retrieves the current barMarginScalar from the 
	 *  settings object.
	 * 
	 * @return The current bar margin scalar. Base bar margin is 2px.
	 */
	public float getBarMarginScalar() {
		return (float)settings.getBarMarginMultiplier();
	}
	
	/**
	 * Retrieves the current edgeMarginScalar from the 
	 *  settings object.
	 * 
	 * @return The current edge margin scalar. Base edge margin is 2px.
	 */
	public float getEdgeMarginScalar() {
		return (float)settings.getEdgeMarginMultiplier();
	}
	
	/**
	 * Accessor for the current dynamic lighting setting.
	 * 
	 * @return Whether to or to not use dynamic lighting.
	 */
	public boolean usesDynamicLighting() {
		return settings.usesDynamicLighting();
	}
	
	/**
	 * Retrieves whether 12 hour time is active from the 
	 *  settings object.
	 * 
	 * @return Whether or not 12 hour time is active.
	 */
	public boolean usesTwelveHourTime() {
		return settings.usesTwelveHourTime();
	}

	/**
	 * Packs the current background color into a color int.
	 * 
	 * @return A color int representing the current background color.
	 */
	public int getBackgroundColor() {
		
		return Color.argb((int)(backgroundColor[3]*255.0f),
						   (int)(backgroundColor[0]*255.0f),
						   (int)(backgroundColor[1]*255.0f),
						   (int)(backgroundColor[2]*255.0f) );
	}
	
	/**
	 * Bars need to use this buffer to set the specular of the material.
	 * 
	 * @return A java.nio.FloatBuffer containing the specular light color.
	 */
	public FloatBuffer getSpecularBuffer() {
		return specularLightBuffer;
	}
	
	/**
	 * Bars need to use this buffer to set the emission of the material.
	 * 
	 * @return A java.nio.FloatBuffer containing the emission light color of the object being drawn.
	 */
	public FloatBuffer getEmissionLightBuffer() {
		return emissionLightBuffer;
	}
	
	/**
	 * Bars will need this buffer to set the material shininess.
	 * 
	 * @return A java.nio.FloatBuffer containing the shininess parameter.
	 */
	public FloatBuffer getShininessBuffer() {
		return specularShininessBuffer;
	}
	
	/**
	 * This method breaks the new color into its constituent bytes.
	 * 
	 * @param argb A packed color in of which to set the background color.
	 */
	public void setBackgroundColor(int argb) {
		
		backgroundColor[0] = (float)Color.red(argb)/255.0f;
		backgroundColor[1] = (float)Color.green(argb)/255.0f;
		backgroundColor[2] = (float)Color.blue(argb)/255.0f;
		backgroundColor[3] = (float)Color.alpha(argb)/255.0f;
	}
	
	//For setting the background color
	private static float[] backgroundColor = new float[ChroData._RGBA_COMPONENTS];

	//Data structure for holding ChroBars
	private static HashMap<ChroType, ChroBar> chroBars = new HashMap<ChroType, ChroBar>(8);
	
	//This array holds an up-to-date list of the currently visible bars,
	// either 2D or 3D
	private static ChroBar[] visibleBars = new ChroBar[ChroData._MAX_BARS_TO_DRAW];
	
	//Buffers for OpenGL Lighting
	private static FloatBuffer ambientLight0Buffer, globalAmbientLightBuffer, emissionLightBuffer,
								diffuseLight0Buffer, specularLightBuffer, light0PositionBuffer,
								ambientLight1Buffer, diffuseLight1Buffer, light1PositionBuffer, specularShininessBuffer;
	
	//Context in which this Renderer exists
	private Context activityContext;
	//Settings object reference.
	private static ChroBarsSettings settings;
	//Whether or not late cache textures are ready to load.
	private boolean loadLateTextures = false;
	//Late cache textures to load.
	private ArrayList<ChroTexture> lates;
}