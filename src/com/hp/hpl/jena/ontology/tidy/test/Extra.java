/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Extra.java,v 1.13 2004-01-27 15:45:23 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWLTest;
import com.hp.hpl.jena.ontology.tidy.*;
import java.util.*;
import java.io.*;

import jena.owlsyntax;

//import com.hp.hpl.jena.ontology.tidy.impl.*;

/**
 * @author jjc
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Extra extends TestCase {
	static public Test suite() {
		TestSuite s = new TestSuite(Extra.class);
		s.setName("More OWL syntax");
		return s;
   }
   /*
   public void testNum() {
   	int a = 35;
   	int A = 64;
   	int B = 24*5*23;
   	int b = 24*5*23 / 3;
   	int C = 356*52;
   	int c = C - 1;
   	int x = (c*B+b)*A+a;
	System.out.println(a);
	System.out.println(b);
	System.out.println(c);
   	System.out.println(x);
   	System.out.println(((x%A)+A)%A);
   	System.out.println(((x/A)%B+B)%B);
   	System.out.println(((x/(A*B)+C)%C));
   }
*/
   public void testdisjointWith001() {
	 runTest("disjointWith001", OWLTest.Full);
   }
   public void testdisjointWith002() {
	 runTest("disjointWith002", OWLTest.DL);
   }
   public void testsameAs001() {
	 runTest("sameAs001", OWLTest.Full);
   }
   public void testann001() {
	 runTest("ann001", OWLTest.Lite);
   }
   public void testann002() {
	 runTest("ann002", OWLTest.Lite);
   }
   public void testann003() {
	 runTest("ann003", OWLTest.Lite);
   }
   public void testann004() {
	 runTest("ann004", OWLTest.Lite);
   }
   public void testann005() {
	 runTest("ann005", OWLTest.Full);
   }

   public void testcycle001() {
	 runTest("cycle001", OWLTest.Full);
   }
   public void testcycle002() {
	 runTest("cycle002", OWLTest.DL);
   }
   public void testcycle003() {
	 runTest("cycle003", OWLTest.Full);
   }
   public void testcycle004() {
	 runTest("cycle004", OWLTest.Full);
   }
   public void testcycle005() {
	 runTest("cycle005", OWLTest.Full);
   }
   public void testsubClassOf001() {
   	 runTest("subClassOf001", OWLTest.Full);
   }
   
   public void testsubClassOf002() {
   	runTest("subClassOf002", OWLTest.Full);
   }
   public void testsubClassOf003() {
   	runTest("subClassOf003", OWLTest.Full);
   }
   public void testsubClassOf004() {
   	runTest("subClassOf004", OWLTest.DL);
   }
   public void testsubClassOf005() {
   	runTest("subClassOf005", OWLTest.Lite);
   }
   
   public void testlist001() {
	runTest("list001", OWLTest.DL);
   }

   public void testimports001() {
	runTest("imports001", OWLTest.Full);
   }
   public void testimports002() {
	runTest("imports002", OWLTest.Full);
   }
   public void testontologyProp001() {
	runTest("ontologyProp001", OWLTest.Lite);
   }
   /*
   public void testI5_3_010() {
   	for (int i=0; i<200; i++)
   	  runWGTest("I5.3/consistent010",OWLTest.Full);
  }
  */
/*
   static long t;
   
   public void testHuge() {
	t = System.currentTimeMillis();
	runTest("nciOncology", OWLTest.Lite);
   }
   public void testEndTime() {
	  System.err.println((System.currentTimeMillis()-t)+ " ms");
	 }
*/
  private void runTest(String fn, Resource lvl) {
  	runTestFullName("testing/ontology/tidy/"+fn,lvl);
  }
  private void runWGTest(String fn, Resource lvl) {
	runTestFullName("testing/wg/"+fn,lvl);
  }
   private void runTestFullName(String fn, Resource lvl) {

   	Checker chk = new Checker(lvl.equals(OWLTest.Lite));
   	Model m = ModelFactory.createDefaultModel();
   	m.read("file:"+fn+".rdf");
   	chk.addRaw(m.getGraph());
   	
   	String rslt = chk.getSubLanguage();
   	boolean rsltx = lvl.getURI().endsWith(rslt);
   	if (!rsltx){
   		Iterator it = chk.getProblems();
   		while (it.hasNext()) {
   			SyntaxProblem sp = (SyntaxProblem)it.next();
   			System.err.println(sp.longDescription());
   		}
   	}
   	assertTrue(rsltx);
   }
   
   public void testEMess() {
   	PrintStream oldOut = System.out;
   	PrintStream oldErr = System.err;
   	ByteArrayOutputStream bos = new ByteArrayOutputStream();
   	try {
   	System.setOut(new PrintStream(new OutputStream(){

		public void write(int b) throws IOException {
		}
   	}));
   	System.setErr(new PrintStream(bos));
   	owlsyntax.main(new String[]{"file:testing/ontology/tidy/emess.rdf"});
   	}
   	finally {
   	  System.setOut(oldOut);
   	  System.setErr(oldErr);
   	}
   	String msg = bos.toString();
   	//System.err.println(msg);
   	
   	assertTrue("not enough triples in error message",msg.indexOf("ObjectProperty")!=-1);
   	assertTrue("not enough triples in error message",msg.indexOf("range")!=-1);
   	
   	}

}

/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
 */

