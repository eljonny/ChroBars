package com.psoft.chrobars.opengl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import com.psoft.chrobars.ChroBar;
import com.psoft.chrobars.ChroType;
import com.psoft.chrobars.threading.construction.ChroBarsLateTexLoadThread;
import com.psoft.chrobars.util.ChroPrint;

//DEBUG
//import com.psoft.chrobars.util.ChroUtilities;

//Imports for raw buffer caching.
//import java.nio.ByteOrder;
//import android.graphics.Color;
//import com.psoft.chrobars.data.ChroData;

/**
 * When an instance of this class is created, it caches
 *  the specified texture resources for loading into the 
 *  OpenGL environment.
 * 
 * @author Jonathan Hyry
 */
public class ChroTextures {
	
	/**
	 * This is where we store the cached textures.
	 */
	private static HashMap<ChroType, ArrayList<ChroTexture>> texCache;
	private static ArrayList<ChroTexture> uniqueTextures;
	
	/**
	 * This constructs a texture containers for all 
	 *  ChroBars OpenGL number textures.
	 *  
	 * @param numOfTextures The total number of possible textures to be cached.
	 */
	public ChroTextures(int numOfTextures) {
		if(texCache == null) {
			texCache = new HashMap<ChroType, ArrayList<ChroTexture>>(numOfTextures);
			uniqueTextures = new ArrayList<ChroTexture>(numOfTextures);
			//Initialize the internal lists in the map.
			for(ChroType t : ChroType.values())
				texCache.put(t, new ArrayList<ChroTexture>());
//			DEBUG
//			ChroPrint.println("Created new ByteBuffer containers...\n" + texCache + "\t" + texMap, System.out);
		}
	}
	
	/**
	 * Clears the texture cache.
	 * 
	 * @see #texCache
	 */
	public static void clean() {
		texCache.clear();
		uniqueTextures.clear();
	}
	
	/**
	 * We want to recycle the bitmaps in a batch, so we use this method.
	 */
	public static void recycleBitmaps() {
		for(ChroTexture tex : uniqueTextures) {
			Bitmap texBmp = tex.getBmpTex();
			if(!texBmp.isRecycled()) {
//				DEBUG
//				ChroPrint.println("Recycling " + tex.getBmpTex(), System.out);
				texBmp.recycle();
			}
			else;
//				DEBUG
//				ChroPrint.println("Bitmap has already been recycled. Skipping " + tex, System.out);
		}
	}
	
	/**
	 * Returns a copy of the current cache for examination and use.
	 * The original cache is left untouched.
	 * 
	 * @return A direct copy of the current cache, 
	 * 				if there is cache in memory.
	 * 				Otherwise returns null.
	 */
	public static final ArrayList<ChroTexture> cache() {
		
		if(uniqueTextures == null)
			return null;
		
		return uniqueTextures;
	}

	/**
	 * The original version of this method was posted by code_zombie at:
	 * 
	 * <a href="http://www.gamedev.net/blog/621/entry-2082291-android-development---loading-an-opengl-texture/">gamedev.net - Loading an OpenGL Texture</a>
	 * 
	 * I have modified it slightly and broken it up into two methods. The 
	 * 	functionality of the original is mostly intact.
	 * 
	 * @param gl The OpenGL environment object.
	 * @param number The bitmap texture to cache for use in OpenGL.
	 * @return The texture address.
	 */
	public static int loadTextureFromBitmap(GL10 gl, ChroTexture number)
	{
		int[] numTexture = new int[1];
		
		ChroPrint.println("Generating texture ID...", System.out);
		gl.glGenTextures(1, numTexture, 0);
		
		ChroPrint.println("Binding texture with ID " + numTexture[0] + "...", System.out);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, numTexture[0]);
		
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		
		ByteBuffer numBuffer = cacheTexture(number).getTexBuffer();
		number.setTexId(numTexture[0]);
		
