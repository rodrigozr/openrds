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
 * OpenRDS.cpp
 * Created by: Rodrigo
 * Created at: Sep 20, 2005 4:07:10 PM
 *
 * This module implements JNI methods of OpenRdsJni.java
 * 
 * $Revision: 1.5 $
 * $Date: 2007/01/09 20:40:30 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */
 
#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers
#include <stdio.h>
#include <windows.h>
#include "net_sf_openrds_OpenRdsJni.h"

#ifdef WIN32
/*
 * Calculates the processor clock on non-NT windows (windows 98)
 */
static jint calculateProcessorClock() {
	/*
		RdTSC:
		It's the Pentium instruction "ReaD Time Stamp Counter". It measures the
		number of clock cycles that have passed since the processor was reset, as a
		64-bit number. That's what the <CODE>_emit lines do.
	*/
	#define RdTSC __asm _emit 0x0f __asm _emit 0x31
	// RZR: Change this value to a value greater than ZERO to reduce measure time
	// but will also reduce the precision of this code
	#define REDUCTOR 1
	// variables for the clock-cycles:
	__int64 cyclesStart = 0, cyclesStop = 0;
	// variables for the High-Res Preformance Counter:
	unsigned __int64 nCtr = 0, nFreq = 0, nCtrStop = 0;
    
	// retrieve performance-counter frequency per second:
    if(!QueryPerformanceFrequency((LARGE_INTEGER *) &nFreq)) return -1;
    // retrieve the current value of the performance counter:
    QueryPerformanceCounter((LARGE_INTEGER *) &nCtrStop);
    // reducts measurement time...
	nFreq /= REDUCTOR;
	// add the frequency to the counter-value:
    nCtrStop += nFreq;
    _asm
        {// retrieve the clock-cycles for the start value:
            RdTSC
            mov DWORD PTR cyclesStart, eax
            mov DWORD PTR [cyclesStart + 4], edx
        }

        do{
	        // retrieve the value of the performance counter
	        // until 1 sec has gone by:
			QueryPerformanceCounter((LARGE_INTEGER *) &nCtr);
        }while (nCtr < nCtrStop);

    _asm
        {// retrieve again the clock-cycles after 1 sec. has gone by:
            RdTSC
            mov DWORD PTR cyclesStop, eax
            mov DWORD PTR [cyclesStop + 4], edx
        }
	// stop-start is speed in Hz divided by 1,000,000 is speed in MHz
	// multipling by REDUCTOR we get the real value (aproximated)
	return (jint) (((float)cyclesStop-(float)cyclesStart) / 1000000)*REDUCTOR;
}
/*
 * WIN32 DLL MAIN
 */
BOOL APIENTRY DllMain( HANDLE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved) {
	return TRUE;
}
#endif


/*
 * Gets the physical memory amount (in MB)
 *
 * Class:     net_sf_openrds_OpenRdsJni
 * Method:    getMemoryAmount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_openrds_OpenRdsJni_getMemoryAmount(JNIEnv *env, jobject obj) {
#ifdef WIN32
	MEMORYSTATUS memStatus;
	GlobalMemoryStatus(&memStatus);
	jint memoryInMegaBytes = (memStatus.dwTotalPhys / 1024 / 1024);
	return memoryInMegaBytes;
#endif
}
/*
 * Calculates the processor clock frequency (in MHz)
 *
 * Class:     net_sf_openrds_OpenRdsJni
 * Method:    getClockFrequency
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_openrds_OpenRdsJni_getClockFrequency(JNIEnv *env, jobject obj) {
#ifdef WIN32
	DWORD dwMHz = 0;
	DWORD bufSize = sizeof(DWORD);
	HKEY hKey;
	long lError;

	// open the key where the proc speed is hidden.
	lError = RegOpenKeyEx(HKEY_LOCAL_MACHINE,
				"HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
				0,
				KEY_READ,
				&hKey);
	if(lError == ERROR_SUCCESS) { // This won't work on Windows 98
		lError = RegQueryValueEx(hKey, "~MHz", NULL, NULL, (LPBYTE) &dwMHz, &bufSize);
		if(lError == ERROR_SUCCESS) {
			// Got the value, so we return it
			return (jint) dwMHz;
		}
	}
    // If the key is not found, we calculate it
    return calculateProcessorClock();
#endif
}
