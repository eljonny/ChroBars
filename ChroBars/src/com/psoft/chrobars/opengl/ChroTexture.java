/**
 * 
 */
package com.psoft.chrobars.opengl;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;

import com.psoft.chrobars.ChroType;
//import com.psoft.chrobars.util.ChroPrint;

/**
 * This stores a texture buffer object and all 
 *  associated information.
 *  
 * @author jhyry
 */
public class ChroTexture {

	private byte orderIndex;
	private int resId, texId;
	private boolean cacheLater;
	private Bitmap bmpTex;
	private String resName;
	private ByteBuffer texBuffer;
	private ArrayList<ChroType> barsForTex;
	
	/**
	 * 
	 */
	public ChroTexture() {
		setResId(-1); setTexId(-1);
		setResName(null); setTexBuffer(null);
		barsForTex = new ArrayList<ChroType>(4);
	}
	
	/**
	 * 
	 * @param res
	 * @param resN
	 * @param bars
	 */
	public ChroTexture(int res, String resN,
						boolean cacheLater,
						Bitmap texBmp		 ) {
		setResId(res); setResName(resN);
		setTexId(-1); setTexBuffer(null);
		setCacheLater(cacheLater); setBmpTex(texBmp);
		barsForTex = new ArrayList<ChroType>(4);
		determineOrder();
//			DEBUG
//			ChroPrint.println("Set bitmap to " + bmpTex + " from " + texBmp, System.out);
	}

	/**
	 * 
	 * @param res
	 * @param tex
	 * @param resN
	 * @param texBuff
	 * @param bars
	 */
	public ChroTexture(int res, int tex,
						String resN,
						ByteBuffer texBuff,
						ChroType... bars	) {
		setResId(res); setTexId(tex);
		setResName(resN); setTexBuffer(texBuff);
		barsForTex = new ArrayList<ChroType>(4);
		addBars(bars);
	}
	
	private void determineOrder() {
		
	}

	/**
	 * 
	 * @param bars
	 */
	private void addBars(ChroType... bars) {
		for(ChroType t : bars)
			barsForTex.add(t);
	}

	/**
	 * @return the resName
	 */
	public String getResName() {
		return resName;
	}

	/**
	 * @param resName the resName to set
	 */
	public void setResName(String resName) {
		this.resName = resName;
	}

	/**
	 * @return the texBuffer
	 */
	public ByteBuffer getTexBuffer() {
		return texBuffer;
	}

	/**
	 * @param texBuffer the texBuffer to set
	 */
	public ChroTexture setTexBuffer(ByteBuffer texBuffer) {
		this.texBuffer = texBuffer;
		return this;
	}

	/**
	 * @return the texId
	 */
	public int getTexId() {
		return texId;
	}

	/**
	 * @param texId the texId to set
	 */
	public void setTexId(int texId) {
		this.texId = texId;
	}

	/**
	 * @return the resId
	 */
	public int getResId() {
		return resId;
	}

	/**
	 * @param resId the resId to set
	 */
	public void setResId(int resId) {
		this.resId = resId;
	}
	
	/**
	 * 
	 * @param types
	 */
	public void addBarTypes(ChroType... types) {
		addBars(types);
	}
	
	/**
	 * 
	 * @return
	 */
	public ChroType[] getBarTypes() {
		return barsForTex.toArray(new ChroType[barsForTex.size()]);
	}

	/**
	 * @return the cacheLater
	 */
	public boolean isCacheLater() {
		return cacheLater;
	}

	/**
	 * @param cacheLater the cacheLater to set
	 */
	public void setCacheLater(boolean cacheLater) {
		this.cacheLater = cacheLater;
	}

	/**
	 * @return the bmpTex
	 */
	public Bitmap getBmpTex() {
		return bmpTex;
	}

	/**
	 * @param bmpTex the bmpTex to set
	 */
	public void setBmpTex(Bitmap bmpTex) {
		this.bmpTex = bmpTex;
	}

	/**
	 * @return the orderIndex
	 */
	public byte getOrderIndex() {
		return orderIndex;
	}

	/**
	 * @param orderIndex the orderIndex to set
	 */
	public void setOrderIndex(byte orderIndex) {
		this.orderIndex = orderIndex;
	}
}
