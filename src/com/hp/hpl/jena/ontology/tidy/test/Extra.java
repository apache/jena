/*
 * Created on 22-Nov-2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.hp.hpl.jena.ontology.tidy.test;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWLTest;
import com.hp.hpl.jena.ontology.tidy.*;
import java.util.*;
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

}
