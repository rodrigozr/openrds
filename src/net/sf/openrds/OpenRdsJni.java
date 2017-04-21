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
 * OpenRdsJni.java
 * Created by: Rodrigo
 * Created at: Sep 20, 2005 4:02:30 PM
 *
 * $Revision: 1.2 $
 * $Date: 2006/07/27 14:33:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */
package net.sf.openrds;

/**
 * This class holds JNI methods for OpenRDS.
 * @author Rodrigo
 */
final class OpenRdsJni {
	/** Singleton instance */
	private static OpenRdsJni instance = null;
	/**
	 * Singleton constructor... loads JNI library.
	 */
	private OpenRdsJni() {
		System.loadLibrary("OpenRDS");
	}
	/**
	 * JNI method to retrieve physical memory amount from SO
	 * @return physical memory in Mega Bytes
	 */
	public native int getMemoryAmount();
	/**
	 * JNI method to retrieve processor clock frequency from SO
	 * @return clock frequency in MHz. (eg: 2400 -> 2.4GHz)
	 */
	public native int getClockFrequency();
	/**
	 * Retrieves the singleton instance
	 * @return OpenRdsJni
	 */
	static OpenRdsJni getInstance() {
		if (instance == null) {
			synchronized (OpenRdsJni.class) {
				instance = new OpenRdsJni();
			}
		}
		return instance;
	}
}
