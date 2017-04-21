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
 * INodeEventListener.java
 * Created by: Rodrigo
 * Created at: Jul 27, 2006 1:56:18 PM
 *
 * $Revision: 1.1 $
 * $Date: 2006/07/27 19:07:29 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

/**
 * Interface for listening to node events.
 * @author Rodrigo
 * @see net.sf.openrds.INode#addNodeEventListener(INodeEventListener)
 * @see net.sf.openrds.INode#removeNodeEventListener(INodeEventListener)
 * @since OpenRDS 0.4
 */
public interface INodeEventListener {
	/**
	 * Called when a node is finished.<BR>
	 * Works for both IMainNode and IProcessNode
	 * @param node node that generated this event
	 */
	void nodeFinished(INode node);
	/**
	 * Called when a node finishs processing a requisition with success.<BR>
	 * IMPORTANT: On IMainNode this works only when calling <code>IMainNode.processRequisition()</code>
	 * and not when calling <code>IMainNode.processAsyncRequisition()</code>
	 * @param node node that generated this event
	 * @param requisition requisition that has been processed
	 * @param result requisition result
	 */
	void nodeRequisitionProcessed(INode node, Requisition requisition, Object result);
	/**
	 * Called when a node fails to process a requisition.<BR>
	 * IMPORTANT: On IMainNode this works only when calling <code>IMainNode.processRequisition()</code>
	 * and not when calling <code>IMainNode.processAsyncRequisition()</code>
	 * @param node node that generated this event
	 * @param requisition requisition that has been processed
	 * @param error error detected during processing
	 */
	void nodeRequisitionFailed(INode node, Requisition requisition, Throwable error);
	/**
	 * Called when a communication failure with the main node is detected.<BR>
	 * Works only for IProcessNode
	 * @param node node that generated this event
	 */
	void nodeLostConnection(INode node);
	/**
	 * Called when the connection with the main node is restored.<BR>
	 * Works only for IProcessNode
	 * @param node node that generated this event
	 */
	void nodeRestoredConnection(INode node);
	/**
	 * Called when a process node is added to main node's control.<BR>
	 * Works only for IMainNode
	 * @param mainNode main node that generated this event
	 * @param registeredNode node that has been added to control
	 */
	void nodeRegistered(IMainNode mainNode, IProcessNode registeredNode);
	/**
	 * Called when a process node is removed from main node's control.<BR>
	 * Works only for IMainNode
	 * @param mainNode main node that generated this event
	 * @param unregisteredNode node that has been removed from control
	 */
	void nodeUnregistered(IMainNode mainNode, IProcessNode unregisteredNode);
}
