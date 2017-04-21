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
 * GenericMainNode.java
 * Created by: Rodrigo
 * Created at: Sep 30, 2005 3:59:19 PM
 *
 * $Revision: 1.5 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds.examples;

import java.rmi.RemoteException;

import net.sf.openrds.IMainNode;
import net.sf.openrds.INode;
import net.sf.openrds.IProcessNode;
import net.sf.openrds.NodeEventAdaptor;
import net.sf.openrds.NodeFactory;
import net.sf.openrds.RegistryHandler;

/**
 * Generic main node example
 * @author Rodrigo
 */
public class GenericMainNode {
	/**
	 * Main
	 * @param args argument
	 */
	public static void main(String[] args) {
		try {
			// Creates a new MainNode that allows dynamic class downloading
			final IMainNode node = NodeFactory.getInstance().startMainNode(true);
			node.addNodeEventListener(new Listener());
			System.out.println("Main node active");
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
		public void nodeRegistered(IMainNode mainNode, IProcessNode registeredNode) {
			try {
				System.out.println("Node registered: " + registeredNode.getNodeName());
			} catch (RemoteException e) {
			}
		}
		/** {@inheritDoc} */
		public void nodeUnregistered(IMainNode mainNode, IProcessNode unregisteredNode) {
			try {
				System.out.println("Node unregistered: " + unregisteredNode.getNodeName());
			} catch (RemoteException e) {
			}
		}
		/** {@inheritDoc} */
		public void nodeFinished(INode node) {
			System.out.println("Finishing main node.");
			System.out.println("Exiting JVM.");
			System.exit(0);
		}
	}
}
