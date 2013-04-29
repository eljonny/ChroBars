package com.psoft.chrobars.util;

import java.io.PrintStream;

import android.util.Printer;

/**
 * A printer. Pretty simple.
 * Directs any calls to this printer to stdout.
 * 
 * Static methods are also included for customized stdout/err printing.
 * 
 * 
 * @author Jonathan Hyry
 */
public class ChroPrint implements Printer {

	/**
	 * This method directs the println request to stdout.
	 * 
	 * @see android.util.Printer#println(java.lang.String)
	 */
	@Override
	public void println(String toPrint) {
		System.out.println(toPrint);
	}
	
	/**
	 * Prints to the given stream.
	 * 
	 * @param message The string to print to stdout.
	 * @param stream The stream into which to print.
	 */
	public static void println(String message, PrintStream stream) {
		stream.println(message);
	}
	
	/**
	 * A more structured way of printing to a stream.
	 * 
	 * @param message The main piece of data you want to print.
	 * @param prefix Any piece of information, such as a tag, that you want to precede the data.
	 * @param postfix Any piece of information, such as a tag, that you want to follow the data.
	 * @param delimiter The seperator between the pre/post -fix and the message. Can be null.
	 * @param stream The stream to which you would like to print.
	 */
	public static void println(String message, String prefix,
							   	 String postfix, Character delimiter,
												 	PrintStream stream) {
		if(prefix == null) {
			if(postfix == null)
				stream.println(message);
			else
				stream.println(message + (delimiter != null ? delimiter : "") + postfix);
		}
		else if(postfix == null)
			stream.println(prefix + (delimiter != null ? delimiter : "") + message);
		else
			stream.println(prefix + (delimiter != null ? delimiter : "") +
						   message + (delimiter != null ? delimiter : "") + postfix);
	}
}
