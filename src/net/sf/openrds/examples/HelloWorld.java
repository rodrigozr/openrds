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
 * HelloWorld.java
 * Created by: Rodrigo
 * Created at: Sep 29, 2005 3:47:53 PM
 *
 * $Revision: 1.3 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds.examples;

import net.sf.openrds.IMainNode;
import net.sf.openrds.IndivisibleRequisition;
import net.sf.openrds.ProcessingException;
import net.sf.openrds.RegistryHandler;

/**
 * "Hello World" Example for OpenRDS.
 * Note that this example requires an started main node and at least one process node.
 * @author Rodrigo
 */
public class HelloWorld {
	/**
	 * Main
	 * @param args pass nothing to use localhost or pass another location to connect to
	 */
	public static void main(String[] args) {
		try {
			// Connects to a registry in the local host or a host passed as argument
			RegistryHandler.getInstance().initialize(args.length == 0 ? "localhost" : args[0]);
			// Gets a reference to the main node
			IMainNode mainNode = RegistryHandler.getInstance().getMainNode();
			// Process the requistion...
			String result = (String) mainNode.processRequisition(new HelloWorldRequisition());
			// Prints the result
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	/**
	 * Hello world
	 * @author Rodrigo
	 */
	private static final class HelloWorldRequisition extends IndivisibleRequisition {
	    private static final long serialVersionUID = 1236737865767710L;
		/** {@inheritDoc} */
		public Object process() throws ProcessingException {
			System.out.println("Hello world from client");
			return "Hello from process node";
		}
	}
}
