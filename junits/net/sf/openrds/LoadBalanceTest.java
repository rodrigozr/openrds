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
 * LoadBalanceTest.java
 * Created by: Rodrigo Rosauro
 * Created at: 30/07/2006 23:37:46
 *
 * $Revision: 1.5 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Load-balance junits
 * @author Rodrigo Rosauro
 */
public class LoadBalanceTest extends OpenRDSTestCase {
	
	/** @throws Exception on any error */
	public void testNoFactorsBalance2() throws Exception {
		performNoFactorsTest(2);
	}
	/** @throws Exception on any error */
	public void testNoFactorsBalance3() throws Exception {
		performNoFactorsTest(3);
	}
	/** @throws Exception on any error */
	public void testNoFactorsBalance4() throws Exception {
		performNoFactorsTest(4);
	}
	/** @throws Exception on any error */
	public void testNoFactorsBalance5() throws Exception {
		performNoFactorsTest(5);
	}
	/** @throws Exception on any error */
	public void testNoFactorsBalance6() throws Exception {
		performNoFactorsTest(6);
	}
	
	/**
	 * Performs a no-factors load-balance test given a number of process
	 * nodes to test.
	 * @param qtyOfNodes quantity of nodes
	 * @throws Exception on any error
	 */
	private void performNoFactorsTest(int qtyOfNodes) throws Exception {
		final MainNode main = startMainNode(false);
		for (int i = 1; i <= qtyOfNodes; i++) {
			startProcessNode(i + "");
		}
		final List resultList = new LinkedList();
		final HoldableRequisition reqs[] = new HoldableRequisition[qtyOfNodes * 2];
		for (int i = 0; i < reqs.length; i++) {
			reqs[i] = new HoldableRequisition() {
				public Object processImpl() throws Exception {
					return (getNodeBeeingProcessed().getClockAmount() + "");
				}
			};
		}
		final Thread threads[] = new Thread[qtyOfNodes * 2];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = processAsync(main, reqs[i], resultList);
			reqs[i].waitProcessingToStart(); // Waits for the requisitions to start processing
		}
		for (int i = 0; i < reqs.length; i++) {
			reqs[i].release(); // Release the requisitions, so they will return value
			threads[i].join(); // Waits the started threads to put result on the list
		}
		assertEquals(qtyOfNodes * 2, resultList.size());
		for (int i = 1; i <= qtyOfNodes; i++) {
			assertTrue(resultList.remove(i + ""));
			assertTrue(resultList.remove(i + "")); // Twice
		}
		assertEquals(0, resultList.size());
	}
	
	/** @throws Exception on any error */
	public void testNoFactorsWithHigherClock() throws Exception {
		final MainNode main = startMainNode();
		final ProcessNode node1 = startProcessNode("100", "500"); // 100 MHz (Memory ignored on this test)
		final ProcessNode node2 = startProcessNode("200", "400"); // 200 MHz
		final ProcessNode node3 = startProcessNode("300", "300"); // 300 MHz
		final ProcessNode node4 = startProcessNode("400", "200"); // 400 MHz
		final ProcessNode node5 = startProcessNode("500", "100"); // 500 MHz
		
		final HoldableRequisition reqs[] = new HoldableRequisition[] {
			new GenericTestRequisition(), // Goes to #5
			new GenericTestRequisition(), // Goes to #4
			new GenericTestRequisition(), // Goes to #3
			new GenericTestRequisition(), // Goes to #2
			new GenericTestRequisition(), // Goes to #1
			new GenericTestRequisition(), // Goes to #5
			new GenericTestRequisition(), // Goes to #4
			new GenericTestRequisition(), // Goes to #3
			new GenericTestRequisition(), // Goes to #2
			new GenericTestRequisition(), // Goes to #1
		};
		
		final String n1 = node1.getNodeName();
		final String n2 = node2.getNodeName();
		final String n3 = node3.getNodeName();
		final String n4 = node4.getNodeName();
		final String n5 = node5.getNodeName();
		final String expected[] = new String[] {
				n5, n4, n3, n2, n1, n5, n4, n3, n2, n1
		};
		assertGenericBalanceOrder(main, reqs, expected);
	}
	/** @throws Exception on any error */
	public void testNoFactorsWithHigherMemory() throws Exception {
		final MainNode main = startMainNode();
		final ProcessNode node1 = startProcessNode("100", "100"); // 100 MB
		final ProcessNode node2 = startProcessNode("100", "200"); // 200 MB
		final ProcessNode node3 = startProcessNode("100", "300"); // 300 MB
		final ProcessNode node4 = startProcessNode("100", "400"); // 400 MB
		final ProcessNode node5 = startProcessNode("100", "500"); // 500 MB
		
		final HoldableRequisition reqs[] = new HoldableRequisition[] {
			new GenericTestRequisition(), // Goes to #5
			new GenericTestRequisition(), // Goes to #4
			new GenericTestRequisition(), // Goes to #3
			new GenericTestRequisition(), // Goes to #2
			new GenericTestRequisition(), // Goes to #1
			new GenericTestRequisition(), // Goes to #5
			new GenericTestRequisition(), // Goes to #4
			new GenericTestRequisition(), // Goes to #3
			new GenericTestRequisition(), // Goes to #2
			new GenericTestRequisition(), // Goes to #1
		};
		
		final String n1 = node1.getNodeName();
		final String n2 = node2.getNodeName();
		final String n3 = node3.getNodeName();
		final String n4 = node4.getNodeName();
		final String n5 = node5.getNodeName();
		final String expected[] = new String[] {
				n5, n4, n3, n2, n1, n5, n4, n3, n2, n1
		};
		assertGenericBalanceOrder(main, reqs, expected);
	}
	/**
	 * Asserts that the given reqs[], when processed at the same time will generate the
	 * result "expected[]".
	 * @param main main node
	 * @param reqs requisitions
	 * @param expected expected result
	 * @throws InterruptedException never
	 */
	private void assertGenericBalanceOrder(final MainNode main, final HoldableRequisition[] reqs, final String[] expected) throws InterruptedException {
		final List resultList = new LinkedList();
		final Thread threads[] = new Thread[reqs.length];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = processAsync(main, reqs[i], resultList);
			reqs[i].waitProcessingToStart(); // Waits for the requisition to start processing
		}
		for (int i = 0; i < reqs.length; i++) {
			reqs[i].release(); // Release the requisition, so it will return value
			threads[i].join(); // Waits the result to be put on the list
		}
		int index = 0;
		for (final Iterator it = resultList.iterator(); it.hasNext(); index++) {
			final String msg = "Wrong node processed the requisition[" + index + "].";
			assertEquals(msg, expected[index], it.next());
		}
	}
	
	/** @throws Exception on any error */
	public void testClockBalance2() throws Exception {
		final MainNode main = startMainNode();
		final ProcessNode node1 = startProcessNode("100"); // 100 MHz
		final ProcessNode node2 = startProcessNode("200"); // 200 MHz
		
		final HoldableRequisition reqs[] = new HoldableRequisition[] {
			new GenericTestRequisition(40), // Goes to #2 - remains 160
			new GenericTestRequisition(40), // Goes to #1 - remains 60
			new GenericTestRequisition(40), // Goes to #2 - remains 120
			new GenericTestRequisition(40), // Goes to #1 - remains 20
			new GenericTestRequisition(20), // Goes to #2 - remains 100
			new GenericTestRequisition(20), // Goes to #1 - remains 0
			new GenericTestRequisition(20), // Goes to #2 - remains 80
			new GenericTestRequisition(20), // Goes to #2 - remains 60
			new GenericTestRequisition(20), // Goes to #2 - remains 40
		};
		
		final String n1 = node1.getNodeName();
		final String n2 = node2.getNodeName();
		final String expected[] = new String[] {
			n2, n1, n2, n1, n2, n1, n2, n2, n2
		};
		
		assertGenericBalanceOrder(main, reqs, expected);
	}
	/** @throws Exception on any error */
	public void testComplexClockBalance2() throws Exception {
		final MainNode main = startMainNode();
		final ProcessNode node1 = startProcessNode("100"); // 100 MHz
		final ProcessNode node2 = startProcessNode("200"); // 200 MHz
		
		final List resultList = new LinkedList();
		final HoldableRequisition reqs[] = new HoldableRequisition[] {
			new GenericTestRequisition(100), // Goes to #2 - remains 100
			new GenericTestRequisition(100), // Goes to #1 - remains 0
			new GenericTestRequisition(100), // Goes to #2 - remains 0
		};
		
		final Thread threads[] = new Thread[reqs.length];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = processAsync(main, reqs[i], resultList);
			reqs[i].waitProcessingToStart(); // Waits for the requisition to start processing
		}
		// Releases the last requisition... Node #2 now has 100 factor remaining
		reqs[2].release();
		threads[2].join();
		// Creates a new requisition and put in the place of that... Goes to #2 - remains 50
		reqs[2] = new GenericTestRequisition(50);
		threads[2] = processAsync(main, reqs[2], resultList);
		reqs[2].waitProcessingToStart();
		// Releases the second requisition... Node #1 now has 100 factor remaining (zero used)
		reqs[1].release();
		threads[1].join();
		// Creates a new requisition and put in the place of that... Goes to #1 - remains 0 (150 used)
		reqs[1] = new GenericTestRequisition(150);
		threads[1] = processAsync(main, reqs[1], resultList);
		reqs[1].waitProcessingToStart();
		
		// Now lets run all other requisitions
		for (int i = 0; i < reqs.length; i++) {
			reqs[i].release(); // Release the requisition, so it will return value
			threads[i].join(); // Waits the result to be put on the list
		}
		final String n1 = node1.getNodeName();
		final String n2 = node2.getNodeName();
		final String expected[] = new String[] {
			n2, n1, n2, n1, n2
		};
		int index = 0;
		for (final Iterator it = resultList.iterator(); it.hasNext(); index++) {
			final String msg = "Wrong node processed the requisition[" + index + "].";
			assertEquals(msg, expected[index], it.next());
		}
	}
	
	/** @throws Exception on any error */
	public void testMemBalance2() throws Exception {
		final MainNode main = startMainNode();
		final ProcessNode node1 = startProcessNode("-1", "100"); // 100 MB
		final ProcessNode node2 = startProcessNode("-1", "200"); // 200 MB
		
		final HoldableRequisition reqs[] = new HoldableRequisition[] {
			new GenericTestRequisition(-1, 40), // Goes to #2 - remains 160
			new GenericTestRequisition(-1, 40), // Goes to #1 - remains 60
			new GenericTestRequisition(-1, 40), // Goes to #2 - remains 120
			new GenericTestRequisition(-1, 40), // Goes to #1 - remains 20
			new GenericTestRequisition(-1, 20), // Goes to #2 - remains 100
			new GenericTestRequisition(-1, 20), // Goes to #1 - remains 0
			new GenericTestRequisition(-1, 20), // Goes to #2 - remains 80
			new GenericTestRequisition(-1, 20), // Goes to #2 - remains 60
			new GenericTestRequisition(-1, 20), // Goes to #2 - remains 40
		};
		
		final String n1 = node1.getNodeName();
		final String n2 = node2.getNodeName();
		final String expected[] = new String[] {
			n2, n1, n2, n1, n2, n1, n2, n2, n2
		};
		assertGenericBalanceOrder(main, reqs, expected);
	}
	/** @throws Exception on any error */
	public void testComplexMemBalance2() throws Exception {
		final MainNode main = startMainNode();
		final ProcessNode node1 = startProcessNode("-1", "100"); // 100 MB
		final ProcessNode node2 = startProcessNode("-1", "200"); // 200 MB
		
		final List resultList = new LinkedList();
		final HoldableRequisition reqs[] = new HoldableRequisition[] {
			new GenericTestRequisition(-1, 100), // Goes to #2 - remains 100
			new GenericTestRequisition(-1, 100), // Goes to #1 - remains 0
			new GenericTestRequisition(-1, 100), // Goes to #2 - remains 0
		};
		
		final Thread threads[] = new Thread[reqs.length];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = processAsync(main, reqs[i], resultList);
			reqs[i].waitProcessingToStart(); // Waits for the requisition to start processing
		}
		// Releases the last requisition... Node #2 now has 100 factor remaining
		reqs[2].release();
		threads[2].join();
		// Creates a new requisition and put in the place of that... Goes to #2 - remains 50
		reqs[2] = new GenericTestRequisition(-1, 50);
		threads[2] = processAsync(main, reqs[2], resultList);
		reqs[2].waitProcessingToStart();
		// Releases the second requisition... Node #1 now has 100 factor remaining (zero used)
		reqs[1].release();
		threads[1].join();
		// Creates a new requisition and put in the place of that... Goes to #1 - remains 0 (150 used)
		reqs[1] = new GenericTestRequisition(-1, 150);
		threads[1] = processAsync(main, reqs[1], resultList);
		reqs[1].waitProcessingToStart();
		
		// Now lets run all other requisitions
		for (int i = 0; i < reqs.length; i++) {
			reqs[i].release(); // Release the requisition, so it will return value
			threads[i].join(); // Waits the result to be put on the list
		}
		final String n1 = node1.getNodeName();
		final String n2 = node2.getNodeName();
		final String expected[] = new String[] {
			n2, n1, n2, n1, n2
		};
		int index = 0;
		for (final Iterator it = resultList.iterator(); it.hasNext(); index++) {
			final String msg = "Wrong node processed the requisition[" + index + "].";
			assertEquals(msg, expected[index], it.next());
		}
	}
	/** @throws Exception on any error */
	public void testBothBalance2() throws Exception {
		final MainNode main = startMainNode();
		final ProcessNode node1 = startProcessNode("100", "100"); // 100 MHz, 100 MB
		final ProcessNode node2 = startProcessNode("200", "200"); // 200 MHz, 200 MB
		
		final HoldableRequisition reqs[] = new HoldableRequisition[] {
			new GenericTestRequisition(150, 150), // Goes to #2
			new GenericTestRequisition(-1, -1),   // Goes to #1
			new GenericTestRequisition(150, 150), // Goes to #1
			new GenericTestRequisition(-1, -1),   // Goes to #2
			new GenericTestRequisition(25, 25),   // Goes to #2
			new GenericTestRequisition(25, 25),   // Goes to #2
			new GenericTestRequisition(-1, -1),   // Goes to #1
		};
		
		final String n1 = node1.getNodeName();
		final String n2 = node2.getNodeName();
		final String expected[] = new String[] {
			n2, n1, n1, n2, n2, n2, n1
		};
		assertGenericBalanceOrder(main, reqs, expected);
	}
	
	/**
	 * A generic test requisition used for junits
	 */
	private static final class GenericTestRequisition extends HoldableRequisition {
		private int clockFactor = -1;
		private int memFactor = -1;
		/** Default Constructor. */
		private GenericTestRequisition() {
		}
		/**
		 * Default Constructor.
		 * @param clockFactor clock factor
		 */
		private GenericTestRequisition(int clockFactor) {
			this.clockFactor = clockFactor; 
		}
		/**
		 * Default Constructor.
		 * @param clockFactor clock factor
		 * @param memFactor mem factor
		 */
		private GenericTestRequisition(int clockFactor, int memFactor) {
			this.clockFactor = clockFactor; 
			this.memFactor = memFactor;
		}
		/** {@inheritDoc} */
		public int getProcessingFactor() {
			return clockFactor;
		}
		/** {@inheritDoc} */
		public int getMemoryFactor() {
			return memFactor;
		}
		/** {@inheritDoc} */
		public Object processImpl() throws Exception {
			return getNodeBeeingProcessed().getNodeName();
		}
	}

}
