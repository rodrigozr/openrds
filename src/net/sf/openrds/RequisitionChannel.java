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
 * RequisitionChannel.java
 * Created by: Rodrigo
 * Created at: Sep 21, 2005 5:39:39 PM
 *
 * $Revision: 1.2 $
 * $Date: 2006/07/27 14:33:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.util.LinkedList;

/**
 * Implements a FIFO channel (queue) of requisitions
 * @author Rodrigo
 */
final class RequisitionChannel {
	/** Internal queue */
	private final LinkedList queue = new LinkedList();
	
	/**
	 * Puts a requisition in this channel
	 * @param req requisition
	 */
	public synchronized void put(RequisitionWrapper req) {
		queue.addLast(req);
		notify();
	}
	/**
	 * Puts a requisition on the top of this channel
	 * @param req requisition
	 */
	public synchronized void putOnTop(RequisitionWrapper req) {
		queue.addFirst(req);
		notify();
	}
	/**
	 * Gets the next requisition in the channel, it the channel is empty, then
	 * this method will block until a requisition is available
	 * @return next requisition in the channel
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public synchronized RequisitionWrapper getNext() throws InterruptedException {
		if (this.queue.isEmpty()) {
			this.wait();
		}
		if (!this.queue.isEmpty()) {
			return (RequisitionWrapper) queue.removeFirst();
		}
		return null;
	}
	/**
	 * Gets the next requisition in the channel, it the channel is empty, then
	 * this method will block until a requisition is available
	 * @param timeOut wait time-out (in millis)
	 * @return next requisition in the channel or null if time-out has been reached
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public synchronized RequisitionWrapper getNext(int timeOut) throws InterruptedException {
		RequisitionWrapper returnData = null;
		if (this.queue.isEmpty()) {
			this.wait(timeOut);
			if (!this.queue.isEmpty()) {
				returnData = (RequisitionWrapper) this.queue.removeFirst();
			}
		} else {
			returnData = (RequisitionWrapper) this.queue.removeFirst();
		}
		return returnData;
	}
	/**
	 * Checks how many requisitions are in the channel
	 * @return integer
	 */
	public synchronized int size() {
		return this.queue.size();
	}
	/**
	 * Checks if the queue has any element to be consumed.
	 * @return true if any element is readily available to be consumed.
	 */
	public synchronized boolean hasNext() {
		return !this.queue.isEmpty();
	}
}
