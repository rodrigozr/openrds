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
 * HighCalc.java
 * Created by: Rodrigo
 * Created at: Sep 3, 2005 1:40:27 PM
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
 * High calculations balancing test
 * @author Rodrigo
 */
public class HighCalc {

	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) {
		try {
			final String registryHost = args.length > 0 ? args[0] : "localhost";
			// Initilizes the registry
			RegistryHandler.getInstance().initialize(registryHost);
			// Gets the main node
			final IMainNode main = RegistryHandler.getInstance().getMainNode();
			while (main.getControlledNodes().length == 0) {
				// Waits for at least one process node
				System.out.println("Waiting for a process node..");
				Thread.sleep(1000);
			}
			long time = System.currentTimeMillis();
			// Starts sending the requisitions
			final IndivisibleRequisition req = new TestRequisition();
			System.out.println("Starting test!");
			main.processRequisition(req);
			for (int i = 0; i < 20; i++) {
				main.processAsyncRequisition(req);
			}
			System.out.println("Waiting for processing to finish...");
			// This will block until all requisitions have been processed.
			main.waitIdle();
			
			time = (System.currentTimeMillis() - time);
			System.out.println("Total time: " + time + " milliseconds.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	/**
	 * Requisition used for tests
	 * @author Rodrigo
	 */
	private static class TestRequisition extends IndivisibleRequisition {
	    private static final long serialVersionUID = 1263768786576710L;
		/** {@inheritDoc} */
	    public Object process() throws ProcessingException {
			System.out.println("Processing TestRequisition");
	    	long res = System.currentTimeMillis();
	    	for (int i = 0; i < 10000000; i++) {
				res *= 2;
				res = res ^ (res / 5);
			}
			//throw new ProcessingException("Processing exception test");
			return "Result: " + res;
		}
		/** {@inheritDoc} */
		public int getProcessingFactor() {
			// By returning a very-high value, we force that only one requisition will be processed
			// at a time, on each process node.
			return 100000;
		}
	}
}
