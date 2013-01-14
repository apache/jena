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

}