		ChroPrint.println("Loading texture" + number + "...", System.out);
		
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, number.getBmpTex().getWidth(),
						number.getBmpTex().getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, numBuffer);
		return numTexture[0];
	}
	
	/**
	 * This is the other part of the method from the URL in the loadTextureFromBitmap method comment.
	 * 
	 * This buffers the bitmap pixel-by-pixel to preserve alpha values.
	 * 
	 * @param number The bitmap to cache as an OpenGL texture
	 * @return The texture buffer that was cached.
	 */
	public static ChroTexture cacheTexture(ChroTexture number) {

//		Bitmap numBmp = number.getBmpTex();
//		ByteBuffer tex = ByteBuffer.allocateDirect(numBmp.getHeight() * numBmp.getWidth() * ChroData._RGBA_COMPONENTS).order(ByteOrder.nativeOrder());
//		byte pixelBuffer[] = new byte[ChroData._RGBA_COMPONENTS];
//		for(int i = 0; i < numBmp.getHeight(); i++) {
//			for(int j = 0; j < numBmp.getWidth(); j++) {
//				int color = numBmp.getPixel(j, i);
//				pixelBuffer[0] = (byte)Color.red(color);
//				pixelBuffer[1] = (byte)Color.green(color);
//				pixelBuffer[2] = (byte)Color.blue(color);
//				pixelBuffer[3] = (byte)Color.alpha(color);
//				tex.put(pixelBuffer);
//			}
//		}
		
//		DEBUG
//		ChroPrint.println("Adding " + number + " with types " + ChroUtilities.buildArrayString(number.getBarTypes()) + " to the texture cache...", System.out);
		synchronized(texCache) {
			synchronized(uniqueTextures) {
				for(ChroType t : number.getBarTypes())
					texCache.get(t).add(number/*.setTexBuffer((ByteBuffer) tex.position(0))*/);
				uniqueTextures.add(number);
			}
		}
		
		return number;
	}
	
	/**
	 * Loads all initially cached textures into OpenGL.
	 * 
	 * @param gl The OpenGL environment object.
	 * @param texSize The width/height dimension. An exponential of base 2, aka 2^n=texSize.
	 * @return The array of texture names assigned by OpenGL.
	 */
	public static int[] loadTextures(GL10 gl, int texSize) {
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		int[] textures = new int[uniqueTextures.size()];
		
		ChroPrint.println("Generating " + uniqueTextures.size() + " texture IDs...", System.out);
		gl.glGenTextures(textures.length, textures, 0);
		
		//Texture index
		int texture = 0;
		
		ChroPrint.println("Binding and loading new textures into OpenGL...", System.out);
//		DEBUG
//		ChroPrint.println("Current texture cache: " + texCache, System.out);
		
		synchronized(uniqueTextures) {
			for(ChroTexture number : uniqueTextures) {
//				DEBUG
//				ChroPrint.println("Binding texture " + texture + ":" + textures[texture] + "...", System.out);
				number.setTexId(textures[texture]);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[texture]);

		    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
		    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
				
//				DEBUG
//				ChroPrint.println("Loading texture " + texture + "...", System.out);
//				gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, texSize,
//								texSize, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, number.getTexBuffer());
//				gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, texSize,
//				texSize, 0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE, number.getTexBuffer());
//				gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE_ALPHA, texSize,
//				texSize, 0, GL10.GL_LUMINANCE_ALPHA, GL10.GL_UNSIGNED_BYTE, number.getTexBuffer());
				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, number.getBmpTex(), 0);
				texture++;
				glCheckError(gl);
			}
		}

		gl.glDisable(GL10.GL_BLEND);
		
		ChroPrint.println("Loaded " + textures.length + " textures into OpenGL.", System.out);
		
		return textures;
	}
	
	/**
	 * Loads given cached textures into OpenGL.
	 * 
	 * @param gl The OpenGL environment object.
	 * @param givenTexs The given textures to load into gl.
	 * @param texSize The width/height dimension. An exponential of base 2, aka 2^n=texSize.
	 * @param chroBars We need these to add the newly cached textures to the bar-specific storage.
	 * @return The array of texture names assigned by OpenGL.
	 */
	public static int[] loadTextures(GL10 gl, ArrayList<ChroTexture> givenTexs,
									   int texSize, HashMap<ChroType, ChroBar> chroBars) {
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		int[] textures = new int[givenTexs.size()];
		
		ChroPrint.println("Loading " + givenTexs.size() + " textures into bars...", System.out);
		(new ChroBarsLateTexLoadThread(givenTexs, chroBars)).start();
		
		ChroPrint.println("Generating " + givenTexs.size() + " texture IDs...", System.out);
		gl.glGenTextures(textures.length, textures, 0);
		
		//Texture index
		int texture = 0;
		
		ChroPrint.println("Binding and loading new textures into OpenGL...", System.out);
//		DEBUG
//		ChroPrint.println("Current texture cache: " + givenTexs, System.out);
		for(ChroTexture number : givenTexs) {
//			DEBUG
//			ChroPrint.println("Binding texture " + texture + ":" + textures[texture] + "...", System.out);
			number.setTexId(textures[texture]);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[texture]);

	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_CLAMP_TO_EDGE);
	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			
//			DEBUG
//			ChroPrint.println("Loading texture " + texture + "...", System.out);
//			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, texSize,
//							texSize, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, number.getTexBuffer());
//			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, texSize,
//					texSize, 0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE, number.getTexBuffer());
//			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE_ALPHA, texSize,
//					texSize, 0, GL10.GL_LUMINANCE_ALPHA, GL10.GL_UNSIGNED_BYTE, number.getTexBuffer());
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, number.getBmpTex(), 0);
			texture++;
			glCheckError(gl);
		}

		gl.glDisable(GL10.GL_BLEND);
		
		ChroPrint.println("Loaded " + textures.length + " textures into OpenGL.", System.out);
		
		return textures;
	}

	/**
	 * @param gl
	 */
	private static void glCheckError(GL10 gl) {
		int error = gl.glGetError();
		if (error != GL10.GL_NO_ERROR) { 
		    ChroPrint.println("GL Texture Load Error: " + error, System.err);
		}
	}

	/**
	 * Return all the textures relevant to a given type.
	 * 
	 * @param ct The bar type for which to return textures.
	 * @return The list of textures relevant to bar type ct.
	 */
	public static ArrayList<ChroTexture> getTexturesForBarType(ChroType ct) {
		return texCache.get(ct);
	}
}
