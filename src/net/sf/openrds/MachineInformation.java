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
 * MachineInformation.java
 * Created by: Rodrigo
 * Created at: Sep 4, 2006 2:28:41 PM
 *
 * $Revision: 1.2 $
 * $Date: 2006/09/20 00:36:30 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helper class for finding machine's clock and memory amount. This class
 * acts as an isolation layer between the application and the implementation that
 * finds the necessary information.
 * @author Rodrigo
 */
final class MachineInformation {
	
	private static int clock = -1;
	private static int mem = -1;

	/** Private constructor (static helper) */
	private MachineInformation() {
	}
	
	/**
	 * Retrieves the physical memory of this machine in MB.
	 * @return int
	 */
	public static int getMemoryAmount() {
		if (mem == -1) {
			findInformation();
		}
		return mem;
	}

	/**
	 * Retrieves the clock frequency in MHz for this machine.
	 * @return int
	 */
	public static int getClockFrequency() {
		if (clock == -1) {
			findInformation();
		}
		return clock;
	}
	
	/**
	 * Reset cache variables (mostly used for junits)
	 */
	static void reset() {
		mem = -1;
		clock = -1;
	}

	/**
	 * Find and set information about clock and memory for this machine.
	 */
	private static void findInformation() {
		final String strClock = System.getProperty(ISystemProperties.CLOCK_AMOUNT);
		final String strMem = System.getProperty(ISystemProperties.MEMORY_AMOUNT);
		if (strClock != null) {
			clock = Integer.parseInt(strClock);
		}
		if (strMem != null) {
			mem = Integer.parseInt(strMem);
		}
		if (strClock == null || strMem == null) {
			if (new File("/proc/cpuinfo").exists()) { // Linux
				findLinuxInformation();
			} else { // Native (JNI) implementation
				if (strClock == null) {
					clock = OpenRdsJni.getInstance().getClockFrequency();
				}
				if (strMem == null) {
					mem = OpenRdsJni.getInstance().getMemoryAmount();
				}
			}
		}
	}

	/**
	 * Find clock and memory information on linux
	 */
	private static void findLinuxInformation() {
		if (clock == -1) {
			findLinuxClock();
		}
		if (mem == -1) {
			findLinuxMem();
		}
	}
	/**
	 * Find memory information on linux
	 */
	private static void findLinuxMem() {
		final File memInfo = new File("/proc/meminfo");
		try {
			final String s = lookForLinuxProperty(memInfo, "memtotal");
			if (s != null) {
				final int val = Integer.parseInt(s.substring(0, s.length() - 2).trim()); // Removes "kb" or "mb"
				if (s.endsWith("kb")) { // This is the standard
					mem = val / 1024;
				} else if (s.endsWith("mb")) {
					mem = val;
				} else if (s.endsWith("gb")) {
					mem = val * 1024;
				} else {
					throw new RuntimeException("Unknown value of MemTotal: " + s);
				}
			}
			if (mem == -1) {
				throw new RuntimeException("MemTotal not found on /proc/meminfo");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error getting memory information.", e);
		}
	}

	/**
	 * Find clock information on linux
	 */
	private static void findLinuxClock() {
		final File cpuInfo = new File("/proc/cpuinfo");
		try {
			final String val = lookForLinuxProperty(cpuInfo, "mhz");
			if (val != null) {
				clock = (int) Double.parseDouble(val);
			}
			if (clock == -1) {
				throw new RuntimeException("cpu MHz not found on /proc/cpuinfo");
			}
		} catch (Exception e) {
			throw new RuntimeException("Error getting cpu information.", e);
		}
	}
	/**
	 * Finds a property value in a linux information file. (one of /proc/cpuinfo or /proc/meminfo)
	 * @param f file
	 * @param propertyPartName part of the property name to find
	 * @return property value (lower-case, trimmed), or null if not found
	 * @throws IOException on any error reading files
	 */
	private static String lookForLinuxProperty(File f, String propertyPartName) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 512);
		try {
			String s = null;
			while ((s = in.readLine()) != null) {
				s = s.toLowerCase();
				final int valStart = s.indexOf(':');
				final int propIndex = s.indexOf(propertyPartName);
				if (valStart != -1 && propIndex != -1 && propIndex < valStart) {
					return s.substring(valStart + 1).trim();
				}
			}
			return null;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
	}
}
