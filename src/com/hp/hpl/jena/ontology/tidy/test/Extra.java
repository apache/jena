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
import com.hp.hpl.jena.ontology.tidy.impl.*;

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

   public void testann001() {
	 runTest("ann001", OWLTest.Lite);
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

   	Checker chk = new Checker(lvl.equals(OWLTest.Lite));
   	Model m = ModelFactory.createDefaultModel();
   	m.read("file:testing/ontology/tidy/"+fn+".rdf");
   	chk.add(m.getGraph());
   	
   	String rslt = chk.getSubLanguage();
   	assertTrue(lvl.getURI().endsWith(rslt));
   }

}
