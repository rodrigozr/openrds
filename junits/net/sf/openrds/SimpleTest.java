/**
 * SimpleTest.java
 * Created by: Rodrigo
 * Created at: Jul 26, 2006 3:50:12 PM
 *
 * $Revision: 1.4 $
 * $Date: 2007/08/02 18:07:12 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;



/**
 * Simple junit tests
 * @author Rodrigo
 */
public class SimpleTest extends OpenRDSTestCase {
	
	/**
	 * junit
	 * @throws Exception on any error
	 */
	public void testSimpleRequisition() throws Exception {
		final IMainNode main = startMainNode(false);
		startProcessNode();
		final IndivisibleRequisition req = new IndivisibleRequisition() {
			public Object process() throws ProcessingException {
				return "Simple Test Result";
			}
		};
		final String result = (String) main.processRequisition(req);
		assertEquals("Simple Test Result", result);
	}
	
	/**
	 * junit
	 * @throws Exception on any error
	 */
	public void testSimpleEvents() throws Exception {
		final List events = new LinkedList();
		final IMainNode main = startMainNode(false);
		main.addNodeEventListener(new NodeEventAdaptor() {
			public void nodeRegistered(IMainNode mainNode, IProcessNode registeredNode) {
				try {
					events.add("registed:" + registeredNode.getNodeName());
				} catch (RemoteException e) {
				}
			}
			public void nodeUnregistered(IMainNode mainNode, IProcessNode unregisteredNode) {
				try {
					events.add("unregisted:" + unregisteredNode.getNodeName());
				} catch (RemoteException e) {
				}
			}
			public void nodeRequisitionProcessed(INode node, Requisition requisition, Object result) {
				events.add("mainProcessed:" + result);
			}
			public void nodeRequisitionFailed(INode node, Requisition requisition, Throwable error) {
				events.add("mainFailed:" + error.getMessage());
			}
			public void nodeFinished(INode node) {
				try {
					events.add("finished:" + node.getNodeName());
				} catch (RemoteException e) {
				}
			}
		});
		ProcessNode process = startProcessNode();
		process.addNodeEventListener(new NodeEventAdaptor() {
			public void nodeRequisitionProcessed(INode node, Requisition requisition, Object result) {
				events.add("processed:" + result);
			}
			public void nodeRequisitionFailed(INode node, Requisition requisition, Throwable error) {
				events.add("failed:" + error.getMessage());
			}
			public void nodeFinished(INode node) {
				try {
					events.add("finished:" + node.getNodeName());
				} catch (RemoteException e) {
				}
			}
		});
		final IndivisibleRequisition req = new IndivisibleRequisition() {
			public Object process() throws ProcessingException {
				return "Simple Test Result";
			}
		};
		final IndivisibleRequisition failReq = new IndivisibleRequisition() {
			public Object process() throws ProcessingException {
				throw new ProcessingException("FAIL");
			}
		};
		final String result = (String) main.processRequisition(req);
		try {
			main.processRequisition(failReq);
		} catch (Exception ignored) {
		}
		process.finish();
		main.finish();
		assertTrue(events.remove("registed:" + process.getNodeName()));
		assertTrue(events.remove("unregisted:" + process.getNodeName()));
		assertTrue(events.remove("processed:" + result));
		assertTrue(events.remove("mainProcessed:" + result));
		assertTrue(events.remove("failed:FAIL"));
		assertTrue(events.remove("mainFailed:FAIL"));
		assertTrue(events.remove("finished:" + process.getNodeName()));
		assertTrue(events.remove("finished:" + main.getNodeName()));
		assertEquals(0, events.size());
	}
	
	/**
	 * junit
	 * @throws Exception on any error
	 */
	public void testSimpleNoFactorLoadBalance() throws Exception {
		final MainNode main = startMainNode(false);
		final ProcessNode node1 = startProcessNode("1");
		final ProcessNode node2 = startProcessNode("2");
		assertEquals(1, node1.getClockAmount());
		assertEquals(2, node2.getClockAmount());
		
		final List resultList = new LinkedList();
		final HoldableRequisition reqs[] = new HoldableRequisition[2];
		for (int i = 0; i < reqs.length; i++) {
			reqs[i] = new HoldableRequisition() {
				public Object processImpl() throws Exception {
					return (getNodeBeeingProcessed().getClockAmount() + "");
				}
			};
		}
		final Thread threads[] = new Thread[] {
			processAsync(main, reqs[0], resultList),
			processAsync(main, reqs[1], resultList)
		};
		for (int i = 0; i < reqs.length; i++) {
			reqs[i].waitProcessingToStart(); // Waits for the requisitions to start processing
		}
		for (int i = 0; i < reqs.length; i++) {
			reqs[i].release(); // Release the requisitions, so they will return value
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].join(); // Waits the started threads to put result on the list
		}
		assertEquals(2, resultList.size());
		assertTrue(resultList.contains("1"));
		assertTrue(resultList.contains("2"));
	}
	
	/**
	 * junit
	 * @throws Exception on any error
	 */
	public void testNoNodesAvailable() throws Exception {
		final IMainNode main = startMainNode(false);
		final IndivisibleRequisition req = new IndivisibleRequisition() {
			public Object process() throws ProcessingException {
				return "This cannot be processed";
			}
		};
		final DivisibleRequisition div = new DivisibleRequisition() {
			public Object getResponse(Object[] subResults) {
				return null;
			}
			public SubRequisition[] getSubRequisitions(int availableNodes) {
				return new SubRequisition[0];
			}
		};
		try {
			main.processRequisition(req);
			fail("There are no nodes available, we can't process anything.");
		} catch (NoNodesAvailableException good) {
			// This is expected to happen
		}
		try {
			main.processRequisition(div);
			fail("There are no nodes available, we can't process anything.");
		} catch (NoNodesAvailableException good) {
			// This is expected to happen
		}
	}
}
