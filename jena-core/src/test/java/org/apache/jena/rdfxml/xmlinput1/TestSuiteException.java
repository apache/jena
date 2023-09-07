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

package org.apache.jena.rdfxml.xmlinput1;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFErrorHandler ;
import org.apache.jena.rdf.model.RDFReaderI ;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler ;
import org.apache.jena.shared.JenaException ;

public class TestSuiteException
	extends TestCase
	implements ARPErrorNumbers {
	static public Test suite() {
		TestSuite suite = new TestSuite("ARP Exceptions");

		suite.addTest(new TestSuiteException("testDefaultFatal"));
		suite.addTest(new TestSuiteException("testDefaultError"));
		suite.addTest(new TestSuiteException("testDefaultWarning"));
		suite.addTest(new TestSuiteException("testDefaultDemotedFatal"));
		suite.addTest(new TestSuiteException("testDefaultPromotedError"));
		suite.addTest(new TestSuiteException("testDefaultPromotedWarning"));
		suite.addTest(new TestSuiteException("testNonExceptionFatal"));
		suite.addTest(new TestSuiteException("testExceptionError"));
		suite.addTest(new TestSuiteException("testExceptionWarning"));
		return suite;
	}

	TestSuiteException(String s) {
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
		  RDFReaderI rdr = new RDFXMLReader();
		  rdr.setProperty("ERR_SAX_FATAL_ERROR","EM_ERROR");
		  rdr.read(m,"file:testing/arp/error-msgs/test06.rdf");
		}
		catch (JenaException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
			  fail("Demoted.error error threw an exception");
		}
		finally {
			RDFDefaultErrorHandler.silent = false;
		}

	}
	public void testDefaultPromotedError() {

		RDFDefaultErrorHandler.silent = true;
		try {
		  Model m = ModelFactory.createDefaultModel();
		  RDFReaderI rdr = new RDFXMLReader();
		  rdr.setProperty("ERR_BAD_RDF_ATTRIBUTE","EM_FATAL");
		  rdr.read(m,"file:testing/wg/rdfms-abouteach/error002.rdf");

		  fail("Promoted error did not throw exception");
		}
		catch (JenaException e){
	//		System.err.println(e.getMessage());
		}
	}

	public void testDefaultPromotedWarning() {

		RDFDefaultErrorHandler.silent = true;
		try {

		  Model m = ModelFactory.createDefaultModel();
		  RDFReaderI rdr = new RDFXMLReader();
		  rdr.setProperty("WARN_BAD_NAME","EM_FATAL");
		  rdr.read(m,"file:testing/arp/qname-in-ID/bug74_0.rdf");

		  fail("Promoted warning did not throw exception");
		}
		catch (JenaTestException e){
		}
        catch (JenaException e) {}
		finally {
			RDFDefaultErrorHandler.silent = false;
		}

	}

	public void testNonExceptionFatal() {
		try {
		  Model m = ModelFactory.createDefaultModel();
		  RDFReaderI rdr = new RDFXMLReader();
		  rdr.setErrorHandler(new ErrorHandlerNoWarnings());
		  rdr.read(m,"file:testing/arp/error-msgs/test06.rdf");
		}
		catch (JenaTestException e){
		    System.err.println(e);
		    System.err.println(e.getCause());
		    fail("Fatal error threw an exception with non-exception handler");
		}
		catch (JenaException e){ }


	}
	public void testExceptionError() {
		try {
		  Model m = ModelFactory.createDefaultModel();
		  RDFReaderI rdr = new RDFXMLReader();
		  rdr.setErrorHandler(new ErrorHandlerEx());
		  rdr.read(m,"file:testing/wg/rdfms-abouteach/error002.rdf");

		  fail("Error did not throw exception with non-standard handler");
		}
		catch (JenaTestException e){ }
		catch (JenaException e){ }

	}

	static class JenaTestException extends JenaException {
	    JenaTestException(Exception e) { super(e); }
	}

	public void testExceptionWarning() {
		try {

		  Model m = ModelFactory.createDefaultModel();
		  RDFReaderI rdr = new RDFXMLReader();
		  rdr.setErrorHandler(new ErrorHandlerEx());
		  rdr.read(m,"file:testing/arp/qname-in-ID/bug74_0.rdf");
		  fail("Warning did not throw exception with non-standard handler");
		}
		catch (JenaTestException e){}
		catch (JenaException e){}

	}

	static class ErrorHandlerEx implements RDFErrorHandler {
	    @Override
	    public void warning(Exception e) {
	        throw new JenaException(e);
	    }

	    @Override
	    public void error(Exception e) {
	        throw new JenaException(e);
	    }

	    @Override
	    public void fatalError(Exception e) {
	        //throw new JenaException(e);
	    }
	}

	static class ErrorHandlerNoWarnings implements RDFErrorHandler {
	    @Override
	    public void warning(Exception e) { }

	    @Override
	    public void error(Exception e) {
	        throw new JenaException(e);
	    }

	    @Override
	    public void fatalError(Exception e) {
	        throw new JenaException(e);
	    }
	}

}
