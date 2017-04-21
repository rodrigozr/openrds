/**
 * AllTests.java
 * Created by: Rodrigo
 * Created at: Jul 26, 2006 6:16:06 PM
 *
 * $Revision: 1.3 $
 * $Date: 2006/12/19 17:54:53 $ (of revision)
 * $Author: rodrigorosauro $ (of revision)
 */

package net.sf.openrds;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test suite with all junit classes
 * @author Rodrigo
 */
public class AllTests {

	/** @return Test suite */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for net.sf.openrds");
		//$JUnit-BEGIN$
		suite.addTestSuite(SimpleTest.class);
		suite.addTestSuite(LoadBalanceTest.class);
		suite.addTestSuite(SimpleRmiHttpServerTest.class);
		//$JUnit-END$
		return suite;
	}

}
