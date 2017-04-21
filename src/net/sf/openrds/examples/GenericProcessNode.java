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
 * GenericProcessNode.java
 * Created by: Rodrigo
 * Created at: Sep 3, 2005 3:48:33 PM
 *
 * $Revision: 1.6 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */
package net.sf.openrds.examples;

import net.sf.openrds.INode;
import net.sf.openrds.IProcessNode;
import net.sf.openrds.NodeEventAdaptor;
import net.sf.openrds.NodeFactory;
import net.sf.openrds.RegistryHandler;


/**
 * Generic process node example
 * @author Rodrigo
 */
public class GenericProcessNode {
	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) {
		try {
			final String server = args.length > 0 ? args[0] : "localhost";
			// Starts a process node that will connect on "server" main node.
			// Since the second parameter (testConnection) is "false",
			// this node will start even if the server is not active
			// and it will try to connect to the main node as soon as it
			// is available.
			final IProcessNode node = NodeFactory.getInstance().startProcessNode(server, false);
			node.addNodeEventListener(new Listener());
			System.out.println("Process node active");
			System.out.println("Using network address '" + RegistryHandler.getInstance().getInetAddress().getHostAddress() + "'.");
			// Waits for the 'q' key to be pressed.
			while (System.in.read() != 'q') {
				System.out.println("Press 'q' to finish node...");
			}
			// Finish the node.
			node.finish();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Node events listener
	 */
	private static final class Listener extends NodeEventAdaptor {
		/** {@inheritDoc} */
		public void nodeLostConnection(INode node) {
			System.out.println("Lost connection with main node.");
		}
		/** {@inheritDoc} */
		public void nodeRestoredConnection(INode node) {
			System.out.println("Connected to the main node.");
		}
		/** {@inheritDoc} */
		public void nodeFinished(INode node) {
			System.out.println("Finishing node.");
			System.out.println("Exiting JVM.");
			System.exit(0);
		}
	}
}
