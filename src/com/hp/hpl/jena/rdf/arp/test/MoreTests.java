/*
 *  (c)     Copyright 2000, 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: MoreTests.java,v 1.10 2003-12-10 11:58:48 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.rdf.arp.test;
import junit.framework.*;
import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.*;
/**
 * @author jjc
 *
 */
public class MoreTests 
    extends TestCase implements RDFErrorHandler, ARPErrorNumbers {
    
   static public Test suite() {
    TestSuite suite = new TestSuite("ARP Plus");
    suite.addTest(TestErrorMsg.suite());
    suite.addTest(TestScope.suite());
    suite.addTest(new MoreTests("testEncodingMismatch1"));
    suite.addTest(new MoreTests("testEncodingMismatch2"));
	suite.addTest(new MoreTests("testNullBaseParamOK"));
	suite.addTest(new MoreTests("testNullBaseParamError"));
	suite.addTest(new MoreTests("testEmptyBaseParamOK"));
	suite.addTest(new MoreTests("testEmptyBaseParamError"));
	suite.addTest(new MoreTests("testWineDefaultNS"));
    return suite;
   }
   
   MoreTests(String s){
    super(s);
   }
   
    protected Model createMemModel() {
        return ModelFactory.createDefaultModel();
    }
    
    public void testWineDefaultNS() throws IOException {
    	testWineNS(createMemModel());
		  testWineNS(ModelFactory.createOntologyModel()); 
    }

	private void testWineNS(Model m) throws FileNotFoundException, IOException {
		InputStream in =new FileInputStream("testing/arp/xmlns/wine.rdf");
		  m.read(in,"");  
		  in.close();
		  assertEquals("http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#",m.getNsPrefixURI(""));
	}
  
   public void testEncodingMismatch1() throws IOException {
      Model m = createMemModel();
      RDFReader rdr = m.getReader();
      FileReader r = new FileReader("testing/wg/rdfms-syntax-incomplete/test001.rdf");
     if ( r.getEncoding().startsWith("UTF")) {
       System.err.println("WARNING: Encoding mismatch tests not executed on platform with default UTF encoding.");
       return;
     }
      rdr.setErrorHandler(this);
    expected = new int[]{WARN_ENCODING_MISMATCH};
      rdr.read(m,r,"http://example.org/");
      //System.err.println(m.size() + " triples read.");
      checkExpected();
      
   }
   
    public void testEncodingMismatch2() throws IOException {
       Model m = createMemModel();
       RDFReader rdr = m.getReader();
       FileReader r = new FileReader("testing/wg/rdf-charmod-literals/test001.rdf");
       if ( r.getEncoding().startsWith("UTF")) {
        // see above for warning message.
         return;
       }
       rdr.setErrorHandler(this);
       expected = new int[]{WARN_ENCODING_MISMATCH,ERR_ENCODING_MISMATCH};
       rdr.read(m,r,"http://example.org/");

        checkExpected();
    }
    
    public void testNullBaseParamOK() throws IOException {
		Model m = createMemModel();
		Model m1 = createMemModel();
		RDFReader rdr = m.getReader();
		FileInputStream fin = new FileInputStream("testing/wg/rdfms-identity-anon-resources/test001.rdf");
		
		rdr.setErrorHandler(this);
		expected = new int[]{  };
		rdr.read(m,fin,"http://example.org/");
		fin.close();
		fin = new FileInputStream("testing/wg/rdfms-identity-anon-resources/test001.rdf");
		rdr.read(m1,fin,null);
		fin.close();
        assertTrue("Base URI should have no effect.",m.isIsomorphicWith(m1));
		checkExpected();
    }
    
    public void testNullBaseParamError() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		FileInputStream fin = new FileInputStream("testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
		rdr.setErrorHandler(this);
		expected = new int[]{ ERR_RESOLVING_URI_AGAINST_NULL_BASE }; 
		rdr.read(m,fin,null);
		fin.close();
		checkExpected();
    }
    

	public void testEmptyBaseParamOK() throws IOException {
		Model m = createMemModel();
		Model m1 = createMemModel();
		RDFReader rdr = m.getReader();
		FileInputStream fin = new FileInputStream("testing/wg/rdfms-identity-anon-resources/test001.rdf");
		
		rdr.setErrorHandler(this);
		expected = new int[]{};
		rdr.read(m,fin,"http://example.org/");
		fin.close();
		fin = new FileInputStream("testing/wg/rdfms-identity-anon-resources/test001.rdf");
		rdr.read(m1,fin,"");
		fin.close();
		assertTrue("Empty base URI should have no effect.[" + m1.toString() +"]",m.isIsomorphicWith(m1));
		checkExpected();
	}
    
	public void testEmptyBaseParamError() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		FileInputStream fin = new FileInputStream("testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
		rdr.setErrorHandler(this);
		expected = new int[]{ WARN_RESOLVING_URI_AGAINST_EMPTY_BASE}; 
		rdr.read(m,fin,"");
		fin.close();
		Model m1 = createMemModel();
		m1.createResource("#foo").addProperty(RDF.value,"abc");
		assertTrue("Empty base URI should produce relative URI.[" + m.toString() +"]",m.isIsomorphicWith(m1));
		checkExpected();		
	}
    
    private void checkExpected() {
        for (int i=0; i<expected.length; i++)
          if (expected[i]!=0) {
            fail("Expected error: "
            + JenaReader.errorCodeName(expected[i]) + " but it did not occur.");
          }
    }
    
    public void warning(Exception e) {
        error(0, e);
    }
    
    public void error(Exception e) {
        error(1, e);
    }
    
    public void fatalError(Exception e) {
        error(2, e);
    }
    
    private void error(int level, Exception e) {
        //System.err.println(e.getMessage());
        if (e instanceof ParseException) {
            int eCode = ((ParseException) e).getErrorNumber();
            onError(level, eCode);
        } else {
            fail("Not expecting an Exception: " + e.getMessage());
        }
    }
    
    private int expected[];
    
    private void println(String m) {
        System.err.println(m);
    }
    
    void onError(int level, int num) {
        for (int i=0; i<expected.length; i++)
          if (expected[i]==num) {
            expected[i] = 0;
            return;
          }
        String msg =
            "Parser reports unexpected "
                + WGTestSuite.errorLevelName[level]
                + ": "
                + JenaReader.errorCodeName(num);
        println(msg);
        fail(msg);
    }
}

/*
    (c) Copyright 2000, 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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