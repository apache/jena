/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.ontology.tidy.*;
import com.hp.hpl.jena.ontology.tidy.impl.*;
import com.hp.hpl.jena.shared.ReificationStyle;

/**
 * @author Jeremy J. Carroll
 *  
 */
public class ErrorMsgTest extends TestCase {
    
    // TODO update OWL tests ...

    // TODO add test for rdf:_1 etc. seems wrong to me.
    /**
     * @param arg0
     */
    public ErrorMsgTest(String arg0) {
        super(arg0);
    }

    static public Test suite() {
        TestSuite s = new TestSuite(ErrorMsgTest.class);
        s.setName("OWL Syntax Error Messages");
        return s;
    }

    private void add(Checker chk, Triple t) {
        Graph g = Factory.createDefaultGraph(ReificationStyle.Minimal);
        g.add(t);
        chk.add(g);
    }

    private Checker basicTest(String str, String expected[]) {
        Triple t[] = GraphTestBase.tripleArray(str);
        Checker chk = new Checker(false);
        int i;
        for (i = 0; i < t.length - 1; i++) {
            add(chk, t[i]);
            assertFalse("Test is full too early.",
                    chk.getMonotoneLevel() == Levels.Full);
        }
        add(chk, t[i]);
        assertTrue("Test not detected as full.",
                chk.getMonotoneLevel() == Levels.Full);
        MonotonicProblem problems[] = MonotonicErrorAnalyzer.allProblems(chk,
                t[i]);
     /*   
        System.out.println(getName()+":");
        for (int j=0;j<problems.length;j++)
        System.out.println("\t"+problems[j].toString());
       */
         assertEquals("Number of errors", expected.length, problems.length);
        int done = 0;
        jloop:
        for (int j = 0; j < expected.length; j++) {
            for (int k = 0; k < problems.length; k++) {
                if ((done & (1 << k)) != 0)
                    continue;
                if (problems[k].toString().indexOf(expected[j]) != -1) {
                    done |= (1 << k);
                    continue jloop;
                }
            }
            fail("Didn't find error: " + expected[j]);
        }
        return chk;
    }

    public void testSingleTriple() {
        Checker chk = basicTest("eg:a owl:OntologyProperty eg:b", new String[]{"OntologyProperty is not permitted as the predicate"});

    }

    public void testDoubleConflict() {
        basicTest("eg:a rdfs:subPropertyOf eg:b; " + "eg:x eg:a eg:c;"
                + "eg:x eg:a 'foo'", new String[]{"a is used as a datatype property or an annotation property here, but elsewhere as an object property"});
    }

    public void testTransitivity() {
        basicTest("eg:a rdfs:subPropertyOf eg:b; "
                + "eg:a rdf:type owl:TransitiveProperty;"
                + "eg:b owl:inverseOf eg:c;" + "_n owl:cardinality 1;"
                + "_n owl:onProperty eg:c", new String[]{"trans"});
    }

    public void testTransitivity2() {
        basicTest("eg:a rdfs:subPropertyOf eg:b; " + "eg:b owl:inverseOf eg:c;"
                + "_n owl:cardinality 1;" + "_n owl:onProperty eg:c;"
                + "eg:a rdf:type owl:TransitiveProperty", new String[]{"trans"});
    }

    public void testTwoErrors() {
        basicTest("eg:c rdf:type owl:Class; " + "eg:cc rdf:type owl:Class; "
                + "eg:c eg:p eg:cc;" + "eg:p rdf:type owl:ObjectProperty", new String[]{"as an object property here","as an object property here"});
    }

    public void testTwoErrors2() {
        basicTest("eg:c rdf:type owl:Class; " + "eg:cc rdf:type owl:Class; "
                + "eg:p rdf:type owl:ObjectProperty;" + "eg:c eg:p eg:cc", new String[]{"expect an object which is an individual","expect a subject which is an individual"});
    }
    /*
// TODO get these right
    public void testUserDatatype1() {
        basicTest("eg:c eg:p '1'eg:c; " 
                + "eg:p rdf:type owl:DatatypeProperty", 
                new String[]{""});
        
    }
    public void testUserDatatype2() {
        basicTest("_n eg:c '1'eg:c", 
                new String[]{""});
        
    }
    public void testUserDatatype3() {
        basicTest("eg:a eg:p '1'eg:c; " 
                + "eg:c rdf:type owl:DatatypeProperty", 
                new String[]{""});
        
    }
    public void testUserDatatype4() {
        basicTest("eg:c rdf:type owl:DatatypeProperty; " +
                "eg:a eg:p '1'eg:c " ,
                new String[]{""});
        
    }
*/
}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

