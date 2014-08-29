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

// Package
///////////////
package com.hp.hpl.jena.rdfxml.xmloutput;

// Imports
///////////////

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Pattern;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class PrettyWriterTest extends ModelTestBase {

	/**
	 * Constructor requires that all tests be named
	 *
	 * @param name The name of this test
	 */
	public PrettyWriterTest(String name) {
		super(name);
	}

	// Test cases
	/////////////
//	static AwkCompiler awk = new AwkCompiler();
//	static AwkMatcher matcher = new AwkMatcher();

	/**
	 * @param filename Read this file, write it out, read it in.
	 * @param regex    Written file must match this.
	 */
	private void check( String filename, String regex ) throws IOException {
		check(filename, regex, true);
	}

	private void checkNoMatch(String filename, String regex ) throws IOException {
		check(filename, regex, false);
		
	}
	private void check( String filename, String regex, boolean match ) throws IOException {
		String contents = null;
		try {
			Model m = createMemModel();
			m.read( filename );
			try ( StringWriter sw = new StringWriter() ) {
			    m.write( sw, "RDF/XML-ABBREV", filename );
			    contents = sw.toString();
			}
			Model m2 = createMemModel();
			m2.read( new StringReader( contents ), filename );
			assertTrue( m.isIsomorphicWith( m2 ) );
            
			assertTrue(
				"Looking for /" + regex + "/ ",
//                +contents,
                match==Pattern.compile( regex,Pattern.DOTALL ).matcher( contents ).find()
//				matcher.contains(contents, awk.compile(regex))
                );
			contents = null;
		} finally {
			if (contents != null) {
				System.err.println("Incorrect contents:");
				System.err.println(contents);
			}
		}
	}
	
	public void testConsistency() throws IOException {
		checkNoMatch(
				"file:testing/abbreviated/consistency.rdf",
	            "rdf:resource");
	}


	public void testRDFCollection() throws IOException {
		check(
			"file:testing/abbreviated/collection.rdf",
			"rdf:parseType=[\"']Collection[\"']");
	}

	public void testOWLPrefix() {
		//		check(
		//			"file:testing/abbreviated/collection.rdf",
		//			"xmlns:owl=[\"']http://www.w3.org/2002/07/owl#[\"']");
	}

	public void testLi() throws IOException {
		check(
			"file:testing/abbreviated/container.rdf",
			"<rdf:li.*<rdf:li.*<rdf:li.*<rdf:li");
	}
	public void test803804() {
		String sourceT =
			"<rdf:RDF "
				+ " xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
				+ " xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
				+ " xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
				+ " <owl:ObjectProperty rdf:about="
				+ "'http://example.org/foo#p'>"
				+ " </owl:ObjectProperty>"
				+ "</rdf:RDF>";

		OntModel m =
			ModelFactory.createOntologyModel(
				OntModelSpec.OWL_MEM_RULE_INF,
				null);
		m.read(
			new ByteArrayInputStream(sourceT.getBytes()),
			"http://example.org/foo");

		Model m0 = ModelFactory.createModelForGraph(m.getGraph());
		/*
			  Set copyOfm0 = new HashSet();
			  Set blankNodes = new HashSet();
			  Iterator it = m0.listStatements();
			  while (it.hasNext()) {
			  	Statement st = (Statement)it.next(); 
				  copyOfm0.add(st);
				  Resource subj = st.getSubject();
				  if (subj.isAnon())
				    blankNodes.add(subj);
			  }
			  
			  it = blankNodes.iterator();
			  while (it.hasNext()) {
			  	Resource b = (Resource)it.next();
			  	Statement st = m0.createStatement(b,OWL.sameAs,b);
			//  	assertEquals(m0.contains(st),copyOfm0.contains(st));
			  }
		*/
		XMLOutputTestBase.blockLogger();
		try {
			m0.write(new OutputStream() {
				@Override
                public void write(int b) throws IOException {
				}
			}, "RDF/XML-ABBREV");

		} finally {
			// This will need to change when the bug is finally fixed.
			
			assertTrue(XMLOutputTestBase.unblockLogger());
		}
	}
}
