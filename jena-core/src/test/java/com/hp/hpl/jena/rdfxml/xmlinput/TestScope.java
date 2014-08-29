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

package com.hp.hpl.jena.rdfxml.xmlinput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.RDFErrorHandler;

public class TestScope extends TestCase {
	public static Test suite() {
		TestSuite s = new TestSuite(TestScope.class);
		s.setName("ARP Scoping");
		return s;
	}
  public	TestScope(String nm){
		super(nm);
	}
	/*
	public void test01() throws Exception {

		check("testing/wg/rdfms-syntax-incomplete/test004.rdf");
	//	check("testing/arp/scope/test01.rdf");
	}
	public void test02() throws Exception {
		check("testing/arp/scope/test02.rdf");
	}
	public void test03() throws Exception {
		check("testing/arp/scope/test03.rdf");
	}
	

	public void test04() throws Exception {
		check("testing/arp/scope/test04.rdf");
	}
	
	public void test05() throws Exception {
		check("testing/arp/scope/test05.rdf");
	}
*/
    public void test06() throws Exception {
        check("testing/arp/scope/test06.rdf");
    }
	static RDFErrorHandler suppress = new RDFErrorHandler(){

		@Override
        public void warning(Exception e) {
		}

		@Override
        public void error(Exception e) {
		}

		@Override
        public void fatalError(Exception e) {
		}
		
	};
	private void check(final String fn) throws IOException {
		
		NTripleTestSuite.loadRDFx(new InFactoryX(){

			@Override
            public InputStream open() throws IOException {
				return new FileInputStream(fn);
			}
		},suppress,"http://example.org/a",false,0);
	//	in.close();
	}

}
