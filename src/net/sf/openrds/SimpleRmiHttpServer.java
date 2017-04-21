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
 * SimpleRmiHttpServer.java
 * Created by: Rodrigo
 * Created at: Aug 27, 2005 1:13:04 PM
 * 
 * $Revision: 1.5 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Responsible for starting a very simple HTTP server that will receive GET
 * requests for classes and respond to them.
 * @author Rodrigo
 * @version Aug 27, 2005 1:06:55 PM
 */
final class SimpleRmiHttpServer extends Thread {
	/** Data written when a class is not found */
	private static final byte[] NOT_FOUND_404 = "HTTP/1.1 404 Not Found\nContent-Length: 0\n\n".getBytes();
	/** Thread group for socket workers */
	private ThreadGroup group = new ThreadGroup("SocketWorker Group");
	/** Port number */
	private final int port;
	/** Registers if this thread is running or not */
	private boolean running = true;
	/** Holds IO error from thread */
	private IOException error = null;
	/** ServerSocket to listen for requests */
	private ServerSocket serverSocket = null;
	
	/**
	 * Creates and starts a new HTTP server that will answer on the given port number.
	 * @param port port number to listen for requests.
	 * @throws IOException if it was not possible to start the server
	 */
	public SimpleRmiHttpServer(final int port) throws IOException {
		this.port = port;
		this.setName("RMI Http Server");
		this.setDaemon(true);
		synchronized (this) {
			// Starts the new thread
			this.start();
			// Waits for the child thread to notify
			this.doWait();
		}
		// An error occurred while starting the child thread, throws it to caller
		if (this.error != null) {
			throw this.error;
		}
	}
	/** {@inheritDoc} */
	public void run() {
		try {
			// Creates a server socket that will listen on the given port number
			this.serverSocket = new ServerSocket(this.port);
			// Notify the parent thread that the socket could be opened with success
			this.doNotify();
			// Keep running until stopped
			while (this.running) {
				// Listen for any connection
				final Socket socket = this.serverSocket.accept();
				// Starts a new worker for the given connection
				new SocketWorker(socket, this.group);
			}
		} catch (IOException e) {
			// Sets error so that the parent thread can handle it
			this.error = e;
		} finally {
			// Notify parent thread...
			this.doNotify();
		}
	}
	/** Calls <b>wait()</b> and handles <b>InterruptedException</b> */
	private void doWait() {
		try {
			this.wait();
		} catch (InterruptedException e) {
			// Ignores
		}
	}
	/** Acquires lock on <b>this</b> and calls <b>notifyAll()</b> */
	private void doNotify() {
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	/**
	 * Stops the execution of this daemon and closes the socket listener.
	 * @param block if true, this method will block untill every child thread
	 * has terminated it's execution.
	 */
	public void stopRunning(final boolean block) {
		this.running = false;
		this.interrupt();
		try {
			this.serverSocket.close();
		} catch (IOException e1) {
			// Ignores
		}
		if (block) {
			final Thread threads [] = new Thread [20];
			while (group.activeCount() > 0) {
				final int size = group.enumerate(threads);
				// Waits for all child theads to die.
				for (int i = 0; i < size; i++) {
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}
	}
	
	/**
	 * This worker is responsible for reading data from an incomming connection
	 * and writing back the requested class.
	 * @author Rodrigo
	 */
	private static final class SocketWorker extends Thread {
		/** Connection beeing handled */
		private final Socket socket;
		
		/**
		 * Creates and starts a new worker to handle the given socket
		 * connection
		 * @param socket incomming connection
		 * @param group thread group of this thread
		 */
		private SocketWorker(final Socket socket, final ThreadGroup group) {
			super(group, "Socket worker for " + socket);
			this.socket = socket;
			this.start();
		}
		
		/** {@inheritDoc} */
		public void run() {
			try {
				// Wraps incomming data in a buffer
				final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String className = null;
				String str = null;
				// Keeps reading data until we find the requested class or EOF
				while (className == null && (str = in.readLine()) != null) {
					// We have to find a string in the format: "GET /package/className.class httpversion"
					if (str.startsWith("GET ") || str.startsWith("get ")) {
						final int endIndex = str.indexOf(' ', 4);
						className = str.substring(4, endIndex != -1 ? endIndex : str.length());
					}
				}
				// Found the requested class name
				if (className != null) {
					if (className.startsWith("/")) {
						// Removes the first '/' from string
						className = className.substring(1);
					}
					writeClass(className);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// Closes the connection
					socket.close();
				} catch (Exception e) {
				}
			}
		}

		/**
		 * Writes the given class to socket's output.
		 * @param className class name
		 * @throws IOException on any IO error
		 */
		private void writeClass(String className) throws IOException {
			// Gets the class input stream
			final InputStream classIn = ClassLoader.getSystemResourceAsStream(className);
			final OutputStream out = socket.getOutputStream();
			if (classIn != null) {
				try {
					final byte [] buffer = new byte [2048];
					int read;
					// Writes back class data 
					while ((read = classIn.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}
				} finally {
					// Relases class input
					classIn.close();
				}
			} else { // Not found
				out.write(NOT_FOUND_404);
			}
			// Ensures that all data has been written
			out.flush();
		}
	}
}