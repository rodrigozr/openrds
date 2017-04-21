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
 * IMainNode.java
 * Created by: Rodrigo
 * Created at: Aug 29, 2005 2:39:26 PM
 *
 * $Revision: 1.3 $
 * $Date: 2006/09/29 17:35:09 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.rmi.RemoteException;

/**
 * Interface for the Main Node implementation.<BR>
 * A main node is responsible for distributing requisitions, controlling process nodes
 * and load-balancing.<BR>
 * All requisitions to be processed must be passed to the main node, and never directly
 * to a process node.<BR>
 * When the main node receives a new requisition, it will determine the best available
 * node to process it and send the requistion for that node, to process it.
 * @author Rodrigo
 */
public interface IMainNode extends INode {
	
	/**
	 * Adds the given requisition to be processed asynchronously.<BR>
	 * Warning: using this method won't give you any result, even
	 * if the requisition fails to process by any reason. The only possible
	 * errors that you get when calling this method are communication errors
	 * while sending the requisition to the main node.
	 * @param requisition requisition to be processed asynchronously
	 * @throws RemoteException on any communication error
	 */
	void processAsyncRequisition(final Requisition requisition) throws RemoteException;
	/**
	 * Adds the given process node to be controlled by this main node.
	 * Client applications should never call this method directly, since
	 * it is only used by the process node itself.
	 * @param processNode node to be controlled.
	 * @throws RemoteException on any communication or runtime error
	 */
	void addToControl(IProcessNode processNode) throws RemoteException;
	/**
	 * Removes the given process node from beeing controlled by this main node.
	 * Client applications should never call this method directly, since
	 * it is only used by the process node itself.
	 * @param processNode node to be removed.
	 * @throws RemoteException on any communication or runtime error
	 */
	void removeFromControl(IProcessNode processNode) throws RemoteException;
	/**
	 * Checks if this main node controls a node with the given name
	 * @param nodeName node name
	 * @return true if the given node is controlled by this main node.
	 * @throws RemoteException on any communication or runtime error
	 */
	boolean controlsNode(String nodeName) throws RemoteException;
	/**
	 * Retrieves the list of nodes beeing controlled by this main node.<BR>
	 * Please note that it is impossible to ensure that all nodes are reachable.
	 * @return IProcessNode[] nodes
	 * @throws RemoteException on any communication or runtime error
	 */
	IProcessNode[] getControlledNodes() throws RemoteException;
	/**
	 * Waits this main node to be IDLE (with no pending requisitions).<BR>
	 * Be aware that this method can block forever since the main node can
	 * be constantly receiving requisitions.
	 * @throws RemoteException on any communication or runtime error
	 * @since OpenRDS 0.3
	 */
	void waitIdle() throws RemoteException;
}
