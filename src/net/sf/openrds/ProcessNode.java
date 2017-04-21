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
 * ProcessNode.java
 * Created by: Rodrigo
 * Created at: Aug 30, 2005 11:48:37 AM
 *
 * $Revision: 1.6 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.rmi.RemoteException;


/**
 * Represents a node with the ability of processing a requisition on it's own process.
 * @author Rodrigo
 */
public final class ProcessNode extends Node implements IProcessNode {
	/** Register if this node is active */
	private boolean active = true;
	/** Tells if this node has been registered at startup or not */
	private boolean registered = false;
	/** Sequencial number that register the number of incomming requests from the main node */
	private int lastIncommingRequest = 0;
	/** Connection integrity handler for this node */
	private ConnectionIntegrityHandler connectionHandler;
	
	/**
	 * Default Constructor.
	 * @param name node name
	 * @param clock amount of clock
	 * @param memory amount of memory
	 * @throws RemoteException if failed to export object.
	 */
	public ProcessNode(String name, int clock, int memory) throws RemoteException {
		super(name, clock, memory);
	}
	/** {@inheritDoc} */
	public Object processRequisition(final Requisition requisition) throws RemoteException {
		if (!active) {
			throw new RemoteException("Node not active...");
		}
		lastIncommingRequest++;
		try {
			requisition.onBeforeProcessImpl(this); // Event
			final Object result = requisition.process();
			getEventDispacher().nodeRequisitionProcessed(this, requisition, result); // Event
			return result;
		} catch (ProcessingException e) {
			getEventDispacher().nodeRequisitionFailed(this, requisition, e); // Event
			throw e;
		} catch (Throwable e) {
			getEventDispacher().nodeRequisitionFailed(this, requisition, e); // Event
			// This way we guarantee that any RemoteException other than a ProcessingException
			// represents a communication failure
			throw new ProcessingException("Exception trap from requisition execution", e);
		}
	}
	/** {@inheritDoc} */
	public void finish() throws RemoteException {
		if (active) {
			active = false;
			if (this.connectionHandler != null) {
				this.connectionHandler.interrupt(); // Stops the integrity handler
				this.connectionHandler = null;
			}
			RegistryHandler.getInstance().unregisterNode(this);
			getEventDispacher().nodeFinished(this); // Event
		}
	}
	/**
	 * Initializes this node.
	 * Must be called after it has been registered
	 * @since OpenRDS 0.4
	 */
	public void start() {
		if (this.connectionHandler == null) {
			this.connectionHandler = new ConnectionIntegrityHandler(this);
			// Starts the thread
			this.connectionHandler.start();
		}
	}
	/**
	 * Marks this node as already registered on main node (during startup)
	 */
	void setRegistered() {
		this.registered = true;
	}
	/**
	 * Checks if this node is still active (if finish() hasn't been called).
	 * Note that this is not a method available over RMI, it should be used
	 * only by the JVM that instantiated the node.
	 * @return boolean
	 * @deprecated This will be removed in future versions. Listeners should
	 * be used instead.
	 */
	public boolean isActive() {
		return this.active;
	}
	/**
	 * Retrieves a sequential number that represents the number of incomming
	 * requests processed.
	 * Note that this is not a method available over RMI, it should be used
	 * only be the JVM that instantiated the node.
	 * @return int
	 * @deprecated This will be removed in future versions. Listeners should
	 * be used instead.
	 */
	public int getLastIncommingRequest() {
		return lastIncommingRequest;
	}

	/**
	 * This class is responsible for handling communication errors and/or disconnections
	 * between the main node and a process node. After detecting that the main node has lost
	 * contact with the given node, it will try to restablish the connection.
	 * @author Rodrigo
	 */
	private static final class ConnectionIntegrityHandler extends Thread {
		/* Default values */
		private static final long DEFAULT_VERIFICATION_INTERVAL = 30000;
		private static final long DEFAULT_RECONNECTION_INTERVAL = 5000;
		/** Node beeing handled */
		private final ProcessNode node;
		/** Verification time on a normal situation */
		private final long verificationInterval;
		/** Verification time after any error has been detected */
		private final long reconnectionInterval;
		
		/**
		 * Creates and starts a new integrity handler for the given node.
		 * @param node process node to
		 */
		ConnectionIntegrityHandler(ProcessNode node) {
			super("Connection integrity handler for " + node);
			this.setDaemon(true);
			this.node = node;
			// Checks which intervals to use...
			if (System.getProperty(ISystemProperties.VERIFICATION_INTERVAL) == null) {
				verificationInterval = DEFAULT_VERIFICATION_INTERVAL;
			} else {
				verificationInterval = Long.parseLong(System.getProperty(ISystemProperties.VERIFICATION_INTERVAL));
			}
			if (System.getProperty(ISystemProperties.RECONNECTION_INTERVAL) == null) {
				reconnectionInterval = DEFAULT_RECONNECTION_INTERVAL;
			} else {
				reconnectionInterval = Long.parseLong(System.getProperty(ISystemProperties.RECONNECTION_INTERVAL));
			}
		}
		
		/** {@inheritDoc} */
		public void run() {
			long delay = node.registered ? verificationInterval : reconnectionInterval;
			int lastRequest = 0;
			// Keep running while the node is active
			while (node.active) {
				try {
					final int currentRequest = node.lastIncommingRequest;
					if (currentRequest == lastRequest) {
						// It means that we didn't receive any request since the last
						// verification, so we should check if everything is ok
						checkMainNodeConnection();
					} else {
						// We received at least one request, so we can assume that
						// everything is ok.
						lastRequest = currentRequest;
					}
					if (delay == reconnectionInterval) {
						// This means that we just restored connection
						node.getEventDispacher().nodeRestoredConnection(node); // Event
					}
					delay = verificationInterval;
				} catch (Exception e) {
					if (delay == verificationInterval) {
						// This means that we had connection and just lost it
						node.getEventDispacher().nodeLostConnection(node); // Event
					}
					// Main node and/or registry is unreachable.. reduce delay time
					delay = reconnectionInterval;
				}
				try {
					// Waits for next verification time
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// Ignores
				}
			}
		}
		/**
		 * Checks if the connection with MainNode is ok and restablish control
		 * if needed.
		 * @throws Exception on any error
		 */
		private void checkMainNodeConnection() throws Exception {
			if (!node.registered) { // This node has not been registered yet
				RegistryHandler.getInstance().registerNode(node);
				node.registered = true;
				return; // Okay, we could successfuly register it
			}
			final IMainNode mainNode = RegistryHandler.getInstance().getMainNode();
			// Tries to contact registry and main node...
			if (!mainNode.controlsNode(node.getNodeName())) {
				// It means that we have lost communication with main node
				// but it is now reachable... Register this node again
				try {
					RegistryHandler.getInstance().registerNode(node);
				} catch (NodeAlreadyExistsException e) {
					// This node is already on registry... let's be sure that it is controlled
					// by the main node
					mainNode.addToControl(node);
				}
			}
		}
	}
}
