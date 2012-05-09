/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.test.JenaTestBase;



/**
 * <p>
 * This is a wrapper class, which implements a set of basic regression tests as
 * a set of JUnit tests.
 * </p>
 * 
 * <p>
 * This is the first step of reworking the old Regression tests as a JUnit test
 * suite. This is a simple wrapper class for the Regression test package. The
 * idea is that if time permits (fat chance) the old regression tests will be
 * reworked properly into this framework.
 * </p>
 * 
 * <p>
 * This class is inteded to be subclassed with setup and teardown methods to
 * create models required for the tests.
 * </p>
 * 
 * @author bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1 $ $Date: 2009-06-29 08:55:39 $
 */
public class TestCaseBasic extends JenaTestBase {

	protected Model m1 = null;
	protected Model m2 = null;
	protected Model m3 = null;
	protected Model m4 = null;

	public TestCaseBasic(String name) {
		super(name);
	}

	public void test0() {
		// empty the test models in case they are persistent
		empty(m1);
		empty(m2);
		empty(m3);
		empty(m4);
	}

	protected void empty(Model m) {
		try {
			StmtIterator iter = m.listStatements();
			while (iter.hasNext()) {
				iter.nextStatement();
				iter.remove();
			}
			assertTrue(m.size() == 0);
		} catch (Exception e) {
			System.err.println(e);
			assertTrue(false);
		}
	}

	public void test1() {
		try {
			Regression test = new Regression();
			test.test1(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test2() {
		try {
			Regression test = new Regression();
			test.test2(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test3() {
		try {
			Regression test = new Regression();
			test.test3(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test4() {
		try {
			Regression test = new Regression();
			test.test4(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test5() {
		try {
			Regression test = new Regression();
			test.test5(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test6() throws Exception {
		try {
			Regression test = new Regression();
			test.test6(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
	      System.err.println( "PONGLE" );
	      throw e;
		}
	}

	public void test7() {
		try {
			Regression test = new Regression();
			test.test7(m1, m2);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test8() {
	    new Regression().test8( m1 );
//		try {
//			Regression test = new Regression();
//			test.test8(m1);
//			assertTrue(!test.getErrors());
//		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
//		}
	}

	public void test9() {
	    new Regression().test9( m2 );
//		try {
//			Regression test = new Regression();
//			test.test9(m2);
//			assertTrue(!test.getErrors());
//		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
//		}
	}

	public void test10() {
	    new Regression().test10( m3 );
//		try {
//			Regression test = new Regression();
//			test.test10(m3);
//			assertTrue(!test.getErrors());
//		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
//		}
	}

	public void test11() {
	    new Regression().test11( m1, m2 );
//		try {
//			Regression test = new Regression();
//			test.test11(m1, m2);
//			assertTrue(!test.getErrors());
//		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
//		}
	}

	public void test12() {
		try {
			Regression test = new Regression();
			test.test12(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test13() {
		try {
			Regression test = new Regression();
			test.test13(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test14() {
		try {
			Regression test = new Regression();
			test.test14(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test15() {
	    new Regression().test15( m1 );
//		try {
//			Regression test = new Regression();
//			test.test15(m1);
//			assertTrue(!test.getErrors());
//		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
//		}
	}

	public void test16() {
	    new Regression().test16( m1 );
//		try {
//			Regression test = new Regression();
//			test.test16(m1);
//			assertTrue(!test.getErrors());
//		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
//		}
	}

	public void test17() {
	    new Regression().test17( m1 );
//		try {
//			Regression test = new Regression();
//			test.test17(m1);
//			assertTrue(!test.getErrors());
//		} catch (Exception e) {
//			System.out.println(e);
//			assertTrue(false);
//		}
	}

	public void test18() {
		try {
			Regression test = new Regression();
			test.test18(m4);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test19() {
		try {
			Regression test = new Regression();
			test.test19(m2, m3);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void test97() {
		try {
			Regression test = new Regression();
			test.test97(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}
	private GetModel getGetModel() {
		return new GetModel() {

			Model cache[] = new Model[4];
			int i = 4;
			@Override
            public Model get() {
				if (i == 4) {
					try {
						tearDown();
						setUp();
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
					cache[0] = m1;
					cache[1] = m2;
					cache[2] = m3;
					cache[3] = m4;
					i = 0;
				}
				return cache[i++];
			}
		};
	}
	public void testMatch() {
		try {
			testMatch test = new testMatch(0xfab, getGetModel());
			test.test();
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}
	/*
	 * public void testWriterAndReader() { try { testWriterAndReader test = new
	 * testWriterAndReader(); test.test(m1,m2,m3,m4); assertTrue(!
	 * test.getErrors()); } catch (Exception e) { System.out.println(e);
	 * assertTrue(false); } }
	 */
	public void testNTripleReader() {
		try {
			testNTripleReader test = new testNTripleReader();
			test.test(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}
	/*
	 * public void testWriterInterface() { try { testWriterInterface test = new
	 * testWriterInterface(); test.test(m1); assertTrue(! test.getErrors()); }
	 * catch (Exception e) { System.out.println(e); assertTrue(false); } }
	 */
	public void testReaderInterface() {
		try {
			testReaderInterface test = new testReaderInterface();
			test.test(m1);
			assertTrue(!test.getErrors());
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}

	public void soaktest() { // a very crude soak test
		try {
			int errCount = 0;

			for (int i = 1; i <= 100; i++) {
				Regression test = new Regression();
				test0();
				test.test1(m1);
				test.test2(m1);
				test.test3(m1);
				test.test4(m1);
				test.test5(m1);
				test.test6(m1);
				test.test7(m1, m2);
				test.test8(m1);
				test.test9(m2);
				test.test10(m3);
				test.test11(m1, m2);
				test.test12(m1);
				test.test13(m1);
				test.test14(m1);
				test.test15(m1);
				test.test16(m1);
				test.test17(m1);
				test.test18(m4);
				test.test19(m2, m3);
				test.test97(m1);
				if (test.getErrors())
					errCount++;
				if ((i % 10) == 0) {
					System.out.println(
						"error count = " + errCount + " rounds = " + i);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			assertTrue(false);
		}
	}
}
