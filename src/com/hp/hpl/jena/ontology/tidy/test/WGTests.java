/*
 *  (c) Copyright 2001, 2002 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 *
*
 * WGTests.java
 *
 * Created on September 18, 2001, 7:50 PM
 */

package com.hp.hpl.jena.ontology.tidy.test;

import com.hp.hpl.jena.shared.wg.*;
import com.hp.hpl.jena.shared.wg.URI;
import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.rdql.*;

import junit.framework.TestSuite;
import junit.framework.Test;
import java.io.*;
import java.net.*;
import java.util.*;
/**
 * The JUnit test suite for ARP.
 *
 * @author  jjc

 */
public class WGTests extends java.lang.Object {
	/**
	 * Setting this field to true uses the tests found
	 * on the W3C web site.
	 * The default value false uses the cached (possibly edited)
	 * copy of the tests.
	 */
	static public boolean internet = false;
	static private URI wgTestDir = URI.create("http://www.w3.org/2002/03owlt/");
	/**
	 * Setting this before invoking suite can permit the use of a zip based factory
	 * etc.
	 */
	static private TestInputStreamFactory factory;
	
	static private String manifestURI = "OWLManifest.rdf";
	static private boolean manifestInFactory = true;
	
	static public Test suite() throws IOException {
		TestSuite s = new TestSuite("OWL-Syntax");
		InputStream manifest;
		if (factory == null) {
			if (internet) {
				factory = new TestInputStreamFactory(wgTestDir, wgTestDir);
			} else {
				factory = new TestInputStreamFactory(wgTestDir, "wg");
			}
		}
		if ( manifestInFactory ) {
			manifest = factory.open(manifestURI);
		} else {
			try {
		     manifest = new URL(manifestURI).openStream();
			}
			catch (MalformedURLException e) {
				manifest = new FileInputStream(manifestURI);
			}
		}
		Model m = ModelFactory.createDefaultModel();
		m.read(manifest,"");

		Query query =  new Query(
				"SELECT ?s, ?t, ?f, ?l " +
				"WHERE (?f rdf:type " +
				" <http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#RDF-XML-Document> )" +
				" (?t ?p ?f ) " +
                " (?t rtest:status ?s) " +
                " (?f otest:level ?l ) " +
				"AND ?s ne \"OBSOLETED\""+
				"USING rtest FOR <http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#>" +
				" otest FOR <http://www.w3.org/2002/03owlt/testOntology#>"
				);
				
//		   Need to set the source if the query does not.
		query.setSource(m);
		QueryExecution qe = new QueryEngine(query) ;

		QueryResults results = qe.exec() ;
		
		for ( Iterator iter = results ; iter.hasNext() ; )
		{
			ResultBinding res = (ResultBinding)iter.next() ;
			Object status = res.get("s");
			Object testResource = res.get("t") ;
			Object testFile = res.get("f") ;
			Object level = res.get("l");
			
			TestSuite st = (TestSuite)getTest(s, ((Literal)status).getLexicalForm(), false );
			String testURI = ((Resource)testResource).getURI();
			System.err.println(testURI);
			int lastSl = testURI.lastIndexOf('/');
			int penUltimateSl = testURI.lastIndexOf('/', lastSl -1 );
			int hash = testURI.lastIndexOf('#');
			System.err.println(lastSl + " " + penUltimateSl + " " + hash);
			String dirName = testURI.substring(penUltimateSl+1,lastSl);
			TestSuite dir = (TestSuite)getTest(st, dirName, false );
			String number = testURI.substring(hash-3,hash);
			SyntaxTest test = (SyntaxTest)getTest( dir, number, true );
			String fileURI = ((Resource)testFile).getURI();
		    test.add( factory.open(fileURI), (Resource)level, fileURI );
		}
		results.close() ;
		
		return s;
	}

    static private Test getTest( TestSuite s, String nm, boolean syntaxTest ) {
    	Enumeration already = s.tests();
    	Test t;
    	while ( already.hasMoreElements() ) {
    		t = (Test)already.nextElement();
    		if ( ( syntaxTest 
    		         ? ((SyntaxTest) t).getName()
    		         : ((TestSuite)t).getName()   ).equals(nm) )
    		    return t;
    	}
    	
    	if ( !syntaxTest ) {
    		t = new TestSuite(nm);
    	} else {
    		t = new SyntaxTest( nm, factory);
    	}
		s.addTest(t);
    	return t;
    }

	static public void main(String args[]) {

	}

}