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
 * RegistryHandler.java
 * Created by: Rodrigo
 * Created at: Aug 28, 2005 1:11:40 AM
 * 
 * $Revision: 1.8 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */
package net.sf.openrds;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.Enumeration;

/**
 * This class is responsible for the RMI Registry control.
 * It is able to start a new registry, control objects on that register and
 * finish it when necessary.
 * @author Rodrigo
 */
public final class RegistryHandler {
	/** Default port for the RMI Registry */
	public static final int REGISTRY_PORT = 1099;
	/** Default port for the Http Daemon used for dynamic class downloads */
	public static final int HTTP_PORT = 1098;
	/** Singleton instance */
	private static final RegistryHandler INSTANCE = new RegistryHandler();
	/** Initialized registry instance */
	private Registry registry;
	/** Http daemon for dynamic class download */
	private SimpleRmiHttpServer httpServer;
	/** Registers if the registry handler has been initialized */
	private boolean initialized;
	/** IP Address end-point */
	private InetAddress inetAddress;
	
	/** Private constructor to ensure singleton instance */
	private RegistryHandler() {
	}
	
	
	/**
	 * Retrieves InetAddress that is being used by OpenRDS. This takes in consideration
	 * the System-Property "openrds.base.ip" (if it's set).
	 * @return InetAddress
	 * @throws IOException on any error getting network interfaces
	 * @since OpenRDS 1.1-beta
	 */
	public InetAddress getInetAddress() throws IOException {
		if (inetAddress == null) {
			bindInetAddress();
		}
		return inetAddress;
	}


	/**
	 * Detects and binds the correct InetAddress
	 * @throws IOException on any IO error
	 */
	private synchronized void bindInetAddress() throws IOException {
		if (inetAddress == null) {
			setRmiTimeout();
			final String baseIp = System.getProperty(ISystemProperties.BASE_IP);
			final Enumeration itfs = NetworkInterface.getNetworkInterfaces();
			InetAddress candidate = null;
			while (itfs.hasMoreElements() && (inetAddress == null)) {
				final NetworkInterface itf = (NetworkInterface) itfs.nextElement();
				final Enumeration addresses = itf.getInetAddresses();
				while (addresses.hasMoreElements() && (inetAddress == null)) {
					final InetAddress address = (InetAddress) addresses.nextElement();
					if (address.getHostAddress().indexOf(':') != -1) {
						// This seens to be an IPv6 address, let's ignore it.
						// This must be revised in future versions
						continue;
					}
					if (baseIp == null || address.getHostAddress().startsWith(baseIp)) { 
						if (address.getHostAddress().equals(baseIp)) {
							inetAddress = address; // Exact match, don't look further
						} else if (!address.isLoopbackAddress() || baseIp != null) {
							candidate = address; // Found a candidate
						}
					}
				}
			}
			if (inetAddress == null) {
				if (candidate != null) {
					inetAddress = candidate; // Use the best candidate
				} else { // Last option... Use "localhost"
					inetAddress = InetAddress.getLocalHost();
				}
			}
			// This System property defines which IP will be annotated on exported objects' stubs.
			// Machines that use 2 or more network interfaces may have problems if this is not set.
			System.setProperty("java.rmi.server.hostname", inetAddress.getHostAddress());
		}
	}
	
	/**
	 * Overrides default RMISocketFactory with a socket factory that implements
	 * socket connect timeouts. This prevents RMI calls from taking a long time
	 * to fail when a node crashes.
	 */
	private void setRmiTimeout() {
		try {
			final String prop = System.getProperty(ISystemProperties.CONNECT_TIMEOUT);
			final int timeout = (prop != null) ? Integer.parseInt(prop) : 10000; // Default is 10000
			RMISocketFactory.setSocketFactory(new TimeoutSocketFactory(timeout));
		} catch (Exception e) {
		}
	}


