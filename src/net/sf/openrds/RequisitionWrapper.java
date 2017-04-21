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
 * RequisitionWrapper.java
 * Created by: Rodrigo
 * Created at: Sep 22, 2005 12:01:34 PM
 *
 * $Revision: 1.2 $
 * $Date: 2006/07/27 14:33:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

/**
 * This class wraps a requisiton to maintain some processing status. 
 * @author Rodrigo
 */
final class RequisitionWrapper {
	// STATIC - This controls the number of wrappers that have not been processed yet.
	private static int qtyRemaining = 0;
	// STATIC - Mutex to notify when the number of requisitions is zero (used to finish the Main Node)
	private static final Object MUTEX = new Object();
	
	private final IndivisibleRequisition req;
	private Throwable error;
	private Object result;
	private boolean done;
	/**
	 * Creates a new wrapper for the given requisition
	 * @param req requisition
	 */
	protected RequisitionWrapper(IndivisibleRequisition req) {
		this.req = req;
		RequisitionWrapper.qtyRemaining++;
	}
	/**
	 * Retrieves the wrapped requisition
	 * @return requisition
	 */
	public IndivisibleRequisition getRequition() {
		return req;
	}
	/**
	 * Blocks execution until the wrapped requisition has been processed and returns
	 * it's result.
	 * @return the requisition processing result
	 * @throws ProcessingException if ProcessingException is thrown by the requisition
	 * @throws NoNodesAvailableException NoNodesAvailableException if the requisition could not
	 * be processed because there weren't nodes available to process it.
	 */
	public synchronized Object waitProcessing() throws ProcessingException, NoNodesAvailableException {
		if (!done) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				throw new ProcessingException("Thread has been interrupted!", e);
			}
		}
		if (error != null) {
			if (error instanceof ProcessingException) {
				throw (ProcessingException) error;
			} else if (error instanceof NoNodesAvailableException) {
				throw (NoNodesAvailableException) error;
			} else if (error instanceof RuntimeException) {
				throw (RuntimeException) error;
			} else if (error instanceof Error) {
				throw (Error) error;
			} else {
				throw new Error("BUG: Assertion error", error);
			}
		}
		return result;
	}
	/**
	 * Sets an error that ocurred while processing the requisition
	 * @param error ProcessingException
	 */
	public synchronized void setError(Throwable error) {
		this.error = error;
		done();
	}
	/**
	 * Sets the result of this requisition
	 * @param result requisition result
	 */
	public synchronized void setResult(Object result) {
		this.result = result;
		done();
	}
	/**
	 * Set this wrapper as processed
	 */
	private void done() {
		done = true;
		this.notify();
		if (--RequisitionWrapper.qtyRemaining == 0) {
			synchronized (MUTEX) {
				MUTEX.notifyAll();
			}
		}
	}
	
	/** @return number of wrappers that have not been processed yet. */
	static int getQtyRemaining() {
		return qtyRemaining;
	}
	
	/** @return mutex that is notified when the number of pending requisitions is zero. */
	static Object getMutex() {
		return MUTEX;
	}
}
