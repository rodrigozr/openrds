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
 * NodeFactory.java
 * Created by: Rodrigo
 * Created at: Aug 30, 2005 12:54:21 PM
 *
 * $Revision: 1.7 $
 * $Date: 2006/09/29 17:35:37 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.io.IOException;
import java.rmi.RemoteException;


/**
 * This class can be used to help starting a main node and/or a process node, setting up
 * a distributed system.
 * In most cases, applications will need just the helper methods on this class
 * to setup a distributed system, but applications that require more control
 * over the system flow may want to initialize node classes manually.<BR>
 * This class does basically 4 things:
 * <li>Instantiates a Main/Process Node</li>
 * <li>Starts a new registry or connect to a remote registry</li>
 * <li>Starts an http daemon for allowing dynamic class download (if wanted)</li>
 * <li>Register instantiated objects</li>
 * @author Rodrigo
 */
public final class NodeFactory {
	
	/** Singleton instance */
	private static final NodeFactory INSTANCE = new NodeFactory();

	/** Singleton constructor */
	private NodeFactory() {
	}
	
	/**
	 * Creates a MainNode able to distribute requisitions over the network
	 * to other nodes, and starts a new local registry.
	 * The started main node will allow dynamic class downloads.<BR>
	 * Please note that this implementation will use the default values for the
	 * registry port and http port, but you can change it if necessary by calling
	 * <code>RegistryHandler.getInstance().initialize(registryPort, httpPort)</code>
	 * BEFORE starting the main node.
	 * @return the newly created and registered node
	 * @throws RemoteException if failed to export or register object
	 * @throws IOException if any error prevents the initialization of the registry and/or http daemon.
	 * @throws NodeAlreadyExistsException if there is an started MainNode already.
	 * @since OpenRDS 1.1-beta
	 */
	public IMainNode startMainNode() throws RemoteException, IOException, NodeAlreadyExistsException {
		return startMainNode(true);
	}
	/**
	 * Creates a MainNode able to distribute requisitions over the network
	 * to other nodes, and starts a new local registry.<BR>
	 * Please note that this implementation will use the default values for the
	 * registry port and http port, but you can change it if necessary by calling
	 * <code>RegistryHandler.getInstance().initialize(registryPort, httpPort)</code>
	 * @param allowDynamicClassDownload if true, an http daemon will be started to
	 * upload classes to client nodes. 
	 * @return the newly created and registered node
	 * @throws RemoteException if failed to export or register object
	 * @throws IOException if any error prevents the initialization of the registry and/or http deadaemonmon.
	 * @throws NodeAlreadyExistsException if there is an started MainNode already.
	 */
	public IMainNode startMainNode(final boolean allowDynamicClassDownload) throws RemoteException, IOException, NodeAlreadyExistsException {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new NoSecurity());
		}
		if (allowDynamicClassDownload) {
			setCodebase();
		}
		final MainNode node = new MainNode();
		final int httpPort = allowDynamicClassDownload ? RegistryHandler.HTTP_PORT : -1;
		RegistryHandler.getInstance().initialize(RegistryHandler.REGISTRY_PORT, httpPort);
		RegistryHandler.getInstance().registerNode(node);
		node.start();
		return node;
	}
	/**
	 * Starts a new process node and register it to the given remote host.
	 * This method will use a JNI interface for getting the processor clock and memory amount
	 * so be aware that an UnsatisfiedLinkError may be thrown.
	 * @param remoteHost remote host
	 * @param testConnection boolean indicating if the conectivity should be tested
	 * @return the started node
	 * @throws RemoteException if testConnection is true and the registry or the MainNode could not be contacted
	 * @throws IOException on any IO error
	 * @throws NodeAlreadyExistsException if there is a node already started.
	 */
	public IProcessNode startProcessNode(String remoteHost, boolean testConnection) throws RemoteException, IOException, NodeAlreadyExistsException {
		return startProcessNode(remoteHost, RegistryHandler.REGISTRY_PORT, RegistryHandler.HTTP_PORT, testConnection);
	}
	/**
	 * Starts a new process node and register it to the given remote host.
	 * This method may use a JNI interface for getting the processor clock and memory amount
	 * so be aware that an UnsatisfiedLinkError may be thrown.
	 * @param remoteHost remote host
	 * @param registryPort port where the registry is located
	 * @param httpPort port for dynamic class download
	 * @param testConnection boolean indicating if the conectivity should be tested
	 * @return the started node
	 * @throws RemoteException if testConnection is true and the registry or the MainNode could not be contacted
	 * @throws IOException on any IO error
	 * @throws NodeAlreadyExistsException if there is a node already started.
	 */
	public IProcessNode startProcessNode(String remoteHost, int registryPort, int httpPort, boolean testConnection) throws RemoteException, IOException, NodeAlreadyExistsException {
		RegistryHandler.getInstance().initialize(remoteHost, registryPort, httpPort);
		final ProcessNode node = createProcessNode();
		if (testConnection) {
			RegistryHandler.getInstance().registerNode(node);
			node.setRegistered();
		}
		node.start();
		return node;
	}
	/**
	 * Creates a new process node but does not start it. The client application can start it later,
	 * after performing any necessary modification, such as adding node listeners to it.<BR>
	 * Just ensure that <code>RegistryHandler.getInstance().initialize()</code> is called before
	 * starting the process node, or it will not be able to connect to the main node.<BR>
	 * This method may use a JNI interface for getting the processor clock and memory amount
	 * so be aware that an UnsatisfiedLinkError may be thrown.
	 * @return the process node
	 * @throws IOException on any error detecting local IP address
	 * @since OpenRDS 1.1-beta
	 * @see RegistryHandler#initialize(String, int, int)
	 * @see ProcessNode#start()
	 */
	public ProcessNode createProcessNode() throws IOException {
		final int clock	= MachineInformation.getClockFrequency();
		final int mem	= MachineInformation.getMemoryAmount();
		final String ip = RegistryHandler.getInstance().getInetAddress().getHostAddress();
		final String nodeName = ip + "|" + clock + "|" + mem;
		return new ProcessNode(nodeName, clock, mem);
	}
	/**
	 * Automatically finds and sets the RMI codebase
	 * @throws IOException on any IO error
	 */
	private void setCodebase() throws IOException {
		final String ip = RegistryHandler.getInstance().getInetAddress().getHostAddress();
		// This codebase is annotated on every object that this JVM exports using Registry.bind()
		// and is only used for remote downloading STUBS.
		// It is different than the "java.rmi.server.codebase" witch tells the JVM where to look
		// for ANY object needed during an RMI call.
		System.setProperty("java.rmi.codebase", "http://" + ip + ":" + RegistryHandler.HTTP_PORT + "/");
	}
	/**
	 * Retrieves the singleton instance of this class
	 * @return NodeFactory singleton intance
	 */
	public static NodeFactory getInstance() {
		return INSTANCE;
	}
}
