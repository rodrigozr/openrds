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
 * PrimeNumber.java
 * Created by: Rodrigo
 * Created at: Oct 10, 2005 1:40:27 PM
 *
 * $Revision: 1.3 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */
package net.sf.openrds.examples;

import java.util.LinkedList;
import java.util.List;

import net.sf.openrds.DivisibleRequisition;
import net.sf.openrds.IMainNode;
import net.sf.openrds.ProcessingException;
import net.sf.openrds.RegistryHandler;
import net.sf.openrds.SubRequisition;


/**
 * Prime number calculation test with a divisible requisition
 * @author Rodrigo
 */
public class PrimeNumber {

	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) {
		try {
			final String registryHost = args.length > 0 ? args[0] : "localhost";
			RegistryHandler.getInstance().initialize(registryHost);
			final IMainNode main = RegistryHandler.getInstance().getMainNode();
			while (main.getControlledNodes().length == 0) {
				System.out.println("Waiting for a process node..");
				Thread.sleep(1000);
			}
			final DivisibleRequisition req = new PrimeNumberRequisition(args.length > 1 ? Integer.parseInt(args[1]) : 60000);
			long time = System.currentTimeMillis();
			System.out.println("Starting test!");
			final List primeNumbers = (List) main.processRequisition(req);
			System.out.println("Found " + primeNumbers.size() + " prime numbers...");
			//System.out.println(primeNumbers);
			time = System.currentTimeMillis() - time;
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
	private static class PrimeNumberRequisition extends DivisibleRequisition {
	    private static final long serialVersionUID = 1263768786576710L;
	    private final int qtyNumbers;
	    /**
	     * Default Constructor.
	     * @param qtyNumbers quantity of numbers to check
	     */
	    private PrimeNumberRequisition(int qtyNumbers) {
	    	this.qtyNumbers = qtyNumbers;
	    }
		/** {@inheritDoc} */
		public SubRequisition[] getSubRequisitions(int availableNodes) {
			final int qtyReqs = availableNodes * 50;
			final int numbersPerReq = qtyNumbers / qtyReqs;
			final int missing = qtyNumbers - (numbersPerReq * qtyReqs);
			final SubRequisition reqs [] = new SubRequisition[qtyReqs];
			for (int i = 0; i < reqs.length; i++) {
				final int start = (i * numbersPerReq);
				if (i == 0) {
					// First requisition... has more if needed
					reqs[i] = new PrimeSubRequisition(start, numbersPerReq + missing);
				} else {
					reqs[i] = new PrimeSubRequisition(start, numbersPerReq);
				}
			}
			return reqs;
		}
		/** {@inheritDoc} */
		public Object getResponse(Object[] subResults) {
			final LinkedList resp = new LinkedList();
			for (int i = 0; i < subResults.length; i++) {
				resp.addAll((List) subResults[i]);
			}
			return resp;
		}
	}
	/**
	 * Sub requisition
	 * @author Rodrigo
	 */
	private static class PrimeSubRequisition extends SubRequisition {
	    private static final long serialVersionUID = 1263156879651230L;
	    private final int start;
	    private final int qty;
	    /**
	     * Default Constructor.
	     * @param start start
	     * @param qty end
	     */
	    private PrimeSubRequisition(int start, int qty) {
	    	this.start = start;
	    	this.qty = qty;
	    }
		/** {@inheritDoc} */
		public Object process() throws ProcessingException {
			System.out.print('.');
			final LinkedList list = new LinkedList();
			for (int i = start, x = 0; x < qty; i++, x++) {
				if (i > 0 && i != 2) {
					boolean b = true;
					for (int j = i - 1; j > 1; j--) {
						if (i % j == 0) {
							b = false;
							break;
						}
					}
					if (b) {
						list.add(new Integer(i));
					}
				}
			}
			return list;
		}
		/** {@inheritDoc} */
		public int getProcessingFactor() {
			// By returning a very-high value, we force that only one requisition will be processed
			// at a time, on each process node.
			return 100000;
		}
	}
}
