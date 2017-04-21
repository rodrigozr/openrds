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
 * MainNode.java
 * Created by: Rodrigo
 * Created at: Aug 30, 2005 11:46:29 AM
 *
 * $Revision: 1.11 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.rmi.RemoteException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


/**
 * A MainNode is responsible for distributing requisitions to other nodes.
 * @author Rodrigo
 */
class MainNode extends Node implements IMainNode {
	/** Registers if threads are running */
	private boolean running							= true;
	/** Registers if the main node has been finished */
	private boolean finished						= false;
	/** Holds controlled nodes */
	private final Map controlledNodes				= new HashMap();
	/** Requisition channel */
	private final RequisitionChannel channel		= new RequisitionChannel();
	/** Pool of workers */
	private final LinkedList workersPool			= new LinkedList();
	/** Complete list of started workers. */
	private final LinkedList workersList			= new LinkedList();
	/** Thread that consume requisitions */
	private final RequisitionsDispatcher dispatcher	= new RequisitionsDispatcher();
	
	/**
	 * Default constructor
	 * @throws RemoteException if failed to export object.
	 */
	MainNode() throws RemoteException {
		// A main node has hard-coded name and no clock/memory since it never process a requisition
		super("MainNode", -1, -1);
	}
	/**
	 * This method should be called to start the operation of this main node.
	 */
	void start() {
		dispatcher.start();
	}
	/** {@inheritDoc} */
	public void processAsyncRequisition(Requisition requisition) throws RemoteException {
		if (finished) {
			throw new RemoteException("Main node is not running");
		}
		if (requisition instanceof IndivisibleRequisition) {
			final RequisitionWrapper wrap = new RequisitionWrapper((IndivisibleRequisition) requisition);
			channel.put(wrap);
		} else {
			final DivisibleRequisition divisible = (DivisibleRequisition) requisition;
			final int nodes = this.controlledNodes.size();
			final SubRequisition[] subRequisitions = divisible.getSubRequisitions(nodes);
			for (int i = 0; i < subRequisitions.length; i++) {
				channel.put(new RequisitionWrapper(subRequisitions[i]));
			}
		}
	}
	/** {@inheritDoc} */
	public Object processRequisition(Requisition requisition) throws RemoteException {
		if (finished) {
			throw new RemoteException("Main node is not running");
		}
		try {
			if (requisition instanceof IndivisibleRequisition) {
				final RequisitionWrapper wrap = new RequisitionWrapper((IndivisibleRequisition) requisition);
				channel.put(wrap);
				final Object result = wrap.waitProcessing();
				getEventDispacher().nodeRequisitionProcessed(this, requisition, result); // Event
				return result;
			} else {
				final int nodes = this.controlledNodes.size();
				if (nodes > 0) {
					return processDivisibleReq(requisition, nodes);
				}
			}
			throw new NoNodesAvailableException("Could not find any node to process the given requisition.");
		} catch (RemoteException error) {
			getEventDispacher().nodeRequisitionFailed(this, requisition, error); // Event
			throw error;
		}
	}
	/**
	 * Process a divisible requisition
	 * @param requisition requisition
	 * @param nodes number of nodes available
	 * @return processing result
	 * @throws ProcessingException on any processing error
	 * @throws NoNodesAvailableException if we find out that we don't have any node to process anymore
	 */
	private Object processDivisibleReq(Requisition requisition, final int nodes) throws ProcessingException, NoNodesAvailableException {
		final DivisibleRequisition divisible = (DivisibleRequisition) requisition;
		final SubRequisition[] subRequisitions = divisible.getSubRequisitions(nodes);
		final RequisitionWrapper[] wrappers = new RequisitionWrapper[subRequisitions.length];
		final Object[] results = new Object[subRequisitions.length];
		for (int i = 0; i < wrappers.length; i++) {
			wrappers[i] = new RequisitionWrapper(subRequisitions[i]);
			channel.put(wrappers[i]);
		}
		for (int i = 0; i < results.length; i++) {
			results[i] = wrappers[i].waitProcessing();
		}
		final Object result = divisible.getResponse(results);
		getEventDispacher().nodeRequisitionProcessed(this, requisition, result); // Event
		return result;
	}
	/** {@inheritDoc} */
	public void addToControl(IProcessNode processNode) throws RemoteException {
		final RemoteNodeRef ref = new RemoteNodeRef(processNode);
		synchronized (this.controlledNodes) {
			this.controlledNodes.put(ref.name, ref);
			getEventDispacher().nodeRegistered(this, processNode); // Event
		}
	}
	/** {@inheritDoc} */
	public void removeFromControl(IProcessNode processNode) throws RemoteException {
		synchronized (this.controlledNodes) {
			if (this.controlledNodes.remove(processNode.getNodeName()) != null) {
				getEventDispacher().nodeUnregistered(this, processNode); // Event
			}
		}
	}
	/** {@inheritDoc} */
	public boolean controlsNode(String nodeName) throws RemoteException {
		// Not synchronous
		return this.controlledNodes.containsKey(nodeName);
	}
	/** {@inheritDoc} */
	public void finish() throws RemoteException {
		if (!this.finished) {
			this.finished = true;
			waitIdle(); // Waits all pending requisitions to finish
			this.running = false; // This cause workers and dispatcher to stop
			this.dispatcher.interrupt(); // Stops dispatcher
			try {
				RegistryHandler.getInstance().unregisterNode(this);
			} catch (Exception ignored) {
				// We have to stop...
			}
			// Stops all workers
			waitAllWorkers();
			getEventDispacher().nodeFinished(this); // Event
		}
	}
	/** {@inheritDoc} */
	public void waitIdle() throws RemoteException {
		final Object mutex = RequisitionWrapper.getMutex();
		synchronized (mutex) {
			while (RequisitionWrapper.getQtyRemaining() > 0) {
				try {
					mutex.wait();
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
	/** {@inheritDoc} */
	public IProcessNode[] getControlledNodes() throws RemoteException {
		synchronized (this.controlledNodes) {
			final IProcessNode[] nodes = new IProcessNode[this.controlledNodes.size()];
			final Iterator it = this.controlledNodes.values().iterator();
			for (int i = 0; it.hasNext(); i++) {
				nodes[i] = ((RemoteNodeRef) it.next()).node;
			}
			return nodes;
		}
	}
	/**
	 * Waits for all workers to die.
	 */
	private void waitAllWorkers() {
		Iterator it;
		synchronized (workersList) {
			it = workersList.iterator();
		}
		do {
			try {
				while (it.hasNext()) {
					final RequisitionWorker worker = (RequisitionWorker) it.next();
					synchronized (worker) {
						worker.notify();
						worker.interrupt();
					}
					try {
						worker.join();
					} catch (InterruptedException e) {
						// Ignores
					}
				}
				it = null;
			} catch (ConcurrentModificationException e) {
				synchronized (workersList) {
					it = workersList.iterator();
				}
			}
		} while (it != null);
		workersList.clear();
	}
	/**
	 * Gets the next available RequisitionWorker or allocates a new one if necessary
	 * @return RequisitionWorker
	 */
	private RequisitionWorker getWorker() {
		synchronized (this.workersPool) {
			if (this.workersPool.size() == 0) {
				return new RequisitionWorker();
			} else {
				return (RequisitionWorker) this.workersPool.removeFirst();
			}
		}
	}
	/**
	 * Chooses the best node to process a requisition
	 * @param req requisition
	 * @return best node to process it, or null if none is available
	 * @throws NoNodesAvailableException if there are no nodes available
	 */
	private RemoteNodeRef chooseBestNodeFor(RequisitionWrapper req) throws NoNodesAvailableException {
		final boolean considerClock		= (req.getRequition().getProcessingFactor() > 0);
		final boolean considerMem		= (req.getRequition().getMemoryFactor() > 0);
		RemoteNodeRef best				= null;
		RemoteNodeRef lowestQtyReq		= null;
		RemoteNodeRef lowestClockUse	= null;
		RemoteNodeRef lowestMemUse		= null;
		boolean isAnyValid				= false;
		synchronized (this.controlledNodes) {
			if (this.controlledNodes.isEmpty()) {
				throw new NoNodesAvailableException("No nodes available to process the requisition");
			}
			final Iterator it = this.controlledNodes.values().iterator();
			while (it.hasNext()) {
				final RemoteNodeRef ref = (RemoteNodeRef) it.next();
				if (isValidCandidate(ref, considerClock, considerMem)) {
					isAnyValid = true;
					if (lowestQtyReq == null || isBetterQtyOption(ref, lowestQtyReq)) {
						lowestQtyReq = ref; // This node is processing less requisitions
					}
					if (lowestClockUse == null || isBetterOption(ref.usedClockFactor, lowestClockUse.usedClockFactor, ref.clock, lowestClockUse.clock)) {
						lowestClockUse = ref; // This node is a better option
					}
					if (lowestMemUse == null || isBetterOption(ref.usedMemFactor, lowestMemUse.usedMemFactor, ref.mem, lowestMemUse.mem)) {
						lowestMemUse = ref; // This node is a better option
					}
				}
			}
		}
		if (!isAnyValid) {
			return null; // Not a single node is valid, we don't need to go further
		}
		if (considerClock && considerMem) {
			best = (lowestClockUse.qtyReqs <= lowestMemUse.qtyReqs) ? lowestClockUse : lowestMemUse;
		} else if (considerClock) {
			best = lowestClockUse;
			if ((lowestQtyReq != lowestClockUse) && isSmallDifference(lowestQtyReq.usedClockFactor, lowestClockUse.usedClockFactor, req.getRequition().getProcessingFactor())) {
				best = lowestQtyReq; // If there is just a small factor difference, use lowestQtyReq instead
			}
		} else if (considerMem) {
			best = lowestMemUse;
			if ((lowestQtyReq != lowestMemUse) && isSmallDifference(lowestQtyReq.usedMemFactor, lowestMemUse.usedMemFactor, req.getRequition().getMemoryFactor())) {
				best = lowestQtyReq; // If there is just a small factor difference, use lowestQtyReq instead
			}
		} else { // No factors
			best = lowestQtyReq;
		}
		return best;
	}
	/**
	 * Checks if a candidate node is a better option than last-known best
	 * option, based on the number of requisitions being processed.
	 * If the number of requistions is the same, it will then take the decision
	 * looking for the given factors in order: clock, memory, usedClock, usedMemory.
	 * @param candidate candidate node
	 * @param bestNode last-known best option
	 * @return <code>true</code> if this candidate is a better option
	 */
	private boolean isBetterQtyOption(RemoteNodeRef candidate, RemoteNodeRef bestNode) {
		if (candidate.qtyReqs < bestNode.qtyReqs) {
			return true;
		} else if (candidate.qtyReqs == bestNode.qtyReqs) {
			// Matched! Let's look clock factor
			if (candidate.clock > bestNode.clock) {
				return true;
			} else if (candidate.clock == bestNode.clock) {
				// Matched again! Let's look memory factor
				if (candidate.mem > bestNode.mem) {
					return true;
				} else if (candidate.mem == bestNode.mem) {
					// Matched again!! Let's look used clock
					if (candidate.usedClockFactor < bestNode.usedClockFactor) {
						return true;
					} else if (candidate.usedClockFactor == bestNode.usedClockFactor) {
						// Matched again!!! Let's look used memory
						if (candidate.usedMemFactor < bestNode.usedMemFactor) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/**
	 * Checks if a candidate node is a better option than last-known best option
	 * @param candidateUsedFactor candidate's used factor
	 * @param bestUsedFactor best node's used factor
	 * @param candidateFactor candidate's total declared factor
	 * @param bestFactor best node's total declared factor
	 * @return <code>true</code> if this candidate is a better option
	 */
	private boolean isBetterOption(final int candidateUsedFactor, final int bestUsedFactor, final int candidateFactor, final int bestFactor) {
		return (candidateUsedFactor < bestUsedFactor || (candidateUsedFactor == bestUsedFactor && candidateFactor > bestFactor));
	}
	/**
	 * Checks if the factor difference beeing considered is small between the node with lowest
	 * quantity of requisitions and the best candidate 
	 * @param lowestQtyFactor lowestQtyFactor
	 * @param bestCandidateFactor bestCandidateFactor
	 * @param requisitionFactor requisitionFactor
	 * @return boolean
	 */
	private boolean isSmallDifference(int lowestQtyFactor, int bestCandidateFactor, int requisitionFactor) {
		return (lowestQtyFactor - bestCandidateFactor) < (requisitionFactor / 10D);
	}
	/**
	 * Checks if a node is a valid candidate to process a requisition.
	 * A node is not considered as a valid node if any factor that we should
	 * consider has exceeded it's maximum value.
	 * @param candidate candidate node
	 * @param considerClock true if we should consider clock factor
	 * @param considerMem true if we should consider memory factor
	 * @return true if this is a valid candidate
	 */
	private boolean isValidCandidate(final RemoteNodeRef candidate, final boolean considerClock, final boolean considerMem) {
		if (candidate.qtyReqs == 0) {
			// This node is not processing anything, so we can surely use it
			return true;
		} else {
			final boolean clockOk	= !considerClock || candidate.usedClockFactor < candidate.clock;
			final boolean memOk		= !considerMem || candidate.usedMemFactor < candidate.mem;
			// Will return true if both factors are ok
			return (clockOk && memOk);
		}
	}
	/**
	 * This class used like a cache for common node properties, avoiding
	 * an excess of network communication for calling getName, etc...
	 * @author Rodrigo
	 */
	private static final class RemoteNodeRef {
		private final String name;
		private final int clock;
		private final int mem;
		private final int hash;
		private final IProcessNode node;
		// Quantity of requisitions beeing processed by this node
		private int qtyReqs = 0;
		// Total quantity of clock factors beeing processed at this node
		private int usedClockFactor = 0;
		// Total quantity of memory factors beeing processed at this node
		private int usedMemFactor = 0;
		
		/**
		 * Creates a new remote node reference
		 * @param node node
		 * @throws RemoteException on any error
		 */
		private RemoteNodeRef(final IProcessNode node) throws RemoteException {
			this.name	= node.getNodeName();
			this.clock	= node.getClockAmount();
			this.mem	= node.getMemoryAmount();
			this.hash	= this.name.hashCode();
			this.node	= node;
		}
		/** @return clock frequency */
		public int getClock() {
			return clock;
		}
		/** @return memory amount */
		public int getMem() {
			return mem;
		}
		/** {@inheritDoc} */
		public boolean equals(Object obj) {
			if (obj instanceof RemoteNodeRef) {
				return this.name.equals(((RemoteNodeRef) obj).name);
			}
			return obj.equals(this.name);
		}
		/** {@inheritDoc} */
		public int hashCode() {
			return this.hash;
		}
		/** {@inheritDoc} */
		public String toString() {
			return this.name;
		}
	}
	
	/**
	 * This thread is responsible for consuming requisitions to be processed and
	 * dispatching then to their respective targets
	 * @author Rodrigo
	 */
	private final class RequisitionsDispatcher extends Thread {
		/** Default Constructor. */
		private RequisitionsDispatcher() {
			super("Requisitions dispatcher");
		}
		/** {@inheritDoc} */
		public void run() {
			while (running) {
				try {
					// Gets the next requistion to dispatch
					final RequisitionWrapper req = channel.getNext();
					if (req != null) {
						try {
							// Finds the best node to process the requisition
							RemoteNodeRef node = chooseBestNodeFor(req);
							while (node == null) { // All nodes are busy
								synchronized (workersPool) {
									// Let's execute the synchronous check to be sure
									node = chooseBestNodeFor(req);
									if (node == null) {
										// This means that the nodes are really busy and we have to wait
										// until one is available to process this requisition
										workersPool.wait();
									}
								}
							}
							// Sends this requisition to a worker thread, that will wait it to be processed
							final RequisitionWorker worker = getWorker();
							worker.workOn(req, node);
						} catch (NoNodesAvailableException e) {
							// No nodes available... error
							req.setError(e);
						} catch (Throwable t) {
							// Unexpected error (out of memory, etc)
							req.setError(t);
						}
					}
				} catch (InterruptedException e) {
					// Ignores
				} catch (Throwable t) {
					// Exception trap... should never happen
					System.err.println("[OpenRDS] - Requisition dispatcher exception trap.");
					t.printStackTrace(System.err);
				}
			}
		}
	}
	/**
	 * This worker is called by the RequisitionsDispatcher to send the requisition
	 * to a remote node and wait it to be processed.
	 * @author Rodrigo
	 */
	private final class RequisitionWorker extends Thread {
		private RequisitionWrapper req;
		private RemoteNodeRef nodeRef;
		/* Those vars are used to update information at node reference...  */
		private long reqClock	= 0;
		private long reqMem		= 0;
		private int reqQty		= 0;
		
		/** Default Constructor. */
		private RequisitionWorker() {
			super("Requisition worker");
			synchronized (workersList) {
				workersList.addLast(this);
			}
			this.start();
		}
		/**
		 * Works on the given requisition and node
		 * @param req requisiton
		 * @param node node
		 */
		private void workOn(RequisitionWrapper req, RemoteNodeRef node) {
			synchronized (this) {
				this.req = req;
				this.nodeRef = node;
				updateInfo(req);
				this.notify();
			}
			Thread.yield();
		}
		/** {@inheritDoc} */
		public void run() {
			while (running) {
				try {
					synchronized (this) {
						if (req == null) {
							reqClock = 0;
							reqMem = 0;
							reqQty = 0;
							// Waits for a requisition to work on...
							this.wait();
						}
					}
					if (nodeRef != null) {
						// Sends the requisition to be processed at the process node
						final Object result = nodeRef.node.processRequisition(req.getRequition());
						// The requisition has been processed with success, sets the result
						req.setResult(result);
					}
				} catch (InterruptedException ignored) {
					// Ignore... we are finishing main node...
				} catch (Throwable t) {
					// Handles any error
					handleError(t);
				} finally {
					removeReqData();
					synchronized (workersPool) {
						// Adds this worker to be reused in the pool
						workersPool.addLast(this);
						workersPool.notify();
					}
				}
			}
		}
		/**
		 * Removes data about the requisition from the node reference
		 */
		private void removeReqData() {
			if (nodeRef != null) {
				if (nodeRef.qtyReqs == 1 && reqQty == 1) {
					nodeRef.usedClockFactor	= 0;
					nodeRef.usedMemFactor	= 0;
					nodeRef.qtyReqs			= 0;
				} else {
					nodeRef.usedClockFactor	-= reqClock;
					nodeRef.usedMemFactor	-= reqMem;
					nodeRef.qtyReqs			-= reqQty;
				}
			}
			req = null;
			nodeRef = null;
		}
		/**
		 * Updates node information
		 * @param req requistion
		 */
		private void updateInfo(RequisitionWrapper req) {
			reqClock	= req.getRequition().getProcessingFactor();
			reqClock	= reqClock < 0 ? 0 : reqClock;
			reqMem		= req.getRequition().getMemoryFactor();
			reqMem		= reqMem < 0 ? 0 : reqMem;
			reqQty		= 1;
			nodeRef.usedClockFactor	+= reqClock;
			nodeRef.usedMemFactor	+= reqMem;
			nodeRef.qtyReqs			+= reqQty;
		}
		/**
		 * Handles an error in the processing
		 * @param t throwable
		 */
		private void handleError(Throwable t) {
			if (t instanceof RemoteException) {
				if (t instanceof ProcessingException) {
					// A processing error ocurred and the process node is on the same JVM
					// Set that error to be thrown
					req.setError(t);
				} else if (t.getCause() instanceof ProcessingException) {
					// A processing error ocurred and the process node is on a remote JVM
					// Set that error to be thrown
					req.setError(t.getCause());
				} else {
					// This means a sort of communication error... let's remove that node from the list...
					try {
						RegistryHandler.getInstance().unregisterNode(nodeRef.node);
					} catch (Exception e) {
						// Ignores
					}
					synchronized (controlledNodes) {
						controlledNodes.remove(nodeRef.name);
					}
					// ... and put the requisiton back to be processed.
					channel.putOnTop(req);
				}
			} else {
				// Unexpected error
				req.setError(t);
			}
		}
	}
}
