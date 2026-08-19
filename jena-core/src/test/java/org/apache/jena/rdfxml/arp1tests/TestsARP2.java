/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdfxml.arp1tests;

import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ARP - Tests after migration of Jena users to RRX */
public class TestsARP2 extends TestCase {

	static private Logger logger = LoggerFactory.getLogger(TestsARP2.class);
	static public Test suite() {
		TestSuite suite = new TestSuite(TestsARP2.class);
		return suite;
	}

	public TestsARP2(String s) {
		super(s);
	}

	protected Model createMemModel() {
		return ModelFactory.createDefaultModel();
	}

	// GH-4157
	public void testVersionLocalname() {
	    String x = """
	            <?xml version="1.0"?>
	            <rdf:Description xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	                xmlns:oslc_rm="http://open-services.net/xmlns/rm/1.0/">
	                <rdf:type rdf:resource="http://open-services.net/ns/core#RootServices"/>
	                <oslc_rm:version>7.0.2</oslc_rm:version>
	            </rdf:Description>
	            """;
	    Model m = createMemModel();
	    m.read(new StringReader(x), "");
	    assertEquals(2, m.size());
	}
}
