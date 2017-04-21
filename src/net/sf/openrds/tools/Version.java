/**
 * OpenRDS - Open Requisition Distribution System
 * Copyright (c) 2006 Rodrigo Zechin Rosauro
 * 
 * This software program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * Version.java
 * Created by: Rodrigo Rosauro
 * Created at: 12/09/2006 23:10:53
 *
 * $Revision: 1.2 $
 * $Date: 2006/09/13 17:19:17 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds.tools;
/**
 * Holds OpenRDS version information during runtime.
 * This tool also has a "Main" that prints the version information
 * to console.
 * @author Rodrigo Rosauro
 * @since OpenRDS 1.1-beta
 */
public class Version {
	// This will be replaced during build
	private static final String VERSION		= "[REPLACE_VERSION]";
	private static final String BUILD_DATE	= "[REPLACE_DATE]";
	
	/**
	 * Gets the OpenRDS build version.
	 * @return String version
	 */
	public static String getVersion() {
		if (VERSION.charAt(0) == '[') {
			return "N/A";
		}
		return VERSION;
	}
	/**
	 * Gets the OpenRDS build date and time.
	 * @return String date and time
	 */
	public static String getBuildDate() {
		if (BUILD_DATE.charAt(0) == '[') {
			return "N/A";
		}
		return BUILD_DATE;
	}
	/**
	 * Prints the version information to standard out.
	 */
	public static void printVersion() {
		final String s = "OpenRDS " + getVersion() + " (Built on " + getBuildDate() + ")";
		System.out.println(s);
	}
	/**
	 * Main (Prints the version information to standard out.)
	 * @param args none
	 */
	public static void main(String[] args) {
		printVersion();
	}

}
