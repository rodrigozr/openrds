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
 * INode.java
 * Created by: Rodrigo
 * Created at: Aug 29, 2005 12:31:37 PM
 *
 * $Revision: 1.4 $
 * $Date: 2006/07/27 19:26:53 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * This RMI interface represents a generic node.
 * @author Rodrigo
 */
public interface INode extends Remote, Serializable {
	
	/**
	 * @return the clock amount for this node.
	 * @throws RemoteException on any communication or runtime error.
	 */
	int getClockAmount() throws RemoteException;
	/**
	 * @return the memory amount for this node.
	 * @throws RemoteException on any communication or runtime error.
	 */
	int getMemoryAmount() throws RemoteException;
	/**
	 * @return the name of this node.
	 * @throws RemoteException on any communication or runtime error.
	 */
	String getNodeName() throws RemoteException;
	/**
	 * Process the given requisition and returns it's result.<BR>
	 * Note that the behavior of this method beeing called on a MainNode is to look
	 * for an available ProcessNode and repass the requisition to it.
	 * @param requisition requisition to process
	 * @return requisition's result
	 * @throws RemoteException on any communication or runtime error.
	 */
	Object processRequisition(final Requisition requisition) throws RemoteException;
	/**
	 * Removes this node from registry handler and destroys it.<BR>
	 * Results of trying to use this node after calling this method are
	 * unpredictable.<BR>
	 * This method may result in a RemoteException if it was not possible to notify
	 * the registry or the main node (in the case of a process node).
	 * @throws RemoteException on any communication or runtime error.
	 */
	void finish() throws RemoteException;
	/**
	 * Adds a node event listener to this node.
	 * IMPORTANT: You can only add event listeners on a node started in the same JVM.
	 * Trying to add listeners on a remote node will result in a "RemoteException" beeing
	 * thrown.
	 * @param listener listener to add
	 * @throws RemoteException if this node is Remote.
	 * @since OpenRDS 0.4
	 */
	void addNodeEventListener(INodeEventListener listener) throws RemoteException;
	/**
	 * Removes a node event listener from this node.
	 * IMPORTANT: You can only remove event listeners from a node started in the same JVM.
	 * Trying to remove listeners from a remote node will result in a "RemoteException" beeing
	 * thrown.
	 * @param listener listener to remove
	 * @throws RemoteException if this node is Remote.
	 * @since OpenRDS 0.4
	 */
	void removeNodeEventListener(INodeEventListener listener) throws RemoteException;
}
