/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestPackage.java,v 1.5 2003-01-30 18:31:39 der Exp $
*/

package com.hp.hpl.jena.test;

import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * All developers should edit this file to add their tests.
 * Please try to name your tests and test suites appropriately.
 * Note, it is better to name your test suites on creation
 * rather than in this file.
 * @author  jjc
 */
public class TestPackage extends TestSuite {

    static public Test suite() {
        return new TestPackage();
    }

    /** Creates new TestPackage */
    private TestPackage() {
        super("jena");
        addTest("Graph", com.hp.hpl.jena.graph.test.TestPackage.suite());
        addTest("Util", com.hp.hpl.jena.util.test.TestPackage.suite());
        addTest(com.hp.hpl.jena.enhanced.test.TestPackage.suite());
        addTest("Output", com.hp.hpl.jena.xmloutput.test.TestPackage.suite());
        addTest("RDQL", com.hp.hpl.jena.rdql.test.RDQLTestSuite.suite());
        addTest("N3", com.hp.hpl.jena.n3.test.N3TestSuite.suite());
        addTest( "Model", com.hp.hpl.jena.rdf.model.test.TestModel.suite() );
        addTest( com.hp.hpl.jena.util.iterator.test.TestPackage.suite() );
        addTest("Mega", com.hp.hpl.jena.regression.MegaTestSuite.suite());
        addTest( com.hp.hpl.jena.rdf.arp.test.TestPackage.suite());
        addTest("Reasoners", com.hp.hpl.jena.reasoner.test.TestPackage.suite());
    }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}

/*
    (c) Copyright Hewlett-Packard Company 2002
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