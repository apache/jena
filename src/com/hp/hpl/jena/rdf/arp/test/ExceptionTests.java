/*
 *  (c)     Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: ExceptionTests.java,v 1.5 2005-02-21 12:11:05 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdf.arp.test;
import junit.framework.*;



import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.shared.JenaException;
/**
 * @author jjc
 *
 */
public class ExceptionTests
	extends TestCase
	implements RDFErrorHandler, ARPErrorNumbers {
	static public Test suite() {
		TestSuite suite = new TestSuite("ARP Exceptions");
		
		suite.addTest(new ExceptionTests("testDefaultFatal"));
		suite.addTest(new ExceptionTests("testDefaultError"));
		suite.addTest(new ExceptionTests("testDefaultWarning"));
		suite.addTest(new ExceptionTests("testDefaultDemotedFatal"));
		suite.addTest(new ExceptionTests("testDefaultPromotedError"));
		suite.addTest(new ExceptionTests("testDefaultPromotedWarning"));
		suite.addTest(new ExceptionTests("testNonExceptionFatal"));
		suite.addTest(new ExceptionTests("testExceptionError"));
		suite.addTest(new ExceptionTests("testExceptionWarning"));
		return suite;
	}

	ExceptionTests(String s) {
		super(s);
	}
	
	public void testDefaultFatal() {
		//E301
		RDFDefaultErrorHandler.silent = true;
		try {
		  Model m = ModelFactory.createDefaultModel();
		  m.read("file:testing/arp/error-msgs/test06.rdf");
		  fail("Fatal error did not throw exception");
		}
		catch (JenaException e){
			
		}
		finally {
			RDFDefaultErrorHandler.silent = false;			
		}
		
	}
	public void testDefaultError() {

		// E206
		RDFDefaultErrorHandler.silent = true;
		try {
		  Model m = ModelFactory.createDefaultModel();
		  m.read("file:testing/wg/rdfms-abouteach/error002.rdf");
		}
		catch (JenaException e){
			  fail("Error threw exception");
		}
		finally {
			RDFDefaultErrorHandler.silent = false;			
		}
		
	}
	public void testDefaultWarning() {
		// W108
		RDFDefaultErrorHandler.silent = true;
		try {
		  Model m = ModelFactory.createDefaultModel();
		  m.read("file:testing/arp/qname-in-ID/bug74_0.rdf");
		}
		catch (JenaException e){
			  fail("Warning threw exception");
		}
		finally {
			RDFDefaultErrorHandler.silent = false;			
		}
	}

	public void testDefaultDemotedFatal() {

		RDFDefaultErrorHandler.silent = true;
		try {
		  Model m = ModelFactory.createDefaultModel();
		  RDFReader rdr = m.getReader();
		  rdr.setProperty("ERR_SAX_FATAL_ERROR","EM_ERROR");
		  rdr.read(m,"file:testing/arp/error-msgs/test06.rdf");
		}
		catch (JenaException e){
		//	System.err.println(e.getMessage());
		//	e.printStackTrace();
			  fail("Demoted fatal error threw an exception");
		}
		finally {
			RDFDefaultErrorHandler.silent = false;			
		}
	
	}
	public void testDefaultPromotedError() {

		RDFDefaultErrorHandler.silent = true;
		try {
		  Model m = ModelFactory.createDefaultModel();
		  RDFReader rdr = m.getReader();
		  rdr.setProperty("ERR_BAD_RDF_ATTRIBUTE","EM_FATAL");
		  rdr.read(m,"file:testing/wg/rdfms-abouteach/error002.rdf");

		  fail("Promoted error did not throw exception");
		}
		catch (JenaException e){
	//		System.err.println(e.getMessage());
		}
		finally {
			RDFDefaultErrorHandler.silent = false;			
		}
		
		
	}
	public void testDefaultPromotedWarning() {

		RDFDefaultErrorHandler.silent = true;
		try {

		  Model m = ModelFactory.createDefaultModel();
		  RDFReader rdr = m.getReader();
		  rdr.setProperty("WARN_BAD_NAME","EM_FATAL");
		  rdr.read(m,"file:testing/arp/qname-in-ID/bug74_0.rdf");

		  fail("Promoted warning did not throw exception");
		}
		catch (JenaException e){
		}
		finally {
			RDFDefaultErrorHandler.silent = false;			
		}
		
	}
	public void testNonExceptionFatal() {

		try {
		  Model m = ModelFactory.createDefaultModel();
		  RDFReader rdr = m.getReader();
		  rdr.setErrorHandler(this);
		  rdr.read(m,"file:testing/arp/error-msgs/test06.rdf");
		}
		catch (JenaException e){
			  fail("Fatal error threw an exception with non-exception handler");
		}
		
		
	}
	public void testExceptionError() {
		try {
		  Model m = ModelFactory.createDefaultModel();
		  RDFReader rdr = m.getReader();
		  rdr.setErrorHandler(this);
		  rdr.read(m,"file:testing/wg/rdfms-abouteach/error002.rdf");

		  fail("Error did not throw exception with non-standard handler");
		}
		catch (JenaException e){
		}
				
	}
	public void testExceptionWarning() {
		try {

		  Model m = ModelFactory.createDefaultModel();
		  RDFReader rdr = m.getReader();
		  rdr.setErrorHandler(this);
		  rdr.read(m,"file:testing/arp/qname-in-ID/bug74_0.rdf");

		  fail("Warning did not throw exception with non-standard handler");
		}
		catch (JenaException e){
		}
		
	}
	


	public void warning(Exception e) {
		throw new JenaException(e);
	}

	public void error(Exception e) {
		throw new JenaException(e);
	}

	public void fatalError(Exception e) {
	}

}

/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/