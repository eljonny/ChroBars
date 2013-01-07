package com.ampsoft.chrobars;

import java.util.Calendar;

/**
 * 
 * @author jhyry
 *
 */
public class ChroUtils {

	/**
	 * 
	 * @return
	 */
	public static String getTimeString() {
		
		return (Calendar.YEAR + "-" + Calendar.MONTH + "-" +
				Calendar.DAY_OF_MONTH + "{" + Calendar.HOUR +
				":" + Calendar.MINUTE + ":" + Calendar.SECOND +
				"." + Calendar.MILLISECOND + "}");
	}
}
