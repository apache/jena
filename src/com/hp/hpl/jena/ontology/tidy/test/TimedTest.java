/*
 * Created on 23-Nov-2003
 *
 */
package com.hp.hpl.jena.ontology.tidy.test;

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * @author Jeremy J. Carroll
 *
 */
public class TimedTest {

	static public Test suite() {
		TestSuite s = new TestSuite("TimedTest");
		s.addTest(WGTests.suite());
		final long startTime[] = new long[1];
		s.addTest(new TestCase("start timer") {
			public void runTest() {
				startTime[0] = System.currentTimeMillis();
			}
		});
		WGTests.doLargeTests = true;
		s.addTest(WGTests.suite());
		s.addTest(new TestCase("end timer") {
			public void runTest() {
				System.err.println(
(				System.currentTimeMillis() - startTime[0])
+ " ms");
			}
		});
		return s;
	}

}
