/*
    (c) Copyright 2001, 2002 Hewlett-Packard Development Company, LP
    All rights reserved.
    [See end of file]
    $Id: PrettyWriterTest.java,v 1.7 2003-12-11 12:17:20 jeremy_carroll Exp $
*/

// Package
///////////////
package com.hp.hpl.jena.xmloutput.test;

// Imports
///////////////

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.*;
import java.util.*;

import org.apache.oro.text.awk.AwkCompiler;
import org.apache.oro.text.awk.AwkMatcher;
import org.apache.oro.text.regex.MalformedPatternException;

import java.io.*;

/**
 * JUnit regression tests for the Jena DAML model.
 *
 * @author Jeremy Carroll
 * @version CVS info: $Id: PrettyWriterTest.java,v 1.7 2003-12-11 12:17:20 jeremy_carroll Exp $,
 */

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
	static AwkCompiler awk = new AwkCompiler();
	static AwkMatcher matcher = new AwkMatcher();

	/**
	 * 
	 * @param filename Read this file, write it out, read it in.
	 * @param regex    Written file must match this.
	 */
	private void check(String filename, String regex)
		throws IOException, MalformedPatternException {
		String contents = null;
		try {
			Model m = createMemModel();
			m.read(filename);
			StringWriter sw = new StringWriter();
			m.write(sw, "RDF/XML-ABBREV", filename);
			sw.close();
			contents = sw.toString();
			Model m2 = createMemModel();
			m2.read(new StringReader(contents), filename);
			assertTrue(m.isIsomorphicWith(m2));
			assertTrue(
				"Looking for /" + regex + "/",
				matcher.contains(contents, awk.compile(regex)));
			contents = null;
		} finally {
			if (contents != null) {
				System.err.println("Incorrect contents:");
				System.err.println(contents);
			}
		}
	}

	public void testAnonDamlClass()
		throws IOException, MalformedPatternException {
		check(
			"file:testing/abbreviated/daml.rdf",
			"rdf:parseType=[\"']daml:collection[\"']");
	}

	public void testRDFCollection()
		throws IOException, MalformedPatternException {
		check(
			"file:testing/abbreviated/collection.rdf",
			"rdf:parseType=[\"']Collection[\"']");
	}

	public void testOWLPrefix() throws IOException, MalformedPatternException {
		//		check(
		//			"file:testing/abbreviated/collection.rdf",
		//			"xmlns:owl=[\"']http://www.w3.org/2002/07/owl#[\"']");
	}

	public void testLi() throws IOException, MalformedPatternException {
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
		TestXMLFeatures.blockLogger();
		try {
			m0.write(new OutputStream() {
				public void write(int b) throws IOException {
				}
			}, "RDF/XML-ABBREV");

		} finally {
			// This will need to change when the bug is finally fixed.
			
			assertTrue(TestXMLFeatures.unblockLogger());
		}
	}
}

/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Jeremy Carroll, HP Labs Bristol
 * Author email       jjc@hpl.hp.com
 * Package            Jena
 * Created            10 Nov 2000
 * Filename           $RCSfile: PrettyWriterTest.java,v $
 * Revision           $Revision: 1.7 $
 *
 * Last modified on   $Date: 2003-12-11 12:17:20 $
 *               by   $Author: jeremy_carroll $
 *
 * (c) Copyright 2002 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/
