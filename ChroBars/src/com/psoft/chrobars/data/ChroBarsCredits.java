package com.psoft.chrobars.data;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Context;

import com.psoft.chrobars.R;
import com.psoft.chrobars.util.ChroUtilities;

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
		
		for(Field stringId : R.string.class.getFields()) {
			if(stringId.getName().startsWith("about_team")) {
				try {
					credit = chroContext.getString(R.string.class.getField(stringId.getName()).getInt(null));
				}
				catch (Exception unknownEx) { ChroUtilities.printExDetails(unknownEx); }
				credits.add(credit);
			}
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
