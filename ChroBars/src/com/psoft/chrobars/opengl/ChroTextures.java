package com.psoft.chrobars.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.util.ChroPrint;

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
	private static ArrayList<ByteBuffer> texCache;
	
	/**
	 * Provides an (int, ByteBuffer) map for when we 
	 *  generate texture ID's and bind them to the OpenGL 
	 *  environment object.
	 */
	private static HashMap<ByteBuffer, Integer> texMap;
	
	/**
	 * This constructs a texture containers for all 
	 *  ChroBars OpenGL number textures.
	 *  
	 * @param numOfTextures The total number of possible textures to be cached.
	 */
	public ChroTextures(int numOfTextures) {
		if(texCache == null) {
			ChroTextures.texCache = new ArrayList<ByteBuffer>(numOfTextures);
			ChroTextures.texMap = new HashMap<ByteBuffer, Integer>(numOfTextures);
//			DEBUG
//			ChroPrint.println("Created new ByteBuffer containers...\n" + texCache + "\t" + texMap, System.out);
		}
	}
	
	/**
	 * Gets the texture name with a given buffer reference.
	 * 
	 * @param buff Get the texture name of this buffer.
	 * @return The texture ID as an integer primitive.
	 */
	public static int getTexId(ByteBuffer buff) {
		return texMap.get(buff);
	}
	
	/**
	 * Clears the texture cache.
	 * 
	 * @see #texCache
	 */
	public static void clean() {
		texCache.clear();
	}
	
	/**
	 * Returns a copy of the current cache for examination and use.
	 * The original cache is left untouched.
	 * 
	 * @return A direct copy of the current cache, 
	 * 				if there is cache in memory.
	 * 				Otherwise returns null.
	 */
	public static ArrayList<ByteBuffer> cache() {
		if(texCache == null)
			return null;
		ArrayList<ByteBuffer> cacheCopy = new ArrayList<ByteBuffer>(texCache.size());
		for(ByteBuffer buf : texCache)
			cacheCopy.add(buf.asReadOnlyBuffer());
		return cacheCopy;
	}

	/**
	 * The original version of this method was posted by code_zombie at:
	 * 
	 * <a href="http://www.gamedev.net/blog/621/entry-2082291-android-development---loading-an-opengl-texture/">gamedev.net - Loading an OpenGL Texture</a>
	 * 
	 * I have modified it slightly in a trivial manner by breaking it up into two methods. The 
	 * 	functionality of the original is entirely intact.
	 * 
	 * @param gl The OpenGL environment object.
	 * @param number The bitmap texture to cache for use in OpenGL.
	 * @return The texture address.
	 */
	public static int loadTextureFromBitmap(GL10 gl, Bitmap number)
	{
		int[] numTexture = new int[1];
		gl.glGenTextures(1, numTexture, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, numTexture[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		
		ByteBuffer numBuffer = cacheTexture(number);
		
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, number.getWidth(),
						number.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, numBuffer);
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
	public static ByteBuffer cacheTexture(Bitmap number) {
		
		ByteBuffer tex = ByteBuffer.allocateDirect(number.getHeight() * number.getWidth() * ChroData._RGBA_COMPONENTS).order(ByteOrder.nativeOrder());
		byte pixelBuffer[] = new byte[ChroData._RGBA_COMPONENTS];
			
		for(int i = 0; i < number.getHeight(); i++) {
			for(int j = 0; j < number.getWidth(); j++) {
				int color = number.getPixel(j, i);
				pixelBuffer[0] = (byte)Color.red(color);
				pixelBuffer[1] = (byte)Color.green(color);
				pixelBuffer[2] = (byte)Color.blue(color);
				pixelBuffer[3] = (byte)Color.alpha(color);
				tex.put(pixelBuffer);
			}
		}
		
//		DEBUG
//		ChroPrint.println("Adding " + tex + "to the texture cache...", System.out);
		synchronized(texCache) {
			ChroTextures.texCache.add((ByteBuffer) tex.position(0));
		}
		
		return tex;
	}
	
	/**
	 * Loads all cached textures into OpenGL.
	 * 
	 * @param gl The OpenGL environment object.
	 * @param texSize The width/height dimension. An exponential of base 2, aka 2^n=texSize.
	 * @return The array of texture names assigned by OpenGL.
	 */
	public static int[] loadTextures(GL10 gl, int texSize) {
		
		int[] textures = new int[texCache.size()];
		
		ChroPrint.println("Generating texture IDs...", System.out);
		gl.glGenTextures(textures.length, textures, 0);
		
		//Texture index
		int texture = 0;
		
		ChroPrint.println("Binding and loading new textures into OpenGL...", System.out);
//		DEBUG
//		ChroPrint.println("Current texture cache: " + texCache, System.out);
		
		for(ByteBuffer number : texCache) {
//			DEBUG
//			ChroPrint.println("Binding texture " + texture + ":" + textures[texture] + "...", System.out);
			texMap.put(number, textures[texture]);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[texture++]);
			
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
//			DEBUG
//			ChroPrint.println("Loading texture " + texture + "...", System.out);
			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, texSize,
							texSize, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, number);
		}
		
		ChroPrint.println("Loaded " + textures.length + " textures into OpenGL.", System.out);
		
		return textures;
	}
}
