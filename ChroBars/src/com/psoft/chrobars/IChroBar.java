package com.psoft.chrobars;

import java.nio.ByteOrder;

/**
 * @author jhyry
 */
public interface IChroBar {

	/**
	 * 
	 * @param order
	 */
	public void barGLAllocate(ByteOrder order);

	/**
	 * 
	 */
	public void initVertices();
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void initNormals() throws Exception;
	
	/**
	 * 
	 * @param leftXCoord
	 * @param rightXCoord
	 */
	public void setBarWidth(float leftXCoord, float rightXCoord);

	/**
	 * 
	 * @param height
	 */
	public void setBarHeight(float height);
	
	/**
	 * Accessor for the length of the
	 *  bar-specific draw vertices sequence buffer to be used.
	 * @return An integer representation of the length of the draw sequence buffer.
	 */
	public int getBarDrawSequenceBufferLength();
	
	/**
	 * 
	 * @return
	 */
	public int getEdgeDrawSequenceBufferLength();
}
