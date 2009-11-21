/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql;

import junit.framework.TestSuite;

import com.hp.hpl.jena.sparql.junit.QueryTestSuiteFactory;

/** The test suite for all DAWG (the first SPARQL working group) approved tests. 
 *  Many are the same as or overlap with ARQ tests (because the ARQ ones were 
 *  contributed to DAWG or developed in response the feature design within DAWG)
 *  but we keep this set here as a reference.  
 * 
 *  */
public class TS_DAWG extends TestSuite
{
    static final String testSetNameDAWG        = "DAWG - Misc" ;

    static final public String testDirDAWG         = "testing/DAWG" ;
    static final public String testDirWGApproved   = "testing/DAWG-Final" ;
//    static final public String testDirWGPending    = "testing/DAWG-Pending" ;

    static public TestSuite suite() { return new TS_DAWG(); }

    public TS_DAWG()
    {
        super("DAWG") ;

        // One test, dawg-optional-filter-005-simplified or dawg-optional-filter-005-not-simplified
        // must fail because it's the same query and data with different interpretations of the
        // spec.  ARQ implements dawg-optional-filter-005-not-simplified.

        TestSuite ts1 = new TestSuite("Approved") ;
        ts1.addTest(QueryTestSuiteFactory.make(testDirWGApproved+"/manifest-evaluation.ttl")) ;

        // These merely duplicate ARQ's syntax tests because Andy wrote the DAWG syntax tests,
        // but they are quick so include the snapshot
        // But Eclipse get confused and may make them as not run (but they have).
        ts1.addTest(QueryTestSuiteFactory.make(testDirWGApproved+"/manifest-syntax.ttl")) ;
        addTest(ts1) ;

        TestSuite ts3 = new TestSuite("Misc") ;
        ts3.addTest(QueryTestSuiteFactory.make(testDirDAWG+"/Misc/manifest.n3")) ;
        ts3.addTest(QueryTestSuiteFactory.make(testDirDAWG+"/Syntax/manifest.n3")) ;
        ts3.addTest(QueryTestSuiteFactory.make(testDirDAWG+"/regex/manifest.n3")) ;
        ts3.addTest(QueryTestSuiteFactory.make(testDirDAWG+"/examples/manifest.n3")) ;  // Value testing examples
        ts3.addTest(QueryTestSuiteFactory.make(testDirDAWG+"/i18n/manifest.ttl")) ;
        addTest(ts3) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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