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
 * IndivisibleRequisition.java
 * Created by: Rodrigo
 * Created at: Aug 29, 2005 1:31:52 PM
 *
 * $Revision: 1.3 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;


/**
 * Represents a requisition to be processed on a single node.
 * @author Rodrigo
 */
public abstract class IndivisibleRequisition extends Requisition {

	/** {@inheritDoc} */
	public abstract Object process() throws ProcessingException;
	
	/**
	 * This method should return an estimated factor of processor time used by
	 * this requisition. This factor will be used by the load balance algorithm
	 * to determine the best node to process this requisition.<BR>
	 * If the value returned is less than 1 (default behaviour), then this factor
	 * will be absolutely ignored by the load balance system.
	 * @return estimated factor of processor time used by this requisiton.
	 */
	public int getProcessingFactor() {
		return -1;
	}
	/**
	 * This method should return an estimated factor of memory used by
	 * this requisition. This factor will be used by the load balance algorithm
	 * to determine the best node to process this requisition.<BR>
	 * If the value returned is less than 1 (default behaviour), then this factor
	 * will be absolutely ignored by the load balance system.
	 * @return estimated factor of memory used by this requisiton.
	 */
	public int getMemoryFactor() {
		return -1;
	}
}
