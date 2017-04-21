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
 * LocalRegistryHandler.java
 * Created by: Rodrigo
 * Created at: Oct 8, 2005 5:09:06 PM
 *
 * $Revision: 1.3 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Remote Registry wrapper 
 * @author Rodrigo
 */
final class RemoteRegistryHandler extends UnicastRemoteObject implements Registry {
	private final String host;
	private final int port;
	private final Registry readOnlyRegistry;
	
	/**
	 * Creates a new remote registry handler
	 * @param remoteHost remote host
	 * @param port port
	 * @throws RemoteException on any error
	 */
	public RemoteRegistryHandler(String remoteHost, int port) throws RemoteException {
		this.host = remoteHost;
		this.port = port;
		this.readOnlyRegistry = LocateRegistry.getRegistry(this.host, this.port);
	}
	/** {@inheritDoc} */
	public String[] list() throws RemoteException, AccessException {
		try {
			return getRegistry().list();
		} catch (RemoteException e) {
			if (e.getCause() != null) {
				if (e.getCause() instanceof RemoteException) {
					throw (RemoteException) e.getCause();
				}
				if (e.getCause() instanceof AccessException) {
					throw (AccessException) e.getCause();
				}
			}
			throw e;
		}
	}
	/** {@inheritDoc} */
	public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
		try {
			getRegistry().unbind(name);
		} catch (RemoteException e) {
			if (e.getCause() != null) {
				if (e.getCause() instanceof RemoteException) {
					throw (RemoteException) e.getCause();
				}
				if (e.getCause() instanceof NotBoundException) {
					throw (NotBoundException) e.getCause();
				}
				if (e.getCause() instanceof AccessException) {
					throw (AccessException) e.getCause();
				}
			}
			throw e;
		}
	}
	/** {@inheritDoc} */
	public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
		try {
			return getRegistry().lookup(name);
		} catch (RemoteException e) {
			if (e.getCause() != null) {
				if (e.getCause() instanceof RemoteException) {
					throw (RemoteException) e.getCause();
				}
				if (e.getCause() instanceof NotBoundException) {
					throw (NotBoundException) e.getCause();
				}
				if (e.getCause() instanceof AccessException) {
					throw (AccessException) e.getCause();
				}
			}
			throw e;
		}
	}
	/** {@inheritDoc} */
	public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
		try {
			getRegistry().bind(name, obj);
		} catch (RemoteException e) {
			if (e.getCause() != null) {
				if (e.getCause() instanceof RemoteException) {
					throw (RemoteException) e.getCause();
				}
				if (e.getCause() instanceof AlreadyBoundException) {
					throw (AlreadyBoundException) e.getCause();
				}
				if (e.getCause() instanceof AccessException) {
					throw (AccessException) e.getCause();
				}
			}
			throw e;
		}
	}
	/** {@inheritDoc} */
	public void rebind(String name, Remote obj) throws RemoteException, AccessException {
		try {
			getRegistry().rebind(name, obj);
		} catch (RemoteException e) {
			if (e.getCause() != null) {
				if (e.getCause() instanceof RemoteException) {
					throw (RemoteException) e.getCause();
				}
				if (e.getCause() instanceof AccessException) {
					throw (AccessException) e.getCause();
				}
			}
			throw e;
		}
	}
	/**
	 * Gets the remote registry reference
	 * @return Registry
	 * @throws RemoteException on an error
	 */
	private Registry getRegistry() throws RemoteException {
		try {
			return (Registry) readOnlyRegistry.lookup("Registry");
		} catch (NotBoundException e) {
			throw new RemoteException("Remote registry not bound...");
		}
	}
}
