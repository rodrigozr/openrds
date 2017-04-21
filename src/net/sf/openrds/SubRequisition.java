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
 * SubRequisition.java
 * Created by: Rodrigo
 * Created at: Aug 29, 2005 1:33:13 PM
 *
 * $Revision: 1.3 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;


/**
 * Represents a sub-requisition to be processed.
 * @author Rodrigo
 */
public abstract class SubRequisition extends IndivisibleRequisition {

	/** {@inheritDoc} */
	public abstract Object process() throws ProcessingException;
}
