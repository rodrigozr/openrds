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
 * DivisibleRequisition.java
 * Created by: Rodrigo
 * Created at: Aug 29, 2005 1:29:06 PM
 *
 * $Revision: 1.4 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;


/**
 * Represents a requisition that can be splitted into many sub-requisitions.
 * @author Rodrigo
 */
public abstract class DivisibleRequisition extends Requisition {
	/**
	 * Process this divisible requisition in a single node.
	 * The default behavior of this method is to call
	 * <code><b>getSubRequisitions(1)[0].process()</b><code>
	 * @return execution result
	 * @throws ProcessingException on any error processing the requisition
	 * @see net.sf.openrds.Requisition#process()
	 */
	public Object process() throws ProcessingException {
		return getSubRequisitions(1)[0].process();
	}
	
	/**
	 * Splits this requisition into many sub-requisitions to be processed
	 * simultaneously on a number of nodes. The order that the requisitions
	 * are processed cannot be guaranteed, since every requisition will be
	 * analized individually by the load balance algorithm.<BR>
	 * Also note that the number of nodes passed is not guaranteed to be real, since
	 * a node can be closed (or crashed) at any time.
	 * @param availableNodes number of nodes available for processing sub-requisitions
	 * @return a number of sub-requisitions to be processed.
	 */
	public abstract SubRequisition[] getSubRequisitions(int availableNodes);
	
	/**
	 * This method will be called when all sub-requisitions have been processed, to
	 * join all sub-results in a single result. The array received will have results
	 * in the exactly the same order that the sub-requisitions where returned by
	 * getSubRequisitions().
	 * @param subResults results from all sub-requisitions
	 * @return consolidated result (this will be the result returned by the MainNode)
	 */
	public abstract Object getResponse(Object[] subResults);
}
