/**
 * OpenRDSTestCase.java
 * Created by: Rodrigo
 * Created at: Jul 26, 2006 3:55:47 PM
 *
 * $Revision: 1.5 $
 * $Date: 2007/08/02 18:16:36 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * Test-case with helper methods for other junits
 * @author Rodrigo
 */
public class OpenRDSTestCase extends TestCase {
	private MainNode startedMainNode;
	private List startedProcessNodes = new Vector();
	
	/** {@inheritDoc} */
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty(ISystemProperties.BASE_IP, "127.0.0.1");
		System.setProperty(ISystemProperties.CLOCK_AMOUNT, "100");
		System.setProperty(ISystemProperties.MEMORY_AMOUNT, "100");
	}
	/** {@inheritDoc} */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (startedMainNode != null) {
			startedMainNode.finish();
		}
		for (final Iterator it = this.startedProcessNodes.iterator(); it.hasNext();) {
			final IProcessNode node = (IProcessNode) it.next();
			node.finish();
		}
		RegistryHandler.getInstance().finish();
	}
	
	/**
	 * Starts a main node
	 * @param allowDynamicClassDownload true to allow dynamic class download
	 * @return MainNode
	 */
	protected static MainNode startMainNode(boolean allowDynamicClassDownload) {
		try {
			return (MainNode) NodeFactory.getInstance().startMainNode(allowDynamicClassDownload);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not start main node. Cause: " + e.toString());
		}
		return null;
	}
	/**
	 * Starts a main node that does not allow dynamic class download
	 * @return MainNode
	 */
	protected static MainNode startMainNode() {
		return startMainNode(false);
	}
	
	/**
	 * Starts a process node
	 * @return ProcessNode
	 */
	protected static ProcessNode startProcessNode() {
		MachineInformation.reset();
		try {
			return (ProcessNode) NodeFactory.getInstance().startProcessNode("127.0.0.1", true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not start process node. Cause: " + e.toString());
		}
		return null;
	}
	/**
	 * Starts a process node with specific clock and memory amount
	 * Note that this is needed to start more than one process node on the same JVM.
	 * Also note that this will modify system properties
	 * @param clockAmount clock amount
	 * @param memAmount memory amount
	 * @return ProcessNode
	 */
	protected static ProcessNode startProcessNode(String clockAmount, String memAmount) {
		System.setProperty(ISystemProperties.CLOCK_AMOUNT, clockAmount);
		System.setProperty(ISystemProperties.MEMORY_AMOUNT, memAmount);
		return startProcessNode();
	}
	/**
	 * Starts a process node with specific clock amount and default memory
	 * Note that this is needed to start more than one process node on the same JVM.
	 * Also note that this will modify system properties
	 * @param clockAmount clock amount
	 * @return ProcessNode
	 */
	protected static ProcessNode startProcessNode(String clockAmount) {
		return startProcessNode(clockAmount, "100");
	}
	/**
	 * Performs a notify() or notifyAll() operation on the given mutex
	 * @param mutex mutex
	 * @param notifyAll if true, notifyAll() will be called
	 */
	protected static void notifyMutex(Object mutex, boolean notifyAll) {
		synchronized (mutex) {
			if (notifyAll) {
				mutex.notifyAll();
			} else {
				mutex.notify();
			}
		}
	}
	/**
	 * Performs a notify() operation on the given mutex
	 * @param mutex mutex
	 */
	protected static void notifyMutex(Object mutex) {
		notifyMutex(mutex, false);
	}
	/**
	 * Performs a wait() operation on te given mutex
	 * @param mutex mutex
	 */
	protected static void waitMutex(Object mutex) {
		synchronized (mutex) {
			try {
				mutex.wait();
			} catch (InterruptedException ignored) {
			}
		}
	}
	/**
	 * Performs a wait(millis) operation on te given mutex
	 * @param mutex mutex
	 * @param millis time to wait
	 */
	protected static void waitMutex(Object mutex, long millis) {
		synchronized (mutex) {
			try {
				mutex.wait(millis);
			} catch (InterruptedException ignored) {
			}
		}
	}
	/**
	 * Process a requisition on a separated thread. When the requisition finish, puts the
	 * result on "resultList".
	 * @param node node to process on
	 * @param req requisition to process
	 * @param resultList result list to add to (can be null to ignore)
	 * @return the new thread, that can be used to join() with
	 */
	protected Thread processAsync(final INode node, final Requisition req, final List resultList) {
		Thread t = new Thread() {
			public void run() {
				Object res = null;
				try {
					res = node.processRequisition(req);
				} catch (Throwable t) {
					res = t;
				}
				if (resultList != null) {
					synchronized (resultList) {
						resultList.add(res);
					}
				}
			}
		};
		t.start();
		return t;
	}
	
	/**
	 * An indivisible requisition that have some methods to help making junits.
	 * @author Rodrigo
	 */
	protected abstract static class HoldableRequisition extends IndivisibleRequisition {
		private boolean processStarted = false;
		private Object processMutex = new Object();
		private boolean released = false;
		private Object releaseMutex = new Object();
		private INode processNode;
		
		/** Blocks until this requisition has started processing */
		public void waitProcessingToStart() {
			synchronized (processMutex) {
				while (!processStarted) {
					waitMutex(processMutex);
				}
			}
		}
		/**
		 * Release this requisition to call "processImpl()" and return result.
		 */
		public void release() {
			released = true;
			notifyMutex(releaseMutex, true);
		}
		/** @return the node where this requisition is beeing processed */
		public INode getNodeBeeingProcessed() {
			return this.processNode;
		}
		/** {@inheritDoc} */
		public final void onBeforeProcess(IProcessNode node) throws Exception {
			this.processNode = node;
		}
		/** {@inheritDoc} */
		public final Object process() throws ProcessingException {
			processStarted = true;
			notifyMutex(processMutex, true);
			synchronized (releaseMutex) {
				while (!released) {
					waitMutex(releaseMutex);
				}
			}
			try {
				return processImpl();
			} catch (Exception e) {
				throw new ProcessingException("Error processing", e);
			}
		}
		/**
		 * This must be implemented by sub classes with the desired processing behaviour
		 * @return Object
		 * @throws Exception on any error
		 */
		public abstract Object processImpl() throws Exception;
	}
}
