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
import java.util.Hashtable;

/**
 * Local Registry wrapper 
 * @author Rodrigo
 */
final class LocalRegistryHandler extends UnicastRemoteObject implements Registry {
	private final Registry localRegistry;
	private final Hashtable binds = new Hashtable();
	/**
	 * Starts a new registry on this JVM
	 * @param port port
	 * @throws RemoteException on any error
	 */
	public LocalRegistryHandler(int port) throws RemoteException {
		localRegistry = LocateRegistry.createRegistry(port);
		localRegistry.rebind("Registry", this);
	}
	/** {@inheritDoc} */
	public String[] list() throws RemoteException, AccessException {
		synchronized (this.binds) {
			return (String[]) this.binds.keySet().toArray(new String [this.binds.size()]);
		}
	}
	/** {@inheritDoc} */
	public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
		if (this.binds.remove(name) == null) {
			throw new NotBoundException(name + " not bound");
		}
	}
	/** {@inheritDoc} */
	public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
		final Remote obj = (Remote) this.binds.get(name);
		if (obj == null) {
			throw new NotBoundException(name + " not bound");
		}
		return obj;
	}
	/** {@inheritDoc} */
	public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException, AccessException {
		if (this.binds.containsKey(name)) {
			throw new AlreadyBoundException(name + " already bound");
		}
		this.binds.put(name, obj);
	}
	/** {@inheritDoc} */
	public void rebind(String name, Remote obj) throws RemoteException, AccessException {
		this.binds.put(name, obj);
	}
}
