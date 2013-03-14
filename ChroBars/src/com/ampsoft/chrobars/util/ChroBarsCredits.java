package com.ampsoft.chrobars.util;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Context;

import com.ampsoft.chrobars.R;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBarsCredits {

	private int currentCredit = 0;
	private ArrayList<String> credits = new ArrayList<String>();
	private String credit;
	
	/**
	 * 
	 * @param chroContext
	 */
	public ChroBarsCredits(Context chroContext) {
		
		for(Field stringId : R.string.class.getFields())
			if(stringId.getName().startsWith("about_team")) {
				try {
					credit = chroContext.getString(R.string.class.getField(stringId.getName()).getInt(null));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				credits.add(credit);
			}
		
		credits.trimToSize();
	}
	
	/**
	 * 
	 * @return
	 */
	public String nextCredit() {
		
		//If we have gone through the entire list,
		// start over at the beginning.
		if(currentCredit >= credits.size())
			currentCredit = 0;
		//no credits to run through.
		else if(credits.size() < 1)
			return null;
		
		return credits.get(currentCredit++);
	}
	
	/**
	 * 
	 * @return
	 */
	public int numberOfEntries() {
		return credits.size();
	}
}
