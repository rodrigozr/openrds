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
 * Node.java
 * Created by: Rodrigo
 * Created at: Aug 29, 2005 12:02:23 PM
 *
 * $Revision: 1.4 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This class holds common properties and methods of a node.
 * @author Rodrigo
 */
public abstract class Node extends UnicastRemoteObject implements INode {
	private final String nodeName;
	private final int clockAmount;
	private final int memoryAmount;
	private final EventDispatcher eventDispatcher = new EventDispatcher();
	
	/**
	 * Instantiates a new Node with the given name, clock and memory and exports it
	 * to RMI engine.
	 * @param name node name
	 * @param clock amount of clock of this node
	 * @param memory amount of memory of this node
	 * @throws RemoteException if failed to export object.
	 */
	protected Node(String name, int clock, int memory) throws RemoteException {
		this.clockAmount = clock;
		this.memoryAmount = memory;
		this.nodeName = name;
	}
	/** {@inheritDoc} */
	public int getClockAmount() throws RemoteException {
		return this.clockAmount;
	}
	/** {@inheritDoc} */
	public int getMemoryAmount() throws RemoteException  {
		return this.memoryAmount;
	}
	/** {@inheritDoc} */
	public String getNodeName() throws RemoteException {
		return this.nodeName;
	}
	/** {@inheritDoc} */
	public abstract Object processRequisition(final Requisition requisition) throws RemoteException;
	/** {@inheritDoc} */
	public abstract void finish() throws RemoteException;
	/** {@inheritDoc} */
	public void addNodeEventListener(INodeEventListener listener) throws RemoteException {
		assertLocalCall();
		this.eventDispatcher.add(listener);
	}
	/** {@inheritDoc} */
	public void removeNodeEventListener(INodeEventListener listener) throws RemoteException {
		assertLocalCall();
		this.eventDispatcher.remove(listener);
	}
	/**
	 * Throws "RemoteException" if the current RMI call is not local
	 * @throws RemoteException if the current RMI call is not local
	 */
	protected void assertLocalCall() throws RemoteException {
		try {
			RemoteServer.getClientHost();
			// If we passed to this point it means that this call is remote
			throw new RemoteException("Remote call not allowed.");
		} catch (ServerNotActiveException e) {
			// This means that the call is local, so it's ok
		}
	}
	/**
	 * Retrieves the event dispatcher used to dispach events to listeners
	 * @return dispacher
	 */
	protected INodeEventListener getEventDispacher() {
		return this.eventDispatcher;
	}
	/** {@inheritDoc} */
	public String toString() {
		return this.nodeName;
	}
	/** {@inheritDoc} */
	public boolean equals(Object obj) {
		return this.nodeName.equals(obj.toString());
	}
	/** {@inheritDoc} */
	public int hashCode() {
		return this.nodeName.hashCode();
	}
	
	/**
	 * Node event dispatcher
	 */
	private final class EventDispatcher implements INodeEventListener {
		private final List listeners = new LinkedList();
		/**
		 * Adds a listener
		 * @param listener listener
		 */
		private void add(INodeEventListener listener) {
			synchronized (this.listeners) {
				this.listeners.add(listener);
			}
		}
		/**
		 * Removes a listener
		 * @param listener listener
		 */
		private void remove(INodeEventListener listener) {
			synchronized (this.listeners) {
				this.listeners.remove(listener);
			}
		}
		/** {@inheritDoc} */
		public void nodeFinished(INode node) {
			if (this.listeners.size() > 0) {
				synchronized (this.listeners) {
					for (final Iterator it = this.listeners.iterator(); it.hasNext();) {
						final INodeEventListener listener = (INodeEventListener) it.next();
						try {
							listener.nodeFinished(node);
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		/** {@inheritDoc} */
		public void nodeLostConnection(INode node) {
			if (this.listeners.size() > 0) {
				synchronized (this.listeners) {
					for (final Iterator it = this.listeners.iterator(); it.hasNext();) {
						final INodeEventListener listener = (INodeEventListener) it.next();
						try {
							listener.nodeLostConnection(node);
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		/** {@inheritDoc} */
		public void nodeRestoredConnection(INode node) {
			if (this.listeners.size() > 0) {
				synchronized (this.listeners) {
					for (final Iterator it = this.listeners.iterator(); it.hasNext();) {
						final INodeEventListener listener = (INodeEventListener) it.next();
						try {
							listener.nodeRestoredConnection(node);
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		/** {@inheritDoc} */
		public void nodeRequisitionProcessed(INode node, Requisition requisition, Object result) {
			if (this.listeners.size() > 0) {
				synchronized (this.listeners) {
					for (final Iterator it = this.listeners.iterator(); it.hasNext();) {
						final INodeEventListener listener = (INodeEventListener) it.next();
						try {
							listener.nodeRequisitionProcessed(node, requisition, result);
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		/** {@inheritDoc} */
		public void nodeRequisitionFailed(INode node, Requisition requisition, Throwable error) {
			if (this.listeners.size() > 0) {
				synchronized (this.listeners) {
					for (final Iterator it = this.listeners.iterator(); it.hasNext();) {
						final INodeEventListener listener = (INodeEventListener) it.next();
						try {
							listener.nodeRequisitionFailed(node, requisition, error);
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		/** {@inheritDoc} */
		public void nodeRegistered(IMainNode mainNode, IProcessNode registeredNode) {
			if (this.listeners.size() > 0) {
				synchronized (this.listeners) {
					for (final Iterator it = this.listeners.iterator(); it.hasNext();) {
						final INodeEventListener listener = (INodeEventListener) it.next();
						try {
							listener.nodeRegistered(mainNode, registeredNode);
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		/** {@inheritDoc} */
		public void nodeUnregistered(IMainNode mainNode, IProcessNode unregisteredNode) {
			if (this.listeners.size() > 0) {
				synchronized (this.listeners) {
					for (final Iterator it = this.listeners.iterator(); it.hasNext();) {
						final INodeEventListener listener = (INodeEventListener) it.next();
						try {
							listener.nodeUnregistered(mainNode, unregisteredNode);
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
	}
}