	/**
	 * Initializes a new RMI registry on the current JVM, on the default port.<BR>
	 * The initialized registry will have access to all classes available in the
	 * classpath of this JVM.<BR>
	 * Note that this will also start an http daemon for answering remote 
	 * "Dynamic Class Download" requests on the default port.<BR>
	 * Calling this method has the same effect of calling
	 * <b><code>initialize(REGISTRY_PORT, HTTP_PORT)</code></b><BR>
	 * This method is NOT thread safe, results of two threads trying to access
	 * it at the same time are unpredictable.
	 * @throws RemoteException if any error prevented the registry startup
	 * @throws IOException if any error prevented the http daemon startup
	 * @see #REGISTRY_PORT
	 * @see #HTTP_PORT
	 */
	public void initialize() throws RemoteException, IOException {
		this.initialize(REGISTRY_PORT, HTTP_PORT);
	}
	/**
	 * Initializes a new RMI registry on the current JVM, on the given registry port.<BR>
	 * The initialized registry will have access to all classes available in the
	 * classpath of this JVM.<BR>
	 * Note that this will also start an http daemon for answering remote 
	 * "Dynamic Class Download" requests on the given http port.<BR>
	 * This method is NOT thread safe, results of two threads trying to access
	 * it at the same time are unpredictable.
	 * @param registryPort port to bind RMI registry to, or -1 if the registry should not
	 * be started.
	 * @param httpPort port to bind http daemon to, or -1 if the daemon should not be started
	 * @throws RemoteException if any error prevented the registry startup
	 * @throws IOException if any error prevented the http daemon startup
	 */
	public void initialize(final int registryPort, final int httpPort) throws RemoteException, IOException {
		this.initialized = true;
		this.getInetAddress(); // Ensures that InetAddress has been set
		if (registry == null && registryPort != -1) {
			registry = new LocalRegistryHandler(registryPort);
		}
		if (httpServer == null && httpPort != -1) {
			httpServer = new SimpleRmiHttpServer(httpPort);
		}
	}
	/**
	 * Sets a remote rmi registry to be controlled.
	 * This method will never start an http daemon it will just setup a property to
	 * allow dynamic class downloads from an http server running on <b>httpPort</b>.<BR>
	 * This method is NOT thread safe, results of two threads trying to access
	 * it at the same time are unpredictable.
	 * @param remoteHost host-name or ip of the remote registry
	 * @param registryPort remote registry port number
	 * @param httpPort remote http port for dynamic class downloads, or -1 if not allowed.
	 * @throws IOException on any IO error
	 */
	public void initialize(String remoteHost, int registryPort, int httpPort) throws IOException {
		this.initialized = true;
		this.getInetAddress(); // Ensures that InetAddress has been set
		if (httpPort != -1) {
			// This is the most important property to allow dynamic class downloads, it will
			// be used by the JVM every time that a RMI call tries to access a class that
			// is not present on the local classpath.
			System.setProperty("java.rmi.server.codebase", "http://" + remoteHost + ":" + httpPort + "/");
		}
		if (registry == null) {
			registry = new RemoteRegistryHandler(remoteHost, registryPort);
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new NoSecurity());
			}
		}
	}
	/**
	 * Sets a remote rmi registry to be controlled.<BR>
	 * Calling this method has the same effect of calling
	 * <b><code>initialize(remoteRegistryHost, REGISTRY_PORT, HTTP_PORT)</code></b>.
	 * @param remoteRegistryHost host-name or ip of the remote registry
	 * @throws IOException on any IO error
	 * @see #initialize(String, int, int)
	 */
	public void initialize(String remoteRegistryHost) throws IOException {
		initialize(remoteRegistryHost, REGISTRY_PORT, HTTP_PORT);
	}
	/**
	 * Unbinds any object remaining on the started RMI Registry (if any)
	 * and closes the started http daemon.<BR>
	 * Note that if the registry has been started, it will be still alive after
	 * calling this method, since the RMI does not provide any way for stopping
	 * an started registry.
	 */
	public void finish() {
		this.initialized = false;
		this.inetAddress = null;
		if (this.registry != null) {
			try {
				final String[] binds = registry.list();
				for (int i = 0; i < binds.length; i++) {
					try {
						registry.unbind(binds[i]);
					} catch (NotBoundException e1) {
						// Ignores
					}
				}
			} catch (AccessException e) {
			} catch (RemoteException e) {
			}
		}
		if (this.httpServer != null) {
			this.httpServer.stopRunning(true);
			this.httpServer = null;
		}
	}
	/**
	 * Registers the given node on the RMI registry.<BR>
	 * After calling this method that node will be available to any
	 * remote machine thru the registry.
	 * @param node node to register
	 * @throws RemoteException RemoteException if remote communication with the registry failed;
	 * if exception is a ServerException containing an AccessException, then the registry denies
	 * the caller access to perform this operation (if originating from a non-local host, for example)
	 * @throws AccessException if this registry is local and it denies the caller access to perform this
	 * operation
	 * @throws NodeAlreadyExistsException if another node with the same name is already registered
	 * on the RMI registry
	 * @throws RegistryNotActiveException if the registry has not been initialized
	 */
	public void registerNode(INode node) throws AccessException, RemoteException, NodeAlreadyExistsException, RegistryNotActiveException {
		if (!this.initialized) {
			throw new RegistryNotActiveException("initialize() must be called first.");
		}
		try {
			this.registry.bind(node.getNodeName(), node);
		} catch (AlreadyBoundException e) {
			try {
				final INode remoteNode = (INode) this.registry.lookup(node.getNodeName());
				remoteNode.getNodeName();
				// There is a node with the same name at the registry and it was possible
				// to contact it, so we cannot replace.
				throw new NodeAlreadyExistsException(node.getNodeName(), e);
			} catch (NodeAlreadyExistsException error) {
				throw error;
			} catch (Exception error) {
				// An error occured, let's assume that the given node is dead and replace it
				this.registry.rebind(node.getNodeName(), node);
			}
		}
		if (node instanceof IProcessNode) {
			try {
				// Tells the main node that it will control this node
				getMainNode().addToControl((IProcessNode) node);
			} catch (Exception e) {
				// Ignores
			}
		}
	}
	/**
	 * Removes the given node from the registry (if registered).
	 * @param node node to remove from registry
	 * @throws RemoteException RemoteException if remote communication with the registry failed;
	 * if exception is a ServerException containing an AccessException, then the registry denies
	 * the caller access to perform this operation (if originating from a non-local host, for example)
	 * @throws AccessException if this registry is local and it denies the caller access to perform this
	 * operation
	 * @throws RegistryNotActiveException if the registry has not been initialized
	 */
	public void unregisterNode(INode node) throws AccessException, RemoteException, RegistryNotActiveException {
		if (!this.initialized) {
			throw new RegistryNotActiveException("initialize() must be called first.");
		}
		try {
			this.registry.unbind(node.getNodeName());
			if (node instanceof IProcessNode) {
				try {
					// Tells the main node that it will not control this node anymore
					getMainNode().removeFromControl((IProcessNode) node);
				} catch (Exception e) {
					// Ignores
				}
			}
		} catch (NotBoundException e) {
			// Ignores
		}
	}
	/**
	 * Retrieves a remote reference for the system's main node, if available.
	 * @return IMainNode registered at RMI Registry
     * @throws NotBoundException if the main node hasn't been found on registry
     * @throws	RemoteException if remote communication with the
     * registry failed; if exception is a <code>ServerException</code>
     * containing an <code>AccessException</code>, then the registry
     * denies the caller access to perform this operation
     * @throws	AccessException if this registry is local and it denies
     * the caller access to perform this operation
	 * @throws RegistryNotActiveException if the registry has not been initialized
	 */
	public IMainNode getMainNode() throws AccessException, RemoteException, NotBoundException, RegistryNotActiveException {
		if (!this.initialized) {
			throw new RegistryNotActiveException("initialize() must be called first.");
		}
		return (IMainNode) this.registry.lookup("MainNode");
	}
	/**
	 * Retrieves the singleton instance of this class
	 * @return RegistryHandler singleton
	 */
	public static RegistryHandler getInstance() {
		return INSTANCE;
	}
	/**
	 * RMISocketFactory that implements connect timeouts
	 * @author Rodrigo
	 */
	private static final class TimeoutSocketFactory extends RMISocketFactory {
		private final int connectTimeout;
		/**
		 * Default Constructor.
		 * @param connectTimeout connect timeout in millis
		 */
		private TimeoutSocketFactory(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}
		/** {@inheritDoc} */
		public ServerSocket createServerSocket(int port) throws IOException {
			return new ServerSocket(port);
		}
		/** {@inheritDoc} */
		public Socket createSocket(String host, int port) throws IOException {
			return new TimeoutSocket(host, port);
		}
		/**
		 * Socket that implements a connect timeout
		 * @author Rodrigo
		 */
		private final class TimeoutSocket extends Socket {
			/**
			 * Default Constructor.
			 * @param host host
			 * @param port port 
			 * @throws UnknownHostException on IO error
			 * @throws IOException on IO error
			 */
			private TimeoutSocket(String host, int port) throws UnknownHostException, IOException {
				super();
				bind(new InetSocketAddress(0));
				connect(host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(InetAddress.getByName(null), port));
			}
			/** {@inheritDoc} */
			public void connect(SocketAddress endpoint) throws IOException {
				super.connect(endpoint, connectTimeout);
			}
			/** {@inheritDoc} */
			public void connect(SocketAddress endpoint, int timeout) throws IOException {
				timeout = (timeout == 0) ? connectTimeout : Math.min(timeout, connectTimeout);
				super.connect(endpoint, Math.min(timeout, timeout));
			}
		}
	}
}
