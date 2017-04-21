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
 * CloseAll.java
 * Created by: Rodrigo
 * Created at: Jul 21, 2006 9:29:14 AM
 *
 * $Revision: 1.3 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds.examples;

import net.sf.openrds.IMainNode;
import net.sf.openrds.IProcessNode;
import net.sf.openrds.IndivisibleRequisition;
import net.sf.openrds.ProcessingException;
import net.sf.openrds.RegistryHandler;

/**
 * Example class that closes all process nodes.
 * @author Rodrigo
 * @since OpenRDS 0.2
 */
public class CloseAll {
	/**
	 * Main
	 * @param args pass nothing to use localhost or pass another location to connect to
	 */
	public static void main(String[] args) {
		try {
			// Connects to a registry in the local host or a host passed as argument
			RegistryHandler.getInstance().initialize(args.length == 0 ? "localhost" : args[0]);
			// Gets a reference to the main node
			final IMainNode mainNode = RegistryHandler.getInstance().getMainNode();
			// Creates a requisition to close JVM
			final IndivisibleRequisition req = new CloseRequisition();
			// Gets the list of registered process nodes
			final IProcessNode[] nodes = mainNode.getControlledNodes();
			// Iterates over all of them
			for (int i = 0; i < nodes.length; i++) {
				// Sends the requisition to be processed at that expecific node
				nodes[i].processRequisition(req);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	/** Requisition used to close a process node */
	private static final class CloseRequisition extends IndivisibleRequisition {
		/** {@inheritDoc} */
		public Object process() throws ProcessingException {
			new Thread() {
				/** {@inheritDoc} */
				public void run() {
					try {
						Thread.sleep(200); // Waits some time
					} catch (InterruptedException ignored) {
					}
					System.exit(0); // Closes JVM of the process node
				}
			} .start(); // Starts a thread to close JVM
			return null;
		}
	}
}
