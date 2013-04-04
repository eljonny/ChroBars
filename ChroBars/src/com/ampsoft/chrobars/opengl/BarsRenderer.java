package com.ampsoft.chrobars.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.ampsoft.chrobars.ChroBar;
import com.ampsoft.chrobars.ChroType;
import com.ampsoft.chrobars.data.ChroBarStaticData;
import com.ampsoft.chrobars.util.ChroBarsSettings;
import com.ampsoft.chrobars.util.ChroUtils;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

/**
 * 
 * @author jhyry
 *
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
		
//		refreshVisibleBars();
		
		ByteOrder order_native = ByteOrder.nativeOrder();
		int bytesInFloat = ChroBarStaticData._BYTES_IN_FLOAT;
		
		//Set up local ambient light parameters
		lightBufferTemp = ByteBuffer.allocateDirect(ChroBarStaticData._light_ambient.length*bytesInFloat);
		lightBufferTemp.order(order_native);
		ambientLightBuffer = lightBufferTemp.asFloatBuffer();
		ambientLightBuffer.put(ChroBarStaticData._light_ambient).position(0);
		
		//Set up global ambient light parameters
		lightBufferTemp = ByteBuffer.allocateDirect(ChroBarStaticData._light_global_ambient.length*bytesInFloat);
		lightBufferTemp.order(order_native);
		globalAmbientLightBuffer = lightBufferTemp.asFloatBuffer();
		globalAmbientLightBuffer.put(ChroBarStaticData._light_global_ambient).position(0);
		
		
		//Set up diffuse light parameters
		lightBufferTemp = ByteBuffer.allocateDirect(ChroBarStaticData._light_diffuse.length*bytesInFloat);
		lightBufferTemp.order(order_native);
		diffuseLightBuffer = lightBufferTemp.asFloatBuffer();
		diffuseLightBuffer.put(ChroBarStaticData._light_diffuse).position(0);
		
		//Set up the emission buffer
		lightBufferTemp = ByteBuffer.allocateDirect(ChroBarStaticData._light_emission.length*bytesInFloat);
		lightBufferTemp.order(order_native);
		emissionLightBuffer = lightBufferTemp.asFloatBuffer();
		emissionLightBuffer.put(ChroBarStaticData._light_emission).position(0);
		
		//Set up specular light parameters
		lightBufferTemp = ByteBuffer.allocateDirect(ChroBarStaticData._light_specular.length*bytesInFloat);
		lightBufferTemp.order(order_native);
		specularLightBuffer = lightBufferTemp.asFloatBuffer();
		specularLightBuffer.put(ChroBarStaticData._light_specular).position(0);
		
		//Set up material shininess buffer
		lightBufferTemp = ByteBuffer.allocateDirect(ChroBarStaticData._specular_shininess.length*bytesInFloat);
		lightBufferTemp.order(order_native);
		specularShininessBuffer = lightBufferTemp.asFloatBuffer();
		specularShininessBuffer.put(ChroBarStaticData._specular_shininess).position(0);
		
		//Set up the position of the light in the scene.
		lightBufferTemp = ByteBuffer.allocateDirect(ChroBarStaticData._light_position.length*bytesInFloat);
		lightBufferTemp.order(order_native);
		lightPositionBuffer = lightBufferTemp.asFloatBuffer();
		lightPositionBuffer.put(ChroBarStaticData._light_position).position(0);
		
		//Apply the buffered light parameters to the GL object
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuseLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, specularLightBuffer);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);
		
		//Set the default global ambient light
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, globalAmbientLightBuffer);
		
		// Set OpenGL Parameters:
		// - Background of the OpenGL surface to white
		// - Smooth GL shader model
		// - Clear the depth buffer for usage
		// - Enable the OpenGL depth testing
		// - Set the OpenGL depth testing function to be used
		// - Use NICEST perspective correction.
		//System.out.println("Calling glClearColor");
		gl.glClearColor(backgroundColor[0],
						backgroundColor[1],
						backgroundColor[2],
						backgroundColor[3] );
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}
	
	/**
	 * Clears the openGL draw buffer and writes the new bars to the screen.
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		gl.glClearColor(backgroundColor[0],
						backgroundColor[1],
						backgroundColor[2],
						backgroundColor[3] );
		
		//Enable OpenGL light facilities
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_COLOR_MATERIAL);

		for(ChroBar cb : visibleBars) {
			System.out.println("Drawing " + cb);
			cb.draw(gl);
		}
		
		gl.glTranslatef(0f, 0f, -5f);
		
		gl.glDisable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_LIGHT0);
		gl.glDisable(GL10.GL_COLOR_MATERIAL);

		gl.glLoadIdentity();
	}

	/**
	 * 
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
	 * 
	 * @param context
	 */
	protected void setActivityContext(Context context) {
		activityContext = context;
	}
	
	/**
	 * 
	 * @param settings
	 */
	protected void setSettingsObjectReference(ChroBarsSettings settings) {
		
		//Set settings object reference
		BarsRenderer.settings = settings;
		
		System.out.println("Constructing bars...");
		//Then populate the HashMap so we can load settings
		for(ChroType ct : ChroType.values())
			chroBars.put(ct, ChroBar.getInstance(ct, activityContext));
		
		System.out.println("Loading settings...");
		//Then load all the relevant settings into the renderer.
		loadSettings();
	}
	
	/**
	 * Loads the stored settings into the OpenGL renderer.
	 */
	private void loadSettings() {
		
		ArrayList<Boolean> visibility = settings.getBarsVisibility();
		
		setBackgroundColor(settings.getBackgroundColor(false));
		
		//Loads the appropriate settings for each bar.
		for(ChroType t : ChroType.values()) {
			
			ChroBar current = chroBars.get(t);
			
			current.setDrawBar(visibility.get(t.getType()));
			//TODO in the future this should be per-bar.
			current.setDrawNumber(settings.isDisplayNumbers());
			ChroUtils.changeChroBarColor(current, settings.getBarColor(t, false));
		}
		
		refreshVisibleBars();
	}

	/**
	 * 
	 * @return
	 */
	public int numberOfBarsToDraw() {
		
		int sum = 0;
		
		for(ChroBar cb : refreshVisibleBars())
			if(cb.isDrawn())
				sum++;
		
		return  sum;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public ChroBar getChroBar(ChroType type) {
		return chroBars.get(type);
	}
	
	/**
	 * 
	 * @return
	 */
	public ChroBar[] refreshVisibleBars() {
		
		if(settings.isThreeD()) {
			for(ChroType t : chroBars.keySet())
				if(t.is3D())
					visibleBars[t.getType() - 4] = chroBars.get(t);
		}
		else {
			for(ChroType t : chroBars.keySet())
				if(!t.is3D())
					visibleBars[t.getType()] = chroBars.get(t);
		}
		
		return visibleBars;
	}
	
	/**
	 * Returns the current level of bar drawing precision.
	 * @return The level of precision as an integer.
	 */
	public float getPrecision() {
		return (float)settings.getPrecision();
	}

	/**
	 * 
	 * @return
	 */
	public int getBackgroundColor() {
		
		return Color.argb((int)(backgroundColor[3]*255.0f),
						  (int)(backgroundColor[0]*255.0f),
						  (int)(backgroundColor[1]*255.0f),
						  (int)(backgroundColor[2]*255.0f) );
	}
	
	/**
	 * Bars need to use this buffer to set the specular of the material.
	 * @return A java.nio.FloatBuffer containing the specular light color.
	 */
	public FloatBuffer getSpecularBuffer() {
		return specularLightBuffer;
	}
	
	/**
	 * Bars need to use this buffer to set the emission of the material.
	 * @return A java.nio.FloatBuffer containing the emission light color of the object being drawn.
	 */
	public FloatBuffer getEmissionLightBuffer() {
		return emissionLightBuffer;
	}
	
	/**
	 * Bars will need this buffer to set the material shininess.
	 * @return A java.nio.FloatBuffer containing the shininess parameter.
	 */
	public FloatBuffer getShininessBuffer() {
		return specularShininessBuffer;
	}
	
	/**
	 * 
	 * @param argb
	 */
	public void setBackgroundColor(int argb) {
		
		backgroundColor[0] = (float)Color.red(argb)/255.0f;
		backgroundColor[1] = (float)Color.green(argb)/255.0f;
		backgroundColor[2] = (float)Color.blue(argb)/255.0f;
		backgroundColor[3] = (float)Color.alpha(argb)/255.0f;
	}
	
	//For setting the background color
	private static float[] backgroundColor = new float[ChroBarStaticData._RGBA_COMPONENTS];

	//Data structure for holding ChroBars
	private static HashMap<ChroType, ChroBar> chroBars = new HashMap<ChroType, ChroBar>(8);
	private static ChroBar[] visibleBars = new ChroBar[ChroBarStaticData._MAX_BARS_TO_DRAW];
	
	//For OpenGL Lighting
	private static ByteBuffer lightBufferTemp;
	private static FloatBuffer ambientLightBuffer, globalAmbientLightBuffer, emissionLightBuffer,
									diffuseLightBuffer, specularLightBuffer, lightPositionBuffer, specularShininessBuffer;
	
	//Context in which this Renderer exists
	private Context activityContext;
	private static ChroBarsSettings settings;
}