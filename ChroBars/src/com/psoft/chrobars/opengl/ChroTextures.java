package com.psoft.chrobars.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL10;
import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * When an instance of this class is created, it caches
 *  the specified texture resources for loading into the 
 *  OpenGL environment.
 * 
 * @author Jonathan Hyry
 */
public class ChroTextures {
	
	/**
	 * This constructs a textures container that caches all 
	 *  OpenGL number textures.
	 *  
	 * @param gl The OpenGL environment object.
	 */
	protected ChroTextures(GL10 gl) {
		
	}

	/**
	 * This method was posted by code_zombie at:
	 * 
	 * <a href="http://www.gamedev.net/blog/621/entry-2082291-android-development---loading-an-opengl-texture/">gamedev.net - Loading an OpenGL Texture</a>
	 * 
	 * I have modified it slightly in a trivial manner. The 
	 * 	functionality of the original is entirely intact.
	 * 
	 * @param gl The OpenGL environment object.
	 * @param number The bitmap texture to cache for use in OpenGL.
	 * @return The texture address.
	 */
	private int loadTextureFromBitmap(GL10 gl, Bitmap number)
	{
		int[] numTexture = new int[1];
		gl.glGenTextures(1, numTexture, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, numTexture[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		ByteBuffer numBuffer = ByteBuffer.allocateDirect(number.getHeight() * number.getWidth() * 4);
		numBuffer.order(ByteOrder.nativeOrder());
		byte pixelBuffer[] = new byte[4];
		for(int i = 0; i < number.getHeight(); i++)
		{
			for(int j = 0; j < number.getWidth(); j++)
			{
				int color = number.getPixel(j, i);
				pixelBuffer[0] = (byte)Color.red(color);
				pixelBuffer[1] = (byte)Color.green(color);
				pixelBuffer[2] = (byte)Color.blue(color);
				pixelBuffer[3] = (byte)Color.alpha(color);
				numBuffer.put(pixelBuffer);
			}
		}
		numBuffer.position(0);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, number.getWidth(),
						number.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, numBuffer);
		return numTexture[0];
	}
}
