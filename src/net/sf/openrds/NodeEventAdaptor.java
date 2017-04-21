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
 * NodeEventAdaptor.java
 * Created by: Rodrigo
 * Created at: Jul 27, 2006 2:56:32 PM
 *
 * $Revision: 1.2 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

/**
 * Empty implementation of INodeEventListener, to avoid having to
 * declare every listener's method.
 * @author Rodrigo
 */
public class NodeEventAdaptor implements INodeEventListener {
	/** {@inheritDoc} */
	public void nodeFinished(INode node) {
	}
	/** {@inheritDoc} */
	public void nodeRequisitionProcessed(INode node, Requisition requisition, Object result) {
	}
	/** {@inheritDoc} */
	public void nodeRequisitionFailed(INode node, Requisition requisition, Throwable error) {
	}
	/** {@inheritDoc} */
	public void nodeLostConnection(INode node) {
	}
	/** {@inheritDoc} */
	public void nodeRestoredConnection(INode node) {
	}
	/** {@inheritDoc} */
	public void nodeRegistered(IMainNode mainNode, IProcessNode registeredNode) {
	}
	/** {@inheritDoc} */
	public void nodeUnregistered(IMainNode mainNode, IProcessNode unregisteredNode) {
	}
}
