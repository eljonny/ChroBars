package com.psoft.chrobars.threading;

/**
 * This interface defines method prototypes that the texture caching thread uses.
 * 
 * @author jhyry
 */
public interface IChroLoadThread {

	/**
	 * Increments a progress counter.
	 * 
	 * @param incBy
	 */
	public void incProgress(int incBy);
}
