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
 * NoNodesAvailableException.java
 * Created by: Rodrigo
 * Created at: Aug 30, 2005 1:47:06 PM
 *
 * $Revision: 1.2 $
 * $Date: 2006/07/27 14:33:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.rmi.RemoteException;

/**
 * This exception is thrown when no nodes were found for a determined action.
 * @author Rodrigo
 */
public final class NoNodesAvailableException extends RemoteException {

	/**
	 * Default Constructor.
	 * @param message error message
	 */
	public NoNodesAvailableException(String message) {
		super(message);
	}

	/**
	 * Default Constructor.
	 * @param message error message
	 * @param cause error cause
	 */
	public NoNodesAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
