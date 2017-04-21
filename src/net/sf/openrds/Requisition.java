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
 * Requisition.java
 * Created by: Rodrigo
 * Created at: Aug 29, 2005 12:14:24 PM
 *
 * $Revision: 1.4 $
 * $Date: 2006/09/29 21:22:10 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.io.Serializable;

/**
 * Represents a requisition that can be serialized thru the
 * network to be processed at any remote machine.
 * @see net.sf.openrds.DivisibleRequisition
 * @see net.sf.openrds.IndivisibleRequisition
 * @author Rodrigo
 * @since OpenRDS 1.1-beta (public version)
 */
public abstract class Requisition implements Serializable {
	
	/** Protected constructor. */
	protected Requisition() {
	}
	
	/**
	 * Process this requisition and returns the result of the execution.
	 * @return execution result
	 * @throws ProcessingException on any error processing the requisition
	 */
	public abstract Object process() throws ProcessingException;
	
	/**
	 * This method is called by the process node just before calling "process()".
	 * This can be used by the requisition to get information about the node where
	 * it will be processed.
	 * @param node node that will process this requisition
	 * @throws Exception any exception thrown by this method will be ignored (Errors won't)
	 * @since OpenRDS 0.4
	 */
	public void onBeforeProcess(IProcessNode node) throws Exception {
		node.getClass(); // just to avoid checkstyle warning
	}
	
	/**
	 * Called by process node. See onBeforeProcess()
	 * @param node node
	 * @see #onBeforeProcess(IProcessNode)
	 */
	final void onBeforeProcessImpl(IProcessNode node) {
		try {
			onBeforeProcess(node);
		} catch (Exception ignored) {
		}
	}
}
