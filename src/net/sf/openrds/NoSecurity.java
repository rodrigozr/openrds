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
 * NoSecurity.java
 * Created by: Rodrigo
 * Created at: Sep 3, 2005 8:55:58 PM
 *
 * $Revision: 1.3 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * This SecurityManager grants ALL permisions and should NEVER be used on systems
 * with security requirements.
 * @author Rodrigo
 */
final class NoSecurity extends SecurityManager {
	/** {@inheritDoc} */
	public void checkAccept(String host, int port) {
	}
	/** {@inheritDoc} */
	public void checkAccess(Thread t) {
	}
	/** {@inheritDoc} */
	public void checkAccess(ThreadGroup g) {
	}
	/** {@inheritDoc} */
	public void checkAwtEventQueueAccess() {
	}
	/** {@inheritDoc} */
	public void checkConnect(String host, int port, Object context) {
	}
	/** {@inheritDoc} */
	public void checkConnect(String host, int port) {
	}
	/** {@inheritDoc} */
	public void checkCreateClassLoader() {
	}
	/** {@inheritDoc} */
	public void checkDelete(String file) {
	}
	/** {@inheritDoc} */
	public void checkExec(String cmd) {
	}
	/** {@inheritDoc} */
	public void checkExit(int status) {
	}
	/** {@inheritDoc} */
	public void checkLink(String lib) {
	}
	/** {@inheritDoc} */
	public void checkListen(int port) {
	}
	/** {@inheritDoc} */
	public void checkMemberAccess(Class clazz, int which) {
	}
	/** {@inheritDoc} */
	public void checkMulticast(InetAddress maddr) {
	}
	/** {@inheritDoc} */
	public void checkPackageAccess(String pkg) {
	}
	/** {@inheritDoc} */
	public void checkPackageDefinition(String pkg) {
	}
	/** {@inheritDoc} */
	public void checkPermission(Permission perm, Object context) {
	}
	/** {@inheritDoc} */
	public void checkPermission(Permission perm) {
	}
	/** {@inheritDoc} */
	public void checkPrintJobAccess() {
	}
	/** {@inheritDoc} */
	public void checkPropertiesAccess() {
	}
	/** {@inheritDoc} */
	public void checkPropertyAccess(String key) {
	}
	/** {@inheritDoc} */
	public void checkRead(FileDescriptor fd) {
	}
	/** {@inheritDoc} */
	public void checkRead(String file, Object context) {
	}
	/** {@inheritDoc} */
	public void checkRead(String file) {
	}
	/** {@inheritDoc} */
	public void checkSecurityAccess(String target) {
	}
	/** {@inheritDoc} */
	public void checkSetFactory() {
	}
	/** {@inheritDoc} */
	public void checkSystemClipboardAccess() {
	}
	/** {@inheritDoc} */
	public boolean checkTopLevelWindow(Object window) {
		return super.checkTopLevelWindow(window);
	}
	/** {@inheritDoc} */
	public void checkWrite(FileDescriptor fd) {
	}
	/** {@inheritDoc} */
	public void checkWrite(String file) {
	}
	/** {@inheritDoc} */
	public Object getSecurityContext() {
		return super.getSecurityContext();
	}
	/** {@inheritDoc} */
	public ThreadGroup getThreadGroup() {
		return super.getThreadGroup();
	}
}
